package com.kronosempire.casos.security

import android.content.Context

class PinHelper(private val context: Context) {
    companion object {
        const val PIN_LENGTH = 4
        const val MAX_ATTEMPTS = 5
        const val BLOCK_TIME_MS = 300000 // 5 minutos
    }

    private val attempts = mutableMapOf<String, Int>()
    private val blockedUntil = mutableMapOf<String, Long>()

    fun isValidPin(pin: String): Boolean {
        return pin.length == PIN_LENGTH && pin.all { it.isDigit() }
    }

    fun checkAttempts(key: String): Boolean {
        val currentAttempts = attempts[key] ?: 0
        if (currentAttempts >= MAX_ATTEMPTS) {
            val blockedTime = blockedUntil[key] ?: 0
            if (System.currentTimeMillis() < blockedTime) {
                return false
            } else {
                attempts[key] = 0
                blockedUntil.remove(key)
                return true
            }
        }
        return true
    }

    fun registerAttempt(key: String, success: Boolean) {
        if (!success) {
            val currentAttempts = (attempts[key] ?: 0) + 1
            attempts[key] = currentAttempts
            if (currentAttempts >= MAX_ATTEMPTS) {
                blockedUntil[key] = System.currentTimeMillis() + BLOCK_TIME_MS
            }
        } else {
            attempts[key] = 0
            blockedUntil.remove(key)
        }
    }

    fun getRemainingAttempts(key: String): Int {
        return MAX_ATTEMPTS - (attempts[key] ?: 0)
    }

    fun isBlocked(key: String): Boolean {
        val blockedTime = blockedUntil[key] ?: 0
        return System.currentTimeMillis() < blockedTime
    }

    fun getBlockedTimeRemaining(key: String): Long {
        val blockedTime = blockedUntil[key] ?: 0
        return (blockedTime - System.currentTimeMillis()) / 1000
    }
}
