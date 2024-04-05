package com.makeit.eduapp.model

// Base class representing a user
data class User(
    val id: String,
    val email: String,
    val name: String,
    val isParent: Boolean,
    val token: String,
    val childList: MutableList<Child>
)


data class Child(
    val id: String,
    val email: String,
    val name: String,
    val parentEmail: String,
    val category: String,
    val token: String,
    val screenTime: Long,
    val address: String,
    var screenTimeLimit: Long
)