package `in`.scamurai.satarkpay.domain

/**
 * M4 · Verdict engine — demo ke web/_part_app.js se 1:1 port karna hai (TODO finale).
 *
 * Order (mat badalna, demo se parity isi order me hai):
 *   1. redactPII(text)                     R28
 *   2. PASS rule match                     (safety-warning / legit patterns)
 *   3. non-PASS rule family match          F1..F35
 *   4. negation handling                   R27 — dono word-order; scam-words ho to override nahi
 *   5. sebiCheck(text)                     R26 — format INZ/INH/INA + 9 digits, portals, watch-list
 *   6. checkDomain(link)                   M3 — L1..L4 + gateway≠merchant
 *   7. verdict bucket: SCAM LIKELY | CAUTION | SEEMS OK | UNCERTAIN(PAKA NAHI)
 *   8. reasons + KARO / MAT KARO + citations
 *
 * LLM (production) sirf step 8 ka wording sudhaarta hai — verdict kabhi nahi banata.
 * Parity test: web/smoke_test.js ke M4 ke 8 cases + eval/run_eval.js ke 33 messages.
 */
