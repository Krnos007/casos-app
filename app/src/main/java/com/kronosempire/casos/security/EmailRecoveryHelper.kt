package com.kronosempire.casos.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kronosempire.casos.utils.sha256
import java.security.SecureRandom
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailRecoveryHelper(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "recovery_prefs"
        private const val KEY_EMAIL = "recovery_email"
        private const val KEY_CODE = "recovery_code"
        private const val KEY_CODE_TIMESTAMP = "recovery_code_timestamp"
        private const val KEY_QUESTION = "recovery_question"
        private const val KEY_ANSWER = "recovery_answer"
        private const val CODE_EXPIRATION_MS = 300000

        private const val SMTP_HOST = "smtp.gmail.com"
        private const val SMTP_PORT = "587"
        private const val SMTP_USER = "kronosempire79@gmail.com"
        private const val SMTP_PASSWORD = "tu_contrase?a_app"
        private const val FROM_EMAIL = "kronosempire79@gmail.com"
        private const val FROM_NAME = "Kronos Empire - Seguridad CASOS"
    }

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun setRecoveryEmail(email: String): Boolean {
        if (!isValidEmail(email)) return false
        prefs.edit().putString(KEY_EMAIL, email).apply()
        return true
    }

    fun getRecoveryEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun setSecurityQuestion(question: String, answer: String) {
        prefs.edit().apply {
            putString(KEY_QUESTION, question)
            putString(KEY_ANSWER, answer.sha256())
            apply()
        }
    }

    fun verifySecurityAnswer(answer: String): Boolean {
        val storedAnswer = prefs.getString(KEY_ANSWER, null)
        if (storedAnswer == null) return true
        return answer.sha256() == storedAnswer
    }

    fun getSecurityQuestion(): String? {
        return prefs.getString(KEY_QUESTION, null)
    }

    fun sendRecoveryCode(email: String): Boolean {
        val recoveryEmail = getRecoveryEmail()
        if (recoveryEmail == null || recoveryEmail != email) {
            return false
        }

        val code = generateRecoveryCode()
        val timestamp = System.currentTimeMillis()

        prefs.edit().apply {
            putString(KEY_CODE, code)
            putLong(KEY_CODE_TIMESTAMP, timestamp)
            apply()
        }

        return sendEmail(
            to = email,
            subject = "? C¨®digo de Recuperaci¨®n - CASOS",
            body = """
¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T
KRONOS EMPIRE - SISTEMA CASOS
¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T

Hola,

Has solicitado un c¨®digo de recuperaci¨®n para tu cuenta de CASOS.

? C?DIGO DE VERIFICACI?N: $code

?? Este c¨®digo expirar¨¢ en 5 minutos.

Si NO solicitaste este c¨®digo, ignora este mensaje
y contacta a soporte inmediatamente.

? Protege tu informaci¨®n.

©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤
? 2026 Kronos Empire
Todos los derechos reservados.
Soporte: kronosempire79@gmail.com
¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T¨T
            """.trimIndent()
        )
    }

    fun verifyRecoveryCode(code: String): Boolean {
        val storedCode = prefs.getString(KEY_CODE, null)
        val timestamp = prefs.getLong(KEY_CODE_TIMESTAMP, 0)
        if (storedCode == null || timestamp == 0L) return false
        if (System.currentTimeMillis() - timestamp > CODE_EXPIRATION_MS) {
            return false
        }
        return code == storedCode
    }

    private fun generateRecoveryCode(): String {
        val random = SecureRandom()
        val code = StringBuilder()
        for (i in 0..5) {
            code.append(random.nextInt(10))
        }
        return code.toString()
    }

    private fun sendEmail(to: String, subject: String, body: String): Boolean {
        return try {
            val props = Properties().apply {
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.connectiontimeout", "10000")
                put("mail.smtp.timeout", "10000")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SMTP_USER, SMTP_PASSWORD)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(FROM_EMAIL, FROM_NAME))
                setRecipient(Message.RecipientType.TO, InternetAddress(to))
                setSubject(subject)
                setText(body)
                sentDate = Date()
            }

            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearRecoveryData() {
        prefs.edit().clear().apply()
    }

    fun hasRecoverySetup(): Boolean {
        return getRecoveryEmail() != null && getSecurityQuestion() != null
    }
}
