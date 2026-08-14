package com.kronosempire.casos.ui.security

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivityTwoFactorBinding
import com.kronosempire.casos.security.EmailRecoveryHelper
import com.kronosempire.casos.security.SecurityManager

class TwoFactorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTwoFactorBinding
    private lateinit var securityManager: SecurityManager
    private lateinit var recoveryHelper: EmailRecoveryHelper
    private var attempts = 0
    private val MAX_ATTEMPTS = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTwoFactorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securityManager = SecurityManager.getInstance(this)
        recoveryHelper = EmailRecoveryHelper(this)

        setupToolbar()
        loadQuestion()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Verificaci¨®n en 2 Pasos"
    }

    private fun loadQuestion() {
        val question = recoveryHelper.getSecurityQuestion()
        if (question != null) {
            binding.tvQuestion.text = question
            binding.tvCompanyInfo.text = "? 2026 Kronos Empire"
        } else {
            Toast.makeText(this, "Error: No hay pregunta configurada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnVerify.setOnClickListener { verifyAnswer() }
        binding.btnCancel.setOnClickListener { moveTaskToBack(true) }
    }

    private fun verifyAnswer() {
        val answer = binding.etAnswer.text.toString().trim()
        if (answer.isEmpty()) {
            Toast.makeText(this, "Ingrese la respuesta", Toast.LENGTH_SHORT).show()
            return
        }

        if (recoveryHelper.verifySecurityAnswer(answer)) {
            securityManager.unlockApp()
            Toast.makeText(this, "? Verificaci¨®n exitosa", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            attempts++
            if (attempts >= MAX_ATTEMPTS) {
                Toast.makeText(
                    this,
                    "? Demasiados intentos. Reinicie la aplicaci¨®n.",
                    Toast.LENGTH_LONG
                ).show()
                moveTaskToBack(true)
            } else {
                Toast.makeText(
                    this,
                    "? Respuesta incorrecta. ${MAX_ATTEMPTS - attempts} intentos restantes",
                    Toast.LENGTH_SHORT
                ).show()
                binding.etAnswer.text?.clear()
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
