package se.accidis.fmfg.app.ui.documents;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.math.BigDecimal;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;
import se.accidis.fmfg.app.model.DocumentRow;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.ui.materials.ValueHelper;
import se.accidis.fmfg.app.utils.AndroidUtils;

/**
 * Adapter for the document list view. The list contains both the document rows and additional information.
 */
public final class DocumentAdapter extends BaseAdapter {
	public static final int AUTHOR_POSITION = 2;
	public static final int RECIPIENT_POSITION = 1;
	public static final int SENDER_POSITION = 0;
	public static final int VIEW_TYPE_ADDRESS = 1;
	public static final int VIEW_TYPE_EMPTY = 4;
	public static final int VIEW_TYPE_INFO = 2;
	public static final int VIEW_TYPE_ROW = 0;
	public static final int VIEW_TYPE_SEPARATOR = 3;
	private static final int ROW_BASE_TOP_OFFSET = 3;
	private static final int ROW_BOTTOM_OFFSET = 2;
	private final Context mContext;
	private final LayoutInflater mInflater;
	private Document mDocument;
	private boolean mIsCurrentDocument;
	private int mRowTopOffset = ROW_BASE_TOP_OFFSET;
	private List<DocumentRow> mRows;
	private boolean mShowAuthor;
	private boolean mShowFbet;

