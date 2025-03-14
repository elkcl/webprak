package ru.elkcl.webprak.entities

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import ru.elkcl.webprak.entities.billinginfo.BaseBillingInfo

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