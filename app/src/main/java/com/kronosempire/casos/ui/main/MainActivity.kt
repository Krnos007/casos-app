package com.kronosempire.casos.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kronosempire.casos.CASOSApplication
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivityMainBinding
import com.kronosempire.casos.security.SecurityManager
import com.kronosempire.casos.ui.add.AddRegistroActivity
import com.kronosempire.casos.ui.cierre.CierreActivity
import com.kronosempire.casos.ui.detail.DetailActivity
import com.kronosempire.casos.ui.search.SearchActivity
import com.kronosempire.casos.ui.security.PinActivity
import com.kronosempire.casos.ui.security.TwoFactorActivity
import com.kronosempire.casos.ui.settings.SettingsActivity
import com.kronosempire.casos.utils.DateUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: RegistroAdapter
    private lateinit var securityManager: SecurityManager

    companion object {
        private const val REQUEST_UNLOCK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securityManager = SecurityManager.getInstance(this)
        viewModel = MainViewModel(CASOSApplication.repository)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupChips()
        setupFab()
        checkSecurity()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "CASOS"
    }

    private fun setupRecyclerView() {
        adapter = RegistroAdapter(
            onItemClick = { registro ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("registro_id", registro.id)
                startActivity(intent)
            }
        )
        binding.rvRegistros.layoutManager = LinearLayoutManager(this)
        binding.rvRegistros.adapter = adapter

        viewModel.registros.observe(this) { registros ->
            adapter.submitList(registros)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupSearch() {
        binding.etBuscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    viewModel.buscarGlobal(query)
                    binding.ivClearSearch.visibility = View.VISIBLE
                } else if (query.isEmpty()) {
                    cargarRegistrosHoy()
                    binding.ivClearSearch.visibility = View.GONE
                }
            }
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etBuscador.text?.clear()
            cargarRegistrosHoy()
            binding.ivClearSearch.visibility = View.GONE
        }
    }

    private fun setupChips() {
        binding.chipHoy.setOnCheckedChangeListener { _, checked ->
            if (checked) cargarRegistrosHoy()
        }

        binding.chipMes.setOnCheckedChangeListener { _, checked ->
            if (checked) cargarRegistrosMes()
        }

        binding.chipCierre.setOnClickListener {
            startActivity(Intent(this, CierreActivity::class.java))
        }

        binding.chipBuscar.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddRegistroActivity::class.java))
        }
    }

    private fun checkSecurity() {
        val securityLevel = securityManager.getCurrentSecurityLevel()
        when {
            securityLevel == SecurityManager.SecurityLevel.NONE -> {
                showSecuritySetupDialog()
            }
            securityManager.isAppLocked() -> {
                showUnlockScreen()
            }
            else -> {
                cargarRegistrosHoy()
            }
        }
    }

    private fun showSecuritySetupDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("? Configurar Seguridad")
            .setMessage("""
                ?Desea configurar la seguridad de la aplicaci車n?

                Puede configurar:
                ? PIN de acceso
                ? Huella digital
                ? Verificaci車n en 2 pasos
                ? Recuperaci車n por correo

                Esto proteger芍 la informaci車n de las investigaciones.

                ? 2026 Kronos Empire
            """.trimIndent())
            .setPositiveButton("Configurar") { _, _ ->
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            .setNegativeButton("Ahora no") { _, _ ->
                cargarRegistrosHoy()
            }
            .setCancelable(false)
            .show()
    }

    private fun showUnlockScreen() {
        when {
            securityManager.isTwoFactorEnabled() -> {
                startActivityForResult(
                    Intent(this, TwoFactorActivity::class.java),
                    REQUEST_UNLOCK
                )
            }
            securityManager.isBiometricAvailable() -> {
                authenticateWithBiometric()
            }
            securityManager.hasPin() -> {
                startActivityForResult(
                    Intent(this, PinActivity::class.java),
                    REQUEST_UNLOCK
                )
            }
            else -> {
                securityManager.unlockApp()
                cargarRegistrosHoy()
            }
        }
    }

    private fun authenticateWithBiometric() {
        val prompt = securityManager.getBiometricPrompt(
            onSuccess = {
                securityManager.unlockApp()
                runOnUiThread { cargarRegistrosHoy() }
            },
            onFailed = {
                runOnUiThread {
                    if (securityManager.hasPin()) {
                        startActivityForResult(
                            Intent(this, PinActivity::class.java),
                            REQUEST_UNLOCK
                        )
                    } else {
                        securityManager.unlockApp()
                        cargarRegistrosHoy()
                    }
                }
            },
            onError = { code, message ->
                runOnUiThread {
                    if (securityManager.hasPin()) {
                        startActivityForResult(
                            Intent(this, PinActivity::class.java),
                            REQUEST_UNLOCK
                        )
                    } else {
                        securityManager.unlockApp()
                        cargarRegistrosHoy()
                    }
                }
            }
        )
        val promptInfo = securityManager.createBiometricPromptInfo()
        prompt.authenticate(promptInfo)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_UNLOCK -> {
                if (resultCode == RESULT_OK) {
                    cargarRegistrosHoy()
                } else {
                    finishAffinity()
                }
            }
        }
    }

    private fun cargarRegistrosHoy() {
        val fecha = DateUtils.getFechaActual()
        viewModel.cargarRegistrosPorFecha(fecha)
        binding.chipHoy.isChecked = true
        binding.chipMes.isChecked = false
    }

    private fun cargarRegistrosMes() {
        val fecha = DateUtils.getFechaActual()
        val inicio = DateUtils.getPrimerDiaMes(fecha)
        val fin = DateUtils.getUltimoDiaMes(fecha)
        viewModel.cargarRegistrosPorPeriodo(inicio, fin)
        binding.chipHoy.isChecked = false
        binding.chipMes.isChecked = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_cierre -> {
                startActivity(Intent(this, CierreActivity::class.java))
                true
            }
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (securityManager.isAppLocked()) {
            showUnlockScreen()
        }
        if (binding.etBuscador.text.isNullOrEmpty()) {
            cargarRegistrosHoy()
        }
    }
}
