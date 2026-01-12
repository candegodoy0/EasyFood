package com.example.easyfoodapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyfoodapp.model.Receta

class MisRecetasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var etBuscarReceta: EditText
    private lateinit var tvEstadoVacio: TextView
    private lateinit var adapter: RecetaAdapter // mantiene la referencia al adaptador

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_recetas)

        dbHelper = DBHelper(this)

        // conexion de vistas
        recyclerView = findViewById(R.id.rvMisRecetas)
        etBuscarReceta = findViewById(R.id.etBuscarReceta)
        tvEstadoVacio = findViewById(R.id.tvEstadoVacio)

        // configurar recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        cargarRecetas() // carga inicial de todas las recetas

        // boton de volver
        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // logica de busqueda en tiempo real: observa los cambios en el campo de texto
        etBuscarReceta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // filtra la lista cada vez que el texto cambia
                filtrarRecetas(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // obtiene todas las recetas de la bd y configura el adaptador
    private fun cargarRecetas() {
        // obtenemos todas las recetas sin filtro
        val listaRecetasInmutable = dbHelper.obtenerRecetas()
        val listaRecetasMutable = listaRecetasInmutable.toMutableList()

        if (::adapter.isInitialized) {
            // si el adaptador ya existe, solo actualiza la lista para eficiencia
            adapter.actualizarLista(listaRecetasMutable)
        } else {
            // si es la primera carga, crea el adaptador
            adapter = RecetaAdapter(
                listaRecetasMutable,
                dbHelper,
                // callback: si el adaptador elimina una receta, vuelve a llamar a cargarrecetas() para refrescar
                onDataChanged = { cargarRecetas() }
            )
            recyclerView.adapter = adapter
        }

        // verifica el estado de la lista completa para mostrar el mensaje de "vacio" inicial
        actualizarEstadoVacio(listaRecetasMutable.isEmpty())
    }

    // maneja la logica de busqueda y actualiza el recyclerview
    private fun filtrarRecetas(filtro: String) {
        // 1. obtener la lista filtrada de la base de datos
        val listaFiltrada = dbHelper.obtenerRecetas(filtro)

        // 2. actualiza el adaptador con la nueva lista filtrada
        adapter.actualizarLista(listaFiltrada)

        // 3. actualiza el mensaje de vacio segun el resultado de la busqueda
        if (filtro.isBlank()) {
            // si no hay filtro, mostramos el estado de la lista completa
            actualizarEstadoVacio(dbHelper.obtenerRecetas().isEmpty())
        } else {
            // si hay filtro, mostramos el estado de la lista filtrada (resultados no encontrados)
            actualizarBusquedaVacia(listaFiltrada.isEmpty())
        }
    }

    // muestra un mensaje si la busqueda no arrojo resultados
    private fun actualizarBusquedaVacia(estaVacio: Boolean) {
        if (estaVacio) {
            tvEstadoVacio.text = "No se encontraron resultados para su búsqueda."
            tvEstadoVacio.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEstadoVacio.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // muestra un mensaje si la base de datos esta completamente vacia
    private fun actualizarEstadoVacio(estaVacio: Boolean) {
        if (estaVacio) {
            tvEstadoVacio.text = "¡Aún no tienes recetas guardadas!"
            tvEstadoVacio.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEstadoVacio.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // recarga la lista al volver a la actividad (por si se agrego/edito/elimino una receta)
    override fun onResume() {
        super.onResume()
        cargarRecetas()
    }
}
