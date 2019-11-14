@file:Suppress("UNUSED_VARIABLE", "unused")


package com.example.flows

import co.paralleluniverse.fibers.Suspendable
import com.example.contracts.MurabahaContract
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import com.example.states.*
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
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
class MurabahaOfferFlow( val goodsId:String) :FlowLogic<SignedTransaction>() {

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object FECHING_STATE :Step ("Fetching the application from the vault")
        object GETTING_NOTARY :Step ("getting Notary")
        object BUILDING_OUTPUTSTATE : Step("Building Outputstate of Murabaha.")

        object GENERATING_TRANSACTION : Step("Generating Murabaha transaction based on application.")
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
        val applicationList = serviceHub.vaultService.queryBy<MurabahaApplicationState>().states
        val application = applicationList.find { stateAndRef -> stateAndRef.state.data.proforma.goods.internalReference == goodsId }
                ?: throw IllegalArgumentException("No proforma with ID $goodsId found.")
        val applicationData =application.state.data

        progressTracker.currentStep = BUILDING_OUTPUTSTATE
        /**
         * gathering data for Murabaha agreement
         */

        val agreementReference ="MUR-"+ applicationData.referenceId//Purchase order reference Id TODO change logic of numbering
        val bank= serviceHub.myInfo.legalIdentities.first() //bank Id
        val borrower = applicationData.applicant
        val tenor = applicationData.tenor
        val costPrice = applicationData.proforma.amount
        val annualProfitRate = 19.1// just change the rate
        val sellingPrice =costPrice* 1+(annualProfitRate/12*tenor)/100
        val date = "MM-DD-YY"
                val goods = applicationData.proforma.goods.copy(takaful=true)
/*
fething goods State AndRef
 */
        val goodsList = serviceHub.vaultService.queryBy<GoodsState>().states
        val inputGoods = goodsList.find { stateAndRef -> stateAndRef.state.data.internalReference == goodsId }
                ?: throw IllegalArgumentException("No proforma with ID $goodsId found.")

        val murabaha = MurabahaState(bank,borrower,agreementReference,date,goods,costPrice,sellingPrice,annualProfitRate,tenor,true,false)
        val offerCommand = Command(MurabahaContract.Commands.Offer(), listOf(ourIdentity.owningKey))// TODO, try adding vendor key
        progressTracker.currentStep = GENERATING_TRANSACTION
        val builder = TransactionBuilder(notary)
        builder.addOutputState(murabaha, MurabahaContract.CONTRACT_ID)
      //  progressTracker.currentStep = GENERATING_TRANSACTION
        //TODO add inputState
        builder.addCommand(offerCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        builder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val stx = serviceHub.signInitialTransaction(builder)

        val bankSession = initiateFlow(borrower)
        progressTracker.currentStep = FINALISING_TRANSACTION

       // val notarisedTx1: SignedTransaction = subFlow(FinalityFlow(stx, listOf(counterpartySession), FINALISING_TRANSACTION.childProgressTracker()))
     //   return notarizedTx
        return subFlow(FinalityFlow(stx, setOf(bankSession), FINALISING_TRANSACTION.childProgressTracker()))




    }
}

@InitiatedBy(MurabahaOfferFlow::class)
class MurabahaOfferResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
     val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                        override fun checkTransaction(stx: SignedTransaction) = requireThat {
                        //TODO Checking
                        }
     }
                      val txId = subFlow(signTransactionFlow).id
                      subFlow(ReceiveFinalityFlow(counterpartySession))//, expectedTxId = idOfTxWeSigned))
                        }


    }

