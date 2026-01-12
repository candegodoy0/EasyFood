package com.example.easyfoodapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // conexion de vistas
        val etEmail = findViewById<EditText>(R.id.etEmailRegistro)
        val etPass = findViewById<EditText>(R.id.etPasswordRegistro)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvIrALogin = findViewById<TextView>(R.id.tvIrALogin)
        val dbHelper = DBHelper(this) // inicializa el dbhelper

        btnRegistrar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPass.text.toString().trim()

            // validacion 1: campos vacios
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Debe completar email y contraseña.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // validacion 2: formato de email valido
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingrese un email válido.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // llama al metodo de la bd para registrar el usuario
            val exito = dbHelper.registrarUsuario(email, password)

            if (exito) {
                // si el registro es exitoso, notifica y vuelve a login
                Toast.makeText(this, "¡Registro exitoso! Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                // si falla, probablemente el email ya existe
                Toast.makeText(this, "Error: email ya se encuentra registrado.", Toast.LENGTH_LONG).show()
            }
        }

        // navegación de vuelta a login (cierra la actividad actual)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        tvIrALogin.setOnClickListener { finish() }
    }
}