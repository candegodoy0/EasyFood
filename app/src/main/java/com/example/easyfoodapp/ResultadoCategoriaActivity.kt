package com.example.easyfoodapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyfoodapp.model.Receta
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ResultadoCategoriaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var rvRecetas: RecyclerView
    private lateinit var adapter: RecetaAdapter
    private lateinit var recetasList: MutableList<Receta>
    private lateinit var txtNoRecetas: TextView
    private lateinit var layoutBotonesCategoria: LinearLayout
    private lateinit var txtTituloCategoria: TextView
    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var fabAgregar: FloatingActionButton

    // variables para los colores, con manejo de error por si fallan
    private var colorAmarillo: Int = 0
    private var colorBlanco: Int = 0
    private var colorGrisStroke: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado_categoria)

        dbHelper = DBHelper(this)

        // intenta cargar los colores definidos en resources
        try {
            colorAmarillo = ContextCompat.getColor(this, R.color.amarilloBoton)
            colorBlanco = ContextCompat.getColor(this, R.color.white)
            colorGrisStroke = ContextCompat.getColor(this, R.color.gris_texto)
        } catch (e: Exception) {
            // si falla, usa colores por defecto
            colorAmarillo = Color.YELLOW
            colorBlanco = Color.WHITE
            colorGrisStroke = Color.GRAY
        }

        // 1. conexiones de vistas
        layoutBotonesCategoria = findViewById(R.id.layoutBotonesCategoria)
        txtNoRecetas = findViewById(R.id.txtNoRecetas)
        rvRecetas = findViewById(R.id.rvRecetas)
        txtTituloCategoria = findViewById(R.id.txtTituloCategoria)
        horizontalScrollView = findViewById(R.id.horizontalScrollView)
        fabAgregar = findViewById(R.id.fabAgregarDesdeCategoria)

        rvRecetas.layoutManager = LinearLayoutManager(this)

        // 2. inicializar adapter
        recetasList = mutableListOf()
        adapter = RecetaAdapter(recetasList, dbHelper) {
            // callback: si se elimina una receta, recarga la lista para la categoria actual
            cargarRecetasYMarcarBoton(txtTituloCategoria.text.toString())
        }
        rvRecetas.adapter = adapter

        // 3. obtener categoria inicial del intent y cargar las recetas
        val nombreCategoria = intent.getStringExtra("categoria_seleccionada") ?: "Desayuno"

        cargarRecetasYMarcarBoton(nombreCategoria)

        // 4. configurar listeners para los botones de categoría
        configurarListenersCategorias()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // logica de clic del fab agregar
        fabAgregar.setOnClickListener {
            val intent = Intent(this, AgregarRecetaActivity::class.java)
            // se pasa la categoria actual para que se pre-seleccione en la nueva pantalla
            intent.putExtra("pre_seleccionar_categoria", txtTituloCategoria.text.toString())
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // recargamos la lista con la categoria actual al volver a la actividad
        cargarRecetasYMarcarBoton(txtTituloCategoria.text.toString())
    }

    // obtiene las recetas para una categoria, actualiza el adapter y maneja el estado vacio
    private fun cargarRecetasYMarcarBoton(categoria: String) {
        val nuevasRecetas = dbHelper.obtenerRecetasPorCategoria(categoria)

        // actualiza la lista del adaptador
        recetasList.clear()
        recetasList.addAll(nuevasRecetas)
        adapter.notifyDataSetChanged()

        // actualiza el título para reflejar la categoria actual
        txtTituloCategoria.text = categoria

        // logica de mensaje vacio
        if (nuevasRecetas.isEmpty()) {
            rvRecetas.visibility = View.GONE
            txtNoRecetas.visibility = View.VISIBLE
        } else {
            rvRecetas.visibility = View.VISIBLE
            txtNoRecetas.visibility = View.GONE
        }

        // marca el boton de la categoria seleccionada y hace scroll hacia el
        marcarBotonSeleccionado(categoria)
    }

    // cambia el estilo del boton seleccionado y deselecciona los demas
    private fun marcarBotonSeleccionado(categoria: String) {
        val stroke1dp = (1 * resources.displayMetrics.density).toInt()
        var botonSeleccionadoView: MaterialButton? = null

        // itera sobre todos los botones del layout
        for (i in 0 until layoutBotonesCategoria.childCount) {
            val view = layoutBotonesCategoria.getChildAt(i)
            if (view is MaterialButton) {
                // compara la categoria con el tag del boton
                val esSeleccionado =
                    view.tag?.toString()?.equals(categoria, ignoreCase = true) == true

                if (esSeleccionado) {
                    // si esta seleccionado: color amarillo, sin borde
                    view.backgroundTintList = ColorStateList.valueOf(colorAmarillo)
                    view.strokeWidth = 0
                    botonSeleccionadoView = view
                } else {
                    // si no esta seleccionado: color blanco, con borde gris
                    view.backgroundTintList = ColorStateList.valueOf(colorBlanco)
                    view.strokeWidth = stroke1dp
                    view.setStrokeColorResource(R.color.gris_texto)
                }
            }
        }

        if (botonSeleccionadoView != null) {
            scrollToButton(botonSeleccionadoView)
        }
    }

    // hace un scroll horizontal suave para centrar el boton seleccionado
    private fun scrollToButton(botonSeleccionado: View) {
        horizontalScrollView.post {
            if (horizontalScrollView.width > 0) {
                // calcula la posicion x necesaria para centrar el boton
                val scrollX = botonSeleccionado.left -
                        (horizontalScrollView.width / 2) +
                        (botonSeleccionado.width / 2)

                horizontalScrollView.smoothScrollTo(scrollX, 0)
            }
        }
    }

    // asigna el listener de clic a cada boton de categoria
    private fun configurarListenersCategorias() {
        for (i in 0 until layoutBotonesCategoria.childCount) {
            val view = layoutBotonesCategoria.getChildAt(i)
            if (view is MaterialButton) {
                view.setOnClickListener {
                    val nuevaCategoria = it.tag?.toString() ?: ""
                    if (nuevaCategoria.isNotBlank()) {
                        cargarRecetasYMarcarBoton(nuevaCategoria)
                    }
                }
            }
        }
    }
}
