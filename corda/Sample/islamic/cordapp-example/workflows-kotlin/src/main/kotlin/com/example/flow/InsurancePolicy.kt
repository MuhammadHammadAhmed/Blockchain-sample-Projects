package com.example.flows

import co.paralleluniverse.fibers.Suspendable
import com.example.contracts.InsuranceContract
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import com.example.states.InsuranceState
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
@StartableByRPC  //-----------------------------val purchaseOrderProperties: PurchaseOrderProperties
class IssueInsuranceFlow(val insuree: Party, val asset: String, val policyRef : String,val sumInsured:Int,val ppremiumpaid : Boolean) :FlowLogic<SignedTransaction>() {

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
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
       // progressTracker.currentStep = GETTING_NOTARY
        // val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        progressTracker.currentStep = GENERATING_TRANSACTION
        val insurerId= serviceHub.myInfo.legalIdentities.first()
      /*  val buyer = serviceHub.identityService.partiesFromName(buyerId, false).singleOrNull()
               ?: throw IllegalArgumentException("No exact match found for buyer name $buyerId.") */

        val insurancePolicy = InsuranceState(insurerId,insuree, policyRef,asset, sumInsured,100,2020,2022,ppremiumpaid)

        val issueCommand = Command(InsuranceContract.Commands.IssueInsurance(), listOf(ourIdentity.owningKey))

        val builder = TransactionBuilder(notary)
                .addOutputState(insurancePolicy, InsuranceContract.CONTRACT_ID)
                .addCommand(issueCommand)
//TODO-onwards
        progressTracker.currentStep = VERIFYING_TRANSACTION
        builder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val stx = serviceHub.signInitialTransaction(builder)

        val counterpartySession = initiateFlow(insuree)
        progressTracker.currentStep = FINALISING_TRANSACTION

       // val notarisedTx1: SignedTransaction = subFlow(FinalityFlow(stx, listOf(counterpartySession), FINALISING_TRANSACTION.childProgressTracker()))
     //   return notarizedTx
        return subFlow(FinalityFlow(stx, setOf(counterpartySession), FINALISING_TRANSACTION.childProgressTracker()))




    }
}

@InitiatedBy(IssueInsuranceFlow::class)
class InsuranceResponder1(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        subFlow(ReceiveFinalityFlow(counterpartySession))//, expectedTxId = idOfTxWeSigned))


    }
}
