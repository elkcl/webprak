package ru.elkcl.webprak

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest(
    properties = [
        "spring.jpa.generate-ddl=true",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ClientDAOTests @Autowired constructor(
    val entityManager: TestEntityManager,
    val clientDAO: ClientDAO,
) {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @Test
    fun `When findByIdOrNull then Client`() {
        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val client2 = Client(
            "Петя",
            IndividualClientInfo(
                "133789",
                LocalDate.of(2023, 3, 12)
            )
        )
        entityManager.persist(client2)
        entityManager.flush()

        val found = clientDAO.findByIdOrNull(client1.id)
        assertEquals(found, client1)
    }

    @Test
    fun `When clientNameContains then Client`() {
        val client1 = Client(
            "Вася Пупкин",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val client2 = Client(
            "Петя Рубчинский",
            IndividualClientInfo(
                "133789",
                LocalDate.of(2023, 3, 12)
            )
        )
        entityManager.persist(client2)
        entityManager.flush()

        val found = clientDAO.findAll(clientNameContains("Вася"))
        assertThat(found).hasSameElementsAs(listOf(client1))
    }

    @Test
    fun `When clientContactInfoContains then Client`() {
        val client1 = Client(
            "Вася Пупкин",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val client2 = Client(
            "Петя Рубчинский",
            IndividualClientInfo(
                "133789",
                LocalDate.of(2023, 3, 12)
            )
        )
        entityManager.persist(client2)
        entityManager.flush()

        val contact1 = Contact(
            "Максбетов",
            "Макс",
            "Васильевич",
            null,
            "+78005553535"
        )
        entityManager.persist(contact1)
        entityManager.flush()

        val client3 = Client(
            "ООО Рога и Копыта",
            LegalEntityClientInfo("42069", "333333", "Плутон"),
            mutableSetOf(contact1)
        )
        entityManager.persist(client3)
        entityManager.flush()

        val found = clientDAO.findAll(clientContactInfoContains("Макс"))
        assertThat(found).hasSameElementsAs(listOf(client3))
    }

    @Test
    fun `When client passport number and issue date pass then Client`() {
        val client1 = Client(
            "Вася Пупкин",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val client2 = Client(
            "Петя Рубчинский",
            IndividualClientInfo(
                "133789",
                LocalDate.of(2023, 3, 12)
            )
        )
        entityManager.persist(client2)
        entityManager.flush()

        val contact1 = Contact(
            "Максбетов",
            "Макс",
            "Васильевич",
            null,
            "+78005553535"
        )
        entityManager.persist(contact1)
        entityManager.flush()

        val client3 = Client(
            "ООО Рога и Копыта",
            LegalEntityClientInfo("42069", "333333", "Плутон"),
            mutableSetOf(contact1)
        )
        entityManager.persist(client3)
        entityManager.flush()

        val found = clientDAO.findAll(
            clientTypeIs(ClientType.INDIVIDUAL)
                .and(clientIndividualPassportNumberContains("1337"))
                .and(clientIndividualPassportIssueDateMin(LocalDate.of(2022, 3, 14)))
                .and(clientIndividualPassportIssueDateMax(LocalDate.of(2025, 3, 12)))
        )
        assertThat(found).hasSameElementsAs(listOf(client2))
    }

    @Test
    fun `When client registry number, taxpayer number and address pass then Client`() {
        val client1 = Client(
            "Вася Пупкин",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val client2 = Client(
            "Петя Рубчинский",
            IndividualClientInfo(
                "133789",
                LocalDate.of(2023, 3, 12)
            )
        )
        entityManager.persist(client2)
        entityManager.flush()

        val contact1 = Contact(
            "Максбетов",
            "Макс",
            "Васильевич",
            null,
            "+78005553535"
        )
        entityManager.persist(contact1)
        entityManager.flush()

        val client3 = Client(
            "ООО Рога и Копыта",
            LegalEntityClientInfo("42069", "333333", "Плутон"),
            mutableSetOf(contact1)
        )
        entityManager.persist(client3)
        entityManager.flush()

        val found = clientDAO.findAll(
            clientTypeIs(ClientType.LEGAL_ENTITY)
                .and(clientLegalEntityRegistryNumberContains("420"))
                .and(clientLegalEntityTaxpayerNumberContains("3"))
                .and(clientLegalEntityAddressContains("Плутон"))
        )
        assertThat(found).hasSameElementsAs(listOf(client3))
    }

    @Test
    fun `When deposit and client balance is in range then Client`() {
        val client1 = Client(
            "Вася Пупкин",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val client2 = Client(
            "Петя Рубчинский",
            IndividualClientInfo(
                "133789",
                LocalDate.of(2023, 3, 12)
            )
        )
        entityManager.persist(client2)
        entityManager.flush()

        val contact1 = Contact(
            "Максбетов",
            "Макс",
            "Васильевич",
            null,
            "+78005553535"
        )
        entityManager.persist(contact1)
        entityManager.flush()

        val client3 = Client(
            "ООО Рога и Копыта",
            LegalEntityClientInfo("42069", "333333", "Плутон"),
            mutableSetOf(contact1)
        )
        entityManager.persist(client3)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client2,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val found = clientDAO.findAll(
            clientBalanceMin(100).and(clientBalanceMax(100))
        )
        assertThat(found).hasSameElementsAs(listOf(client1))
    }

    @Test
    fun `When creditLimit and creditDue are in range then Client`() {
        val client1 = Client(
            "Вася Пупкин",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            ),
            mutableSetOf(),
            100,
            LocalDate.of(2025, 3, 12)
        )
        entityManager.persist(client1)
        entityManager.flush()

        val client2 = Client(
            "Петя Рубчинский",
            IndividualClientInfo(
                "133789",
                LocalDate.of(2023, 3, 12)
            ),
            mutableSetOf(),
            35,
            LocalDate.of(2025, 10, 12)
        )
        entityManager.persist(client2)
        entityManager.flush()

        val contact1 = Contact(
            "Максбетов",
            "Макс",
            "Васильевич",
            null,
            "+78005553535"
        )
        entityManager.persist(contact1)
        entityManager.flush()

        val client3 = Client(
            "ООО Рога и Копыта",
            LegalEntityClientInfo("42069", "333333", "Плутон"),
            mutableSetOf(contact1),
            1000,
            LocalDate.of(2025, 2, 10)
        )
        entityManager.persist(client3)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client2,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val found = clientDAO.findAll(
            clientCreditLimitMin(10)
                .and(clientCreditLimitMax(300))
                .and(clientCreditDueMin(LocalDate.of(2025, 1, 1)))
                .and(clientCreditDueMax(LocalDate.of(2025, 4, 1)))
        )
        assertThat(found).hasSameElementsAs(listOf(client1))
    }

    @Test
    fun `When creditLimit is in range and sort by balance then Page(Client)`() {
        val client1 = Client(
            "Вася Пупкин",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            ),
            mutableSetOf(),
            100,
            LocalDate.of(2025, 3, 12)
        )
        entityManager.persist(client1)
        entityManager.flush()

        val client2 = Client(
            "Петя Рубчинский",
            IndividualClientInfo(
                "133789",
                LocalDate.of(2023, 3, 12)
            ),
            mutableSetOf(),
            35,
            LocalDate.of(2025, 10, 12)
        )
        entityManager.persist(client2)
        entityManager.flush()

        val contact1 = Contact(
            "Максбетов",
            "Макс",
            "Васильевич",
            null,
            "+78005553535"
        )
        entityManager.persist(contact1)
        entityManager.flush()

        val client3 = Client(
            "ООО Рога и Копыта",
            LegalEntityClientInfo("42069", "333333", "Плутон"),
            mutableSetOf(contact1),
            1000,
            LocalDate.of(2025, 2, 10)
        )
        entityManager.persist(client3)
        entityManager.flush()

        val client4 = Client(
            "Альберт Эйнштейн",
            IndividualClientInfo(
                "99999",
                LocalDate.of(2024, 5, 24),
            ),
            mutableSetOf(),
            115,
            LocalDate.of(2026, 3, 11)
        )
        entityManager.persist(client4)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client2,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val deposit3 = Operation(
            client3,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 3, 1, 2, 3),
            1000
        )
        entityManager.persist(deposit3)
        entityManager.flush()

        val withdraw2 = Operation(
            client2,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 4, 4, 1, 2, 3),
            -40
        )
        entityManager.persist(withdraw2)
        entityManager.flush()

        val deposit4 = Operation(
            client4,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 5, 1, 2, 3),
            120
        )
        entityManager.persist(deposit4)
        entityManager.flush()

        val found = clientDAO.findAll(
            clientCreditLimitMin(10)
                .and(clientCreditLimitMax(300)),
            PageRequest.of(0, 2, Sort.by("balance"))
        )
        assertEquals(found.toList(), listOf(client1, client2))
        assertEquals(found.totalPages, 2)
        assertEquals(found.totalElements, 3)
    }
}