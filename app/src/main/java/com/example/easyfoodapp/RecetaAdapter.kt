package com.example.easyfoodapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.easyfoodapp.model.Receta

// adaptador para mostrar las recetas en un recyclerview
class RecetaAdapter(
    // la lista debe ser mutable porque la modificaremos al filtrar/actualizar
    private var datos: MutableList<Receta>,
    private val dbHelper: DBHelper,
    // callback para notificar a la actividad que la lista necesita ser recargada (ej. despues de eliminar)
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<RecetaAdapter.ViewHolder>() {

    // el viewholder contiene las referencias a los elementos de la ui de cada item
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombreReceta: TextView = view.findViewById(R.id.txtNombreRecetaItem)
        val btnFavoritoItem: ImageView = view.findViewById(R.id.btnFavoritoItem)
        val btnOpciones: ImageView = view.findViewById(R.id.btnOpciones)
        var isFavorite: Boolean = false
        val itemViewContext: Context = view.context
    }

    // crea y devuelve la vista de cada item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receta_simple, parent, false)
        return ViewHolder(view)
    }

    // asigna los datos a los elementos de la vista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recetaActual = datos[position]
        holder.txtNombreReceta.text = recetaActual.nombre

        // 1. logica del botón de favorito (simulacion)
        val iconoVacio = R.drawable.guardado_vacio
        val iconoLleno = R.drawable.guardado_lleno

        holder.btnFavoritoItem.setImageResource(if (holder.isFavorite) iconoLleno else iconoVacio)

        holder.btnFavoritoItem.setOnClickListener {
            holder.isFavorite = !holder.isFavorite
            val newIcon = if (holder.isFavorite) iconoLleno else iconoVacio
            holder.btnFavoritoItem.setImageResource(newIcon)
            val mensaje = if (holder.isFavorite) "Receta guardada" else "receta eliminada de favoritos"
            Toast.makeText(holder.itemViewContext, mensaje, Toast.LENGTH_SHORT).show()
        }

        // 2. clic en el boton de opciones para editar/eliminar
        holder.btnOpciones.setOnClickListener {
            mostrarMenuOpciones(holder.itemViewContext, holder.btnOpciones, recetaActual)
        }

        // 3. clic en el item completo para ir al detalle de la receta
        holder.itemView.setOnClickListener {
            iniciarDetalleReceta(holder.itemViewContext, recetaActual.id)
        }
    }

    // permite actualizar la lista de datos del adaptador desde la actividad (usado para la busqueda/filtrado)
    fun actualizarLista(nuevaLista: List<Receta>) {
        this.datos = nuevaLista.toMutableList()
        notifyDataSetChanged() // notifica al recyclerview que los datos han cambiado
    }

    private fun iniciarDetalleReceta(contexto: Context, recetaId: Int) {
        val intent = Intent(contexto, DetalleRecetaActivity::class.java).apply {
            putExtra("receta_id", recetaId)
        }
        contexto.startActivity(intent)
    }

    // muestra el menu popup para las acciones de editar y eliminar
    private fun mostrarMenuOpciones(contexto: Context, view: View, receta: Receta) {
        val popup = PopupMenu(contexto, view)

        popup.menu.add(0, 1, 0, "Editar")
        popup.menu.add(0, 2, 1, "Eliminar")

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                1 -> {
                    iniciarEdicionCompleta(contexto, receta)
                    true
                }
                2 -> {
                    iniciarEliminacion(contexto, receta)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // navega a la pantalla de agregarrecetaactivity en modo edicion
    private fun iniciarEdicionCompleta(contexto: Context, receta: Receta) {
        val intent = Intent(contexto, AgregarRecetaActivity::class.java).apply {
            putExtra("receta_id_editar", receta.id)
        }
        contexto.startActivity(intent)
    }

    // muestra un dialogo de confirmacion antes de eliminar una receta
    private fun iniciarEliminacion(contexto: Context, receta: Receta) {
        AlertDialog.Builder(contexto)
            .setTitle("Eliminar receta")
            .setMessage("¿Desea eliminar la receta '${receta.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                val filasAfectadas = dbHelper.eliminarReceta(receta.id)
                if (filasAfectadas > 0) {
                    onDataChanged() // llama al callback para que la actividad recargue la lista
                    Toast.makeText(contexto, "Receta '${receta.nombre}' eliminada.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(contexto, "Error al eliminar la receta en la db.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // devuelve el numero total de items
    override fun getItemCount() = datos.size
}
