package com.example.easyfoodapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import android.widget.TextView

class LoginActivity : AppCompatActivity() {

    // estado para saber si la contraseña está visible o no
    private var isPasswordVisible = false
    // helper para interactuar con la base de datos
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // inicializa el dbhelper
        dbHelper = DBHelper(this)

        // conexion de vistas
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val ivOjo = findViewById<ImageView>(R.id.ivOjo)
        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // --- comportamientos ---
        // 1. navegacion a la pantalla de registro
        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        // 2. comportamiento de mostrar/ocultar contraseña
        ivOjo.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // cambia el inputtype a texto normal y el icono a "visible"
                etPass.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivOjo.setImageResource(R.drawable.visible)
            } else {
                // cambia el inputtype a password y el icono a "oculto"
                etPass.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivOjo.setImageResource(R.drawable.oculto)
            }
            // esto asegura que el cursor permanezca al final del texto despues del cambio de inputtype
            etPass.setSelection(etPass.text.length)
        }

        // 3. comportamiento del boton de login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPass.text.toString().trim()

            // validacion de campos vacios
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, completa tu email y contraseña.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // validacion con sqlite
            if (dbHelper.verificarUsuario(email, password)) {
                // login exitoso: notifica, navega y cierra la actividad de login
                Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(this, PantallaPrincipalActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // credenciales incorrectas
                Toast.makeText(
                    this,
                    "Credenciales incorrectas o usuario no registrado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // 4. comportamiento del boton de volver
        btnBack.setOnClickListener {
            finish()
        }
    }
}
