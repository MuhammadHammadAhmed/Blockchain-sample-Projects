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
object MurabahaSchema

/**
 * A Murabaha schema.
 */
object MurabahaSchemaV1 : MappedSchema(
        schemaFamily = MurabahaSchema.javaClass,
        version = 1,

        mappedTypes = listOf(PersistentMurabaha::class.java)) {
    @Entity
    @Table(name = "murabaha_states")
    class PersistentMurabaha(

            @Column(name = "bank")
            var bank:String,

            @Column(name = "borrower")
            var borrower: String,

            @Column(name = "internal_reference")
            var internalReference : String,

            @Column(name = "agreement_date")
            var ageementDate: String,

            @Column(name = "goods")
            var goods : String,

            @Column(name = "cost_price")
            var costPrice :Int,

            @Column(name = "sellng_price")
            var sellingprice : Double,

            @Column(name = "profit_rate")
            var profitrate : Double,

            @Column(name = "term")
            var term : Int,

            @Column(name = "bank_signature")
            var bankSignature:Boolean,

            @Column(name = "borrower_signature")
            var borrowerSignature:Boolean,

            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", "", "","","",0,0.0,0.0,0,false,false, UUID.randomUUID())

          }
}