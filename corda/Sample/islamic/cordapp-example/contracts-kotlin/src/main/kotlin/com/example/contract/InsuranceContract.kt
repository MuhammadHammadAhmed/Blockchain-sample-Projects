package com.example.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class InsuranceContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val CONTRACT_ID = "com.murabaha.contracts.InsuranceContract"
    }

    /*This  Insurance  transaction will be valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    */
    override fun verify(tx: LedgerTransaction) {
        //TODO, add constraints related to Insurance transaction
        }

    interface Commands : CommandData {
        class  IssueInsurance : Commands
        class PayPremium :Commands
        class AddInsuranceConfirmation : Commands
    }
    }