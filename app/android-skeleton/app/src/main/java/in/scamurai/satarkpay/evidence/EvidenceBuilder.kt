package `in`.scamurai.satarkpay.evidence

/** M7 · kit ka pipeline (TODO finale) — demo me JS me poora chalta hai, port karna hai. */
// TODO(finale):
//  1) ingest(files)                          → originals immutable
//  2) cropSmart(bitmap)                       → status bar / nav / letterbox hatao
//  3) ocrOffline(bitmap)                      → ML Kit / Tesseract (opt-in)
//  4) extractEntities(text)                   → UPI VPA, txn id, UTR, URL, ₹ amount
//  5) qrDecode(bitmap)                        → VPA
//  6) redact(text/blocks)                     → 2 copies (full + masked, default masked)
//  7) sha256(per file) + annexure.csv         → chain of custody
//  8) ncrpText(hi + en) + email.eml + zip     → complaint-ready pack
