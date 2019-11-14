package com.example.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction


// ************
// * Contract *
// ************

class DummyContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val CONTRACT_ID = "com.example.contracts.DummyContract"
    }


    override fun verify(tx: LedgerTransaction) {
    }
    interface Commands : CommandData {
        class  Create : Commands

    }
}
