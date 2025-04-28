package ru.elkcl.webprak

enum class OperationType(val desc: String) {
    DEPOSIT("Пополнение"),
    WITHDRAW("Снятие"),
    CHARGE("Оплата");

    override fun toString(): String {
        return desc
    }
}