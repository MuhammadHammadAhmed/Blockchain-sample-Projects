package com.example.schema

import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for IOUState.
 */
object PurchaseOrderschema

/**
 * An IOUState schema.
 */
object PurchaseOrderschemaV1 : MappedSchema(
        schemaFamily = PurchaseOrderschema.javaClass,
        version = 1,

        mappedTypes = listOf(PersistentPurchaseOrder::class.java)) {
    @Entity
    @Table(name = "purchaseOrder_states")
    class PersistentPurchaseOrder(

            @Column(name = "date")
            var date: String,
            @Column(name = "vendor")
            var vendor: String,
            @Column(name = "bank")
            var bank: String,
            @Column(name = "client")
            var client: String,
            @Column(name = "referenceId")
            var referenceId : String,
            @Column(name = "proformaId")
            var proformaId: String,
            @Column(name = "description")
            var description: String,
            @Column(name = "proforma")
            var proforma:String,
            @Column(name = "amount")
            var amount :Int,
            @Column(name = "tenor")
            var tenor: Int,
            @Column(name = "consumaable")
            var consumable : Boolean,
            @Column(name = "insuranceTerms")
            var  insuranceTerms : Boolean,
            @Column(name = "linear_id")
    var linearId: UUID

    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("","","","","","","", "",0,0,false,false,UUID.randomUUID())

    }
}