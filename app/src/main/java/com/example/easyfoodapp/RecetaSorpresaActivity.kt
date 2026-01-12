package com.example.easyfoodapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecetaSorpresaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receta_sorpresa)

        dbHelper = DBHelper(this) // inicializar el dbhelper

        // comportamiento del boton de volver
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // accion al hacer clic en el boton para buscar una receta al azar
        findViewById<Button>(R.id.btnBuscarReceta).setOnClickListener {
            // obtiene una receta aleatoria de la base de datos
            val recetaAlAzar = dbHelper.obtenerRecetaAleatoria()

            if (recetaAlAzar != null) {
                // si encuentra una receta, navega a su detalle
                val intent = Intent(this, DetalleRecetaActivity::class.java)
                intent.putExtra("receta_id", recetaAlAzar.id)
                startActivity(intent)
            } else {
                // si no hay recetas guardadas, notifica al usuario
                Toast.makeText(this, "no se encontraron recetas en la base de datos.", Toast.LENGTH_LONG).show()
            }
        }
    }
}