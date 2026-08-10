package se.accidis.fmfg.app.model

import org.joda.time.DateTime
import org.json.JSONException
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
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): DocumentLink {
            return DocumentLink(
                id = UUID.fromString(json.getString(Document.Keys.ID)),
                name = JSONUtils.getStringOrNull(json, Document.Keys.NAME),
                timestamp = JSONUtils.getDateTimeOrNull(json, Document.Keys.TIMESTAMP)
            )
        }
    }
}
