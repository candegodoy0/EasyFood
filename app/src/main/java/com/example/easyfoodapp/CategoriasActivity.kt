package com.example.easyfoodapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class CategoriasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        // comportamiento del boton de volver
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // mapeo de los id de los botones con el nombre de la categoría
        val categoriaMap = mapOf(
            R.id.btnDesayuno to "Desayuno",
            R.id.btnAlmuerzo to "Almuerzo",
            R.id.btnBebidas to "Bebidas",
            R.id.btnPastas to "Pastas",
            R.id.btnEnsaladas to "Ensaladas",
            R.id.btnPostres to "Postres",
            R.id.btnSopa to "Sopas"
        )

        // asigna el click listener a cada boton usando el mapa
        categoriaMap.forEach { (id, categoriaNombre) ->
            findViewById<MaterialButton>(id).setOnClickListener {
                val intent = Intent(this, ResultadoCategoriaActivity::class.java).apply {
                    // importante: enviar el nombre de la categoria que se selecciono
                    putExtra("categoria_seleccionada", categoriaNombre)
                }
                startActivity(intent)
            }
        }
    }
}