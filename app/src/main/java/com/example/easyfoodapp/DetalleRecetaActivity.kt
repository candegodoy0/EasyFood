package com.example.easyfoodapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easyfoodapp.model.Receta
import java.lang.Exception

class DetalleRecetaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    // variable para simular el estado de favorito
    private var isFavorite = false

    // conexiones de vistas
    private lateinit var txtTituloReceta: TextView
    private lateinit var txtCategoriaReceta: TextView
    private lateinit var txtDescripcionCompleta: TextView
    private lateinit var txtIngredientesCompletos: TextView
    private lateinit var btnFavorito: ImageView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_receta)

        dbHelper = DBHelper(this)

        // 1. conexion de elementos de la ui
        txtTituloReceta = findViewById(R.id.txtTituloReceta)
        txtCategoriaReceta = findViewById(R.id.txtCategoriaReceta)
        txtDescripcionCompleta = findViewById(R.id.txtDescripcionCompleta)
        txtIngredientesCompletos = findViewById(R.id.txtIngredientesCompletos)
        btnFavorito = findViewById(R.id.btnFavorito)
        btnBack = findViewById(R.id.btnBack)

        // 2. carga de datos
        // obtiene el id de la receta que se paso desde la activity anterior
        val recetaId = intent.getIntExtra("receta_id", -1)
        if (recetaId != -1) {
            cargarDatosReceta(recetaId)
        } else {
            Toast.makeText(this, "Error: no se proporcionó el id de la receta.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 3. comportamiento de botones
        btnBack.setOnClickListener {
            finish()
        }

        // simulacion del boton de favorito
        btnFavorito.setOnClickListener {
            isFavorite = !isFavorite
            // cambia el icono segun el estado
            val newIcon = if (isFavorite) R.drawable.guardado_lleno else R.drawable.guardado_vacio
            btnFavorito.setImageResource(newIcon)

            val mensaje = if (isFavorite) "¡Receta '${txtTituloReceta.text}' guardada en favoritos!" else "Receta '${txtTituloReceta.text}' eliminada de favoritos"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    // busca la receta por id en la bd y rellena las vistas
    private fun cargarDatosReceta(id: Int) {
        val receta = dbHelper.obtenerRecetaPorId(id)

        if (receta != null) {
            // rellenar la ui con los datos de la bd
            txtTituloReceta.text = receta.nombre
            txtCategoriaReceta.text = "Categoría: ${receta.categoria}"
            txtDescripcionCompleta.text = receta.descripcion

            // muestra los ingredientes formateados como una lista con puntos
            val ingredientesFormateados = formatearIngredientes(receta.ingredientes)
            txtIngredientesCompletos.text = ingredientesFormateados

        } else {
            Toast.makeText(this, "No se pudo cargar la receta.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // convierte un string de ingredientes (ej: "harina, huevo, leche") en una lista con puntos
    private fun formatearIngredientes(ingredientes: String): String {
        return try {
            // separa por comas o saltos de linea, limpia espacios, y junta con un prefijo de punto
            ingredientes.split(',', '\n')
                .filter { it.trim().isNotBlank() } // evita lineas vacias
                .joinToString("\n• ", prefix = "• ") { it.trim().capitalize() } // capitalize() para que se vea mejor

        } catch (e: Exception) {
            // si hay un error en el formato, devuelve el texto original
            ingredientes
        }
    }
}