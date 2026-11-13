package com.example.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Dao
interface UserReportedPaymentDao {
    @Insert
    suspend fun insert(payment: UserReportedPaymentEntity): Long

    @Query("SELECT amount FROM user_reported_payments WHERE timestamp >= :sinceTimestamp AND status = 'USER_REPORTED_SUCCESS' ORDER BY timestamp ASC")
    suspend fun getRecentSuccessfulAmounts(sinceTimestamp: Long): List<Long>

    @Query("SELECT amount FROM user_reported_payments WHERE payeeHash = :payeeHash AND timestamp >= :sinceTimestamp AND status = 'USER_REPORTED_SUCCESS' ORDER BY timestamp ASC")
    suspend fun getRecentSuccessfulAmountsForPayee(payeeHash: String, sinceTimestamp: Long): List<Long>

    @Query("DELETE FROM user_reported_payments WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)
}

@Database(
    entities = [
        ScamReportEntity::class,
        ScreenshotScanEntity::class,
        MandateAuditEntity::class,
        IntelPatternEntity::class,
        CheckHistoryEntity::class,
        PayeeLedgerEntity::class,
        UserReportedPaymentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scamReportDao(): ScamReportDao
    abstract fun screenshotScanDao(): ScreenshotScanDao
    abstract fun mandateAuditDao(): MandateAuditDao
    abstract fun intelPatternDao(): IntelPatternDao
    abstract fun checkHistoryDao(): CheckHistoryDao
    abstract fun payeeLedgerDao(): PayeeLedgerDao
    abstract fun userReportedPaymentDao(): UserReportedPaymentDao

    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_reported_payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` INTEGER NOT NULL, `payeeHash` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_reported_payments_timestamp` ON `user_reported_payments` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_reported_payments_status` ON `user_reported_payments` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_reported_payments_payeeHash_timestamp` ON `user_reported_payments` (`payeeHash`, `timestamp`)")
                // These exact rows were shipped as sample data; they are not user-verified payees.
                db.execSQL("UPDATE payee_ledger SET isTrustVerified = 0, paymentCount = 0, totalAmountPaid = 0, lastRiskVerdict = 'UNVERIFIED' WHERE (LOWER(upiId) = 'mother.family@oksbi' AND notes = 'Family contact verified') OR (LOWER(upiId) = 'sbpdcl.billpay@sbi' AND notes = 'Official utility biller') OR (LOWER(upiId) = 'sharma.kirana@icici' AND notes = 'Local neighbourhood grocer')")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "satarkpay_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
