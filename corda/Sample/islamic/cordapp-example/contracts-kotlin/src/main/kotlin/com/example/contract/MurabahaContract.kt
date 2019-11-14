package com.example.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction
//import com.murabaha.contracts.MurabahaContract.Commands


// ************
// * Contract *
// ************
class MurabahaContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val CONTRACT_ID = "com.example.contracts.MurabahaContract"
    }

    /*This  Insurance  transaction will be valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    */
    override fun verify(tx: LedgerTransaction) {
     /*   val output= tx.outputs
        val inputs= tx.inputs
        //val command = tx.commands.requireSingleCommand()

            when (command.value){

                is Commands.Create -> {
                    requireThat{
                        //TODO, add conditions for issuance

                    }
                }
                is Commands.Offer ->{
                    requireThat{
                        //TODO, add constraints for Offer
                    }
                }
                is Commands.Acceptance ->{
                    requireThat{
                        //TODO, add constraints for Acceptance
                    }

                }*/
        }



    interface Commands : CommandData {
        class  Offer : Commands
        class Accept : Commands
        class Cancel : Commands
    }
}
