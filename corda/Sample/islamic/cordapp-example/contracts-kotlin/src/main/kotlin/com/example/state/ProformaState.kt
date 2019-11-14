package com.example.states

 import com.example.states.LetterOfCreditDataStructures.Company
 import com.example.states.LetterOfCreditDataStructures.PricedGood
import com.example.contracts.ProformaContract
 import com.example.schema.GoodschemaV1
 import com.example.schema.ProformachemaV1
 import net.corda.core.contracts.BelongsToContract
 import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
 import net.corda.core.schemas.MappedSchema
 import net.corda.core.schemas.PersistentState
 import net.corda.core.schemas.QueryableState
 import net.corda.core.serialization.CordaSerializable
import java.time.LocalDate

// *********
// * State *
// *********
@BelongsToContract(ProformaContract::class)
data class ProformaState(val seller: Party,
                         val buyer: Party,
                         val date : String,
                         val proformaId: String,
                         val description: String,// changed from Purchaseorder properties
                         val goods : GoodsState,
                         val amount: Int,
                         override val participants: List<AbstractParty> = listOf(buyer, seller)) : LinearState, QueryableState {

    override val linearId = UniqueIdentifier()


    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ProformachemaV1 -> ProformachemaV1.PersistentProforma(
                    this.seller.name.toString(),
                    this.buyer.name.toString(),
                    this.date.toString(),
                    this.proformaId.toString(),
                    this.goods.toString(),
                    this.description.toString(),
                    this.amount.toInt(),
                    this.linearId.id
            )

            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ProformachemaV1)

}
