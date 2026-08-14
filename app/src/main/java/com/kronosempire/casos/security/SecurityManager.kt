package com.kronosempire.casos.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kronosempire.casos.utils.sha256
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SecurityManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SecurityManager? = null

        const val COMPANY_NAME = "Kronos Empire"
        const val COPYRIGHT_YEAR = "2026"
        const val SUPPORT_EMAIL = "kronosempire79@gmail.com"

        fun getInstance(context: Context): SecurityManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SecurityManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val prefs: SharedPreferences
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked
    private val _securityLevel = MutableStateFlow(SecurityLevel.NONE)
    val securityLevel: StateFlow<SecurityLevel> = _securityLevel

    enum class SecurityLevel {
        NONE, PIN, BIOMETRIC, TWO_FACTOR
    }

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            "security_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val savedLevel = prefs.getString("security_level", SecurityLevel.NONE.name)
        _securityLevel.value = SecurityLevel.valueOf(savedLevel ?: "NONE")
    }

    fun setPin(pin: String): Boolean {
        if (pin.length < 4) return false
        prefs.edit().putString("user_pin", pin.sha256()).apply()
        if (_securityLevel.value == SecurityLevel.NONE) {
            _securityLevel.value = SecurityLevel.PIN
            prefs.edit().putString("security_level", SecurityLevel.PIN.name).apply()
        }
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val storedPin = prefs.getString("user_pin", null)
        if (storedPin == null) return true
        return pin.sha256() == storedPin
    }

    fun hasPin(): Boolean {
        return prefs.getString("user_pin", null) != null
    }

    fun setSecurityLevel(level: SecurityLevel) {
        _securityLevel.value = level
        prefs.edit().putString("security_level", level.name).apply()
        _isLocked.value = true
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun isTwoFactorEnabled(): Boolean {
        return _securityLevel.value == SecurityLevel.TWO_FACTOR
    }

    fun getBiometricPrompt(
        onSuccess: () -> Unit,
        onFailed: () -> Unit,
        onError: (Int, String) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                _isLocked.value = false
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString.toString())
            }
        }
        return BiometricPrompt(context, executor, callback)
    }

    fun createBiometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verificaci¨®n de Seguridad")
            .setSubtitle("Autent¨ªquese para acceder a CASOS")
            .setDescription("Use su huella digital o PIN del dispositivo")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    fun unlockApp() {
        _isLocked.value = false
    }

    fun lockApp() {
        _isLocked.value = true
    }

    fun isAppLocked(): Boolean {
        if (_securityLevel.value == SecurityLevel.NONE) {
            _isLocked.value = false
            return false
        }
        return _isLocked.value
    }

    fun getCurrentSecurityLevel(): SecurityLevel {
        return _securityLevel.value
    }

    fun resetSecurity() {
        prefs.edit().clear().apply()
        _securityLevel.value = SecurityLevel.NONE
        _isLocked.value = false
    }
}
