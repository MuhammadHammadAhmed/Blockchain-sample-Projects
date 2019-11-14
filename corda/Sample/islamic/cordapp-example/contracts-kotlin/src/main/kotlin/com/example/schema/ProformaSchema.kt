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
object Proformaschema

/**
 * An IOUState schema.
 */
object ProformachemaV1 : MappedSchema(
        schemaFamily = Proformaschema.javaClass,
        version = 1,

        mappedTypes = listOf(PersistentProforma::class.java)) {
    @Entity
    @Table(name = "proforma_states")
    class PersistentProforma(

            @Column(name = "seller")
            var sellerName: String,

            @Column(name = "buyer")
            var buyerName: String,

            @Column(name = "date")
            var proformaDate: String,

            @Column(name = "proformaId")
            var proformaId: String, //TODO, may be changed with a date  data type

            @Column(name = "goods")
            var goods: String,

            @Column(name = "description")
            var description: String,

            @Column(name = "amount")
            var invoiveamount: Int,

            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", "", "","","","",0, UUID.randomUUID())

    }
}