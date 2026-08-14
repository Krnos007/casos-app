package com.kronosempire.casos.security

import android.os.Build

object BiometricHelper {
    enum class BiometricStatus {
        SUPPORTED, NOT_SUPPORTED, ERROR
    }

    fun isHardwareAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun hasEnrolledFingerprints(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    fun getBiometricStatus(): BiometricStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            BiometricStatus.SUPPORTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BiometricStatus.SUPPORTED
        } else {
            BiometricStatus.NOT_SUPPORTED
        }
    }
}
