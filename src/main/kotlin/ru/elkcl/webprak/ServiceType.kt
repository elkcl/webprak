package ru.elkcl.webprak

import java.io.Serializable

enum class ServiceType(val desc: String) : Serializable {
    CALLS("Звонки"),
    SMS("СМС"),
    INTERNET("Интернет");

    override fun toString(): String {
        return desc
    }
}