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

        val client1 = Client(
            "Вася",
            IndividualClientInfo(
                "123456",
                LocalDate.of(2024, 3, 14)
            )
        )
        clientDAO.save(client1)

        val deposit1 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 1, 1, 2, 3),
            100,
            null,
            "Пополнение через СБП"
        )
        operationDAO.save(deposit1)

        val deposit2 = Operation(
            client1,
            OperationType.DEPOSIT,
            LocalDateTime.of(2025, 4, 2, 1, 2, 3),
            150,
            null,
            "Пополнение через автомат"
        )
        operationDAO.save(deposit2)

        val withdraw1 = Operation(
            client1,
            OperationType.WITHDRAW,
            LocalDateTime.of(2025, 5, 1, 1, 2, 3),
            -50
        )
        operationDAO.save(withdraw1)

        val chargeInternet1 = Operation(
            client1,
            OperationType.CHARGE,
            LocalDateTime.of(2025, 5, 8, 1, 2, 3),
            -70,
            serviceInternet1
        )
        operationDAO.save(chargeInternet1)

//        serviceDAO.flush()
//        clientDAO.flush()
//        operationDAO.flush()
    }
}