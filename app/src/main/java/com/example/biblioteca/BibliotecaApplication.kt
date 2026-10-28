package com.example.biblioteca

import android.app.Application
import android.util.Log
import com.example.biblioteca.data.local.BibliotecaDatabase
import com.example.biblioteca.data.local.session.SessionManager
import com.example.biblioteca.data.repository.BibliotecaRepository

class BibliotecaApplication : Application() {

    val database by lazy { BibliotecaDatabase.getDatabase(this) }
    val sessionManager by lazy { SessionManager(this) }
    val repository by lazy {
        BibliotecaRepository(
            libroDao = database.libroDao(),
            prestamoDao = database.prestamoDao(),
            usuarioDao = database.usuarioDao(),
            solicitudPrestamoDao = database.solicitudPrestamoDao(),
            multaDao = database.multaDao(),
            sessionManager = sessionManager
        )
    }

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("BIBLIOTECA_CRASH", "FATAL CRASH in thread ${thread.name}: ${throwable.message}", throwable)
            try {
                getSharedPreferences("crash_logs", MODE_PRIVATE)
                    .edit()
                    .putString("last_crash", "${throwable.javaClass.simpleName}: ${throwable.message}\n${Log.getStackTraceString(throwable)}")
                    .apply()
            } catch (ignored: Exception) {
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
