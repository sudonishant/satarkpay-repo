package com.example.util

import android.net.Uri

data class ParsedUpiData(
    val upiId: String,
    val payeeName: String,
    val amount: Long?,
    val transactionNote: String,
    val rawContent: String,
    val isStandardUpiUri: Boolean
)

object UpiQrParser {

    /**
     * Parses standard NPCI UPI URI specifications:
     * e.g., upi://pay?pa=merchant@upi&pn=Merchant%20Name&am=500.00&cu=INR&tn=Dinner
     * Also handles plain UPI IDs or raw payment strings.
     */
    fun parse(raw: String): ParsedUpiData {
        val trimmed = raw.trim()
        if (trimmed.startsWith("upi://pay", ignoreCase = true)) {
            try {
                val uri = Uri.parse(trimmed)
                val pa = uri.getQueryParameter("pa") ?: ""
                val pn = uri.getQueryParameter("pn") ?: ""
                val amStr = uri.getQueryParameter("am")
                val tn = uri.getQueryParameter("tn") ?: ""

                val amount = amStr?.toDoubleOrNull()?.toLong()

                return ParsedUpiData(
                    upiId = pa.lowercase().trim(),
                    payeeName = pn.trim(),
                    amount = amount,
                    transactionNote = tn.trim(),
                    rawContent = trimmed,
                    isStandardUpiUri = true
                )
            } catch (_: Exception) {
                // fallback to heuristic extraction
            }
        }

        // Check if raw text is a plain UPI ID (contains @ with valid characters)
        val vpaRegex = Regex("""[a-zA-Z0-9.\-_]{2,256}@[a-zA-Z]{2,64}""")
        val match = vpaRegex.find(trimmed)
        if (match != null) {
            return ParsedUpiData(
                upiId = match.value.lowercase().trim(),
                payeeName = "",
                amount = null,
                transactionNote = "",
                rawContent = trimmed,
                isStandardUpiUri = false
            )
        }

        // Return raw content with empty upiId if not parsable
        return ParsedUpiData(
            upiId = trimmed,
            payeeName = "",
            amount = null,
            transactionNote = "",
            rawContent = trimmed,
            isStandardUpiUri = false
        )
    }
}
