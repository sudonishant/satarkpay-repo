package `in`.scamurai.satarkpay.signals

/**
 * M2 · Screenshot Radar ka signal layer (TODO finale).
 * Image store NAHI hoti — sirf extracted entity + SHA-256 (7 din rolling).
 */
// TODO(finale): MediaStore ContentObserver (RELATIVE_PATH = Pictures/Screenshots) →
//  - sourceApp(): us second foreground app/domain (UsageStats se)
//  - burstCount(windowMinutes = 30)
//  - entity + hash → GuardStore; thumbnail turant delete
