package `in`.scamurai.satarkpay.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * R38 · Fraud CONFIRM → 3 kadam (email · call · payment stop) + optional family alert.
 * Sab actions USER ke tap par — app khud email nahi bhejti aur khud call nahi karti.
 * Demo parity: web/_part_app.js → EM_MAIL / EM_CALLS / emOpen() / emStop
 */
object EmergencyActions {

    private val CYBER_CELL_EMAILS = listOf(
        "patnacyberpps-bih@gov.in", "sp-cyber@biharpolice.gov.in", "cciu-bih@nic.in"
    )   // Bihar defaults — Settings (S11) se editable

    val HELPLINES = mapOf(
        "1930" to "Cyber financial fraud helpline",
        "1909" to "Sanchar Saathi (fraud number/SMS)",
        "9031825975" to "Bihar cyber cell (Patna)",
    )

    /** 📧 Step 1 — ready draft ke saath mail app kholo (auto-send NAHI). */
    fun emailDraft(ctx: Context, caseFacts: CaseFacts, cc: String? = null) {
        val subject = "UPI fraud complaint — ₹${caseFacts.totalLoss} · ${caseFacts.txns.size} txn · evidence pack attached"
        val body = buildString {
            append("Respected Sir/Madam,\n\nMain UPI fraud ka victim hoon.\n\n")
            caseFacts.txns.forEach { append("· ₹${it.amount} | ${it.vpa} | Txn ${it.txnId} | UTR ${it.utr} | ${it.date}\n") }
            append("\nRequest: (1) beneficiary accounts par freeze/hold (CFCFRMS), ")
            append("(2) mule mapping, (3) complaint number.\n")
            append("Annexure: crops + SHA-256 hashes attached.\n\n[Naam] · [Mobile] · [City]")
        }
        val to = CYBER_CELL_EMAILS.joinToString(",")
        val uri = Uri.parse("mailto:$to" + (cc?.let { "?cc=$it&" } ?: "?") +
                "subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
        ctx.startActivity(Intent(Intent.ACTION_SENDTO, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** 📞 Step 2 — dialer kholo (number + script screen par). */
    fun callHelpline(ctx: Context, number: String) =
        ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    fun callScript(caseFacts: CaseFacts): List<String> {
        val t = caseFacts.txns.firstOrNull()
        return listOf(
            "Sir, UPI fraud hua hai.",
            "Payment ₹${caseFacts.totalLoss}, VPA ${t?.vpa ?: "-"}.",
            "Txn ${t?.txnId ?: "-"} · UTR ${t?.utr ?: "-"} · date ${t?.date ?: "-"}.",
            "Freeze request chahiye aur complaint number dijiye."
        )
    }

    /** ⛔ Step 3 — payment-stop request (bank/PSP ko bhejne ke liye). */
    fun paymentStopRequest(caseFacts: CaseFacts): String = buildString {
        append("Sir/Madam, mere UPI fraud hua hai. Please turant karein:\n")
        caseFacts.txns.forEachIndexed { i, t ->
            append("${i + 1}) Txn ${t.txnId} (UTR ${t.utr}, ₹${t.amount}) par dispute/chargeback raise karein.\n")
        }
        val n = caseFacts.txns.size
        append("${n + 1}) Beneficiary VPA par hold/freeze request (CFCFRMS) bhejein.\n")
        append("${n + 2}) Mere account par temporary debit freeze/limit laga dein.\n")
        append("${n + 3}) Naye mandate/SMS link block karein.\n")
        append("Evidence pack attached (hashes + crops).")
    }

    /** Optional step 4 — parivaar/community alert (masked numbers). */
    fun familyAlertIntent(text: String) =
        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
}

data class Txn(val amount: Long, val vpa: String, val txnId: String, val utr: String, val date: String)
data class CaseFacts(val totalLoss: Long, val txns: List<Txn>)
