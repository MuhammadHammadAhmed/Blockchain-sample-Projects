package com.example.states


import com.example.schema.GoodschemaV1
import com.example.contracts.GoodsContract
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable



import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

// *********
// * State *
// *********
@BelongsToContract(GoodsContract::class)
@CordaSerializable
data class GoodsState(
        val seller:Party,
         val assetOwner: Party,
        val  quantity:Int, //val client :Party,//TODO remove client and add amount
        val internalReference : String,
        val asset : String, val takaful: Boolean,
         val owner :AbstractParty,
         override val participants: List<AbstractParty> = listOf(assetOwner )) : LinearState, QueryableState {
    override val linearId = UniqueIdentifier()

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is GoodschemaV1 -> GoodschemaV1.PersistentGoods(
                    this.seller.name.toString(),
                    this.assetOwner.name.toString(),
                    this.internalReference.toString(),
                    this.asset,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(GoodschemaV1)

}
