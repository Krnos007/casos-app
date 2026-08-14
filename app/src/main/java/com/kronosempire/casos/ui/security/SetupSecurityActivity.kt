package com.kronosempire.casos.ui.security

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivitySetupSecurityBinding
import com.kronosempire.casos.security.EmailRecoveryHelper
import com.kronosempire.casos.security.SecurityManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SetupSecurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupSecurityBinding
    private lateinit var securityManager: SecurityManager
    private lateinit var recoveryHelper: EmailRecoveryHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securityManager = SecurityManager.getInstance(this)
        recoveryHelper = EmailRecoveryHelper(this)

        setupToolbar()
        setupOptions()
        loadCurrentSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Seguridad"
    }

    private fun setupOptions() {
        binding.btnSetupPin.setOnClickListener {
            startActivity(Intent(this, PinActivity::class.java).apply {
                putExtra("setup", true)
            })
        }

        binding.btnSetupBiometric.setOnClickListener {
            if (securityManager.isBiometricAvailable()) {
                showBiometricSetup()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("? No disponible")
                    .setMessage("Su dispositivo no soporta huella digital o no tiene configurada ninguna.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        binding.btnSetupTwoFactor.setOnClickListener {
            showTwoFactorSetup()
        }

        binding.btnSetupRecovery.setOnClickListener {
            showRecoverySetup()
        }

        binding.btnResetSecurity.setOnClickListener {
            showResetDialog()
        }
    }

    private fun loadCurrentSettings() {
        val level = securityManager.getCurrentSecurityLevel()
        val hasPin = securityManager.hasPin()
        val hasBiometric = securityManager.isBiometricAvailable()
        val hasTwoFactor = securityManager.isTwoFactorEnabled()
        val hasRecovery = recoveryHelper.hasRecoverySetup()

        binding.tvStatusPin.text = if (hasPin) "? Configurado" else "? No configurado"
        binding.tvStatusBiometric.text = if (hasBiometric) "? Disponible" else "? No disponible"
        binding.tvStatusTwoFactor.text = if (hasTwoFactor) "? Activo" else "? Inactivo"
        binding.tvStatusRecovery.text = if (hasRecovery) "? Configurado" else "? No configurado"

        binding.tvSecurityLevel.text = when (level) {
            SecurityManager.SecurityLevel.NONE -> "? Sin seguridad"
            SecurityManager.SecurityLevel.PIN -> "? PIN"
            SecurityManager.SecurityLevel.BIOMETRIC -> "? Huella digital"
            SecurityManager.SecurityLevel.TWO_FACTOR -> "? 2FA (PIN + Huella)"
        }

        binding.tvCompanyInfo.text = "? 2026 Kronos Empire\nTodos los derechos reservados"
    }

    private fun showBiometricSetup() {
        val prompt = securityManager.getBiometricPrompt(
            onSuccess = {
                securityManager.setSecurityLevel(SecurityManager.SecurityLevel.BIOMETRIC)
                loadCurrentSettings()
                runOnUiThread {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("? Configurado")
                        .setMessage("Huella digital configurada exitosamente")
                        .setPositiveButton("OK", null)
                        .show()
                }
            },
            onFailed = {
                runOnUiThread {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("? Error")
                        .setMessage("No se pudo configurar la huella digital")
                        .setPositiveButton("OK", null)
                        .show()
                }
            },
            onError = { code, message ->
                runOnUiThread {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("? Error")
                        .setMessage("Error: $message")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        )
        val promptInfo = securityManager.createBiometricPromptInfo()
        prompt.authenticate(promptInfo)
    }

    private fun showTwoFactorSetup() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recovery_setup, null)
        val etQuestion = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRecoveryQuestion)
        val etAnswer = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRecoveryAnswer)

        MaterialAlertDialogBuilder(this)
            .setTitle("? Configurar 2FA")
            .setMessage("Configure una pregunta de seguridad adicional para la verificaci車n en 2 pasos")
            .setView(dialogView)
            .setPositiveButton("Configurar") { _, _ ->
                val question = etQuestion.text.toString().trim()
                val answer = etAnswer.text.toString().trim()

                if (question.isNotEmpty() && answer.isNotEmpty()) {
                    if (!securityManager.hasPin()) {
                        showPinSetupFirst()
                        return@setPositiveButton
                    }

                    recoveryHelper.setSecurityQuestion(question, answer)
                    securityManager.setSecurityLevel(SecurityManager.SecurityLevel.TWO_FACTOR)
                    loadCurrentSettings()

                    MaterialAlertDialogBuilder(this)
                        .setTitle("? Configurado")
                        .setMessage("2FA configurado exitosamente")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    Toast.makeText(this, "Complete ambos campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRecoverySetup() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recovery_setup, null)
        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRecoveryEmail)
        val etQuestion = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRecoveryQuestion)
        val etAnswer = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRecoveryAnswer)

        etEmail.setText(recoveryHelper.getRecoveryEmail() ?: "")
        etQuestion.setText(recoveryHelper.getSecurityQuestion() ?: "")

        MaterialAlertDialogBuilder(this)
            .setTitle("? Recuperaci車n por Correo")
            .setMessage("Configure un correo y pregunta de seguridad para recuperar su acceso")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val email = etEmail.text.toString().trim()
                val question = etQuestion.text.toString().trim()
                val answer = etAnswer.text.toString().trim()

                if (email.isEmpty() || question.isEmpty() || answer.isEmpty()) {
                    Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Correo inv芍lido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                recoveryHelper.setRecoveryEmail(email)
                recoveryHelper.setSecurityQuestion(question, answer)

                Toast.makeText(this, "? Recuperaci車n configurada", Toast.LENGTH_LONG).show()
                loadCurrentSettings()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPinSetupFirst() {
        MaterialAlertDialogBuilder(this)
            .setTitle("? PIN necesario")
            .setMessage("Para configurar 2FA, primero debe configurar un PIN")
            .setPositiveButton("Configurar PIN") { _, _ ->
                startActivity(Intent(this, PinActivity::class.java).apply {
                    putExtra("setup", true)
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showResetDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("?? Restablecer Seguridad")
            .setMessage("?Est芍 seguro de que desea eliminar toda la configuraci車n de seguridad?")
            .setPositiveButton("Restablecer") { _, _ ->
                securityManager.resetSecurity()
                recoveryHelper.clearRecoveryData()
                loadCurrentSettings()

                MaterialAlertDialogBuilder(this)
                    .setTitle("? Restablecido")
                    .setMessage("La seguridad ha sido restablecida")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadCurrentSettings()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
