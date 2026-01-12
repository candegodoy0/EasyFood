package com.example.easyfoodapp

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import android.util.Log
import com.example.easyfoodapp.model.Receta

// clase que maneja la conexion y las operaciones con la base de datos sqlite
class DBHelper(context: Context) : SQLiteOpenHelper(context, "easyfood.db", null, 1) {

    // constantes para los nombres de las tablas y columnas
    companion object {
        private const val table_users = "usuarios"
        private const val table_recipes = "recetas"

        private const val col_user_email = "email"
        private const val col_user_password = "password"

        const val col_recipe_id = "id"
        const val col_recipe_nombre = "nombre"
        const val col_recipe_categoria = "categoria"
        const val col_recipe_descripcion = "descripcion"
        const val col_recipe_ingredientes = "ingredientes"
    }

    // se llama cuando la base de datos se crea por primera vez
    override fun onCreate(db: SQLiteDatabase) {
        // crea la tabla de usuarios
        val createTableUsers = """
            create table $table_users (
                id integer primary key autoincrement,
                $col_user_email text not null unique,
                $col_user_password text not null
            )
        """.trimIndent()
        db.execSQL(createTableUsers)

        // crea la tabla de recetas
        val createTableRecipes = """
            create table $table_recipes (
                $col_recipe_id integer primary key autoincrement,
                $col_recipe_nombre text not null,
                $col_recipe_categoria text,
                $col_recipe_descripcion text,
                $col_recipe_ingredientes text
            )
        """.trimIndent()
        db.execSQL(createTableRecipes)
    }

