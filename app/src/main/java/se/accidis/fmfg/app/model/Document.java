package se.accidis.fmfg.app.model;

import android.content.Context;
import android.text.TextUtils;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.ui.materials.ValueHelper;
import se.accidis.fmfg.app.utils.JSONUtils;

/**
 * Model object for transport documents.
 */
public final class Document {
	private final List<DocumentRow> mRows = new ArrayList<>();
	private String mAuthor;
	private boolean mHasUnsavedChanges;
	private UUID mId;
	private Boolean mIsProtectedTransport;
	private String mName;
	private String mRecipient;
	private String mSender;
	private DateTime mTimestamp;
	private String mVehicleReg;
	private String mVehicleType;

	public Document() {
		this(UUID.randomUUID());
	}

	private Document(UUID id) {
		mId = id;
	}

	public static Document fromJson(JSONObject json) throws JSONException {
		UUID id = UUID.fromString(json.getString(Keys.ID));
		Document document = new Document(id);
		document.mAuthor = JSONUtils.getStringOrNull(json, Keys.AUTHOR);
		document.mHasUnsavedChanges = json.optBoolean(Keys.UNSAVED_CHANGES);
		document.mIsProtectedTransport = JSONUtils.optBooleanOrNull(json, Keys.PROTECTED_TRANSPORT);
		document.mName = JSONUtils.getStringOrNull(json, Keys.NAME);
		document.mRecipient = JSONUtils.getStringOrNull(json, Keys.RECIPIENT);
		document.mSender = JSONUtils.getStringOrNull(json, Keys.SENDER);
		document.mTimestamp = JSONUtils.getDateTimeOrNull(json, Keys.TIMESTAMP);
		document.mVehicleReg = json.optString(Keys.VEHICLE_REG);
		document.mVehicleType = json.optString(Keys.VEHICLE_TYPE);

		JSONArray rowsArray = json.getJSONArray(Keys.ROWS);
		for (int i = 0; i < rowsArray.length(); i++) {
			DocumentRow row = DocumentRow.fromJson(rowsArray.getJSONObject(i));
			document.mRows.add(row);
		}

		return document;
	}

	public void addOrUpdateRow(DocumentRow row) {
		DocumentRow existing = getRowByMaterial(row.getMaterial());
		if (null != existing) {
			row.copyTo(existing);
		} else {
			mRows.add(row);
		}
	}

	public void assignNewId() {
		mId = UUID.randomUUID();
	}

	public String getAuthor() {
		return mAuthor;
	}

	public void setAuthor(String author) {
		mAuthor = author;
	}

	public BigDecimal getCalculatedTotalValue() {
		BigDecimal result = BigDecimal.ZERO;
		for (DocumentRow row : mRows) {
			result = result.add(row.getCalculatedValue());
		}
		return result;
	}

	public BigDecimal getCalculatedValueByTpKat(int tpKat) {
		BigDecimal result = BigDecimal.ZERO;
		for (DocumentRow row : mRows) {
			if (tpKat == row.getMaterial().getTpKat()) {
				result = result.add(row.getCalculatedValue());
			}
		}
		return result;
	}

	public UUID getId() {
		return mId;
	}

	public Set<Material> getMaterialsSet() {
		Set<Material> materials = new HashSet<>();
		for (DocumentRow row : mRows) {
			materials.add(row.getMaterial());
		}
		return materials;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getRecipient() {
		return mRecipient;
	}

	public void setRecipient(String recipient) {
		mRecipient = recipient;
	}

	public DocumentRow getRowByMaterial(Material material) {
		for (DocumentRow row : mRows) {
			if (row.getMaterial().equals(material)) {
				return row;
			}
		}
		return null;
	}

	public List<DocumentRow> getRows() {
		return Collections.unmodifiableList(mRows);
	}

	public String getSender() {
		return mSender;
	}

	public void setSender(String sender) {
		mSender = sender;
	}

	public DateTime getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		mTimestamp = timestamp;
	}

	public BigDecimal getTotalNEMkg() {
		BigDecimal totalNEM = BigDecimal.ZERO;
		for (DocumentRow row : mRows) {
			if (row.hasNEM()) {
				totalNEM = totalNEM.add(row.getNEMkg());
			}
		}
		return totalNEM;
	}

	public String getVehicleReg() {
		return mVehicleReg;
	}

	public void setVehicleReg(String mVehicleReg) {
		this.mVehicleReg = mVehicleReg;
	}

	public String getVehicleType() {
		return mVehicleType;
	}

	public void setVehicleType(String mVehicleType) {
		this.mVehicleType = mVehicleType;
	}

