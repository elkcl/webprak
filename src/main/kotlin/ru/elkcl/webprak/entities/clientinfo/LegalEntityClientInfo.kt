package ru.elkcl.webprak.entities.clientinfo

class LegalEntityClientInfo(
    val registryNumber: String,
    val taxpayerNumber: String,
    val address: String,
) : BaseClientInfo {
    override fun getType(): String {
        return "legal_entity"
    }
}