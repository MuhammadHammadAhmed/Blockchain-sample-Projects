@file:Suppress("UNUSED_VARIABLE", "unused")


package com.example.flows

import co.paralleluniverse.fibers.Suspendable
import com.example.contracts.GoodsContract
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import com.example.states.GoodsState
import com.example.states.PurchaseOrderState
import net.corda.core.contracts.Command
//import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker.Step

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC  //-----------------------------val goodsDescription: goodsDescription
class GoodsTransferFlow( val referenceId : String) :FlowLogic<SignedTransaction>() {

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object FECHING_STATE :Step ("Fetching the purchase orders from the vault")
        object GETTING_NOTARY :Step ("getting Notary")
        object BUILDING_OUTPUTSTATE : Step("Building Outputstate.")

        object GENERATING_TRANSACTION : Step("Generating Purchase order transaction based on new Proforma.")
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
        val purchaseOrderList = serviceHub.vaultService.queryBy<PurchaseOrderState>().states
        val purchaseOrder = purchaseOrderList.find { stateAndRef -> stateAndRef.state.data.referenceId== referenceId}
                ?: throw IllegalArgumentException("No proforma with ID $referenceId found.")
        val goodsId= purchaseOrder.state.data.proforma.goods.internalReference
        val newOwner = purchaseOrder.state.data.bank
        //Goods State
        val goodsList = serviceHub.vaultService.queryBy<GoodsState>().states
        val goods = goodsList.find { stateAndRef -> stateAndRef.state.data.internalReference== goodsId}
                ?: throw IllegalArgumentException("No proforma with ID $goodsId found.")

        progressTracker.currentStep = BUILDING_OUTPUTSTATE
        val bank= serviceHub.myInfo.legalIdentities.first()
        val newOwnerKey = newOwner.owningKey
        val outputGoods= goods.state.data.copy(assetOwner= newOwner,owner=newOwner,participants=listOf(newOwner))
        val moveCommand = Command(GoodsContract.Commands.Move(), listOf(ourIdentity.owningKey))// TODO, try adding client  key
        progressTracker.currentStep = GENERATING_TRANSACTION
        val builder = TransactionBuilder(notary)
   //     progressTracker.currentStep = GENERATING_TRANSACTION
        builder.addOutputState(outputGoods, GoodsContract.CONTRACT_ID)
      //  progressTracker.currentStep = GENERATING_TRANSACTION
        builder.addCommand(moveCommand)
        builder.addInputState(goods)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        builder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val stx = serviceHub.signInitialTransaction(builder)

        val bankSession = initiateFlow(newOwner)
        progressTracker.currentStep = FINALISING_TRANSACTION

       // val notarisedTx1: SignedTransaction = subFlow(FinalityFlow(stx, listOf(counterpartySession), FINALISING_TRANSACTION.childProgressTracker()))
     //   return notarizedTx
        return subFlow(FinalityFlow(stx, setOf(bankSession), FINALISING_TRANSACTION.childProgressTracker()))




    }
}

@InitiatedBy(GoodsTransferFlow::class)
class GoodsTransferResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        subFlow(ReceiveFinalityFlow(counterpartySession))//, expectedTxId = idOfTxWeSigned))


    }
}
