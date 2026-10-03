package `in`.scamurai.satarkpay.data

/** Room store (TODO finale): verdicts + hashes + mandate fields — koi analytics nahi. */
// TODO(finale): entities
//  - VerdictEntity(ts, family, verdictCode, payeeHash, reasonsJson)   ; retention 30 din
//  - DwellEntity(chatApp, dwellMs, gapS)                             ; retention 7 din
//  - MandateEntity(app, amount, frequency, merchantHash, nextDebit)   ; retention 30 din
//  - EvidenceEntity(fileHash, txnId, utr, amount)                     ; user-managed
//  "Mera data delete karo" → wipeAll() (DPDP erasure)
