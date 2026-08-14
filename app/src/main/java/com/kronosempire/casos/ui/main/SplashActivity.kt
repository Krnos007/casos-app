package com.kronosempire.casos.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivitySplashBinding
import com.kronosempire.casos.security.SecurityManager

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var securityManager: SecurityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securityManager = SecurityManager.getInstance(this)

        binding.tvTitle.text = "CASOS"
        binding.tvSubtitle.text = "Sistema de Gesti¨®n de Investigaciones"
        binding.tvCopyright.text = "? 2026 Kronos Empire"
        binding.tvRights.text = "Todos los derechos reservados"
        binding.tvDevelopedBy.text = "Desarrollado por Kronos Empire"
        binding.tvContact.text = "? kronosempire79@gmail.com"

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)
    }
}
