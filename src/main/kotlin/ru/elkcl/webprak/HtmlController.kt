package ru.elkcl.webprak

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        if (clientId != null) {
            spec = spec.and(operationClientIdIs(clientId.toLongOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "client_id should be a Long")))
        }
        if (serviceId != null) {
            spec = spec.and(operationServiceIdIs(serviceId.toLongOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "service_id should be a Long")))
        }
        if (clientQuery != null) {
            spec = spec.and(operationClientSatisfies(clientNameContains(clientQuery).or(clientContactInfoContains(clientQuery))))
        }
        if (serviceQuery != null) {
            spec = spec.and(operationServiceSatisfies(serviceNameContains(serviceQuery).or(serviceDescriptionContains(serviceQuery))))
        }
        if (descriptionQuery != null) {
            spec = spec.and(operationDescriptionContains(descriptionQuery))
        }
        if (opType != null) {
            val type = OperationType.entries.firstOrNull { it.name == opType }
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid operation type")
            spec = spec.and(operationTypeIs(type))
        }
        if (timeMin != null) {
            spec = spec.and(operationTimeMin(LocalDateTime.parse(timeMin, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        }
        if (timeMax != null) {
            spec = spec.and(operationTimeMax(LocalDateTime.parse(timeMax, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        }
        if (amountMin != null) {
            spec = spec.and(operationAbsAmountMin(amountMin.toIntOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "amount_min should be a Integer")))
        }
        if (amountMax != null) {
            spec = spec.and(operationAbsAmountMin(amountMax.toIntOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "amount_max should be a Integer")))
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

    data class RenderedOperation(
        val id: String,
        val client: String,
        val service: String,
        val description: String,
        val opType: String,
        val timestamp: String,
        val amount: String,
    )

    fun Operation.render() = RenderedOperation(
        id.toString(),
        "[${client.id.toString()}] ${client.name}",
        if (service != null) "[${service!!.id.toString()}] ${service!!.name}" else "—",
        description ?: "—",
        operationType.toString(),
        timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        amount.toString(),
    )
}