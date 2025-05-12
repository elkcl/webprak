package ru.elkcl.webprak

import java.io.Serializable

enum class OperationType(val desc: String, val sign: Int) : Serializable {
    DEPOSIT("Пополнение", 1),
    WITHDRAW("Снятие", -1),
    CHARGE("Оплата", -1);

    override fun toString(): String {
        return desc
    }
}