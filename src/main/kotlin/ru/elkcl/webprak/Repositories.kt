package ru.elkcl.webprak

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ClientDAO : JpaRepository<Client, Long>, JpaSpecificationExecutor<Client>
interface ServiceDAO : JpaRepository<Service, Long>, JpaSpecificationExecutor<Service>
interface OperationDAO : JpaRepository<Operation, Long>, JpaSpecificationExecutor<Operation>
interface ContactDAO : JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact>