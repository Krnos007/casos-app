package com.kronosempire.casos.ui.security

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivityRecoveryBinding
import com.kronosempire.casos.security.EmailRecoveryHelper
import com.kronosempire.casos.security.SecurityManager

class RecoveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecoveryBinding
    private lateinit var recoveryHelper: EmailRecoveryHelper
    private lateinit var securityManager: SecurityManager
    private var recoveryStep = 0
    private var countDownTimer: CountDownTimer? = null
    private var emailForRecovery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recoveryHelper = EmailRecoveryHelper(this)
        securityManager = SecurityManager.getInstance(this)

        setupToolbar()
        setupListeners()
        showStep(0)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Recuperaci車n de Acceso"
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            when (recoveryStep) {
                0 -> handleEmailStep()
                1 -> handleQuestionStep()
                2 -> handleCodeStep()
                3 -> handleNewPinStep()
            }
        }

        binding.btnResendCode.setOnClickListener { resendCode() }

        binding.btnBack.setOnClickListener {
            if (recoveryStep > 0) {
                showStep(recoveryStep - 1)
            } else {
                onBackPressed()
            }
        }

        binding.btnResendCode.isEnabled = false
    }

    private fun showStep(step: Int) {
        recoveryStep = step
        when (step) {
            0 -> {
                binding.stepIndicator.text = "Paso 1/4"
                binding.tvTitle.text = "? Correo de Recuperaci車n"
                binding.tvSubtitle.text = "Ingrese el correo registrado para recuperar su acceso"
                binding.etInput.hint = "correo@ejemplo.com"
                binding.etInput.setText("")
                binding.etInput.visibility = android.view.View.VISIBLE
                binding.tvQuestion.visibility = android.view.View.GONE
                binding.etAnswer.visibility = android.view.View.GONE
                binding.tvCodeInfo.visibility = android.view.View.GONE
                binding.etCode.visibility = android.view.View.GONE
                binding.btnResendCode.visibility = android.view.View.GONE
                binding.tvTimer.visibility = android.view.View.GONE
                binding.btnNext.text = "Siguiente ↙"
                binding.btnBack.visibility = if (step == 0) android.view.View.GONE else android.view.View.VISIBLE
                binding.tvCompanyInfo.text = "? 2026 Kronos Empire"
            }
            1 -> {
                binding.stepIndicator.text = "Paso 2/4"
                binding.tvTitle.text = "? Pregunta de Seguridad"
                binding.tvSubtitle.text = "Responda la pregunta de seguridad"
                binding.etInput.visibility = android.view.View.GONE
                binding.tvQuestion.visibility = android.view.View.VISIBLE
                binding.etAnswer.visibility = android.view.View.VISIBLE
                binding.tvQuestion.text = recoveryHelper.getSecurityQuestion() ?: "?Cu芍l es tu mascota favorita?"
                binding.etAnswer.setText("")
                binding.tvCodeInfo.visibility = android.view.View.GONE
                binding.etCode.visibility = android.view.View.GONE
                binding.btnResendCode.visibility = android.view.View.GONE
                binding.tvTimer.visibility = android.view.View.GONE
                binding.btnNext.text = "Verificar ↙"
                binding.btnBack.visibility = android.view.View.VISIBLE
                binding.tvCompanyInfo.text = "? 2026 Kronos Empire"
            }
            2 -> {
                binding.stepIndicator.text = "Paso 3/4"
                binding.tvTitle.text = "? C車digo de Verificaci車n"
                binding.tvSubtitle.text = "Ingrese el c車digo enviado a su correo"
                binding.etInput.visibility = android.view.View.GONE
                binding.tvQuestion.visibility = android.view.View.GONE
                binding.etAnswer.visibility = android.view.View.GONE
                binding.tvCodeInfo.visibility = android.view.View.VISIBLE
                binding.etCode.visibility = android.view.View.VISIBLE
                binding.etCode.setText("")
                binding.btnResendCode.visibility = android.view.View.VISIBLE
                binding.tvTimer.visibility = android.view.View.VISIBLE
                binding.btnNext.text = "Verificar C車digo ↙"
                binding.btnBack.visibility = android.view.View.VISIBLE
                binding.tvCompanyInfo.text = "? 2026 Kronos Empire"
                startResendTimer()
            }
            3 -> {
                binding.stepIndicator.text = "Paso 4/4"
                binding.tvTitle.text = "? Nuevo PIN"
                binding.tvSubtitle.text = "Configure un nuevo PIN de acceso (4 d赤gitos)"
                binding.etInput.visibility = android.view.View.VISIBLE
                binding.etInput.hint = "Nuevo PIN (4 d赤gitos)"
                binding.etInput.setText("")
                binding.tvQuestion.visibility = android.view.View.GONE
                binding.etAnswer.visibility = android.view.View.GONE
                binding.tvCodeInfo.visibility = android.view.View.GONE
                binding.etCode.visibility = android.view.View.GONE
                binding.btnResendCode.visibility = android.view.View.GONE
                binding.tvTimer.visibility = android.view.View.GONE
                binding.btnNext.text = "? Restablecer Acceso"
                binding.btnBack.visibility = android.view.View.VISIBLE
                binding.tvCompanyInfo.text = "? 2026 Kronos Empire"
            }
        }
    }

    private fun handleEmailStep() {
        val email = binding.etInput.text.toString().trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "? Correo inv芍lido", Toast.LENGTH_SHORT).show()
            return
        }

        val registeredEmail = recoveryHelper.getRecoveryEmail()
        if (registeredEmail == null || registeredEmail != email) {
            Toast.makeText(this, "? Correo no registrado", Toast.LENGTH_SHORT).show()
            return
        }

        emailForRecovery = email
        showStep(1)
    }

    private fun handleQuestionStep() {
        val answer = binding.etAnswer.text.toString().trim()
        if (answer.isEmpty()) {
            Toast.makeText(this, "Ingrese la respuesta", Toast.LENGTH_SHORT).show()
            return
        }

        if (recoveryHelper.verifySecurityAnswer(answer)) {
            if (recoveryHelper.sendRecoveryCode(emailForRecovery)) {
                Toast.makeText(
                    this,
                    "? C車digo enviado a $emailForRecovery",
                    Toast.LENGTH_LONG
                ).show()
                showStep(2)
            } else {
                Toast.makeText(
                    this,
                    "? Error al enviar c車digo. Verifique su conexi車n.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(this, "? Respuesta incorrecta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCodeStep() {
        val code = binding.etCode.text.toString().trim()
        if (code.length != 6) {
            Toast.makeText(this, "? C車digo inv芍lido (6 d赤gitos)", Toast.LENGTH_SHORT).show()
            return
        }

        if (recoveryHelper.verifyRecoveryCode(code)) {
            Toast.makeText(this, "? C車digo verificado", Toast.LENGTH_SHORT).show()
            showStep(3)
        } else {
            Toast.makeText(this, "? C車digo incorrecto o expirado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleNewPinStep() {
        val pin = binding.etInput.text.toString().trim()
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            Toast.makeText(this, "? Ingrese un PIN de 4 d赤gitos", Toast.LENGTH_SHORT).show()
            return
        }

        if (securityManager.setPin(pin)) {
            recoveryHelper.clearRecoveryData()
            Toast.makeText(
                this,
                "? PIN restablecido exitosamente",
                Toast.LENGTH_LONG
            ).show()
            securityManager.unlockApp()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "? Error al restablecer PIN", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = "?? Reenviar en ${seconds}s"
                binding.btnResendCode.isEnabled = false
            }

            override fun onFinish() {
                binding.tvTimer.text = "?? Listo para reenviar"
                binding.btnResendCode.isEnabled = true
            }
        }.start()
    }

    private fun resendCode() {
        if (recoveryHelper.sendRecoveryCode(emailForRecovery)) {
            Toast.makeText(
                this,
                "? Nuevo c車digo enviado a $emailForRecovery",
                Toast.LENGTH_LONG
            ).show()
            startResendTimer()
        } else {
            Toast.makeText(
                this,
                "? Error al enviar c車digo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
