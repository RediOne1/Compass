package co.netguru.compass;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Author:  Adrian Kuta
 * Date:    06.11.2015
 * Index:   204423
 */
public class MyDialogFragment extends DialogFragment {

	private static final String KEY_COORDINATE_TYPE = "coordinate_type";
	public static final int LATITUDE = 1;
	public static final int LONGITUDE = 2;
	private int coordinateType;
	private EditText inputNumber;
	private CoordinatesListener coordinatesListener;

	public static MyDialogFragment newInstance(int coordinateType) {
		MyDialogFragment myDialogFragment = new MyDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_COORDINATE_TYPE, coordinateType);
		myDialogFragment.setArguments(bundle);
		return myDialogFragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_layout, null);
		inputNumber = (EditText) view.findViewById(R.id.input_number);

		Bundle bundle = getArguments();
		coordinateType = bundle.getInt(KEY_COORDINATE_TYPE);
		if (coordinateType == LATITUDE) {
			inputNumber.setHint(R.string.latitude);
		} else if (coordinateType == LONGITUDE) {
			inputNumber.setHint(R.string.longitude);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				float value = Float.parseFloat(inputNumber.getText().toString());
				if (coordinateType == LATITUDE)
					coordinatesListener.onLatitudeSet(value);
				else if (coordinateType == LONGITUDE)
					coordinatesListener.onLongitudeSet(value);

				dialog.cancel();
			}
		})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		coordinatesListener = (CoordinatesListener) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		coordinatesListener = null;
	}

	public interface CoordinatesListener {
		void onLatitudeSet(float latitude);

		void onLongitudeSet(float longitude);
	}
}

