package ru.elkcl.webprak

import jakarta.persistence.*
import org.springframework.data.util.ProxyUtils
import io.hypersistence.utils.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import java.time.LocalDate
import java.time.LocalDateTime

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

@Entity
class BillingType(
    val name: String,
) : BaseEntity<Long>()

@Entity
class ClientType(
    val name: String,
) : BaseEntity<Long>()

@Entity
class OperationType(
    val name: String,
) : BaseEntity<Long>()

@Entity
class ServiceType(
    val name: String,
) : BaseEntity<Long>()

@Entity
class Client(
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val clientType: ClientType,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val clientInfo: BaseClientInfo,

    val balance: Int,
    val creditLimit: Int,
    val creditDue: LocalDate,
) : BaseEntity<Long>()

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
) : BaseEntity<Long>()

@Entity
class Operation(
    @ManyToOne(fetch = FetchType.LAZY)
    val client: Client,

    @ManyToOne(fetch = FetchType.LAZY)
    val operationType: OperationType,

    @ManyToOne(fetch = FetchType.LAZY)
    val service: Service?,

    val timestamp: LocalDateTime,
    val amount: Int,

    @Column(columnDefinition = "text")
    val description: String?,
) : BaseEntity<Long>()

@Entity
class Service(
    val name: String,

    @Column(columnDefinition = "text")
    val description: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val serviceType: ServiceType,

    @ManyToOne(fetch = FetchType.LAZY)
    val billingType: BillingType,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val billingInfo: BaseBillingInfo,
) : BaseEntity<Long>()