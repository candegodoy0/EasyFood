package com.example.easyfoodapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.example.easyfoodapp.model.Receta

class AgregarRecetaActivity : AppCompatActivity() {

    // helper para interactuar con la base de datos sqlite
    private lateinit var dbHelper: DBHelper
    // guarda el id de la receta si estamos en modo edición
    private var recetaId: Int? = null
    private lateinit var txtTituloPantalla: TextView
    private lateinit var spinnerCategoria: Spinner

    // conexión de vistas
    private lateinit var txtNombre: EditText
    private lateinit var txtDescripcion: EditText
    private lateinit var txtIngredientes: EditText
    private lateinit var btnGuardar: Button

    // lista de categorias estáticas para el spinner
    private val categoriasBase = listOf("Desayuno", "Almuerzo", "Bebidas", "Pastas", "Ensaladas", "Postres", "Sopas")
    // se agrega un placeholder para la validacion del spinner
    private val categoriasConPlaceholder = listOf("Seleccione una categoría...") + categoriasBase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_receta)

        dbHelper = DBHelper(this)

        // conexion de elementos de la ui
        txtNombre = findViewById(R.id.txtNombre)
        txtDescripcion = findViewById(R.id.txtDescripcion)
        txtIngredientes = findViewById(R.id.txtIngredientes)
        btnGuardar = findViewById(R.id.btnAgregar)
        txtTituloPantalla = findViewById(R.id.txtTituloPantalla)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)

        // configurar el spinner con las categorias y el placeholder
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriasConPlaceholder)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterSpinner

        // establecer el placeholder ("seleccione...") como seleccionado por defecto
        spinnerCategoria.setSelection(0)

        // verificar si la actividad fue llamada en modo edicion (si se paso un id)
        recetaId = intent.getIntExtra("receta_id_editar", -1).takeIf { it != -1 }

        if (recetaId != null) {
            // modo edicion
            txtTituloPantalla.text = "EDITAR RECETA"
            btnGuardar.text = "ACTUALIZAR"
            cargarDatosReceta()
        } else {
            // modo agregar
            txtTituloPantalla.text = "NUEVA RECETA"
            btnGuardar.text = "AGREGAR"
        }

        // comportamiento del boton de guardar/actualizar
        btnGuardar.setOnClickListener {
            guardarOActualizarReceta()
        }

        // comportamiento del boton de volver (cierra la actividad)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    // obtiene los datos de la receta desde la base de datos y los carga en los campos para edicion
    private fun cargarDatosReceta() {
        recetaId?.let { id ->
            val receta = dbHelper.obtenerRecetaPorId(id)
            if (receta != null) {
                txtNombre.setText(receta.nombre)
                txtDescripcion.setText(receta.descripcion)
                txtIngredientes.setText(receta.ingredientes)

                // selecciona la categoria correcta en el spinner
                val categoriaIndex = categoriasConPlaceholder.indexOf(receta.categoria)
                if (categoriaIndex >= 0) {
                    spinnerCategoria.setSelection(categoriaIndex)
                } else {
                    // si no la encuentra, selecciona el primer elemento (placeholder) o el ultimo
                    spinnerCategoria.setSelection(0)
                }
            } else {
                Toast.makeText(this, "Error al cargar la receta.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // maneja la logica de validacion y la insercion o actualizacion de la receta en la bd
    private fun guardarOActualizarReceta() {
        val nombre = txtNombre.text.toString().trim()
        val categoriaSeleccionada = spinnerCategoria.selectedItem.toString()
        val descripcion = txtDescripcion.text.toString().trim()
        val ingredientes = txtIngredientes.text.toString().trim()

        // validacion 1: campos de texto vacios
        if (nombre.isEmpty() || descripcion.isEmpty() || ingredientes.isEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Debes completar todos los campos.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        // validacion 2: categoria no seleccionada (placeholder sigue seleccionado)
        if (categoriaSeleccionada == "Seleccione una categoría...") {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Por favor, selecciona una categoría válida.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        // la categoria es valida
        val categoria = categoriaSeleccionada

        // crea el objeto receta con el id (si estamos editando) o 0 (si es nueva)
        val recetaAProcesar = Receta(
            id = recetaId ?: 0,
            nombre = nombre,
            categoria = categoria,
            descripcion = descripcion,
            ingredientes = ingredientes
        )

        // decide si insertar o modificar en la base de datos
        val resultado: Long = if (recetaId != null) {
            dbHelper.modificarRecetaCompleta(recetaAProcesar).toLong()
        } else {
            dbHelper.insertarReceta(recetaAProcesar)
        }

        if (resultado > 0L) {
            // operacion exitosa
            val mensaje = if (recetaId != null) "Receta '$nombre' actualizada con éxito." else "receta '$nombre' agregada con éxito."

            // muestra un snackbar con accion para ir a la lista
            Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG)
                .setAction(" VER LISTA") {
                    val intent = Intent(this, MisRecetasActivity::class.java).apply {
                        // limpia las actividades superiores para no volver aqui con el boton 'back'
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(intent)
                }
                .show()

            // si es una receta nueva, limpia los campos y resetea el spinner
            if (recetaId == null) {
                txtNombre.setText("")
                txtDescripcion.setText("")
                txtIngredientes.setText("")
                spinnerCategoria.setSelection(0) // resetear al placeholder
            } else {
                // si estamos editando, cerramos la actividad
                finish()
            }
        } else {
            // error en la operacion de la base de datos
            val mensajeError = if (recetaId != null) "Error: no se pudo actualizar la receta o no se hicieron cambios." else "error al guardar la receta."
            Snackbar.make(findViewById(android.R.id.content), mensajeError, Snackbar.LENGTH_LONG).show()
        }
    }
}
