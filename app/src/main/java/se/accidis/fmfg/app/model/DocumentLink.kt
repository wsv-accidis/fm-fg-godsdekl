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
        return name.compareTo(other.name)
    }

    companion object {
        fun fromJson(json: JSONObject): DocumentLink {
            return DocumentLink(
                id = UUID.fromString(json.getString(Document.Keys.ID)),
                name = json.optString(Document.Keys.NAME),
                timestamp = JSONUtils.optDateTime(json, Document.Keys.TIMESTAMP)
            )
        }
    }
}
