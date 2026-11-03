package com.example.biblioteca.data.local.session

import android.content.Context
import android.content.SharedPreferences
import com.example.biblioteca.model.RolUsuario
import com.example.biblioteca.model.Usuario
import com.example.biblioteca.ui.theme.ThemeMode

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "biblioteca_unamad_session"
        private const val KEY_TOKEN = "session_jwt_token"
        private const val KEY_CODIGO = "session_codigo"
        private const val KEY_ROL = "session_rol"
        private const val KEY_NOMBRE = "session_nombre"
        private const val KEY_EMAIL = "session_email"
        private const val KEY_CARRERA = "session_carrera"
        private const val KEY_TELEFONO = "session_telefono"
        private const val KEY_THEME_MODE = "session_theme_mode"
    }

    fun guardarSesion(usuario: Usuario, token: String? = null) {
        val editor = prefs.edit()
            .putString(KEY_CODIGO, usuario.codigo)
            .putString(KEY_ROL, usuario.rol.name)
            .putString(KEY_NOMBRE, usuario.nombreCompleto)
            .putString(KEY_EMAIL, usuario.email)
            .putString(KEY_CARRERA, usuario.carreraOArea)
            .putString(KEY_TELEFONO, usuario.telefono)

        if (!token.isNullOrBlank()) {
            editor.putString(KEY_TOKEN, token)
        }

        editor.apply()
    }

    fun guardarToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun obtenerCodigoSesion(): String? {
        return prefs.getString(KEY_CODIGO, null)
    }

    fun obtenerRolSesion(): RolUsuario? {
        val rolStr = prefs.getString(KEY_ROL, null) ?: return null
        return try {
            RolUsuario.valueOf(rolStr)
        } catch (e: Exception) {
            null
        }
    }

    fun cerrarSesion() {
        val currentTheme = obtenerThemeMode()
        prefs.edit().clear().apply()
        guardarThemeMode(currentTheme)
    }

    fun tieneSesionActiva(): Boolean {
        return !prefs.getString(KEY_CODIGO, null).isNullOrBlank()
    }

    fun guardarThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun obtenerThemeMode(): ThemeMode {
        val modeStr = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
}
