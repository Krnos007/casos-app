package com.kronosempire.casos.ui.security

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivityPinBinding
import com.kronosempire.casos.security.EmailRecoveryHelper
import com.kronosempire.casos.security.PinHelper
import com.kronosempire.casos.security.SecurityManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPinBinding
    private lateinit var securityManager: SecurityManager
    private lateinit var pinHelper: PinHelper
    private lateinit var recoveryHelper: EmailRecoveryHelper
    private var isForSetup = false
    private var tempPin = ""
    private var countDownTimer: CountDownTimer? = null

    companion object {
        private const val REQUEST_RECOVERY = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securityManager = SecurityManager.getInstance(this)
        pinHelper = PinHelper(this)
        recoveryHelper = EmailRecoveryHelper(this)
        isForSetup = intent.getBooleanExtra("setup", false)

        setupToolbar()
        setupPinPad()

        if (isForSetup) {
            setupSecurityMode()
        } else {
            if (!securityManager.hasPin()) {
                unlockAndFinish()
                return
            }
            binding.tvTitle.text = "Ingrese su PIN"
            binding.tvSubtitle.text = "Para acceder a CASOS"
            binding.tvCompanyInfo.text = "? 2026 Kronos Empire"
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = if (isForSetup) "Configurar PIN" else "Verificaci車n"
    }

    private fun setupSecurityMode() {
        binding.tvTitle.text = "? Configurar PIN de Seguridad"
        binding.tvSubtitle.text = "Ingrese un PIN de 4 d赤gitos"
        binding.btnForgotPin.visibility = View.GONE
        binding.tvCompanyInfo.text = "? 2026 Kronos Empire"
    }

    private fun setupPinPad() {
        val buttons = listOf(
            binding.btn0, binding.btn1, binding.btn2,
            binding.btn3, binding.btn4, binding.btn5,
            binding.btn6, binding.btn7, binding.btn8,
            binding.btn9
        )

        buttons.forEachIndexed { index, button ->
            button.text = index.toString()
            button.setOnClickListener { onNumberClick(index.toString()) }
        }

        binding.btnDelete.setOnClickListener { onDeleteClick() }
        binding.btnClear.setOnClickListener { onClearClick() }

        if (!isForSetup) {
            binding.btnForgotPin.setOnClickListener { onForgotPin() }
        } else {
            binding.btnForgotPin.visibility = View.GONE
        }
    }

    private fun onNumberClick(number: String) {
        val currentPin = binding.etPin.text.toString()
        if (currentPin.length < 4) {
            binding.etPin.append(number)
            updatePinIndicators()

            if (binding.etPin.text.length == 4) {
                if (isForSetup) {
                    handleSetupPin()
                } else {
                    handleVerifyPin()
                }
            }
        }
    }

    private fun onDeleteClick() {
        val currentPin = binding.etPin.text.toString()
        if (currentPin.isNotEmpty()) {
            binding.etPin.setText(currentPin.dropLast(1))
            updatePinIndicators()
        }
    }

    private fun onClearClick() {
        binding.etPin.setText("")
        updatePinIndicators()
    }

    private fun updatePinIndicators() {
        val pinLength = binding.etPin.text.length
        val indicators = listOf(
            binding.dot1, binding.dot2, binding.dot3, binding.dot4
        )
        indicators.forEachIndexed { index, view ->
            view.isSelected = index < pinLength
        }
    }

    private fun handleSetupPin() {
        val pin = binding.etPin.text.toString()
        if (pin.length != 4) {
            Toast.makeText(this, "Ingrese un PIN de 4 d赤gitos", Toast.LENGTH_SHORT).show()
            return
        }

        if (tempPin.isEmpty()) {
            tempPin = pin
            binding.etPin.setText("")
            updatePinIndicators()
            binding.tvSubtitle.text = "Confirme su PIN"
            Toast.makeText(this, "Confirme su PIN", Toast.LENGTH_SHORT).show()
        } else {
            if (pin == tempPin) {
                if (securityManager.setPin(pin)) {
                    Toast.makeText(this, "? PIN configurado exitosamente", Toast.LENGTH_LONG).show()
                    if (securityManager.isBiometricAvailable()) {
                        showBiometricSetupDialog()
                    } else {
                        showRecoverySetupDialog()
                    }
                } else {
                    Toast.makeText(this, "? Error al guardar PIN", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "? Los PIN no coinciden", Toast.LENGTH_SHORT).show()
                tempPin = ""
                binding.etPin.setText("")
                updatePinIndicators()
                binding.tvSubtitle.text = "Ingrese su PIN de 4 d赤gitos"
            }
        }
    }

    private fun handleVerifyPin() {
        val pin = binding.etPin.text.toString()

        if (pinHelper.checkAttempts("pin")) {
            if (securityManager.verifyPin(pin)) {
                pinHelper.registerAttempt("pin", true)
                unlockAndFinish()
            } else {
                pinHelper.registerAttempt("pin", false)
                val remaining = pinHelper.getRemainingAttempts("pin")

                if (remaining <= 0) {
                    Toast.makeText(this, "Demasiados intentos. Espere 5 minutos", Toast.LENGTH_LONG).show()
                    binding.etPin.setText("")
                    updatePinIndicators()
                    binding.etPin.isEnabled = false
                    startBlockTimer()
                } else {
                    Toast.makeText(this, "? PIN incorrecto. $remaining intentos restantes", Toast.LENGTH_SHORT).show()
                    binding.etPin.setText("")
                    updatePinIndicators()
                }
            }
        } else {
            Toast.makeText(this, "Demasiados intentos. Espere 5 minutos", Toast.LENGTH_LONG).show()
            binding.etPin.isEnabled = false
            startBlockTimer()
        }
    }

    private fun startBlockTimer() {
        countDownTimer = object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                binding.tvSubtitle.text = "Espere ${minutes}m ${seconds}s"
            }

            override fun onFinish() {
                binding.etPin.isEnabled = true
                binding.tvSubtitle.text = "Ingrese su PIN"
                Toast.makeText(
                    this@PinActivity,
                    "Ya puede intentar de nuevo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.start()
    }

    private fun showBiometricSetupDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("? Configurar Huella Digital")
            .setMessage("?Desea configurar el acceso por huella digital?")
            .setPositiveButton("S赤") { _, _ ->
                securityManager.setSecurityLevel(SecurityManager.SecurityLevel.BIOMETRIC)
                Toast.makeText(this, "? Huella digital configurada", Toast.LENGTH_LONG).show()
                showRecoverySetupDialog()
            }
            .setNegativeButton("No") { _, _ ->
                showRecoverySetupDialog()
            }
            .show()
    }

    private fun showRecoverySetupDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("? Configurar Recuperaci車n")
            .setMessage("""
                ?Desea configurar la recuperaci車n por correo?

                Esto le permitir芍 recuperar su acceso si olvida su PIN.

                ? 2026 Kronos Empire
            """.trimIndent())
            .setPositiveButton("Configurar") { _, _ ->
                startActivity(Intent(this, SetupSecurityActivity::class.java))
                finish()
            }
            .setNegativeButton("Ahora no") { _, _ ->
                finish()
            }
            .show()
    }

    private fun onForgotPin() {
        if (recoveryHelper.hasRecoverySetup()) {
            val intent = Intent(this, RecoveryActivity::class.java)
            startActivityForResult(intent, REQUEST_RECOVERY)
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("? Recuperaci車n de PIN")
                .setMessage("""
                    Para recuperar su PIN, primero debe configurar:
                    ? Correo electr車nico de recuperaci車n
                    ? Pregunta de seguridad

                    ?Desea configurar la recuperaci車n ahora?

                    ? 2026 Kronos Empire
                """.trimIndent())
                .setPositiveButton("Configurar") { _, _ ->
                    startActivity(Intent(this, SetupSecurityActivity::class.java))
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_RECOVERY -> {
                if (resultCode == RESULT_OK) {
                    unlockAndFinish()
                }
            }
        }
    }

    private fun unlockAndFinish() {
        securityManager.unlockApp()
        setResult(RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    override fun onBackPressed() {
        if (!isForSetup) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }
}
