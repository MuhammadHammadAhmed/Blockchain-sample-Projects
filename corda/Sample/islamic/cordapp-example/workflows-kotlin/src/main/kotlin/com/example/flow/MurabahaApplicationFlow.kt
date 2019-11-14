@file:Suppress("UNUSED_VARIABLE", "unused")


package com.example.flows

import co.paralleluniverse.fibers.Suspendable
import com.example.contracts.MurabahaApplicationContract
import com.example.states.MurabahaApplicationState
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import com.example.states.ProformaState
import net.corda.core.contracts.Command
//import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker.Step

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC  //-----------------------------val goodsDescription: goodsDescription
class MurabahaApplicationFlow(val bank: Party, val proformaId : String, val term:Int) :FlowLogic<SignedTransaction>() {

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object FECHING_STATE :Step ("Fetching the proforma from the vault")
        object GETTING_NOTARY :Step ("getting Notary")
        object BUILDING_OUTPUTSTATE : Step("Building Outputstate.")

        object GENERATING_TRANSACTION : Step("Generating Murabaha Application transaction based on new Proforma.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                FECHING_STATE,
                GETTING_NOTARY,
                BUILDING_OUTPUTSTATE,
                        GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()
/* the flow logic is within the call method below
*/

    @Suspendable
    override fun call() : SignedTransaction {
        progressTracker.currentStep = GETTING_NOTARY
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        progressTracker.currentStep = FECHING_STATE
        val proformaList = serviceHub.vaultService.queryBy<ProformaState>().states
        val proforma = proformaList.find { stateAndRef -> stateAndRef.state.data.proformaId== proformaId }
                ?: throw IllegalArgumentException("No proforma with ID $proformaId found.")
        val proformaData =proforma.state.data

        progressTracker.currentStep = BUILDING_OUTPUTSTATE

        val applicant= serviceHub.myInfo.legalIdentities.first()
        val reference ="MAPP-"+proformaData.proformaId

        val application = MurabahaApplicationState("MM-DD-YY",applicant, bank ,reference,proformaData.seller, proformaData.seller,proformaData.description ,term,proformaData.amount,proformaData)
        val requestCommand = Command(MurabahaApplicationContract.Commands.Request(), listOf(ourIdentity.owningKey))// TODO, try adding babk key
        progressTracker.currentStep = GENERATING_TRANSACTION //TODO fine till here
        val builder = TransactionBuilder(notary)
   //     progressTracker.currentStep = GENERATING_TRANSACTION
        builder.addOutputState(application, MurabahaApplicationContract.CONTRACT_ID)
      //  progressTracker.currentStep = GENERATING_TRANSACTION
        builder.addCommand(requestCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        builder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val stx = serviceHub.signInitialTransaction(builder)

        val bankSession = initiateFlow(bank)
        progressTracker.currentStep = FINALISING_TRANSACTION

       // val notarisedTx1: SignedTransaction = subFlow(FinalityFlow(stx, listOf(counterpartySession), FINALISING_TRANSACTION.childProgressTracker()))
     //   return notarizedTx
        return subFlow(FinalityFlow(stx, setOf(bankSession), FINALISING_TRANSACTION.childProgressTracker()))




    }
}

@InitiatedBy(MurabahaApplicationFlow::class)
class MurabahaApplicationResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        subFlow(ReceiveFinalityFlow(counterpartySession))//, expectedTxId = idOfTxWeSigned))


    }
}
