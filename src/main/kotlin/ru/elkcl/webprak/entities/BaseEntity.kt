package ru.elkcl.webprak.entities

import jakarta.persistence.*
import org.springframework.data.util.ProxyUtils

@MappedSuperclass
abstract class BaseEntity<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: T? = null

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as BaseEntity<*>

        return this.id != null && this.id == other.id
    }

    override fun hashCode() = 25

    override fun toString(): String {
        return "${this.javaClass.simpleName}(id=$id)"
    }
}