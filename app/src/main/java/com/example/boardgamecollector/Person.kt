package com.example.boardgamecollector

data class Person(val id: Int, val name: String){
    fun equals(other: Person): Boolean {
        return id == other.id && name == other.name
    }
}
typealias Location = Person
