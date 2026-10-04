package com.example.data.db

import android.content.Context
import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScamReportDao {
    @Query("SELECT * FROM scam_reports ORDER BY incidentTime DESC")
    fun getAllReports(): Flow<List<ScamReportEntity>>

    @Query("SELECT * FROM scam_reports WHERE id = :id")
    suspend fun getReportById(id: Long): ScamReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ScamReportEntity): Long

    @Update
    suspend fun updateReport(report: ScamReportEntity)

    @Query("DELETE FROM scam_reports WHERE id = :id")
    suspend fun deleteReport(id: Long)
}

@Dao
interface ScreenshotScanDao {
    @Query("SELECT * FROM screenshot_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScreenshotScanEntity>>

    @Query("SELECT * FROM screenshot_scans WHERE timestamp > :sinceTimestamp")
    suspend fun getRecentScans(sinceTimestamp: Long): List<ScreenshotScanEntity>

    @Query("SELECT COUNT(*) FROM screenshot_scans WHERE payeeUpi = :payeeUpi")
    suspend fun countPaymentsToPayee(payeeUpi: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScreenshotScanEntity): Long

    @Query("DELETE FROM screenshot_scans WHERE id = :id")
    suspend fun deleteScan(id: Long)
}

@Dao
interface MandateAuditDao {
    @Query("SELECT * FROM mandate_audits ORDER BY isSuspicious DESC, amount DESC")
    fun getAllMandates(): Flow<List<MandateAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mandates: List<MandateAuditEntity>)

    @Query("UPDATE mandate_audits SET isRevoked = 1 WHERE id = :id")
    suspend fun revokeMandate(id: Long)
}

@Dao
interface IntelPatternDao {
    @Query("SELECT * FROM intel_patterns WHERE approvedByAnalyst = 1 ORDER BY timestamp DESC")
    fun getApprovedPatterns(): Flow<List<IntelPatternEntity>>

    @Query("SELECT * FROM intel_patterns WHERE approvedByAnalyst = 0 ORDER BY timestamp DESC")
    fun getReviewQueuePatterns(): Flow<List<IntelPatternEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patterns: List<IntelPatternEntity>)

    @Query("UPDATE intel_patterns SET approvedByAnalyst = 1 WHERE id = :id")
    suspend fun approvePattern(id: Long)

    @Query("DELETE FROM intel_patterns WHERE id = :id")
    suspend fun rejectPattern(id: Long)
}

@Dao
interface CheckHistoryDao {
    @Query("SELECT * FROM check_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<CheckHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CheckHistoryEntity): Long
}

@Dao
interface PayeeLedgerDao {
    @Query("SELECT * FROM payee_ledger WHERE LOWER(upiId) = LOWER(:upiId) LIMIT 1")
    suspend fun getPayee(upiId: String): PayeeLedgerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayee(payee: PayeeLedgerEntity)

    @Query("SELECT * FROM payee_ledger ORDER BY lastSeenTimestamp DESC")
    fun getAllPayees(): Flow<List<PayeeLedgerEntity>>

    @Query("SELECT COUNT(*) FROM payee_ledger")
    suspend fun getPayeeCount(): Int

    @Query("UPDATE payee_ledger SET isTrustVerified = :trusted WHERE LOWER(upiId) = LOWER(:upiId)")
    suspend fun setTrustStatus(upiId: String, trusted: Boolean)
}

@Database(
    entities = [
        ScamReportEntity::class,
        ScreenshotScanEntity::class,
        MandateAuditEntity::class,
        IntelPatternEntity::class,
        CheckHistoryEntity::class,
        PayeeLedgerEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scamReportDao(): ScamReportDao
    abstract fun screenshotScanDao(): ScreenshotScanDao
    abstract fun mandateAuditDao(): MandateAuditDao
    abstract fun intelPatternDao(): IntelPatternDao
    abstract fun checkHistoryDao(): CheckHistoryDao
    abstract fun payeeLedgerDao(): PayeeLedgerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "satarkpay_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
