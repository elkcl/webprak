package ru.elkcl.webprak

import jakarta.persistence.*
import org.springframework.data.util.ProxyUtils
import io.hypersistence.utils.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import java.time.LocalDate
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as BaseEntity

        return this.id != null && this.id == other.id
    }

    override fun hashCode() = 25

    override fun toString(): String {
        return "${this.javaClass.simpleName}(id=$id)"
    }
}

@Entity
class Client(
    val name: String,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val clientInfo: BaseClientInfo,

    val balance: Int,
    val creditLimit: Int,
    val creditDue: LocalDate,
) : BaseEntity()

@Entity
class Contact(
    @ManyToOne(fetch = FetchType.LAZY)
    val client: Client,

    val familyName: String,
    val givenName: String,
    val patronymic: String?,
    val email: String?,
    val phone: String?,
    val address: String?,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val other: Map<String, String>?,
) : BaseEntity()

@Entity
class Operation(
    @ManyToOne(fetch = FetchType.LAZY)
    val client: Client,

    @Enumerated(EnumType.STRING)
    val operationType: OperationType,

    @ManyToOne(fetch = FetchType.LAZY)
    val service: Service?,

    val timestamp: LocalDateTime,
    val amount: Int,

    @Column(columnDefinition = "text")
    val description: String?,
) : BaseEntity()

@Entity
class Service(
    val name: String,

    @Column(columnDefinition = "text")
    val description: String,

    @Enumerated(EnumType.STRING)
    val serviceType: ServiceType,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val billingInfo: BaseBillingInfo,
) : BaseEntity()