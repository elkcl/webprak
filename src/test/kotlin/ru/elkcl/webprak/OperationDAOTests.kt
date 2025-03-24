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
class OperationDAOTests @Autowired constructor(
    val entityManager: TestEntityManager,
    val operationDAO: OperationDAO,
) {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @Test
    fun `When findByIdOrNull then Operation`() {
        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
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
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150
        )
        entityManager.persist(deposit2)
        entityManager.flush()
        val found = operationDAO.findByIdOrNull(deposit1.id)
        assertEquals(found, deposit1)
    }

    @Test
    fun `When operationClientIdIs then Operation`() {
        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
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
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150
        )
        entityManager.persist(deposit2)
        entityManager.flush()
        val found = operationDAO.findAll(operationClientIdIs(client1.id!!))
        assertThat(found).hasSameElementsAs(listOf(deposit1, deposit2))
    }

    @Test
    fun `When operationClientSatisfies then Operation`() {
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
        val found = operationDAO.findAll(operationClientSatisfies(clientNameContains("Вася")))
        assertThat(found).hasSameElementsAs(listOf(deposit1))
    }

    @Test
    fun `When operationTypeIs then Operation`() {
        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
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
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        entityManager.persist(withdraw1)
        entityManager.flush()

        val found = operationDAO.findAll(operationTypeIs(OperationType.DEPOSIT))
        assertThat(found).hasSameElementsAs(listOf(deposit1, deposit2))
    }

    @Test
    fun `When operationDescriptionContains then Operation`() {
        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100,
            null,
            "Пополнение через СБП"
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150,
            null,
            "Пополнение через автомат"
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        entityManager.persist(withdraw1)
        entityManager.flush()

        val found = operationDAO.findAll(operationDescriptionContains("СБП"))
        assertThat(found).hasSameElementsAs(listOf(deposit1))
    }

    @Test
    fun `When operationServiceIdIs then Operation`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet1)
        entityManager.flush()

        val serviceCalls1 = Service(
            "На телефоне",
            "100 мин на звонки каждый месяц",
            ServiceType.CALLS,
            MonthlyBillingInfo(100, 120)
        )
        entityManager.persist(serviceCalls1)
        entityManager.flush()

        val serviceSMS1 = Service(
            "Телеграфист",
            "100 SMS каждый месяц",
            ServiceType.SMS,
            MonthlyBillingInfo(70, 90)
        )
        entityManager.persist(serviceSMS1)
        entityManager.flush()

        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100,
            null,
            "Пополнение через СБП"
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150,
            null,
            "Пополнение через автомат"
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        entityManager.persist(withdraw1)
        entityManager.flush()

        val chargeInternet1 = Operation(
            client1,
            OperationType.CHARGE,
            LocalDateTime.of(2025, 5, 8, 1, 2, 3),
            -70,
            serviceInternet1
        )
        entityManager.persist(chargeInternet1)
        entityManager.flush()

        val found = operationDAO.findAll(operationServiceIdIs(serviceInternet1.id!!))
        assertThat(found).hasSameElementsAs(listOf(chargeInternet1))
    }

    @Test
    fun `When operationServiceSatisfies then Operation`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet1)
        entityManager.flush()

        val serviceCalls1 = Service(
            "На телефоне",
            "100 мин на звонки каждый месяц",
            ServiceType.CALLS,
            MonthlyBillingInfo(100, 120)
        )
        entityManager.persist(serviceCalls1)
        entityManager.flush()

        val serviceSMS1 = Service(
            "Телеграфист",
            "100 SMS каждый месяц",
            ServiceType.SMS,
            MonthlyBillingInfo(70, 90)
        )
        entityManager.persist(serviceSMS1)
        entityManager.flush()

        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100,
            null,
            "Пополнение через СБП"
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150,
            null,
            "Пополнение через автомат"
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        entityManager.persist(withdraw1)
        entityManager.flush()

        val chargeInternet1 = Operation(
            client1,
            OperationType.CHARGE,
            LocalDateTime.of(2025, 5, 8, 1, 2, 3),
            -70,
            serviceInternet1
        )
        entityManager.persist(chargeInternet1)
        entityManager.flush()

        val found = operationDAO.findAll(operationServiceSatisfies(serviceTypeIs(ServiceType.INTERNET)))
        assertThat(found).hasSameElementsAs(listOf(chargeInternet1))
    }

    @Test
    fun `When operationTime in range then Operation`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet1)
        entityManager.flush()

        val serviceCalls1 = Service(
            "На телефоне",
            "100 мин на звонки каждый месяц",
            ServiceType.CALLS,
            MonthlyBillingInfo(100, 120)
        )
        entityManager.persist(serviceCalls1)
        entityManager.flush()

        val serviceSMS1 = Service(
            "Телеграфист",
            "100 SMS каждый месяц",
            ServiceType.SMS,
            MonthlyBillingInfo(70, 90)
        )
        entityManager.persist(serviceSMS1)
        entityManager.flush()

        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100,
            null,
            "Пополнение через СБП"
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150,
            null,
            "Пополнение через автомат"
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        entityManager.persist(withdraw1)
        entityManager.flush()

        val chargeInternet1 = Operation(
            client1,
            OperationType.CHARGE,
            LocalDateTime.of(2025, 5, 8, 1, 2, 3),
            -70,
            serviceInternet1
        )
        entityManager.persist(chargeInternet1)
        entityManager.flush()

        val found = operationDAO.findAll(
            operationTimeMin(LocalDateTime.of(2025, 4, 2, 0, 0, 0))
                .and(operationTimeMax(LocalDateTime.of(2025, 5, 2, 0, 0, 0)))
        )
        assertThat(found).hasSameElementsAs(listOf(deposit2, withdraw1))
    }

    @Test
    fun `When operationAmount in range then Operation`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet1)
        entityManager.flush()

        val serviceCalls1 = Service(
            "На телефоне",
            "100 мин на звонки каждый месяц",
            ServiceType.CALLS,
            MonthlyBillingInfo(100, 120)
        )
        entityManager.persist(serviceCalls1)
        entityManager.flush()

        val serviceSMS1 = Service(
            "Телеграфист",
            "100 SMS каждый месяц",
            ServiceType.SMS,
            MonthlyBillingInfo(70, 90)
        )
        entityManager.persist(serviceSMS1)
        entityManager.flush()

        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100,
            null,
            "Пополнение через СБП"
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150,
            null,
            "Пополнение через автомат"
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        entityManager.persist(withdraw1)
        entityManager.flush()

        val chargeInternet1 = Operation(
            client1,
            OperationType.CHARGE,
            LocalDateTime.of(2025, 5, 8, 1, 2, 3),
            -70,
            serviceInternet1
        )
        entityManager.persist(chargeInternet1)
        entityManager.flush()

        val found = operationDAO.findAll(
            operationAbsAmountMin(90)
                .and(operationAbsAmountMax(160))
        )
        assertThat(found).hasSameElementsAs(listOf(deposit1, deposit2))
    }

    @Test
    fun `When operationAmount in range and operationType and sort by time then Page(Operation)`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet1)
        entityManager.flush()

        val serviceCalls1 = Service(
            "На телефоне",
            "100 мин на звонки каждый месяц",
            ServiceType.CALLS,
            MonthlyBillingInfo(100, 120)
        )
        entityManager.persist(serviceCalls1)
        entityManager.flush()

        val serviceSMS1 = Service(
            "Телеграфист",
            "100 SMS каждый месяц",
            ServiceType.SMS,
            MonthlyBillingInfo(70, 90)
        )
        entityManager.persist(serviceSMS1)
        entityManager.flush()

        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        entityManager.persist(client1)
        entityManager.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100,
            null,
            "Пополнение через СБП"
        )
        entityManager.persist(deposit1)
        entityManager.flush()

        val deposit2 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150,
            null,
            "Пополнение через автомат"
        )
        entityManager.persist(deposit2)
        entityManager.flush()

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        entityManager.persist(withdraw1)
        entityManager.flush()

        val chargeInternet1 = Operation(
            client1,
            OperationType.CHARGE,
            LocalDateTime.of(2025, 5, 8, 1, 2, 3),
            -70,
            serviceInternet1
        )
        entityManager.persist(chargeInternet1)
        entityManager.flush()

        val found = operationDAO.findAll(
            operationAbsAmountMin(70)
                .and(operationAbsAmountMax(160)),
            PageRequest.of(1, 2, Sort.by("timestamp"))
        )
        assertEquals(found.toList(), listOf(chargeInternet1))
        assertEquals(found.totalPages, 2)
        assertEquals(found.totalElements, 3)
    }
}