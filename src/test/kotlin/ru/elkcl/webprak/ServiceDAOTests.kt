package ru.elkcl.webprak

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest(
    properties = [
        "spring.jpa.generate-ddl=true",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ServiceDAOTests @Autowired constructor(
    val entityManager: TestEntityManager,
    val serviceDAO: ServiceDAO,
) {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @Test
    fun `When findByIdOrNull then return Service`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(100)
        )
        entityManager.persist(serviceInternet1)
        entityManager.flush()
        val serviceInternet2 = Service(
            "SOS-кнопка",
            "Дополнительный пакет интернета 5 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet2)
        entityManager.flush()
        val found = serviceDAO.findByIdOrNull(serviceInternet1.id)
        assertEquals(serviceInternet1, found)
    }

    @Test
    fun `When serviceNameContains then return listOf(Service)`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(100)
        )
        entityManager.persist(serviceInternet1)
        entityManager.flush()
        val serviceInternet2 = Service(
            "SOS-кнопка",
            "Дополнительный пакет интернета 5 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet2)
        entityManager.flush()
        val found = serviceDAO.findAll(serviceNameContains("Турбо"))
        assertEquals(listOf(serviceInternet1), found)
    }

    @Test
    fun `When serviceDescriptionContains then return listOf(Service)`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(100)
        )
        entityManager.persist(serviceInternet1)
        entityManager.flush()
        val serviceInternet2 = Service(
            "SOS-кнопка",
            "Дополнительный пакет интернета 5 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet2)
        entityManager.flush()
        val found = serviceDAO.findAll(serviceDescriptionContains("5 ГБ"))
        assertEquals(listOf(serviceInternet2), found)
    }

    @Test
    fun `When serviceTypeIs then return listOf(Service)`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(100)
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
        val serviceInternet2 = Service(
            "SOS-кнопка",
            "Дополнительный пакет интернета 5 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet2)
        entityManager.flush()
        val found = serviceDAO.findAll(serviceTypeIs(ServiceType.INTERNET))
        assertThat(found).hasSameElementsAs(listOf(serviceInternet1, serviceInternet2))
    }

    @Test
    fun `When serviceBillingTypeIs then return listOf(Service)`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(100)
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
        val serviceInternet2 = Service(
            "SOS-кнопка",
            "Дополнительный пакет интернета 5 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet2)
        entityManager.flush()
        val found = serviceDAO.findAll(serviceBillingTypeIs(BillingType.ONEOFF))
        assertThat(found).hasSameElementsAs(listOf(serviceInternet1, serviceInternet2))
    }

    @Test
    fun `When Oneoff payment is in range then return listOf(Service)`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(100)
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
        val serviceInternet2 = Service(
            "SOS-кнопка",
            "Дополнительный пакет интернета 5 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet2)
        entityManager.flush()
        val found = serviceDAO.findAll(
            serviceBillingTypeIs(BillingType.ONEOFF)
                .and(serviceOneoffAmountMin(60))
                .and(serviceOneoffAmountMax(80))
        )
        assertThat(found).hasSameElementsAs(listOf(serviceInternet2))
    }

    @Test
    fun `When Monthly initial payment is in range then return listOf(Service)`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(100)
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
            "Начинающий телеграфист",
            "100 SMS каждый месяц",
            ServiceType.SMS,
            MonthlyBillingInfo(70, 90)
        )
        entityManager.persist(serviceSMS1)
        entityManager.flush()
        val serviceInternet2 = Service(
            "SOS-кнопка",
            "Дополнительный пакет интернета 5 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet2)
        entityManager.flush()
        val found = serviceDAO.findAll(
            serviceBillingTypeIs(BillingType.MONTHLY)
                .and(serviceMonthlyInitialMin(90))
                .and(serviceMonthlyInitialMax(110))
        )
        assertThat(found).hasSameElementsAs(listOf(serviceCalls1))
    }

    @Test
    fun `When Monthly recurring payment is in range then return listOf(Service)`() {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(100)
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
            "Начинающий телеграфист",
            "100 SMS каждый месяц",
            ServiceType.SMS,
            MonthlyBillingInfo(70, 90)
        )
        entityManager.persist(serviceSMS1)
        entityManager.flush()
        val serviceInternet2 = Service(
            "SOS-кнопка",
            "Дополнительный пакет интернета 5 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        entityManager.persist(serviceInternet2)
        entityManager.flush()
        val found = serviceDAO.findAll(
            serviceBillingTypeIs(BillingType.MONTHLY)
                .and(serviceMonthlyRecurringMin(90))
                .and(serviceMonthlyRecurringMax(110))
        )
        assertThat(found).hasSameElementsAs(listOf(serviceSMS1))
    }
}