package com.example.states


import com.example.contracts.MurabahaContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import com.example.schema.MurabahaSchemaV1
import net.corda.core.schemas.QueryableState

// *********
// * State *
// *********
@BelongsToContract(MurabahaContract::class)
data class MurabahaState(val bank:Party,
                         val borrower: Party,
                         val internalReference : String,
                         val ageementDate: String,

                         val goods : GoodsState,
                         val costPrice :Int,
                         val sellingprice : Double,
                         val profitrate : Double,
                         val term : Int,
                         val bankSignature:Boolean,
                         val borrowerSignature:Boolean,
                 //        val inputGoods : StateAndRef<GoodsState>,

                         override val participants: List<AbstractParty> = listOf(bank, borrower)): LinearState,QueryableState {
    override val linearId = UniqueIdentifier()

override fun generateMappedObject(schema: MappedSchema): PersistentState {
    return when (schema) {
        is MurabahaSchemaV1 -> MurabahaSchemaV1.PersistentMurabaha(

        this.bank.toString(),
                this.borrower.toString(),
                this.internalReference.toString(),
                this.ageementDate,
                this.goods.toString(),
                    this.costPrice.toInt(),
            this.sellingprice,
            this.profitrate,
            this.term,
            this.bankSignature,
            this.borrowerSignature,
                this.linearId.id

        )

        else -> throw IllegalArgumentException("Unrecognised schema $schema")
    }
}
override fun supportedSchemas(): Iterable<MappedSchema> = listOf(MurabahaSchemaV1)

}
