package com.example.easyfoodapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyfoodapp.model.Receta

class ResultadoBusquedaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: RecetaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtTitulo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado_busqueda)

        dbHelper = DBHelper(this)
        recyclerView = findViewById(R.id.recyclerViewResultados)
        txtTitulo = findViewById(R.id.txtTituloResultados)

        // 1. obtener la lista de ingredientes a buscar del intent
        val ingredientesBusqueda = intent.getStringArrayListExtra("ingredientes_busqueda")

        if (ingredientesBusqueda.isNullOrEmpty()) {
            Toast.makeText(this, "Error: no se proporcionaron ingredientes para la búsqueda.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. ejecutar la busqueda en la base de datos
        val resultados = dbHelper.buscarRecetasPorIngredientes(ingredientesBusqueda)

        // 3. configurar la ui y mostrar resultados
        txtTitulo.text = "Recetas encontradas (${resultados.size} recetas)"

        if (resultados.isEmpty()) {
            Toast.makeText(this, "No se encontraron recetas que contengan esos ingredientes.", Toast.LENGTH_LONG).show()
        }

        // inicializa el adaptador con los resultados de la búsqueda
        adapter = RecetaAdapter(
            resultados.toMutableList(), // la lista de resultados
            dbHelper,
            // el callback se deja vacio ya que no necesitamos refrescar la pantalla de resultados de busqueda
            onDataChanged = { }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // boton de volver
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}