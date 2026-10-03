package `in`.scamurai.satarkpay.signals

/**
 * M5 · AutoPay mandate signals (TODO finale).
 * NotificationListenerService se sirf mandate-type notifications parse hote hain (bank/UPI apps).
 */
// TODO(finale): NotificationListenerService → filters:
//  - "AutoPay mandate created/approved" · amount · frequency (Daily/Weekly/Monthly) · merchant
//  - "one-time" batakar daily wala mandate = RED (R20)
//  - notification text device par hi rehta hai; sirf parsed fields store hote hain
