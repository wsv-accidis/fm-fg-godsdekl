package se.accidis.fmfg.app.model

import org.joda.time.DateTime
import org.json.JSONObject
import se.accidis.fmfg.app.utils.JSONUtils
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

    companion object {
        fun fromDocument(document: Document): DocumentLink = DocumentLink(
            id = document.id,
            name = document.name,
            timestamp = document.timestamp
        )

        fun fromJson(json: JSONObject): DocumentLink {
            return DocumentLink(
                id = UUID.fromString(json.getString(Document.Keys.ID)),
                name = json.optString(Document.Keys.NAME),
                timestamp = JSONUtils.optDateTime(json, Document.Keys.TIMESTAMP)
            )
        }
    }
}