	public String getWeightVolumeStringByTpKat(int tpKat, Context context) {
		BigDecimal totalWeight = BigDecimal.ZERO, totalVolume = BigDecimal.ZERO;
		for (DocumentRow row : mRows) {
			if (tpKat == row.getMaterial().getTpKat()) {
				if (row.hasNEM()) {
					totalWeight = totalWeight.add(row.getNEMkg());
				} else if (row.isVolume()) {
					totalVolume = totalVolume.add(row.getWeightVolume());
				} else {
					totalWeight = totalWeight.add(row.getWeightVolume());
				}
			}
		}

		boolean hasWeight = (0.0 != totalWeight.doubleValue()), hasVolume = (0.0 != totalVolume.doubleValue());
		if (!hasWeight && !hasVolume) {
			return "";
		}

		// String will be of the format "1 kg, 2 liter" or either of the two if the other is zero
		StringBuilder builder = new StringBuilder();
		if (hasWeight) {
			builder.append(String.format(context.getString(R.string.unit_kg_format), ValueHelper.formatValue(totalWeight)));
			if (hasVolume) {
				builder.append(", ");
			}
		}
		if (hasVolume) {
			builder.append(String.format(context.getString(R.string.unit_liter_format), ValueHelper.formatValue(totalVolume)));
		}
		return builder.toString();
	}

	public boolean hasOptionalFields() {
		return isProtectedTransportSpecified() || !TextUtils.isEmpty(mVehicleReg) || !TextUtils.isEmpty(mVehicleType);
	}

	public boolean hasUnsavedChanges() {
		return mHasUnsavedChanges;
	}

	public boolean isProtectedTransport() {
		return (null != mIsProtectedTransport && mIsProtectedTransport);
	}

	public boolean isProtectedTransportSpecified() {
		return (null != mIsProtectedTransport);
	}

	public boolean isSaved() {
		return (null != mTimestamp);
	}

	public void removeAllRows() {
		mRows.clear();
	}

	public void removeRowByMaterial(Material material) {
		DocumentRow row = getRowByMaterial(material);
		if (null != row) {
			mRows.remove(row);
		}
	}

	public void reset(boolean keepAddresses, String defaultAuthor) {
		removeAllRows();

		if (!keepAddresses) {
			setSender("");
			setRecipient("");
			setAuthor(defaultAuthor);
		}

		// Ensure saving now does not overwrite an existing document
		assignNewId();

		setName("");
		setIsProtectedTransport(null);
		setVehicleType(null);
		setVehicleReg(null);

		setTimestamp(null);
		setHasUnsavedChanges(false);
	}

	public void setHasUnsavedChanges(boolean value) {
		mHasUnsavedChanges = value;
	}

	public void setIsProtectedTransport(Boolean mIsProtectedTransport) {
		this.mIsProtectedTransport = mIsProtectedTransport;
	}

	public JSONObject toJson() throws JSONException {
		JSONArray rowsArray = new JSONArray();
		for (DocumentRow row : mRows) {
			rowsArray.put(row.toJson());
		}

		JSONObject json = new JSONObject();
		json.put(Keys.ID, mId.toString());
		JSONUtils.putDateTime(json, Keys.TIMESTAMP, mTimestamp);
		json.put(Keys.NAME, mName);
		json.put(Keys.SENDER, mSender);
		json.put(Keys.RECIPIENT, mRecipient);
		json.put(Keys.AUTHOR, mAuthor);
		JSONUtils.putIfTrue(json, Keys.UNSAVED_CHANGES, mHasUnsavedChanges);
		JSONUtils.putBooleanOrNull(json, Keys.PROTECTED_TRANSPORT, mIsProtectedTransport);
		JSONUtils.putIfNotEmpty(json, Keys.VEHICLE_REG, mVehicleReg);
		JSONUtils.putIfNotEmpty(json, Keys.VEHICLE_TYPE, mVehicleType);
		json.put(Keys.ROWS, rowsArray);
		return json;
	}

	public static class Keys {
		public static final String AUTHOR = "Author";
		public static final String ID = "Id";
		public static final String NAME = "Name";
		public static final String PROTECTED_TRANSPORT = "ProtectedTransport";
		public static final String RECIPIENT = "Recipient";
		public static final String ROWS = "Rows";
		public static final String SENDER = "Sender";
		public static final String TIMESTAMP = "Timestamp";
		public static final String UNSAVED_CHANGES = "UnsavedChanges";
		public static final String VEHICLE_REG = "VehicleReg";
		public static final String VEHICLE_TYPE = "VehicleType";

		private Keys() {
		}
	}
}
