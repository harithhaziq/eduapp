package com.makeit.eduapp.model

// Base class representing a user
data class User(
    val email: String,
    val name: String,
    val isParent: Boolean,
    val childList: MutableList<Child>
)


data class Child(
    val email: String,
    val name: String,
    val parentEmail: String,
    val category: String
)