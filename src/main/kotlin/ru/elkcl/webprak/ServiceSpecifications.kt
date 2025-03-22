package ru.elkcl.webprak

import org.hibernate.query.criteria.JpaExpression
import org.springframework.data.jpa.domain.Specification

fun serviceNameContains(serviceName: String): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.like(root.get(Service_.name), "%$serviceName%")
    }
}

fun serviceDescriptionContains(serviceDesc: String): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.like(root.get(Service_.description), "%$serviceDesc%")
    }
}

fun serviceTypeIs(serviceType: ServiceType): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.equal(root.get(Service_.serviceType), serviceType)
    }
}

fun serviceBillingTypeIs(billingType: BillingType): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.equal(
            builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Service_.billingInfo),
                builder.literal("type")
            ), billingInfo(billingType).java.name
        )
    }
}

fun serviceOneoffAmountMin(amount: Int): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.ge(
            (builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Service_.billingInfo),
                builder.literal("amount")
            ) as JpaExpression).cast(Int::class.java), amount
        )
    }
}

fun serviceOneoffAmountMax(amount: Int): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.le(
            (builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Service_.billingInfo),
                builder.literal("amount")
            ) as JpaExpression).cast(Int::class.java), amount
        )
    }
}

fun serviceMonthlyInitialMin(amount: Int): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.ge(
            (builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Service_.billingInfo),
                builder.literal("initial")
            ) as JpaExpression).cast(Int::class.java), amount
        )
    }
}

fun serviceMonthlyInitialMax(amount: Int): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.le(
            (builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Service_.billingInfo),
                builder.literal("initial")
            ) as JpaExpression).cast(Int::class.java), amount
        )
    }
}

fun serviceMonthlyRecurringMin(amount: Int): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.ge(
            (builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Service_.billingInfo),
                builder.literal("recurring")
            ) as JpaExpression).cast(Int::class.java), amount
        )
    }
}

fun serviceMonthlyRecurringMax(amount: Int): Specification<Service> {
    return Specification<Service> { root, _, builder ->
        builder.le(
            (builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Service_.billingInfo),
                builder.literal("recurring")
            ) as JpaExpression).cast(Int::class.java), amount
        )
    }
}