	public DocumentAdapter(Context context, Document document, boolean isCurrentDocument) {
		mContext = context;
		mDocument = document;
		mRows = document.getRows();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mIsCurrentDocument = isCurrentDocument;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	public String getAddressTextByPosition(int position) {
		switch (position) {
			case SENDER_POSITION:
				return mDocument.getSender();
			case RECIPIENT_POSITION:
				return mDocument.getRecipient();
			case AUTHOR_POSITION:
				return mDocument.getAuthor();
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public int getCount() {
		return mRowTopOffset + Math.max(1, mRows.size()) + ROW_BOTTOM_OFFSET;
	}

	@Override
	public Object getItem(int position) {
		if (VIEW_TYPE_ROW == getItemViewType(position)) {
			return mRows.get(position - mRowTopOffset);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		int numRows = Math.max(1, mRows.size());
		if (position == mRowTopOffset - 1 || position == mRowTopOffset + numRows) {
			// There is a separator between the addresses and the list, and another between the list and the summary
			return VIEW_TYPE_SEPARATOR;
		} else if (position < mRowTopOffset) {
			// Above the list are addresses
			return VIEW_TYPE_ADDRESS;
		} else if (position >= mRowTopOffset + numRows) {
			// Below the list is the summary
			return VIEW_TYPE_INFO;
		} else if (mRows.isEmpty()) {
			// Empty list
			return VIEW_TYPE_EMPTY;
		} else {
			// Everything else is the list
			return VIEW_TYPE_ROW;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (getItemViewType(position)) {
			case VIEW_TYPE_ADDRESS:
				return getAddressView(position, convertView, parent);
			case VIEW_TYPE_EMPTY:
				return getEmptyView(convertView, parent);
			case VIEW_TYPE_INFO:
				return getInfoView(convertView, parent);
			case VIEW_TYPE_ROW:
				return getRowView(position - mRowTopOffset, convertView, parent);
			case VIEW_TYPE_SEPARATOR:
				return getSeparatorView(convertView, parent);
		}

		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 5;
	}

	@Override
	public boolean isEnabled(int position) {
		if (!mIsCurrentDocument) {
			return false;
		} else {
			int type = getItemViewType(position);
			return (VIEW_TYPE_INFO != type && VIEW_TYPE_SEPARATOR != type && VIEW_TYPE_EMPTY != type);
		}
	}

	public void setDocument(Document document) {
		mDocument = document;
		mRows = document.getRows();
		notifyDataSetChanged();
	}

	public void setIsCurrentDocument(boolean value) {
		mIsCurrentDocument = value;
		notifyDataSetChanged();
	}

	public void setShowAuthor(boolean value) {
		mShowAuthor = value;
		mRowTopOffset = mShowAuthor ? ROW_BASE_TOP_OFFSET + 1 : ROW_BASE_TOP_OFFSET;
		notifyDataSetChanged();
	}

	public void setShowFbet(boolean value) {
		mShowFbet = value;
		notifyDataSetChanged();
	}

	private int getAddressHeadingByPosition(int position) {
		switch (position) {
			case SENDER_POSITION:
				return R.string.document_sender;
			case RECIPIENT_POSITION:
				return R.string.document_recipient;
			case AUTHOR_POSITION:
				return R.string.document_author;
			default:
				throw new IllegalArgumentException();
		}
	}

	private View getAddressView(int position, View convertView, ViewGroup parent) {
		View view;
		if (null == convertView) {
			view = mInflater.inflate(R.layout.list_item_document_address, parent, false);
		} else {
			view = convertView;
		}

		TextView headingText = (TextView) view.findViewById(R.id.document_address_heading);
		headingText.setText(getAddressHeadingByPosition(position));

		TextView addressText = (TextView) view.findViewById(R.id.document_address_text);
		String text = getAddressTextByPosition(position);

		if (TextUtils.isEmpty(text)) {
			if (mIsCurrentDocument) {
				boolean isAuthor = (AUTHOR_POSITION == position);
				addressText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_create_small), null, null, null);
				addressText.setText(isAuthor ? R.string.document_tap_to_edit_author : R.string.document_tap_to_edit_address);
			} else {
				addressText.setCompoundDrawables(null, null, null, null);
				addressText.setText(R.string.document_no_data);
			}
		} else {
			addressText.setCompoundDrawables(null, null, null, null);
			addressText.setText(text);
		}

		return view;
	}

	private View getEmptyView(View convertView, ViewGroup parent) {
		return (null != convertView ? convertView : mInflater.inflate(R.layout.list_item_document_empty, parent, false));
	}

	private View getInfoView(View convertView, ViewGroup parent) {
		View view;
		if (null == convertView) {
			view = mInflater.inflate(R.layout.list_item_document_info, parent, false);
		} else {
			view = convertView;
		}

		TextView nemView = (TextView) view.findViewById(R.id.document_summary_nem);
		BigDecimal documentNEM = mDocument.getTotalNEMkg();
		if (0.0 != documentNEM.doubleValue()) {
			nemView.setText(String.format(mContext.getString(R.string.document_summary_total_nem_format), ValueHelper.formatValue(documentNEM)));
			nemView.setVisibility(View.VISIBLE);
		} else {
			nemView.setVisibility(View.GONE);
		}

		StringBuilder valueBuilder = new StringBuilder();
		for (int tpKat = Material.TPKAT_MIN; tpKat <= Material.TPKAT_MAX; tpKat++) {
			BigDecimal valueByTpKat = mDocument.getCalculatedValueByTpKat(tpKat);
			if (0.0 != valueByTpKat.doubleValue()) {
				if (0 != valueBuilder.length()) {
					valueBuilder.append(AndroidUtils.LINE_SEPARATOR);
				}
				String weightVolumeByTpKat = mDocument.getWeightVolumeStringByTpKat(tpKat, mContext);
				valueBuilder.append(String.format(mContext.getString(R.string.document_summary_tpkat_format), tpKat, weightVolumeByTpKat, ValueHelper.formatValue(valueByTpKat)));
			}
		}

		TextView totalByTpKatView = (TextView) view.findViewById(R.id.document_summary_tpkat);
		if (0 != valueBuilder.length()) {
			totalByTpKatView.setText(valueBuilder.toString());
			totalByTpKatView.setVisibility(View.VISIBLE);
		} else {
			totalByTpKatView.setVisibility(View.GONE);
		}

		BigDecimal totalValue = mDocument.getCalculatedTotalValue();
		TextView totalView = (TextView) view.findViewById(R.id.document_summary_total);
		totalView.setText(String.format(mContext.getString(R.string.document_summary_total_format), ValueHelper.formatValue(totalValue)));

		boolean isViolatingColoadingRules = ColoadingHelper.isViolationOfColoadingRules(mDocument);
		View warningClass1View = view.findViewById(R.id.document_warning_class1);
		warningClass1View.setVisibility(isViolatingColoadingRules ? View.VISIBLE : View.GONE);

		return view;
	}

	private View getRowView(int rowIndex, View convertView, ViewGroup parent) {
		View view;
		if (null == convertView) {
			view = mInflater.inflate(R.layout.list_item_document_row, parent, false);
		} else {
			view = convertView;
		}

		DocumentRow row = mRows.get(rowIndex);

		TextView fullTextText = (TextView) view.findViewById(R.id.document_row_text);
		fullTextText.setText(row.getMaterial().getFullText());

		TextView materialText = (TextView) view.findViewById(R.id.document_row_fben);

		StringBuilder materialBuilder = new StringBuilder();
		Material material = row.getMaterial();

		if (!TextUtils.isEmpty(material.getFben())) {
			materialBuilder.append(material.getFben());
			if (mShowFbet) {
				materialBuilder.insert(0, material.getFbet() + ' ');
			}
		}

		if (row.hasNEM()) {
			if (materialBuilder.length() > 0) {
				materialBuilder.append(' ');
			}
			materialBuilder.append(String.format(mContext.getString(R.string.document_amount_format), ValueHelper.formatValue(row.getAmount())));
		}

		materialText.setText(materialBuilder.toString());
		materialText.setVisibility(materialBuilder.length() > 0 ? View.VISIBLE : View.GONE);

		TextView nemText = (TextView) view.findViewById(R.id.document_row_nem);
		if (row.hasNEM()) {
			nemText.setText(String.format(mContext.getString(R.string.document_nem_format), ValueHelper.formatValue(row.getNEMkg())));
			nemText.setVisibility(View.VISIBLE);
		} else {
			nemText.setVisibility(View.GONE);
		}

		TextView packagesText = (TextView) view.findViewById(R.id.document_row_packages);
		packagesText.setText(row.getPackagesText(mContext));

		TextView weightVolumeText = (TextView) view.findViewById(R.id.document_row_weightvolume);
		weightVolumeText.setText(row.getWeightVolumeText(mContext));
		weightVolumeText.setVisibility(row.isFreeText() ? View.GONE : View.VISIBLE);

		return view;
	}

	private View getSeparatorView(View convertView, ViewGroup parent) {
		return (null != convertView ? convertView : mInflater.inflate(R.layout.list_item_document_separator, parent, false));
	}
}
