package com.example.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for IOUState.
 */
object Goodschema

/**
 * An IOUState schema.
 */
object GoodschemaV1 : MappedSchema(
        schemaFamily = Goodschema.javaClass,
        version = 1,

        mappedTypes = listOf(PersistentGoods::class.java)) {
    @Entity
    @Table(name = "goods_states")
    class PersistentGoods(
            @Column(name = "seller")
            var sellerName: String,
            @Column(name = "assetOwner")
            var ownerName: String,

            @Column(name = "internalReference")
            var internalReference : String,
            @Column(name = "asset")//TODO add amount
            var asset : String,
            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", "", "","", UUID.randomUUID())
    }
}