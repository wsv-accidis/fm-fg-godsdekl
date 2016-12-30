package se.accidis.fmfg.app.ui.documents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Label;
import se.accidis.fmfg.app.model.Material;
import se.accidis.fmfg.app.services.LabelsRepository;
import se.accidis.fmfg.app.utils.AndroidUtils;
import se.accidis.fmfg.app.utils.SpaceTokenizer;

/**
 * Dialog fragment for creating or editing a custom row.
 */
public final class CustomRowDialogFragment extends DialogFragment {
	private final LabelCheckedListener mLabelCheckedListener = new LabelCheckedListener();
	private final List<String> mSelectedLabels = new ArrayList<>();
	private CustomRowDialogListener mListener;
	private String mOriginalUuid;
	private TextView mSelectedLabelsText;
	private MultiAutoCompleteTextView mText;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_custom_row, null);

		mText = (MultiAutoCompleteTextView) view.findViewById(R.id.document_custom_row_text);
		mText.setTokenizer(new SpaceTokenizer());
		mText.setThreshold(1);
		final ArrayAdapter<String> textSuggestionsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.document_custom_row_text_suggestions));
		mText.setAdapter(textSuggestionsAdapter);

		final Bundle args = getArguments();
		if (null != args) {
			final Material material = Material.fromBundle(args);
			AndroidUtils.assertIsTrue(material.isCustom(), "Custom row dialog loaded with non-custom material.");
			mText.setText(material.getNamn());
			mSelectedLabels.addAll(material.getKlassKod());
			mOriginalUuid = material.getUuid();
		} else {
			mOriginalUuid = null;
		}

		mSelectedLabelsText = (TextView) view.findViewById(R.id.document_custom_row_labels);
		refreshSelectedLabelsText();

		final LinearLayout labelLayout = (LinearLayout) view.findViewById(R.id.document_custom_row_labels_layout);
		populateLabels(labelLayout, inflater);

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final Dialog dialog = builder.setView(view)
			.setPositiveButton(R.string.generic_save, new SaveClickedListener())
			.setNegativeButton(R.string.generic_cancel, null)
			.create();

		AndroidUtils.showSoftKeyboardForDialog(dialog);
		return dialog;
	}

	private void populateLabels(LinearLayout labelLayout, LayoutInflater inflater) {
		final Collection<Label> labels = LabelsRepository.getAllLabels();
		for (Label label : labels) {
			final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.layout_item_label, labelLayout, false);

			final CheckBox checkBox = (CheckBox) view.findViewById(R.id.label_checkbox);
			checkBox.setText(label.getKlassKod());
			checkBox.setTag(label);
			checkBox.setChecked(mSelectedLabels.contains(label.getKlassKod()));
			checkBox.setOnCheckedChangeListener(mLabelCheckedListener);

			final ImageView iconView = (ImageView) view.findViewById(R.id.label_icon);
			iconView.setImageResource(label.getSmallDrawable());
			iconView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					checkBox.setChecked(!checkBox.isChecked());
				}
			});
			labelLayout.addView(view);
		}
	}

	public void refreshSelectedLabelsText() {
		if (!mSelectedLabels.isEmpty()) {
			mSelectedLabelsText.setText(TextUtils.join(", ", mSelectedLabels));
		} else {
			mSelectedLabelsText.setText("");
		}
	}

	public void setDialogListener(CustomRowDialogListener listener) {
		mListener = listener;
	}

	public interface CustomRowDialogListener {
		void onDismiss(Material material);
	}

	private final class LabelCheckedListener implements CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
			if (!(compoundButton.getTag() instanceof Label)) {
				return;
			}

			final Label label = (Label) compoundButton.getTag();
			if (checked) {
				mSelectedLabels.add(label.getKlassKod());
				Collections.sort(mSelectedLabels);
			} else {
				mSelectedLabels.remove(label.getKlassKod());
			}

			refreshSelectedLabelsText();
		}
	}

	private final class SaveClickedListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			final String text = mText.getText().toString().trim();
			final Material material = Material.createCustom(text, mSelectedLabels, mOriginalUuid);

			if (null != mListener) {
				mListener.onDismiss(material);
			}
		}
	}
}
