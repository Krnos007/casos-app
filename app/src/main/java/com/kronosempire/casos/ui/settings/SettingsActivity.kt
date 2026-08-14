package com.kronosempire.casos.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivitySettingsBinding
import com.kronosempire.casos.security.EmailRecoveryHelper
import com.kronosempire.casos.security.SecurityManager
import com.kronosempire.casos.ui.security.SetupSecurityActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var securityManager: SecurityManager
    private lateinit var recoveryHelper: EmailRecoveryHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securityManager = SecurityManager.getInstance(this)
        recoveryHelper = EmailRecoveryHelper(this)

        setupToolbar()
        setupOptions()
        updateSecurityStatus()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuraci¨®n"
    }

    private fun setupOptions() {
        binding.btnSecurity.setOnClickListener {
            startActivity(Intent(this, SetupSecurityActivity::class.java))
        }

        binding.btnAbout.setOnClickListener { showAboutDialog() }
        binding.btnContact.setOnClickListener { showContactDialog() }
        binding.btnTerms.setOnClickListener { showTermsDialog() }
    }

    private fun updateSecurityStatus() {
        val level = securityManager.getCurrentSecurityLevel()
        val levelText = when (level) {
            SecurityManager.SecurityLevel.NONE -> "? Sin seguridad"
            SecurityManager.SecurityLevel.PIN -> "? PIN activo"
            SecurityManager.SecurityLevel.BIOMETRIC -> "? Huella activa"
            SecurityManager.SecurityLevel.TWO_FACTOR -> "? 2FA activo"
        }

        binding.tvSecurityStatus.text = levelText

        val hasRecovery = recoveryHelper.hasRecoverySetup()
        binding.tvStatusRecovery.text = if (hasRecovery) "? Configurado" else "? No configurado"

        binding.tvCompanyInfo.text = "? 2026 Kronos Empire\nTodos los derechos reservados"
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sobre CASOS")
            .setMessage("""
                CASOS v1.0
                Gestor de Investigaciones Criminales

                Desarrollado para el Sgto. Rey David Dom¨ªnguez P¨¦rez

                ? Sistema de seguridad integrado
                ? Offline 100%
                ? B¨²squeda avanzada
                ? Cierres autom¨¢ticos
                ? Recuperaci¨®n por correo

                ©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤
                ? 2026 Kronos Empire
                Todos los derechos reservados.
                Soporte: kronosempire79@gmail.com
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showContactDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("? Contacto de Soporte")
            .setMessage("""
                Para soporte t¨¦cnico, consultas o reportar problemas:

                ? Correo: kronosempire79@gmail.com
                ?? Tiempo de respuesta: 24-48 horas

                ©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤©¤
                ? 2026 Kronos Empire
            """.trimIndent())
            .setPositiveButton("Enviar Correo") { _, _ ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:kronosempire79@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Soporte CASOS - Consulta")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun showTermsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("T¨¦rminos y Condiciones")
            .setMessage("""
                CASOS es una aplicaci¨®n propiedad de Kronos Empire.

                Esta aplicaci¨®n est¨¢ dise?ada exclusivamente para uso oficial en investigaciones.

                Los datos almacenados son propiedad de Kronos Empire.

                Queda prohibida la distribuci¨®n, modificaci¨®n o uso no autorizado.

                Para soporte: kronosempire79@gmail.com

                ? 2026 Kronos Empire. Todos los derechos reservados.
            """.trimIndent())
            .setPositiveButton("Aceptar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateSecurityStatus()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
