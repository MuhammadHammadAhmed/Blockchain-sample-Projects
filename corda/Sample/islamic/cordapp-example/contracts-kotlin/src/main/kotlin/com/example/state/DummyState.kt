package com.example.states


import com.example.contracts.DummyContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party


// *********
// * State *
// *********
@BelongsToContract(DummyContract::class)
data class DummyState(
        val owner : Party,
        val client: Party,
        val number:Int,
                      val other: Int,
                  //    val text : String,

                      override val participants: List<AbstractParty> = listOf(owner,client)): ContractState {

}

