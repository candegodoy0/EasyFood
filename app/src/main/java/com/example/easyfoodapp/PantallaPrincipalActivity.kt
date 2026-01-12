package com.example.easyfoodapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PantallaPrincipalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        // --- conexiones de elementos interactivos ---
        val btnBuscarIngredientes = findViewById<Button>(R.id.btnIngredientes)
        val btnAzar = findViewById<Button>(R.id.btnAzar)
        val fabAgregar = findViewById<FloatingActionButton>(R.id.fabAgregar)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        val btnVerMisRecetas = findViewById<Button>(R.id.btnVerMisRecetas)

        // --- logica de clicks y navegacion ---

        // boton de categorias (menu hamburguesa)
        btnMenu.setOnClickListener {
            val intent = Intent(this, CategoriasActivity::class.java)
            startActivity(intent)
        }

        // boton de busqueda por ingredientes
        btnBuscarIngredientes.setOnClickListener {
            val intent = Intent(this, BuscarIngredientesActivity::class.java)
            startActivity(intent)
        }

        // boton de elegir al azar
        btnAzar.setOnClickListener {
            val intent = Intent(this, RecetaSorpresaActivity::class.java)
            startActivity(intent)
        }

        // boton flotante para agregar receta
        fabAgregar.setOnClickListener {
            val intent = Intent(this, AgregarRecetaActivity::class.java)
            startActivity(intent)
        }

        // clic para ir a la gestion completa de recetas (lista y buscador)
        btnVerMisRecetas.setOnClickListener {
            val intent = Intent(this, MisRecetasActivity::class.java)
            startActivity(intent)
        }

        // --- logica de cierre de sesion (clic en imagen de perfil) ---
        imgPerfil.setOnClickListener {
            mostrarDialogoSalir()
        }
    }

    // --- funcion para mostrar el pop-up de confirmacion ---
    private fun mostrarDialogoSalir() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Confirmar Cierre de Sesión")
        builder.setMessage("¿Estás seguro de que deseas cerrar sesión en EasyFood?")

        // boton positivo
        builder.setPositiveButton("Sí, Cerrar Sesión") { dialog, which ->
            cerrarSesionLogica()
        }

        // boton negativo
        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss() // Cierra el diálogo sin hacer nada
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // --- logica del cierre de sesión y redireccion ---
    private fun cerrarSesionLogica() {

        // redirigir a la pantalla de Login
        val intent = Intent(this, LoginActivity::class.java)

        // no pueda volver a la sesión de recetas.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish() // finaliza esta activity principal
    }
}