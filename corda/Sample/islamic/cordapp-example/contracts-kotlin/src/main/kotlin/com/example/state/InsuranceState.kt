package com.example.states

import com.example.contracts.InsuranceContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

// *********
// * State *
// *********
@BelongsToContract(InsuranceContract::class)
@CordaSerializable
data class InsuranceState(val insurer:Party, val insuree: Party, val policyReference : String, val asset : String, val sumInsured: Int,
                          val premium : Int,val issueDate: Int, val expiry: Int, val premiumpaid : Boolean,
                          override val participants: List<AbstractParty> = listOf(insurer, insuree)): LinearState {//TODO- add provision for insuree endorsement
    override val linearId = UniqueIdentifier()
}