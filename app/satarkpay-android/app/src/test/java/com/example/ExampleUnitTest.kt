package com.example

import com.example.engine.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPiiRedactionR28() {
        val input = "Pay 7349123456@ptaxis or call 9876543210 with OTP 123456"
        val result = RuleEngine.redactPII(input)
        assertTrue(result.itemsRedactedCount >= 2)
        assertFalse(result.redactedText.contains("9876543210"))
        assertTrue(result.redactedText.contains("••••••"))
    }

    @Test
    fun testBankOfficialWarningR27Negation() {
        val text = "SBI Alert: Never share your OTP with anyone. Bank never calls asking for OTP or UPI PIN."
        val result = RuleEngine.analyzeText(text)
        assertEquals(VerdictBucket.SEEMS_OK, result.bucket)
        assertTrue(result.isBankOfficialWarning)
    }

    @Test
    fun testDigitalArrestF1ScamDetection() {
        val text = "This is CBI Mumbai. Your mobile is involved in money laundering case. Do not disconnect video call or you will be arrested."
        val result = RuleEngine.analyzeText(text)
        assertEquals(VerdictBucket.SCAM_LIKELY, result.bucket)
        assertEquals("F1", result.familyCode)
    }

    @Test
    fun testQrRefundTrapF4Detection() {
        val text = "Scan this QR code and enter UPI PIN to receive cashback reward"
        val result = RuleEngine.analyzeText(text)
        assertEquals(VerdictBucket.SCAM_LIKELY, result.bucket)
        assertEquals("F4", result.familyCode)
    }

    @Test
    fun testDomainTrustLadderM3() {
        val gov = RuleEngine.analyzeDomain("rbi.org.in")
        assertEquals(DomainTrustLevel.L1_VERIFIED_GOV_BANK, gov.trustLevel)

        val phishing = RuleEngine.analyzeDomain("qr-pay.top")
        assertEquals(DomainTrustLevel.L4_LOOKALIKE_FAKE, phishing.trustLevel)
        assertTrue(phishing.hasGatewayMismatch)
    }

    @Test
    fun testAppSecurityScannerSummary() {
        val testApps = listOf(
            InstalledAppPermissionInfo(
                packageName = "com.anydesk.anydeskandroid",
                appName = "AnyDesk",
                versionName = "1.0",
                isSystemApp = false,
                hasSmsPermission = false,
                hasLocationPermission = false,
                hasAccessibilityPermission = true,
                hasScreenOverlayPermission = true,
                hasNotificationPermission = false,
                hasCameraMicPermission = false,
                detectedSensitivePermissions = listOf(
                    SensitivePermissionType.ACCESSIBILITY,
                    SensitivePermissionType.SCREEN_OVERLAY
                ),
                sensitivePermissionsCount = 2,
                riskLevel = SensitiveRiskLevel.HIGH_RISK,
                riskSummary = "Remote Desktop Tool"
            ),
            InstalledAppPermissionInfo(
                packageName = "com.normal.app",
                appName = "Normal App",
                versionName = "1.0",
                isSystemApp = false,
                hasSmsPermission = true,
                hasLocationPermission = true,
                hasAccessibilityPermission = false,
                hasScreenOverlayPermission = false,
                hasNotificationPermission = false,
                hasCameraMicPermission = false,
                detectedSensitivePermissions = listOf(
                    SensitivePermissionType.SMS,
                    SensitivePermissionType.LOCATION
                ),
                sensitivePermissionsCount = 2,
                riskLevel = SensitiveRiskLevel.MEDIUM_RISK,
                riskSummary = "SMS & Location Access"
            )
        )

        val summary = AppSecurityScanner.computeAuditSummary(testApps)
        assertEquals(2, summary.totalAppsScanned)
        assertEquals(1, summary.appsWithAccessibilityCount)
        assertEquals(1, summary.appsWithSmsCount)
        assertEquals(1, summary.appsWithLocationCount)
        assertEquals(1, summary.appsWithOverlayCount)
        assertEquals(1, summary.highRiskAppsCount)
    }

    @Test
    fun testUnifiedAttackChainDigitalArrestBlocked() {
        val result = UnifiedAttackChainEngine.evaluate(
            payeeUpi = "cybercbi91@okhdfcbank",
            payeeName = "CBI Digital Account",
            amount = 25000L,
            isFirstTimePayee = true,
            knownPayeeTxnCount = 0,
            isTrustVerified = false,
            sourceChannel = PaymentSourceChannel.WHATSAPP_UNSAVED,
            isActiveCall = true,
            chatTextSnippet = "Transfer security deposit immediately under CBI Mumbai arrest warrant",
            hasRatOrAccessibilityTool = false,
            highRiskAppsCount = 0
        )
        assertEquals(AttackChainRiskTier.CRITICAL_BLOCKED, result.riskTier)
        assertTrue(result.blockPayment)
        assertTrue(result.score >= 90)
        assertTrue(result.signals.any { it.provenance == "LIVE ON DEVICE" })
        assertTrue(result.signals.any { it.provenance == "PAYEE LEDGER" })
    }

    @Test
    fun testUnifiedAttackChainTelegramTaskScam() {
        val result = UnifiedAttackChainEngine.evaluate(
            payeeUpi = "prepaidtask@paytm",
            payeeName = "Telegram VIP Group",
            amount = 5000L,
            isFirstTimePayee = true,
            knownPayeeTxnCount = 0,
            isTrustVerified = false,
            sourceChannel = PaymentSourceChannel.TELEGRAM,
            isActiveCall = false,
            chatTextSnippet = "Recharge ₹5,000 for YouTube VIP task bonus",
            hasRatOrAccessibilityTool = false,
            highRiskAppsCount = 0
        )
        assertEquals(AttackChainRiskTier.HIGH_RISK, result.riskTier)
        assertFalse(result.blockPayment)
        assertTrue(result.score >= 75)
    }

    @Test
    fun testUnifiedAttackChainKnownPayeeSafe() {
        val result = UnifiedAttackChainEngine.evaluate(
            payeeUpi = "sharma.kirana@icici",
            payeeName = "Sharma General Store",
            amount = 450L,
            isFirstTimePayee = false,
            knownPayeeTxnCount = 11,
            isTrustVerified = true,
            sourceChannel = PaymentSourceChannel.DIRECT_SHOP_QR,
            isActiveCall = false,
            chatTextSnippet = "Groceries",
            hasRatOrAccessibilityTool = false,
            highRiskAppsCount = 0
        )
        assertEquals(AttackChainRiskTier.LOW_SAFE, result.riskTier)
        assertFalse(result.blockPayment)
        assertTrue(result.score <= 25)
    }
}
