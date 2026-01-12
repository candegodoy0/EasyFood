package com.example.easyfoodapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayList

class BuscarIngredientesActivity : AppCompatActivity() {

    private lateinit var etIngredientes: EditText // campo para que el usuario ingrese ingredientes
    private lateinit var btnBuscar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_ingredientes)

        // 1. conexiones
        etIngredientes = findViewById(R.id.etIngredientesBusqueda)
        btnBuscar = findViewById(R.id.btnEncontrarReceta)

        // 2. comportamiento del boton de busqueda
        btnBuscar.setOnClickListener {
            iniciarBusquedaPorIngredientes()
        }

        // 3. comportamiento del boton de volver
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }


    // procesa el texto de ingredientes y navega a la pantalla de resultados
    private fun iniciarBusquedaPorIngredientes() {
        val ingredientesRaw = etIngredientes.text.toString().trim()

        // validacian basica de campo vacío
        if (ingredientesRaw.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa al menos un ingrediente.", Toast.LENGTH_SHORT).show()
            return
        }

        // logica para normalizar y convertir el texto a una lista de palabras clave:
        // 1. separa el texto por comas, punto y comas o saltos de línea
        // 2. limpia espacios y convierte a minusculas para una busqueda sin distincion de mayusculas
        // 3. filtra para eliminar entradas vacias que puedan haber quedado
        val ingredientesKeywords = ingredientesRaw
            .split(',', ';', '\n')
            .map { it.trim().toLowerCase() }
            .filter { it.isNotBlank() }

        // validacion si despues de la limpieza la lista queda vacia
        if (ingredientesKeywords.isEmpty()) {
            Toast.makeText(this, "Ingresa ingredientes válidos para buscar.", Toast.LENGTH_SHORT).show()
            return
        }

        // redireccion a la actividad de resultados de busqueda
        val intent = Intent(this, ResultadoBusquedaActivity::class.java).apply {
            // se pasa la lista de palabras clave como stringarraylist (necesario para pasar listas de strings en intents)
            putStringArrayListExtra("ingredientes_busqueda", ArrayList(ingredientesKeywords))
        }

        startActivity(intent)
    }
}