    // se llama cuando la version de la base de datos cambia
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // por simplicidad, borra y recrea las tablas
        db.execSQL("drop table if exists $table_recipes")
        db.execSQL("drop table if exists $table_users")
        onCreate(db)
    }

    // --- metodos de autenticacion ---

    // intenta registrar un nuevo usuario en la base de datos
    fun registrarUsuario(email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(col_user_email, email)
            put(col_user_password, password)
        }
        val result = db.insert(table_users, null, values)
        db.close()
        // si insert devuelve -1, hubo un error (ej. email duplicado)
        return result != -1L
    }

    // verifica si existe un usuario con el email y contraseña dados
    fun verificarUsuario(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "select 1 from $table_users where $col_user_email = ? and $col_user_password = ?",
            arrayOf(email, password)
        )
        // si count es mayor que 0, significa que se encontro una fila
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // --- crud y consultas recetas ---

    // inserta una nueva receta en la base de datos
    fun insertarReceta(receta: Receta): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(col_recipe_nombre, receta.nombre)
            put(col_recipe_categoria, receta.categoria)
            put(col_recipe_descripcion, receta.descripcion)
            put(col_recipe_ingredientes, receta.ingredientes)
        }
        val id = db.insert(table_recipes, null, values)
        db.close()
        return id
    }

    // obtiene una receta especifica usando su id
    fun obtenerRecetaPorId(id: Int): Receta? {
        val db = readableDatabase
        // consulta usando el id como filtro
        val cursor = db.query(
            table_recipes,
            null,
            "$col_recipe_id = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var receta: Receta? = null
        if (cursor.moveToFirst()) {
            // extrae los valores de las columnas y crea el objeto receta
            try {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_nombre))
                val categoria = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_categoria))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_descripcion))
                val ingredientes = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_ingredientes))
                receta = Receta(id, nombre, categoria, descripcion, ingredientes)
            } catch (e: Exception) {
                Log.e("dbhelper", "Error al obtener receta por id: ${e.message}")
            }
        }
        cursor.close()
        db.close()
        return receta
    }

    //obtiene todas las recetas o filtra por nombre

    fun obtenerRecetas(filtroNombre: String = ""): List<Receta> {
        val lista = mutableListOf<Receta>()
        val db = readableDatabase

        // construye la consulta sql, si hay filtro usa like
        val query = if (filtroNombre.isBlank()) {
            "select * from $table_recipes order by $col_recipe_nombre asc"
        } else {
            // usa like para buscar subcadenas en el nombre (ej. '%pollo%')
            "select * from $table_recipes where $col_recipe_nombre like ?"
        }

        val args = if (filtroNombre.isBlank()) null else arrayOf("%$filtroNombre%")
        val cursor = db.rawQuery(query, args)

        // itera sobre los resultados de la consulta
        if (cursor.moveToFirst()) {
            do {
                try {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(col_recipe_id))
                    val nombre = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_nombre))
                    val categoria = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_categoria))
                    val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_descripcion))
                    val ingredientes = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_ingredientes))

                    lista.add(Receta(id, nombre, categoria, descripcion, ingredientes))
                } catch (e: Exception) {
                    Log.e("dbhelper", "error al leer receta: ${e.message}")
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    // actualiza todos los campos de una receta existente
    fun modificarRecetaCompleta(receta: Receta): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(col_recipe_nombre, receta.nombre)
            put(col_recipe_categoria, receta.categoria)
            put(col_recipe_descripcion, receta.descripcion)
            put(col_recipe_ingredientes, receta.ingredientes)
        }
        // actualiza la fila donde el id coincida
        val rows = db.update(
            table_recipes,
            values,
            "$col_recipe_id = ?",
            arrayOf(receta.id.toString())
        )
        db.close()
        // devuelve el numero de filas afectadas
        return rows
    }

    // elimina una receta por su id
    fun eliminarReceta(recetaId: Int): Int {
        val db = writableDatabase
        val rows = db.delete(
            table_recipes,
            "$col_recipe_id = ?",
            arrayOf(recetaId.toString())
        )
        db.close()
        // devuelve el numero de filas eliminadas
        return rows
    }

    // obtiene todas las recetas que pertenecen a una categoria especifica
    fun obtenerRecetasPorCategoria(categoria: String): List<Receta> {
        val lista = mutableListOf<Receta>()
        val db = readableDatabase

        val query = "select * from $table_recipes where $col_recipe_categoria = ? order by $col_recipe_nombre asc"
        val args = arrayOf(categoria)
        val cursor = db.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            do {
                try {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(col_recipe_id))
                    val nombre = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_nombre))
                    val cat = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_categoria))
                    val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_descripcion))
                    val ingredientes = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_ingredientes))

                    lista.add(Receta(id, nombre, cat, descripcion, ingredientes))
                } catch (e: Exception) {
                    Log.e("dbhelper", "Error al leer receta por categoria: ${e.message}")
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    /**
     * busca recetas que contengan al menos uno de los ingredientes proporcionados
     */
    fun buscarRecetasPorIngredientes(ingredientes: List<String>): List<Receta> {
        val lista = mutableListOf<Receta>()
        if (ingredientes.isEmpty()) return lista

        val db = readableDatabase

        // construye la clausula where usando 'like' y 'or' para cada ingrediente
        val whereClauses = ingredientes.joinToString(" or ") {
            // busca si la columna ingredientes contiene el texto del ingrediente (ej: ingredientes like '%pollo%')
            "$col_recipe_ingredientes like ?"
        }

        val args = ingredientes.map { "%$it%" }.toTypedArray()

        val query = "select * from $table_recipes where $whereClauses order by $col_recipe_nombre asc"
        val cursor = db.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            do {
                try {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(col_recipe_id))
                    val nombre = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_nombre))
                    val cat = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_categoria))
                    val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_descripcion))
                    val ingredientesTexto = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_ingredientes))

                    lista.add(Receta(id, nombre, cat, descripcion, ingredientesTexto))
                } catch (e: Exception) {
                    Log.e("dbhelper", "error al leer receta por ingrediente: ${e.message}")
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    /**
     * obtiene una receta al azar para la funcion "sorpresa"
     */
    fun obtenerRecetaAleatoria(): Receta? {
        val db = readableDatabase
        // usa order by random() limit 1 para seleccionar un registro al azar
        val cursor = db.rawQuery("select * from $table_recipes order by random() limit 1", null)

        var receta: Receta? = null
        if (cursor.moveToFirst()) {
            try {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(col_recipe_id))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_nombre))
                val categoria = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_categoria))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_descripcion))
                val ingredientes = cursor.getString(cursor.getColumnIndexOrThrow(col_recipe_ingredientes))
                receta = Receta(id, nombre, categoria, descripcion, ingredientes)
            } catch (e: Exception) {
                Log.e("dbhelper", "Error al obtener receta aleatoria: ${e.message}")
            }
        }
        cursor.close()
        db.close()
        return receta
    }
}
