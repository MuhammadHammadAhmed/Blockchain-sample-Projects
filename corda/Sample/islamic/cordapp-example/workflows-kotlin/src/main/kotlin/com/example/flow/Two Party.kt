@file:Suppress("UNUSED_VARIABLE", "unused")

package com.example.flows

import co.paralleluniverse.fibers.Suspendable
import com.example.contracts.DummyContract
import com.example.states.DummyState

import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.contracts.Command
//import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC  //-----------------------------val purchaseOrderProperties: PurchaseOrderProperties
class TwoParty(val client: Party,val  original: Int, val name: String) :FlowLogic<SignedTransaction>() {


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
    override fun call(): SignedTransaction {
        // progressTracker.currentStep = GETTING_NOTARY

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        progressTracker.currentStep = GENERATING_TRANSACTION
        val myId = serviceHub.myInfo.legalIdentities.first() // i.e my Id



        val otherSession = initiateFlow(client)
        val packet1 =otherSession.sendAndReceive<Int>(true)
        val result = packet1.unwrap { data -> data}


        val output  = DummyState(myId,myId,10, 99)//original

     //  val output  = DummyState(myId,client,10, 99)//original


        val myCommand = Command(DummyContract.Commands.Create(), listOf(myId.owningKey))

        val builder = TransactionBuilder(notary)
                .addOutputState(output, DummyContract.CONTRACT_ID)
        builder.addCommand(myCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        builder.verify(serviceHub)



      //  val stx: SignedTransaction = subFlow(CollectSignaturesFlow(ptx, setOf( insurerSession), GATHERING_SIGS.childProgressTracker()))

        progressTracker.currentStep = SIGNING_TRANSACTION
      //  val otherSession = initiateFlow(client)

        val ptx = serviceHub.signInitialTransaction(builder)


        progressTracker.currentStep = FINALISING_TRANSACTION //- fine till here


       return subFlow(FinalityFlow(ptx, listOf(otherSession), FINALISING_TRANSACTION.childProgressTracker()))


    }
}
    @InitiatedBy(TwoParty::class)
 class TwoPartyResponder( val counterpartySession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call(){
        val packet1 = counterpartySession.receive<Int>()

        val result = packet1.unwrap { data -> data}
val  output = 20 +9
        counterpartySession.send(output)

        subFlow(ReceiveFinalityFlow(counterpartySession))//, expectedTxId = idOfTxWeSigned))

    }



    }


