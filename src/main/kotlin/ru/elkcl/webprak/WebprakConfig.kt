package ru.elkcl.webprak

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
class WebprakConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    fun dataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean
    fun databaseInitializer(
        clientDAO: ClientDAO,
        contactDAO: ContactDAO,
        serviceDAO: ServiceDAO,
        operationDAO: OperationDAO
    ) = ApplicationRunner {
        val serviceInternet1 = Service(
            "Турбо-кнопка",
            "Дополнительный пакет интернета 10 ГБ. Сгорает через один месяц.",
            ServiceType.INTERNET,
            OneoffBillingInfo(70)
        )
        serviceDAO.save(serviceInternet1)

        val serviceCalls1 = Service(
            "На телефоне",
            "100 мин на звонки каждый месяц",
            ServiceType.CALLS,
            MonthlyBillingInfo(100, 120)
        )
        serviceDAO.save(serviceCalls1)

        val serviceSMS1 = Service(
            "Телеграфист",
            "100 SMS каждый месяц",
            ServiceType.SMS,
            MonthlyBillingInfo(70, 90)
        )
        serviceDAO.save(serviceSMS1)

        serviceDAO.flush()

        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        clientDAO.save(client1)

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
        clientDAO.save(client2)

        val contact1 = Contact(
            "Максбетов",
            "Макс",
            "Васильевич",
            null,
            "+78005553535"
        )
        contactDAO.save(contact1)

        val client3 = Client(
            "ООО Рога и Копыта",
            LegalEntityClientInfo("42069", "333333", "Плутон"),
            mutableSetOf(contact1),
            1000,
            LocalDate.of(2025, 2, 10)
        )
        clientDAO.save(client3)

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
        clientDAO.save(client4)

        clientDAO.flush()

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100,
            null,
            "Пополнение через СБП"
        )
        operationDAO.save(deposit1)
        clientDAO.save(client1)

        val deposit2 = Operation(
            client2,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150,
            null,
            "Пополнение через автомат"
        )
        operationDAO.save(deposit2)
        clientDAO.save(client2)

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        operationDAO.save(withdraw1)

        val chargeInternet1 = Operation(
            client2,
            OperationType.CHARGE,
            LocalDateTime.of(2025, 5, 8, 1, 2, 3),
            -70,
            serviceInternet1
        )
        operationDAO.save(chargeInternet1)

        operationDAO.flush()
        clientDAO.flush()
    }
}