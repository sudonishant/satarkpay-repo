package `in`.scamurai.satarkpay.domain

/** M3 · Domain trust ladder (TODO finale) — pure function, unit-test friendly. */
// TODO(finale):
//  L1 verified (RBI/NPCI/bank/PSP allowlist) · L2 verified merchant (registry)
//  L3 clean-unverified (amber) · L4 fake/lookalike (red)
//  signals: homoglyph 0↔o, typosquat Levenshtein ≤2, brand-in-subdomain, punycode xn--,
//           risky TLD, shortener, no-HTTPS, IP-literal, @ trick, RDAP domain-age, DoT FRI, I4C
//  gateway≠merchant: gateway verified ho par andar ka merchant unverified → amber (R18)
//  R26: official SEBI/NSDL portal domain list + lookalike detection
