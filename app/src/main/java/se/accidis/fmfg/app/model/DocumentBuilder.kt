package se.accidis.fmfg.app.model

import org.joda.time.DateTime
import java.util.UUID

/**
 * Builder for creating or mutating Document instances.
 */
class DocumentBuilder(document: Document) {
    private val id: UUID = document.id
    private val timestamp = document.timestamp

    var rows: MutableList<DocumentRow> = document.rows.toMutableList()
    var author: String = document.author
    var name: String = document.name
    var sender: String = document.sender
    var recipient: String = document.recipient
    var isProtectedTransport: Boolean? = document.isProtectedTransport
    var vehicleReg: String = document.vehicleReg
    var vehicleType: String = document.vehicleType

    fun build(): Document = Document(
        id = id,
        rows = rows,
        hasUnsavedChanges = true,
        timestamp = timestamp,
        author = author,
        name = name,
        sender = sender,
        recipient = recipient,
        isProtectedTransport = isProtectedTransport,
        vehicleReg = vehicleReg,
        vehicleType = vehicleType
    )

    companion object {
        fun createNew(): Document = Document(
            id = UUID.randomUUID(),
            rows = emptyList(),
            hasUnsavedChanges = true,
            timestamp = null,
            author = "",
            name = "",
            sender = "",
            recipient = "",
            isProtectedTransport = null,
            vehicleReg = "",
            vehicleType = ""
        )
    }
}

fun Document.mutate(block: DocumentBuilder.() -> Unit): Document {
    val builder = DocumentBuilder(this)
    builder.block()
    return builder.build()
}

fun Document.save(): Document =
    this.copy(
        hasUnsavedChanges = false,
        timestamp = DateTime.now()
    )
