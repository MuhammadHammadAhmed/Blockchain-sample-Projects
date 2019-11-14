package com.example.states

import com.example.contracts.MurabahaApplicationContract
import com.example.schema.MurabahaApplicationschemaV1
import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable

// *********
// * State *
// *********
@BelongsToContract(MurabahaApplicationContract::class)
@CordaSerializable
data class MurabahaApplicationState(
        val date: String,
        val applicant: Party,
        val issuingBank: Party,
        val referenceId : String,
       val beneficiary: Party,
        val advisingBank: Party,
        val description: String,
        val tenor: Int,
        val amount: Int,
        val proforma : ProformaState) : LinearState,QueryableState {//TODO CHANGED TO STRING
    override val participants = listOf(applicant, issuingBank)
    override val linearId = UniqueIdentifier()

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is MurabahaApplicationschemaV1 -> MurabahaApplicationschemaV1.PersistentMurabahaApplication(
                    this.date.toString(),
                    this.applicant.name.toString(),
                    this.issuingBank.toString(),
                    this.referenceId.toString(),
                    this.beneficiary.name.toString(),
                    this.advisingBank.toString(),
                    this.description.toString(),
                    this.tenor.toInt(),
                    this.amount.toInt(),
                    this.proforma.goods.toString(),
                    this.linearId.id
            )

            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(MurabahaApplicationschemaV1)
}
