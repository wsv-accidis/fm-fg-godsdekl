package se.accidis.fmfg.app.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.joda.time.DateTime
import se.accidis.fmfg.app.utils.JSONUtils
import java.io.InputStream
import java.util.UUID

/**
 * Model object for a document link.
 */
data class DocumentLink(val id: UUID?, val name: String, val timestamp: DateTime?) :
    Comparable<DocumentLink> {

    override fun compareTo(other: DocumentLink): Int {
        val nameCmp = name.compareTo(other.name, ignoreCase = true)
        if (nameCmp != 0) return nameCmp

        val timeCmp = compareValues(other.timestamp, timestamp)
        if (timeCmp != 0) return timeCmp

        val caseCmp = name.compareTo(other.name)
        if (caseCmp != 0) return caseCmp

        return compareValues(id, other.id)
    }

    @Serializable
    private class Metadata(
        @SerialName(Document.Keys.ID) val id: String,
        @SerialName(Document.Keys.NAME) val name: String = "",
        @SerialName(Document.Keys.TIMESTAMP) val timestamp: String? = null
    )

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun fromDocument(document: Document): DocumentLink = DocumentLink(
            id = document.id,
            name = document.name,
            timestamp = document.timestamp
        )

        @OptIn(ExperimentalSerializationApi::class)
        fun fromStream(stream: InputStream): DocumentLink {
            val metadata = json.decodeFromStream<Metadata>(stream)
            return DocumentLink(
                id = UUID.fromString(metadata.id),
                name = metadata.name,
                timestamp = JSONUtils.parseDateTime(metadata.timestamp)
            )
        }
    }
}
