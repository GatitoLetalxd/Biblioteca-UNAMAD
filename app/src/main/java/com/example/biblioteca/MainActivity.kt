package com.example.biblioteca

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.biblioteca.data.local.BibliotecaDatabase
import com.example.biblioteca.data.local.session.SessionManager
import com.example.biblioteca.data.repository.BibliotecaRepository
import com.example.biblioteca.ui.navigation.BibliotecaApp
import com.example.biblioteca.ui.theme.BibliotecaTheme
import com.example.biblioteca.ui.viewmodel.BibliotecaViewModel
import com.example.biblioteca.ui.viewmodel.BibliotecaViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: BibliotecaViewModel by viewModels {
        val repo = (application as? BibliotecaApplication)?.repository ?: run {
            val db = BibliotecaDatabase.getDatabase(applicationContext)
            val sessionManager = SessionManager(applicationContext)
            BibliotecaRepository(
                libroDao = db.libroDao(),
                prestamoDao = db.prestamoDao(),
                usuarioDao = db.usuarioDao(),
                solicitudPrestamoDao = db.solicitudPrestamoDao(),
                multaDao = db.multaDao(),
                sessionManager = sessionManager
            )
        }
        BibliotecaViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Log.w("MainActivity", "enableEdgeToEdge no soportado en este dispositivo: ${e.message}")
        }

        setContent {
            BibliotecaTheme {
                BibliotecaApp(viewModel = viewModel)
            }
        }
    }
}
