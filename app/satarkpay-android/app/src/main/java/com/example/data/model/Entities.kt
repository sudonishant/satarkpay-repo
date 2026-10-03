package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scam_reports")
data class ScamReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val upiId: String,
    val utrNumber: String,
    val txnId: String,
    val suspectContact: String,
    val incidentTime: Long = System.currentTimeMillis(),
    val status: String = "CONFIRMED", // DRAFT, CONFIRMED, REPORTED_1930, ESCALATED_NCRP
    val cyberCellEmail: String = "patnacyberpps-bih@gov.in",
    val complaintNumber: String = "",
    val evidenceHashes: String = "",
    val notes: String = ""
)

@Entity(tableName = "screenshot_scans")
data class ScreenshotScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,
    val sourceDomain: String,
    val payeeUpi: String,
    val amount: Long,
    val riskLevel: String, // L1_SAFE, L2_VERIFIED_MERCHANT, L3_UNVERIFIED, L4_FAKE
    val timestamp: Long = System.currentTimeMillis(),
    val isBurst: Boolean = false,
    val sha256Hash: String = ""
)

@Entity(tableName = "mandate_audits")
data class MandateAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appName: String,
    val payeeName: String,
    val amount: Long,
    val frequency: String, // DAILY, WEEKLY, MONTHLY, ONE_TIME_TRICK
    val nextDebitDate: String,
    val isSuspicious: Boolean,
    val reason: String,
    val isRevoked: Boolean = false
)

@Entity(tableName = "intel_patterns")
data class IntelPatternEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val familyCode: String, // F1, F2, F3, F4, F5, F6, etc.
    val platform: String, // WhatsApp, Telegram, UPI, Call, SMS
    val riskLevel: String, // HIGH, MEDIUM, LOW
    val description: String,
    val sampleSnippet: String,
    val reportedCount: Int = 1,
    val approvedByAnalyst: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "check_history")
data class CheckHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checkType: String, // CHAT_PAY, SCREENSHOT, DOMAIN, SANCHALAK
    val summary: String,
    val verdictBucket: String, // SCAM_LIKELY, CAUTION, PAUSE_NAHI_BATA, SEEMS_OK
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
