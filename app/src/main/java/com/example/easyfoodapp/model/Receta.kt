package com.example.easyfoodapp.model

/**
 * modelo de datos que representa una receta en la aplicacion y en la base de datos
 * siguiendo la estructura de los campos del formulario
 */
data class Receta(
    val id: Int = 0,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val ingredientes: String
)