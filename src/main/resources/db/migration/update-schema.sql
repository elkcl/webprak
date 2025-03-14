CREATE TABLE billing_types
(
    id   BIGINT NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_billingtypes PRIMARY KEY (id)
);

CREATE TABLE client_types
(
    id   BIGINT NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_clienttypes PRIMARY KEY (id)
);

CREATE TABLE clients
(
    id             BIGINT  NOT NULL,
    name           VARCHAR(255),
    client_type_id BIGINT,
    client_info    JSONB,
    balance        INTEGER NOT NULL,
    credit_limit   INTEGER NOT NULL,
    credit_due     date,
    CONSTRAINT pk_clients PRIMARY KEY (id)
);

CREATE TABLE contacts
(
    id          BIGINT NOT NULL,
    client_id   BIGINT,
    family_name VARCHAR(255),
    given_name  VARCHAR(255),
    patronymic  VARCHAR(255),
    email       VARCHAR(255),
    phone       VARCHAR(255),
    address     VARCHAR(255),
    other       JSONB,
    CONSTRAINT pk_contacts PRIMARY KEY (id)
);

CREATE TABLE operation_types
(
    id   BIGINT NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_operationtypes PRIMARY KEY (id)
);

CREATE TABLE operations
(
    id                BIGINT  NOT NULL,
    client_id         BIGINT,
    operation_type_id BIGINT,
    service_id        BIGINT,
    timestamp         TIMESTAMP WITHOUT TIME ZONE,
    amount            INTEGER NOT NULL,
    description       TEXT,
    CONSTRAINT pk_operations PRIMARY KEY (id)
);

CREATE TABLE service_types
(
    id   BIGINT NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_servicetypes PRIMARY KEY (id)
);

CREATE TABLE services
(
    id              BIGINT NOT NULL,
    name            VARCHAR(255),
    description     TEXT,
    service_type_id BIGINT,
    billing_type_id BIGINT,
    billing_info    JSONB,
    CONSTRAINT pk_services PRIMARY KEY (id)
);

ALTER TABLE clients
    ADD CONSTRAINT FK_CLIENTS_ON_CLIENTTYPE FOREIGN KEY (client_type_id) REFERENCES client_types (id);

ALTER TABLE contacts
    ADD CONSTRAINT FK_CONTACTS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id);

ALTER TABLE operations
    ADD CONSTRAINT FK_OPERATIONS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id);

ALTER TABLE operations
    ADD CONSTRAINT FK_OPERATIONS_ON_OPERATIONTYPE FOREIGN KEY (operation_type_id) REFERENCES operation_types (id);

ALTER TABLE operations
    ADD CONSTRAINT FK_OPERATIONS_ON_SERVICE FOREIGN KEY (service_id) REFERENCES services (id);

ALTER TABLE services
    ADD CONSTRAINT FK_SERVICES_ON_BILLINGTYPE FOREIGN KEY (billing_type_id) REFERENCES billing_types (id);

ALTER TABLE services
    ADD CONSTRAINT FK_SERVICES_ON_SERVICETYPE FOREIGN KEY (service_type_id) REFERENCES service_types (id);