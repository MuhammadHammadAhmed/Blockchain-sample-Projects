package com.example.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class PurchaseOrderContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val CONTRACT_ID = "com.example.contracts.PurchaseOrderContract"
    }

    /*This  Insurance  transaction will be valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    */
    override fun verify(tx: LedgerTransaction) {


        }

    interface Commands : CommandData {
        class Issue : Commands

        class Withdraw : Commands
    }
    }