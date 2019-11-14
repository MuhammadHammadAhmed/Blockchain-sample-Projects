@file:Suppress("UNUSED_VARIABLE", "unused")


package com.example.flows

import co.paralleluniverse.fibers.Suspendable
import com.example.contracts.GoodsContract
import com.example.states.ProformaState
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import com.example.contracts.ProformaContract
import com.example.states.GoodsState
import net.corda.core.contracts.Command
//import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker.Step

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC  //-----------------------------val goodsDescription: goodsDescription
class CreateProformaFlow(val buyerId: Party, val goodsDescription: String, val proformaId : String, val price:Int) :FlowLogic<SignedTransaction>() {

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object GETTING_NOTARY :Step ("getting Notary")
        object GENERATING_TRANSACTION : Step("Generating transaction based on new Proforma.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GETTING_NOTARY,
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
        progressTracker.currentStep = GENERATING_TRANSACTION
        val sellerId= serviceHub.myInfo.legalIdentities.first()

        val goods = GoodsState(sellerId, sellerId, 1, proformaId, goodsDescription, false, sellerId)// change bank

        val proforma = ProformaState(sellerId, buyerId, "MM-DD-YY",proformaId, goodsDescription,goods,price)
        val proformaIssueCommand = Command(ProformaContract.Commands.Issue(), listOf(ourIdentity.owningKey))
        val goodsIssueCommand =Command(GoodsContract.Commands.Issue(),listOf(ourIdentity.owningKey))

        val builder = TransactionBuilder(notary)
                .addOutputState(proforma, ProformaContract.CONTRACT_ID)
                .addOutputState(goods, GoodsContract.CONTRACT_ID)
                .addCommand(proformaIssueCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        builder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val stx = serviceHub.signInitialTransaction(builder)

        val counterpartySession = initiateFlow(buyerId)
        progressTracker.currentStep = FINALISING_TRANSACTION

       // val notarisedTx1: SignedTransaction = subFlow(FinalityFlow(stx, listOf(counterpartySession), FINALISING_TRANSACTION.childProgressTracker()))
     //   return notarizedTx
        return subFlow(FinalityFlow(stx, setOf(counterpartySession), FINALISING_TRANSACTION.childProgressTracker()))




    }
}

@InitiatedBy(CreateProformaFlow::class)
class ProformaResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        subFlow(ReceiveFinalityFlow(counterpartySession))//, expectedTxId = idOfTxWeSigned))


    }
}
