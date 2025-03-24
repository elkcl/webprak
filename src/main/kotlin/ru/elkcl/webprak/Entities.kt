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

    override fun hashCode() = javaClass.hashCode()

    override fun toString(): String {
        return "${this.javaClass.simpleName}(id=$id)"
    }
}

@Entity
class Client(
    var name: String,
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    var clientInfo: BaseClientInfo,
    @OneToMany(fetch = FetchType.LAZY)
    var contacts: MutableSet<Contact> = mutableSetOf(),
    var creditLimit: Int = 0,
    var creditDue: LocalDate = LocalDate.of(1980, 1, 1),
    var balance: Int = 0,
) : BaseEntity()

@Entity
class Contact(
//    @ManyToOne(fetch = FetchType.LAZY)
//    var client: Client,

    var familyName: String,
    var givenName: String,
    var patronymic: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var address: String? = null,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    var other: Map<String, String>? = null,
) : BaseEntity()

@Entity
class Operation(
    @ManyToOne(fetch = FetchType.LAZY)
    var client: Client,

    @Enumerated(EnumType.STRING)
    var operationType: OperationType,

    var timestamp: LocalDateTime,
    var amount: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    var service: Service? = null,

    @Column(columnDefinition = "text")
    var description: String? = null,
) : BaseEntity() {
    @PostPersist
    fun updateClient() {
        client.balance += amount
    }
}

@Entity
class Service(
    var name: String,

    @Column(columnDefinition = "text")
    var description: String,

    @Enumerated(EnumType.STRING)
    var serviceType: ServiceType,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    var billingInfo: BaseBillingInfo,
) : BaseEntity()