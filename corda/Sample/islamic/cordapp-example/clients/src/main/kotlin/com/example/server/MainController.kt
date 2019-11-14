package com.example.server

import com.example.flow.ExampleFlow.Initiator
import com.example.flows.*
import com.example.state.IOUState
import com.example.states.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest





val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 *  A Spring Boot Server API controller for interacting with the node via RPC.
 */

@RestController
@RequestMapping("/api/murabaha/") // The paths for GET and POST requests are relative to this base path.
class MainController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    /**
     * Returns the node's name.
     */
    @GetMapping(value = [ "me" ], produces = [ APPLICATION_JSON_VALUE ])
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = [ "peers" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all Murabaha Contracts   states that exist in the node's vault.
     */
    @GetMapping(value = [ "murabaha" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMurabaha() : ResponseEntity<List<StateAndRef<MurabahaState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<MurabahaState>().states)
    }
    /**
     * Displays all Purchase Order  states that exist in the node's vault.
     */
    @GetMapping(value = [ "purchaseorder" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getpurchaseOrder() : ResponseEntity<List<StateAndRef<PurchaseOrderState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<PurchaseOrderState>().states)
    }

    /**
     * Displays all Murabaha Applications states that exist in the node's vault.
     */
    @GetMapping(value = [ "applications" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getapplications() : ResponseEntity<List<StateAndRef<MurabahaApplicationState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<MurabahaApplicationState>().states)
    }
    /**
     * Displays all Proformas states that exist in the node's vault.
     */
    @GetMapping(value = [ "proformas" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getProformas() : ResponseEntity<List<StateAndRef<ProformaState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<ProformaState>().states)
    }
    /**
     * Displays all Goods states that exist in the node's vault.(either owned or recorded
     */
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping(value = [ "goods" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getGoods() : ResponseEntity<List<StateAndRef<GoodsState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<GoodsState>().states)
    }

    /**
     * Initiates a flow, where murabaha is offered, automatically accepted and Goods transferred to borrower
     *
     *
     */

    @GetMapping(value = [ "murabaha-offer" ], produces = [ TEXT_PLAIN_VALUE ])//, headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun murabahaOffer(request: HttpServletRequest): ResponseEntity<String> {//request: HttpServletRequest

        val goodsId = request.getParameter("goodsId") //purchase order reference Id

        // val term =  request.getParameter("term").toInt()//Term in days for the Payment order clearing,"0" if on demand , usually its a T+2 settlement under murabaha financing

        if(goodsId == null){
            return ResponseEntity.badRequest().body("Query parameter 'goodsId' must not be null.\n")
        }
        return try {

            val signedTx = proxy.startTrackedFlow(::MurabahaGoodsFlow    ,goodsId).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    /**
     * Initiates a flow, where goods are transferred by  Sellerto Bank on the basis of Purchase Order
     *
     *
     */

    @GetMapping(value = [ "goods-transfer" ], produces = [ TEXT_PLAIN_VALUE ])//, headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun trasferGoods(request: HttpServletRequest): ResponseEntity<String> {//request: HttpServletRequest

        val purchaseOrderId = request.getParameter("purchaseOrderId") //purchase order reference Id

       // val term =  request.getParameter("term").toInt()//Term in days for the Payment order clearing,"0" if on demand , usually its a T+2 settlement under murabaha financing

        if(purchaseOrderId == null){
            return ResponseEntity.badRequest().body("Query parameter 'purchaseOrderId' must not be null.\n")
        }
              return try {

            val signedTx = proxy.startTrackedFlow(::GoodsTransferFlow    ,purchaseOrderId).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    /**
     * Initiates a flow, where bank issues purchase order to seller based on the murabaha application received from the borrower/buyer
     *
     *
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @GetMapping(value = [ "issue-purchaseorder" ], produces = [ TEXT_PLAIN_VALUE ])//, headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun issuePurchaseorder(request: HttpServletRequest): ResponseEntity<String> {//request: HttpServletRequest

        val applicationId = request.getParameter("applicationId") //application reference Id

        val term =  request.getParameter("term").toInt()//Term in days for the Payment order clearing,"0" if on demand , usually its a T+2 settlement under murabaha financing

        if(applicationId == null){
            return ResponseEntity.badRequest().body("Query parameter 'application Id' must not be null.\n")
        }
        if (term < 0 ) {
            return ResponseEntity.badRequest().body("Query parameter 'Term' must be non-negative.\n")
        }

        return try {

            val signedTx = proxy.startTrackedFlow(::PuchaseOrderFlow   ,applicationId,term).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    /**
     * Initiates a flow, where buyer/borrower appply for Murabaha Financing based on the Proforma received from the Seller
     *
     *
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @GetMapping(value = [ "murabaha-application" ], produces = [ TEXT_PLAIN_VALUE ])//, headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun murabahaApllication(request: HttpServletRequest): ResponseEntity<String> {//request: HttpServletRequest
        val bank  =  request.getParameter("bank") // bank
        val proformaId = request.getParameter("proforma") //proforma reference Id

        val term =  request.getParameter("term").toInt()//Term in days for credit period under murabaha financing

        if(bank == null){
            return ResponseEntity.badRequest().body("Query parameter 'partyName' must not be null.\n")
        }
        if (term <= 0 ) {
            return ResponseEntity.badRequest().body("Query parameter 'Term' must be non-negative.\n")
        }
        val bankrX500Name = CordaX500Name.parse(bank)
        val bankParty = proxy.wellKnownPartyFromX500Name(bankrX500Name) ?: return ResponseEntity.badRequest().body("Party named $bank cannot be found.\n")

        return try {

            val signedTx = proxy.startTrackedFlow(::MurabahaApplicationFlow ,bankParty,proformaId,term).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    /**
     * Initiates a flow, where Seller iisues a Proforma to thr buyer/ borrower
     *
     *Simultaneously Seller will self issue a Tokenized title of Goods mentioned in the Proforma,the Goods are owned by the sellerand is  available in its vault.
     *
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @GetMapping(value = [ "create-proforma" ], produces = [ TEXT_PLAIN_VALUE ])//, headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun createProforma(request: HttpServletRequest): ResponseEntity<String> {//request: HttpServletRequest
        val buyerName  =  request.getParameter("buyerId") // buyerId
        val goodsDescription = request.getParameter("goodsDescription") //goodsDescription
        val proformaId =  request.getParameter("proformaId")// prpformaId
        val price =  request.getParameter("price").toInt()

        if(buyerName == null){
            return ResponseEntity.badRequest().body("Query parameter 'partyName' must not be null.\n")
        }
        if (price <= 0 ) {
            return ResponseEntity.badRequest().body("Query parameter 'Amount' must be non-negative.\n")
        }
        val buyerX500Name = CordaX500Name.parse(buyerName)
        val buyerParty = proxy.wellKnownPartyFromX500Name(buyerX500Name) ?: return ResponseEntity.badRequest().body("Party named $buyerName cannot be found.\n")

        return try {

            val signedTx = proxy.startTrackedFlow(::CreateProformaFlow,buyerParty,goodsDescription,proformaId,price).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }
    //----------===================================================================================================================================================
    /**
     * Displays all murabaha states that only this node is the owner.
     */
    @GetMapping(value = [ "my-murabaha" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMyMurabaha(): ResponseEntity<List<StateAndRef<MurabahaState>>>  {
        val myMurabaha = proxy.vaultQueryBy<MurabahaState>().states.filter { it.state.data.borrower.equals(proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myMurabaha)
    }
    /**
     * Displays all Goods states that only this node is the owner.
     */
    @GetMapping(value = [ "my-goods" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMyGoods(): ResponseEntity<List<StateAndRef<GoodsState>>>  {
        val myGoods = proxy.vaultQueryBy<GoodsState>().states.filter { it.state.data.assetOwner.equals(proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myGoods)
    }
    /**
     * Displays all Murabaha Application States thatare issued by the node
     */
    @GetMapping(value = [ "my-application" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMyApplications(): ResponseEntity<List<StateAndRef<MurabahaApplicationState>>>  {
        val myApplications = proxy.vaultQueryBy<MurabahaApplicationState>().states.filter { it.state.data.applicant.equals(proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myApplications)
    }
    /**
 * Displays all PurchaseOrders States thatare issued by the node
 */
@GetMapping(value = [ "my-purchaseOrders" ], produces = [ APPLICATION_JSON_VALUE ])
fun getMypurchaseOrders(): ResponseEntity<List<StateAndRef<PurchaseOrderState>>>  {
    val myPurchaseOrders = proxy.vaultQueryBy<PurchaseOrderState>().states.filter { it.state.data.bank.equals(proxy.nodeInfo().legalIdentities.first()) }
    return ResponseEntity.ok(myPurchaseOrders)
}
    /**
     * Displays all PurchaseOrders States thatare issued by the node
     */
    @GetMapping(value = [ "my-proformas" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMyProformas(): ResponseEntity<List<StateAndRef<ProformaState>>>  {
        val myProformas = proxy.vaultQueryBy<ProformaState>().states.filter { it.state.data.buyer.equals(proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myProformas)
    }

}
