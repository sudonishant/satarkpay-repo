package `in`.scamurai.satarkpay.signals

/**
 * M1 ka signal layer (TODO finale).
 * Sirf: kaunsa app foreground me tha, kab se, aur kitni der — kuch bhi disk par nahi.
 */
// TODO(finale): UsageStatsManager se events poll karo (AppOps: OPSTR_GET_USAGE_STATS).
//  - chatAppDwellMillis(): WhatsApp/Telegram foreground dwell (foreground-dwell only)
//  - secondsSinceLeavingChat(): last transition timestamp
//  - leftAndReturnedWithinMs(): anti-cheat window (5 s rule)
//  - koi content nahi padha jaata, koi network nahi.
