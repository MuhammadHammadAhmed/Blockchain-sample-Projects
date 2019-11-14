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
object MurabahaApplicationschema

/**
 * A Murabaha Application schema.
 */
object MurabahaApplicationschemaV1 : MappedSchema(
        schemaFamily = MurabahaApplicationschema.javaClass,
        version = 1,

        mappedTypes = listOf(PersistentMurabahaApplication::class.java)) {
    @Entity
    @Table(name = "murabahaApplication_states")
    class PersistentMurabahaApplication(

            @Column(name = "date")
            val date: String,
            @Column(name = "applicant")
            val applicant: String,
            @Column(name = "issuuingBank")
            val issuingBank: String,
            @Column(name = "referenceId")
            val referenceId : String,
            @Column(name = "beneficiary")
            val beneficiary: String,
            @Column(name = "advisingBank")
            val advisingBank: String,
            @Column(name = "description")
            val description: String,
            @Column(name = "tenor")
            val tenor: Int,
            @Column(name = "amount")
            val amount: Int,
            val proforma : String,

           @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", "", "","","","","",0,0,"", UUID.randomUUID())
    }
}