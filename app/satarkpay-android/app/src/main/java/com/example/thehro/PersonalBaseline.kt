package com.example.thehro

import kotlin.math.ceil

/** Personal amount baseline from recent user-reported successes; never a seeded or hard-coded default. */
object PersonalBaseline {
    const val WINDOW_DAYS = 90L
    const val MIN_SAMPLES = 20

    /** Nearest-rank p95 in whole INR, or null when history is too small/invalid. */
    fun p95(amounts: Collection<Long>): Double? {
        val sorted = amounts.filter { it > 0L }.sorted()
        if (sorted.size < MIN_SAMPLES) return null
        val rank = ceil(sorted.size * 0.95).toInt().coerceIn(1, sorted.size)
        return sorted[rank - 1].toDouble()
    }
}
