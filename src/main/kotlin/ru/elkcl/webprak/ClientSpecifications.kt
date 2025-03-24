package ru.elkcl.webprak

import org.hibernate.query.criteria.JpaExpression
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate

fun clientNameContains(clientName: String): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.like(root.get(Client_.name), "%$clientName%")
    }
}

fun clientContactInfoContains(query: String): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        val withContacts = root.join(Client_.contacts)
        builder.or(
            builder.like(withContacts.get(Contact_.familyName), "%$query%"),
            builder.like(withContacts.get(Contact_.givenName), "%$query%"),
            builder.like(withContacts.get(Contact_.patronymic), "%$query%"),
            builder.like(withContacts.get(Contact_.email), "%$query%"),
            builder.like(withContacts.get(Contact_.phone), "%$query%"),
            builder.like(withContacts.get(Contact_.address), "%$query%"),
            builder.like(builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                withContacts.get(Contact_.other),
                builder.literal("{}")
            ), "%$query%")
        )
    }
}

fun clientTypeIs(clientType: ClientType): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.equal(
            builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Client_.clientInfo),
                builder.literal("type")
            ), clientInfo(clientType).java.name
        )
    }
}

fun clientIndividualPassportIssueDateMin(date: LocalDate): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.greaterThanOrEqualTo(
            (builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Client_.clientInfo),
                builder.literal("passportIssueDate")
            ) as JpaExpression).cast(LocalDate::class.java), date
        )
    }
}

fun clientIndividualPassportIssueDateMax(date: LocalDate): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.lessThanOrEqualTo(
            (builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Client_.clientInfo),
                builder.literal("passportIssueDate")
            ) as JpaExpression).cast(LocalDate::class.java), date
        )
    }
}

fun clientIndividualPassportNumberContains(query: String): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.like(
            builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Client_.clientInfo),
                builder.literal("passportNumber")
            ), "%$query%"
        )
    }
}

fun clientLegalEntityRegistryNumberContains(query: String): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.like(
            builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Client_.clientInfo),
                builder.literal("registryNumber")
            ), "%$query%"
        )
    }
}

fun clientLegalEntityTaxpayerNumberContains(query: String): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.like(
            builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Client_.clientInfo),
                builder.literal("taxpayerNumber")
            ), "%$query%"
        )
    }
}

fun clientLegalEntityAddressContains(query: String): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.like(
            builder.function(
                "jsonb_extract_path_text",
                String::class.java,
                root.get(Client_.clientInfo),
                builder.literal("address")
            ), "%$query%"
        )
    }
}

fun clientBalanceMin(query: Int): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.ge(root.get(Client_.balance), query)
    }
}

fun clientBalanceMax(query: Int): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.le(root.get(Client_.balance), query)
    }
}

fun clientCreditLimitMin(query: Int): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.ge(root.get(Client_.creditLimit), query)
    }
}

fun clientCreditLimitMax(query: Int): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.le(root.get(Client_.creditLimit), query)
    }
}

fun clientCreditDueMin(query: LocalDate): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.greaterThanOrEqualTo(root.get(Client_.creditDue), query)
    }
}

fun clientCreditDueMax(query: LocalDate): Specification<Client> {
    return Specification<Client> { root, _, builder ->
        builder.lessThanOrEqualTo(root.get(Client_.creditDue), query)
    }
}