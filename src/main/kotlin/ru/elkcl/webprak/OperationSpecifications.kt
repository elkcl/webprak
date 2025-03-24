package ru.elkcl.webprak

import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

fun operationClientIdIs(clientId: Long): Specification<Operation> {
    return Specification<Operation> { root, _, builder ->
        val withClient = root.join(Operation_.client)
        builder.equal(withClient.get(Client_.id), clientId)
    }
}

fun operationClientSatisfies(clientSpec: Specification<Client>): Specification<Operation> {
    return Specification<Operation> { root, query, builder ->
        val subQuery = query!!.subquery(Client::class.java)
        val subRoot = subQuery.from(Client::class.java)
        subQuery.select(subRoot as Expression<Client>).where(clientSpec.toPredicate(subRoot, query, builder))
        builder.`in`(root.get(Operation_.client)).value(subQuery)
    }
}

fun operationTypeIs(operationType: OperationType): Specification<Operation> {
    return Specification<Operation> { root, _, builder ->
        builder.equal(root.get(Operation_.operationType), operationType)
    }
}

fun operationDescriptionContains(operationDesc: String): Specification<Operation> {
    return Specification<Operation> { root, _, builder ->
        builder.like(root.get(Operation_.description), "%$operationDesc%")
    }
}

fun operationServiceIdIs(serviceId: Long): Specification<Operation> {
    return Specification<Operation> { root, _, builder ->
        val withService = root.join(Operation_.service)
        builder.equal(withService.get(Service_.id), serviceId)
    }
}

fun operationServiceSatisfies(serviceSpec: Specification<Service>): Specification<Operation> {
    return Specification<Operation> { root, query, builder ->
        val subQuery = query!!.subquery(Service::class.java)
        val subRoot = subQuery.from(Service::class.java)
        subQuery.select(subRoot as Expression<Service>).where(serviceSpec.toPredicate(subRoot, query, builder))
        builder.`in`(root.get(Operation_.service)).value(subQuery)
    }
}

fun operationTimeMin(ts: LocalDateTime): Specification<Operation> {
    return Specification<Operation> { root, _, builder ->
        builder.greaterThanOrEqualTo(root.get(Operation_.timestamp), ts)
    }
}

fun operationTimeMax(ts: LocalDateTime): Specification<Operation> {
    return Specification<Operation> { root, _, builder ->
        builder.lessThanOrEqualTo(root.get(Operation_.timestamp), ts)
    }
}

fun operationAbsAmountMin(amount: Int): Specification<Operation> {
    return Specification<Operation> { root, _, builder ->
        builder.or(
            builder.ge(root.get(Operation_.amount), amount),
            builder.le(root.get(Operation_.amount), -amount)
        )
    }
}

fun operationAbsAmountMax(amount: Int): Specification<Operation> {
    return Specification<Operation> { root, _, builder ->
        builder.and(
            builder.le(root.get(Operation_.amount), amount),
            builder.ge(root.get(Operation_.amount), -amount)
        )
    }
}