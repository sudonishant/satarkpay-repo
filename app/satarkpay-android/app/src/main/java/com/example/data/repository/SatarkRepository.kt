package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class SatarkRepository(private val db: AppDatabase) {

    val allReports: Flow<List<ScamReportEntity>> = db.scamReportDao().getAllReports()
    val allScans: Flow<List<ScreenshotScanEntity>> = db.screenshotScanDao().getAllScans()
    val allMandates: Flow<List<MandateAuditEntity>> = db.mandateAuditDao().getAllMandates()
    val approvedIntel: Flow<List<IntelPatternEntity>> = db.intelPatternDao().getApprovedPatterns()
    val reviewQueueIntel: Flow<List<IntelPatternEntity>> = db.intelPatternDao().getReviewQueuePatterns()
    val checkHistory: Flow<List<CheckHistoryEntity>> = db.checkHistoryDao().getRecentHistory()
    val allPayees: Flow<List<PayeeLedgerEntity>> = db.payeeLedgerDao().getAllPayees()

    suspend fun insertReport(report: ScamReportEntity): Long = db.scamReportDao().insertReport(report)
    suspend fun updateReport(report: ScamReportEntity) = db.scamReportDao().updateReport(report)
    suspend fun deleteReport(id: Long) = db.scamReportDao().deleteReport(id)

    suspend fun insertScan(scan: ScreenshotScanEntity): Long = db.screenshotScanDao().insertScan(scan)
    suspend fun getRecentScans(sinceTimestamp: Long): List<ScreenshotScanEntity> =
        db.screenshotScanDao().getRecentScans(sinceTimestamp)
    suspend fun countPaymentsToPayee(payeeUpi: String): Int =
        db.screenshotScanDao().countPaymentsToPayee(payeeUpi)

    suspend fun revokeMandate(id: Long) = db.mandateAuditDao().revokeMandate(id)
    suspend fun approveIntel(id: Long) = db.intelPatternDao().approvePattern(id)
    suspend fun rejectIntel(id: Long) = db.intelPatternDao().rejectPattern(id)
    suspend fun insertHistory(item: CheckHistoryEntity): Long = db.checkHistoryDao().insertHistory(item)

    // --- Payee Ledger Methods ---
    suspend fun getPayee(upiId: String): PayeeLedgerEntity? = db.payeeLedgerDao().getPayee(upiId)
    suspend fun upsertPayee(payee: PayeeLedgerEntity) = db.payeeLedgerDao().upsertPayee(payee)
    suspend fun recordSuccessfulPayment(upiId: String, name: String, amount: Long, riskVerdict: String) {
        val existing = db.payeeLedgerDao().getPayee(upiId)
        if (existing == null) {
            db.payeeLedgerDao().upsertPayee(
                PayeeLedgerEntity(
                    upiId = upiId.lowercase().trim(),
                    payeeName = name,
                    firstSeenTimestamp = System.currentTimeMillis(),
                    lastSeenTimestamp = System.currentTimeMillis(),
                    paymentCount = 1,
                    totalAmountPaid = amount,
                    isContactSaved = false,
                    isTrustVerified = false,
                    lastRiskVerdict = riskVerdict
                )
            )
        } else {
            db.payeeLedgerDao().upsertPayee(
                existing.copy(
                    payeeName = if (name.isNotBlank()) name else existing.payeeName,
                    lastSeenTimestamp = System.currentTimeMillis(),
                    paymentCount = existing.paymentCount + 1,
                    totalAmountPaid = existing.totalAmountPaid + amount,
                    lastRiskVerdict = riskVerdict
                )
            )
        }
    }
    suspend fun setPayeeTrustStatus(upiId: String, trusted: Boolean) =
        db.payeeLedgerDao().setTrustStatus(upiId, trusted)

    suspend fun isFirstTimePayee(upiId: String): Boolean {
        val clean = upiId.lowercase().trim()
        val payee = db.payeeLedgerDao().getPayee(clean)
        return payee == null || payee.paymentCount <= 0
    }

    suspend fun seedInitialDataIfEmpty() {
        // Pre-populate trusted known payees in Payee Ledger
        if (db.payeeLedgerDao().getPayeeCount() == 0) {
            val trustedPayees = listOf(
                PayeeLedgerEntity(
                    upiId = "mother.family@oksbi",
                    payeeName = "Maa (Sunita Devi)",
                    firstSeenTimestamp = System.currentTimeMillis() - (180L * 24 * 3600 * 1000),
                    lastSeenTimestamp = System.currentTimeMillis() - (2L * 24 * 3600 * 1000),
                    paymentCount = 24,
                    totalAmountPaid = 48500,
                    isContactSaved = true,
                    isTrustVerified = true,
                    lastRiskVerdict = "SAFE",
                    notes = "Family contact verified"
                ),
                PayeeLedgerEntity(
                    upiId = "sbpdcl.billpay@sbi",
                    payeeName = "Bihar State Power Holding",
                    firstSeenTimestamp = System.currentTimeMillis() - (120L * 24 * 3600 * 1000),
                    lastSeenTimestamp = System.currentTimeMillis() - (25L * 24 * 3600 * 1000),
                    paymentCount = 4,
                    totalAmountPaid = 5820,
                    isContactSaved = false,
                    isTrustVerified = true,
                    lastRiskVerdict = "SAFE",
                    notes = "Official utility biller"
                ),
                PayeeLedgerEntity(
                    upiId = "sharma.kirana@icici",
                    payeeName = "Sharma General Store",
                    firstSeenTimestamp = System.currentTimeMillis() - (45L * 24 * 3600 * 1000),
                    lastSeenTimestamp = System.currentTimeMillis() - (1L * 24 * 3600 * 1000),
                    paymentCount = 11,
                    totalAmountPaid = 3450,
                    isContactSaved = false,
                    isTrustVerified = true,
                    lastRiskVerdict = "SAFE",
                    notes = "Local neighbourhood grocer"
                )
            )
            for (p in trustedPayees) {
                db.payeeLedgerDao().upsertPayee(p)
            }
        }
        // Pre-populate realistic mandate audit items
        val defaultMandates = listOf(
            MandateAuditEntity(
                appName = "PhonePe",
                payeeName = "quick-earn-club.in",
                amount = 299,
                frequency = "DAILY",
                nextDebitDate = "Tomorrow 06:00 AM",
                isSuspicious = true,
                reason = "Hidden recurring mandate disguised as one-time verification fee"
            ),
            MandateAuditEntity(
                appName = "Google Pay",
                payeeName = "FastGamePro VIP",
                amount = 499,
                frequency = "WEEKLY",
                nextDebitDate = "3 Oct 2026",
                isSuspicious = true,
                reason = "Gaming reward subscription with high recurring frequency"
            ),
            MandateAuditEntity(
                appName = "Paytm",
                payeeName = "LIC of India Premium",
                amount = 1847,
                frequency = "MONTHLY",
                nextDebitDate = "1 Nov 2026",
                isSuspicious = false,
                reason = "Verified utility insurance merchant"
            ),
            MandateAuditEntity(
                appName = "BHIM",
                payeeName = "Netflix India Digital",
                amount = 649,
                frequency = "MONTHLY",
                nextDebitDate = "15 Oct 2026",
                isSuspicious = false,
                reason = "Legitimate entertainment subscription"
            )
        )
        db.mandateAuditDao().insertAll(defaultMandates)

        // Seed initial threat intel
        val initialPatterns = listOf(
            IntelPatternEntity(
                title = "TRAI / CBI Digital Arrest Video-Call Scam",
                familyCode = "F1",
                platform = "WhatsApp",
                riskLevel = "HIGH",
                description = "Scammer poses as Police/CBI or TRAI officer in fake uniform stating phone will be blocked and money laundering case filed. Demands webcam session and secret security deposit transfer.",
                sampleSnippet = "Your mobile number is involved in money laundering case under CBI Mumbai. Do not disconnect video call or police will reach your home in 1 hour.",
                reportedCount = 412,
                approvedByAnalyst = true
            ),
            IntelPatternEntity(
                title = "Telegram YouTube Video Like / Part-Time Task Fraud",
                familyCode = "F3",
                platform = "Telegram",
                riskLevel = "HIGH",
                description = "Victim is paid ₹150 for 3 YouTube likes, then invited to VIP prepaid task group. After sending ₹5,000 to ₹50,000 for high returns, withdrawals are frozen demanding 30% tax.",
                sampleSnippet = "Congratulations! You earned ₹150 for 3 YouTube likes. Join our VIP Telegram merchant group for daily guaranteed ₹3,000 to ₹10,000 salary.",
                reportedCount = 829,
                approvedByAnalyst = true
            ),
            IntelPatternEntity(
                title = "Fake Electricity Bill Disconnection Alert",
                familyCode = "F6",
                platform = "SMS",
                riskLevel = "HIGH",
                description = "SMS claiming electricity power will be disconnected at 9:30 PM due to unpaid bill. Gives fake officer mobile number asking to install quick support APK.",
                sampleSnippet = "Dear consumer your electricity power will be disconnected tonight at 09:30 pm from electricity office because your previous month bill was not updated.",
                reportedCount = 310,
                approvedByAnalyst = true
            ),
            IntelPatternEntity(
                title = "Unregistered Stock Advisory WhatsApp Group",
                familyCode = "F5",
                platform = "WhatsApp",
                riskLevel = "HIGH",
                description = "Shares forged SEBI certificate claiming 500% intraday gains. Diverts funds to fake trading website instead of registered stock broker.",
                sampleSnippet = "SEBI Registered Research Analyst INZ99887711. Guaranteed 500% profit in Upper Circuit stocks. Pay ₹10,000 advance fee to receive secret calls.",
                reportedCount = 188,
                approvedByAnalyst = true
            ),
            // Item in review queue for analyst
            IntelPatternEntity(
                title = "Courier Parcel Customs Drugs / FedEx Narcotics Trap",
                familyCode = "F1",
                platform = "Call",
                riskLevel = "HIGH",
                description = "Automated IVR call stating parcel sent to Taiwan contains passport and MDMA narcotics drugs. Transferred to fake Mumbai Cyber Cell officer.",
                sampleSnippet = "This is FedEx customer care. Your courier parcel containing 5 passports and 140 grams MDMA has been detained by Mumbai Customs.",
                reportedCount = 63,
                approvedByAnalyst = false
            )
        )
        db.intelPatternDao().insertAll(initialPatterns)
    }

    companion object {
        fun computeSha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
