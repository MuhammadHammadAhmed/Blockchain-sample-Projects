package com.example.states

import com.example.contracts.PurchaseOrderContract
import com.example.schema.PurchaseOrderschemaV1

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable

// *********
// * State *
// *********
@BelongsToContract(PurchaseOrderContract::class)
@CordaSerializable
data class PurchaseOrderState(
        val date: String,
        val vendor: Party,
        val bank: Party,
        val client: Party,
        val referenceId : String,
        val proformaId: String,
        val description: String,
        val proforma: ProformaState,
        val amount :Int,
        val tenor: Int,
        val consumable : Boolean,
       val  insuranceTerms : Boolean) : LinearState,QueryableState {//TODO CHANGED TO STRING
    override val participants = listOf(vendor, bank)
    override val linearId = UniqueIdentifier()

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is PurchaseOrderschemaV1 -> PurchaseOrderschemaV1.PersistentPurchaseOrder(
                    this.date,
                    this.vendor.name.toString(),
                    this.bank.name.toString(),
                    this.client.name.toString(),
                    this.referenceId,
                    this.proformaId,
                    this.description.toString(),
                    this.proforma.proformaId,
                    this.amount,
                    this.tenor,
                    this.consumable,
                    this.insuranceTerms,
                    this.linearId.id

            )

            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(PurchaseOrderschemaV1)
}