package com.kronosempire.casos

import android.app.Application
import com.kronosempire.casos.data.database.AppDatabase
import com.kronosempire.casos.data.repository.RegistroRepository

class CASOSApplication : Application() {
    companion object {
        lateinit var instance: CASOSApplication
        private set
        lateinit var repository: RegistroRepository
        private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val database = AppDatabase.getDatabase(this)
        repository = RegistroRepository(
            database.registroDao(),
            database.detalleDao()
        )
    }
}
