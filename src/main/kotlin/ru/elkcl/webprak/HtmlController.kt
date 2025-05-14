package ru.elkcl.webprak

import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Controller
class HtmlController(
    private val operationDAO: OperationDAO,
    private val clientDAO: ClientDAO,
    private val serviceDAO: ServiceDAO
) {
    @GetMapping("/")
    fun home(model: Model): String {
        model["title"] = "Домашняя страница"
        return "home"
    }

    @GetMapping("/operations")
    fun operations(
        model: Model,
        @RequestParam(name = "client_id")     clientId         : String?,
        @RequestParam(name = "service_id")    serviceId        : String?,
        @RequestParam(name = "client_query")  clientQuery      : String?,
        @RequestParam(name = "service_query") serviceQuery     : String?,
        @RequestParam(name = "desc_query")    descriptionQuery : String?,
        @RequestParam(name = "type")          opType           : String?,
        @RequestParam(name = "time_min")      timeMin          : String?,
        @RequestParam(name = "time_max")      timeMax          : String?,
        @RequestParam(name = "amount_min")    amountMin        : String?,
        @RequestParam(name = "amount_max")    amountMax        : String?,
        @RequestParam(name = "page")          page             : String?,
        @RequestParam(name = "page_size")     pageSize         : String?,
        @RequestParam(name = "sort_by")       sortBy           : String?,
    ): String {
        model["title"] = "Операции"
        var spec: Specification<Operation> = Specification.where(null)
        if (!clientId.isNullOrEmpty()) {
            spec = spec.and(operationClientIdIs(clientId.toLongOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "client_id must be a Long")))
        }
        if (!serviceId.isNullOrEmpty()) {
            spec = spec.and(operationServiceIdIs(serviceId.toLongOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "service_id must be a Long")))
        }
        if (!clientQuery.isNullOrEmpty()) {
            spec = spec.and(operationClientSatisfies(clientNameContains(clientQuery)).or(operationClientSatisfies(clientContactInfoContains(clientQuery))))
        }
        if (!serviceQuery.isNullOrEmpty()) {
            spec = spec.and(operationServiceSatisfies(serviceNameContains(serviceQuery).or(serviceDescriptionContains(serviceQuery))))
        }
        if (!descriptionQuery.isNullOrEmpty()) {
            spec = spec.and(operationDescriptionContains(descriptionQuery))
        }
        if (!opType.isNullOrEmpty()) {
            val type = OperationType.entries.firstOrNull { it.name == opType }
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid operation type")
            spec = spec.and(operationTypeIs(type))
        }
        if (!timeMin.isNullOrEmpty()) {
            spec = spec.and(operationTimeMin(LocalDateTime.parse(timeMin, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        }
        if (!timeMax.isNullOrEmpty()) {
            spec = spec.and(operationTimeMax(LocalDateTime.parse(timeMax, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        }
        if (!amountMin.isNullOrEmpty()) {
            spec = spec.and(operationAbsAmountMin(amountMin.toIntOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "amount_min must be a Integer")))
        }
        if (!amountMax.isNullOrEmpty()) {
            spec = spec.and(operationAbsAmountMin(amountMax.toIntOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "amount_max must be a Integer")))
        }
        try {
            val resPage = operationDAO.findAll(
                spec,
                PageRequest.of(page?.toInt() ?: 0, pageSize?.toInt() ?: 10, Sort.by(sortBy ?: "id"))
            )
            model["operations"] = resPage.toList().map { it.render() }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request")
        }

//        model["operations"] = operationDAO.findAll(spec).map { it.render() }
        return "operations"
    }
    
    @PostMapping("/operations/add")
    fun addOperation(
        model: Model,
        @RequestParam(name = "client_id")     clientId         : String,
        @RequestParam(name = "service_id")    serviceId        : String?,
        @RequestParam(name = "description")   description      : String?,
        @RequestParam(name = "type")          opType           : String,
        @RequestParam(name = "amount")        amount           : String,
    ): String {
        val client: Client = clientDAO.findByIdOrNull(
            clientId.toLongOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "client_id must be a Long")
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "client does not exist")
        val service: Service? = when (serviceId) {
            null -> null
            "" -> null
            else -> serviceDAO.findByIdOrNull(
                serviceId.toLongOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "service_id must be a Long")
            ) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "service does not exist")
        }
        val type: OperationType = OperationType.entries.firstOrNull { it.name == opType }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid operation type")
        val delta: Int = type.sign * (amount.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be an Int"))
        val operation = Operation(
            client,
            type,
            LocalDateTime.now(),
            delta,
            service,
            when (description) {
                "" -> null
                else -> description
            }
        )
        operationDAO.save(operation)
        clientDAO.save(client)
        model["title"] = "Операция #${operation.id} успешно добавлена!"
        model["goback"] = "/operations"
        return "success"
    }

    data class RenderedOperation(
        val id: String,
        val client: String,
        val opType: String,
        val service: String,
        val description: String,
        val timestamp: String,
        val amount: String,
    )

    fun Operation.render() = RenderedOperation(
        id.toString(),
        "[${client.id.toString()}] ${client.name}",
        operationType.toString(),
        if (service != null) "[${service!!.id.toString()}] ${service!!.name}" else "—",
        description ?: "—",
        timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        abs(amount).toString(),
    )


    @GetMapping("/services")
    fun services(
        model: Model,
        @RequestParam(name = "name_query")    nameQuery        : String?,
        @RequestParam(name = "desc_query")    descriptionQuery : String?,
        @RequestParam(name = "service_type")  serviceType      : String?,
        @RequestParam(name = "billing_type")  billingType      : String?,
        @RequestParam(name = "page")          page             : String?,
        @RequestParam(name = "page_size")     pageSize         : String?,
        @RequestParam(name = "sort_by")       sortBy           : String?,
    ): String {
        model["title"] = "Услуги"
        var spec: Specification<Service> = Specification.where(null)
        if (!nameQuery.isNullOrEmpty()) {
            spec = spec.and(serviceNameContains(nameQuery))
        }
        if (!descriptionQuery.isNullOrEmpty()) {
            spec = spec.and(serviceDescriptionContains(descriptionQuery))
        }
        if (!serviceType.isNullOrEmpty()) {
            val type = ServiceType.entries.firstOrNull { it.name == serviceType }
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid service type")
            spec = spec.and(serviceTypeIs(type))
        }
        if (!billingType.isNullOrEmpty()) {
            val type = BillingType.entries.firstOrNull { it.name == billingType }
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid billing type")
            spec = spec.and(serviceBillingTypeIs(type))
        }
        try {
            val resPage = serviceDAO.findAll(
                spec,
                PageRequest.of(page?.toInt() ?: 0, pageSize?.toInt() ?: 10, Sort.by(sortBy ?: "id"))
            )
            model["services"] = resPage.toList().map { it.render() }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request")
        }

//        model["operations"] = operationDAO.findAll(spec).map { it.render() }
        return "services"
    }

    @PostMapping("/services/add")
    fun addService(
        model: Model,
        @RequestParam(name = "name")           name             : String,
        @RequestParam(name = "description")    description      : String,
        @RequestParam(name = "service_type")   serviceType      : String,
        @RequestParam(name = "billing_type")   billingType      : String,
        @RequestParam(name = "oneoff")         oneoffAmount     : String?,
        @RequestParam(name = "initial")        initialAmount    : String?,
        @RequestParam(name = "recurring")      recurringAmount  : String?,
    ): String {
        val servType: ServiceType = ServiceType.entries.firstOrNull { it.name == serviceType }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid service type")
        val billType: BillingType = BillingType.entries.firstOrNull { it.name == billingType }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid billing type")
        val billingInfo: BaseBillingInfo = when (billType) {
            BillingType.ONEOFF -> OneoffBillingInfo(
                oneoffAmount?.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "oneoff must be an Int"),
            )
            BillingType.MONTHLY -> MonthlyBillingInfo(
                initialAmount?.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "initial must be an Int"),
                recurringAmount?.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "recurring must be an Int"),
            )
        }
        val service = Service(
            name,
            description,
            servType,
            billingInfo,
        )
        serviceDAO.save(service)
        model["title"] = "Услуга #${service.id} успешно добавлена!"
        model["goback"] = "/services"
        return "success"
    }

    @GetMapping("/services/{id}")
    fun service(
        @PathVariable("id") ids: String,
        model: Model,
    ): String {
        val id: Long = ids.toLongOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid id")
        val service: Service = serviceDAO.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "invalid service")
        model["title"] = "Услуга #$ids"
        model["id"] = service.id.toString()
        model["name"] = service.name
        model["description"] = service.description
        model["serviceType"] = Mustache.Lambda { frag, writer ->
            val it = frag?.execute()
            if (writer != null) {
                if (it != null && service.serviceType.name == it) {
                    writer.write("selected")
                }
            }
        }
        model["billingType"] = Mustache.Lambda { frag, writer ->
            val qu = frag?.execute()
            val type = BillingType.entries.firstOrNull { it.name == qu }
            if (type != null) {
                if (billingInfo(type).qualifiedName == service.billingInfo::class.qualifiedName)
                    writer.write("selected")
            }
        }
        when (service.billingInfo) {
            is OneoffBillingInfo -> {
                model["oneoff"] = (service.billingInfo as OneoffBillingInfo).amount
                model["initial"] = ""
                model["recurring"] = ""
            }
            is MonthlyBillingInfo -> {
                model["oneoff"] = ""
                model["initial"] = (service.billingInfo as MonthlyBillingInfo).initial
                model["recurring"] = (service.billingInfo as MonthlyBillingInfo).recurring
            }
        }
        return "service"
    }

    @PostMapping("/services/{id}/edit")
    fun editService(
        @PathVariable("id") ids: String,
        model: Model,
        @RequestParam(name = "name")           name             : String,
        @RequestParam(name = "description")    description      : String,
        @RequestParam(name = "service_type")   serviceType      : String,
        @RequestParam(name = "billing_type")   billingType      : String,
        @RequestParam(name = "oneoff")         oneoffAmount     : String?,
        @RequestParam(name = "initial")        initialAmount    : String?,
        @RequestParam(name = "recurring")      recurringAmount  : String?,
    ): String {
        val id: Long = ids.toLongOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid id")
        val service: Service = serviceDAO.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "invalid service")
        val servType: ServiceType = ServiceType.entries.firstOrNull { it.name == serviceType }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid service type")
        val billType: BillingType = BillingType.entries.firstOrNull { it.name == billingType }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid billing type")
        val billingInfo: BaseBillingInfo = when (billType) {
            BillingType.ONEOFF -> OneoffBillingInfo(
                oneoffAmount?.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "oneoff must be an Int"),
            )
            BillingType.MONTHLY -> MonthlyBillingInfo(
                initialAmount?.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "initial must be an Int"),
                recurringAmount?.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "recurring must be an Int"),
            )
        }
        service.name = name
        service.description = description
        service.serviceType = servType
        service.billingInfo = billingInfo
        serviceDAO.save(service)

        model["title"] = "Услуга #$ids успешно обновлена!"
        model["goback"] = "/services/$id"
        return "success"
    }

    @PostMapping("/services/{id}/delete")
    fun deleteService(@PathVariable("id") ids: String, model: Model): String {
        val id: Long = ids.toLongOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid id")
        val service: Service = serviceDAO.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "invalid service")
        serviceDAO.delete(service)

        model["title"] = "Услуга #$ids успешно удалена!"
        model["goback"] = "/services"
        return "success"
    }

    data class RenderedService(
        val id: String,
        val name: String,
        val description: String,
        val serviceType: String,
        val billingInfo: String,
    )

    fun Service.render() = RenderedService(
        id.toString(),
        name,
        description,
        serviceType.toString(),
        billingInfo.toString(),
    )

    @GetMapping("/clients")
    fun clients(
        model: Model,
        @RequestParam(name = "name_query")    nameQuery        : String?,
        @RequestParam(name = "contact_query") contactQuery     : String?,
        @RequestParam(name = "type")          clientType       : String?,
        @RequestParam(name = "time_min")      timeMin          : String?,
        @RequestParam(name = "time_max")      timeMax          : String?,
        @RequestParam(name = "balance_min")   balanceMin       : String?,
        @RequestParam(name = "balance_max")   balanceMax       : String?,
        @RequestParam(name = "limit_min")     limitMin         : String?,
        @RequestParam(name = "limit_max")     limitMax         : String?,
        @RequestParam(name = "due_min")       dueMin           : String?,
        @RequestParam(name = "due_max")       dueMax           : String?,
        @RequestParam(name = "page")          page             : String?,
        @RequestParam(name = "page_size")     pageSize         : String?,
        @RequestParam(name = "sort_by")       sortBy           : String?,
    ): String {
        model["title"] = "Клиенты"
        var spec: Specification<Client> = Specification.where(null)
        if (!nameQuery.isNullOrEmpty()) {
            spec = spec.and(clientNameContains(nameQuery))
        }
        if (!contactQuery.isNullOrEmpty()) {
            spec = spec.and(clientContactInfoContains(contactQuery))
        }
        if (!clientType.isNullOrEmpty()) {
            val type = ClientType.entries.firstOrNull { it.name == clientType }
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid client type")
            spec = spec.and(clientTypeIs(type))
        }
        if (!timeMin.isNullOrEmpty() || !timeMax.isNullOrEmpty()) {
            var opSpec: Specification<Operation> = Specification.where(null)
            if (!timeMin.isNullOrEmpty()) {
                opSpec = opSpec.and(operationTimeMin(LocalDateTime.parse(timeMin, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            }
            if (!timeMax.isNullOrEmpty()) {
                opSpec = opSpec.and(operationTimeMax(LocalDateTime.parse(timeMax, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            }
            val specIds: List<Long> = operationDAO.findAll(opSpec).map { it.client.id!! }
            spec = spec.and(clientIdIsIn(specIds))
        }
        if (!balanceMin.isNullOrEmpty()) {
            spec = spec.and(
                clientBalanceMin(balanceMin.toIntOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "balance_min must be a Integer"))
            )
        }
        if (!balanceMax.isNullOrEmpty()) {
            spec = spec.and(
                clientBalanceMax(balanceMax.toIntOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "balance_max must be a Integer"))
            )
        }
        if (!limitMin.isNullOrEmpty()) {
            spec = spec.and(
                clientCreditLimitMin(limitMin.toIntOrNull()
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "limit_min must be a Integer"))
            )
        }
        if (!limitMax.isNullOrEmpty()) {
            spec = spec.and(
                clientCreditLimitMax(limitMax.toIntOrNull()
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "limit_max must be a Integer"))
            )
        }
        if (!dueMin.isNullOrEmpty()) {
            spec = spec.and(clientCreditDueMin(LocalDate.parse(dueMin, DateTimeFormatter.ISO_LOCAL_DATE)))
        }
        if (!dueMax.isNullOrEmpty()) {
            spec = spec.and(clientCreditDueMax(LocalDate.parse(dueMax, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        }
        try {
            val resPage = clientDAO.findAll(
                spec,
                PageRequest.of(page?.toInt() ?: 0, pageSize?.toInt() ?: 10, Sort.by(sortBy ?: "id"))
            )
            model["clients"] = resPage.toList().map { it.render() }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request")
        }

        return "clients"
    }

    @PostMapping("/clients/add")
    fun addClient(
        model: Model,
        @RequestParam(name = "name")          name             : String,
        @RequestParam(name = "type")          clientType       : String,
        @RequestParam(name = "passport_num")  passportNumber   : String?,
        @RequestParam(name = "passport_iss")  passportIssue    : String?,
        @RequestParam(name = "registry")      registry         : String?,
        @RequestParam(name = "taxpayer")      taxpayer         : String?,
        @RequestParam(name = "address")       address          : String?,
        @RequestParam(name = "limit")         limit            : String?,
        @RequestParam(name = "due")           due              : String?,
    ): String {
        val type: ClientType = ClientType.entries.firstOrNull { it.name == clientType }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid client type")
        val clientInfo = when (type) {
            ClientType.INDIVIDUAL -> IndividualClientInfo(
                passportNumber ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "passport number missing"),
                LocalDate.parse(passportIssue ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "passport issue date missing"), DateTimeFormatter.ISO_LOCAL_DATE),
            )
            ClientType.LEGAL_ENTITY -> LegalEntityClientInfo(
                registry ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "registry missing"),
                taxpayer ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "taxpayer missing"),
                address ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "address missing"),
            )
        }
        val client = Client(
            name,
            clientInfo,
            mutableSetOf(),
            limit?.toIntOrNull() ?: 0,
            if (!due.isNullOrEmpty()) LocalDate.parse(due, DateTimeFormatter.ISO_LOCAL_DATE) else null,
        )
        clientDAO.save(client)
        model["title"] = "Клиент #${client.id} успешно добавлен!"
        model["goback"] = "/clients"
        return "success"
    }

    @GetMapping("/clients/{id}")
    fun client(
        @PathVariable("id") ids: String,
        model: Model,
    ): String {
        val id: Long = ids.toLongOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid id")
        val client: Client = clientDAO.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "invalid client")
        model["title"] = "Клиент #$ids"
        model["id"] = client.id.toString()
        model["name"] = client.name
        model["clientType"] = Mustache.Lambda { frag, writer ->
            val qu = frag?.execute()
            val type = ClientType.entries.firstOrNull { it.name == qu }
            if (type != null) {
                if (clientInfo(type).qualifiedName == client.clientInfo::class.qualifiedName)
                    writer.write("selected")
            }
        }
        when (client.clientInfo) {
            is IndividualClientInfo -> {
                model["passport_num"] = (client.clientInfo as IndividualClientInfo).passportNumber
                model["passport_iss"] = (client.clientInfo as IndividualClientInfo).passportIssueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                model["registry"] = ""
                model["taxpayer"] = ""
                model["address"] = ""
            }
            is LegalEntityClientInfo -> {
                model["passport_num"] = ""
                model["passport_iss"] = ""
                model["registry"] = (client.clientInfo as LegalEntityClientInfo).registryNumber
                model["taxpayer"] = (client.clientInfo as LegalEntityClientInfo).taxpayerNumber
                model["address"] = (client.clientInfo as LegalEntityClientInfo).address
            }
        }
        model["limit"] = client.creditLimit.toString()
        model["due"] = client.creditDue?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: ""
        model["balance"] = client.balance
        return "client"
    }

    @PostMapping("/clients/{id}/edit")
    fun editClient(
        @PathVariable("id") ids: String,
        model: Model,
        @RequestParam(name = "name")          name             : String,
        @RequestParam(name = "type")          clientType       : String,
        @RequestParam(name = "passport_num")  passportNumber   : String?,
        @RequestParam(name = "passport_iss")  passportIssue    : String?,
        @RequestParam(name = "registry")      registry         : String?,
        @RequestParam(name = "taxpayer")      taxpayer         : String?,
        @RequestParam(name = "address")       address          : String?,
        @RequestParam(name = "limit")         limit            : String?,
        @RequestParam(name = "due")           due              : String?,
    ): String {
        val id: Long = ids.toLongOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid id")
        val client: Client = clientDAO.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "invalid client")
        val clType: ClientType = ClientType.entries.firstOrNull { it.name == clientType }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid client type")
        val clientInfo: BaseClientInfo = when (clType) {
            ClientType.INDIVIDUAL -> IndividualClientInfo(
                passportNumber ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "passport number missing"),
                LocalDate.parse(passportIssue ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "passport issue date missing"), DateTimeFormatter.ISO_LOCAL_DATE),
            )
            ClientType.LEGAL_ENTITY -> LegalEntityClientInfo(
                registry ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "registry missing"),
                taxpayer ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "taxpayer missing"),
                address ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "address missing"),
            )
        }
        client.name = name
        client.clientInfo = clientInfo
        if (!limit.isNullOrEmpty()) {
            client.creditLimit = limit.toIntOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid limit")
        }
        if (!due.isNullOrEmpty()) {
            client.creditDue = LocalDate.parse(due, DateTimeFormatter.ISO_LOCAL_DATE)
        }
        clientDAO.save(client)

        model["title"] = "Клиент #$ids успешно обновлен!"
        model["goback"] = "/clients/$id"
        return "success"
    }

    @PostMapping("/clients/{id}/delete")
    fun deleteClient(@PathVariable("id") ids: String, model: Model): String {
        val id: Long = ids.toLongOrNull() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid id")
        val client: Client = clientDAO.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "invalid client")
        clientDAO.delete(client)

        model["title"] = "Клиент #$ids успешно удален!"
        model["goback"] = "/clients"
        return "success"
    }

    data class RenderedClient(
        val id: String,
        val name: String,
        val clientInfo: String,
        val creditLimit: String,
        val creditDue: String,
        val balance: String,
    )

    fun Client.render() = RenderedClient(
        id.toString(),
        name,
        clientInfo.toString(),
        creditLimit.toString(),
        creditDue?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "—",
        balance.toString(),
    )
}