package com.kronosempire.casos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kronosempire.casos.data.model.Registro
import com.kronosempire.casos.data.model.DetalleCaso

@Database(
    entities = [Registro::class, DetalleCaso::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun registroDao(): RegistroDao
    abstract fun detalleDao(): DetalleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "casos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
