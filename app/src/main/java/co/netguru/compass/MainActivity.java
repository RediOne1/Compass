package co.netguru.compass;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OrientationListener, MyDialogFragment.CoordinatesListener, View.OnClickListener {

	private static final int REQUEST_PERMISSIONS_CODE = 123;
	private View arrow;
	private View bearingArrow;
	private Button latitudeButton;
	private Button longitudeButton;
	private OrientationManager orientationManager;
	private boolean latitudeSet;
	private boolean longitudeSet;
	private TextView myLatitude;
	private TextView myLongitude;
	private TextView destLatitude;
	private TextView destLongitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		arrow = findViewById(R.id.needle);
		bearingArrow = findViewById(R.id.bearing_needle);

		myLatitude = (TextView) findViewById(R.id.my_latitude);
		myLongitude = (TextView) findViewById(R.id.my_longitude);
		destLatitude = (TextView) findViewById(R.id.dest_latitude);
		destLongitude = (TextView) findViewById(R.id.dest_longitude);

		latitudeButton = (Button) findViewById(R.id.latitudeButton);
		longitudeButton = (Button) findViewById(R.id.longitudeButton);

		latitudeButton.setOnClickListener(this);
		longitudeButton.setOnClickListener(this);

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		orientationManager = new OrientationManager(this, sensorManager, locationManager);
	}

	@Override
	protected void onResume() {
		super.onResume();
		orientationManager.start();
	}

	protected void onPause() {
		super.onPause();
		orientationManager.stop();
	}

	@Override
	public void onOrientationChange(float north, float bearingDegrees) {
		arrow.setRotation(north);
		if (bearingArrow.getVisibility() != View.VISIBLE && latitudeSet && longitudeSet)
			bearingArrow.setVisibility(View.VISIBLE);
		bearingArrow.setRotation(bearingDegrees);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSIONS_CODE) {
			for (int grantResult : grantResults)
				if (grantResult == PackageManager.PERMISSION_DENIED)
					Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
				else
					orientationManager.setupLocationManager();
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	public void mRequestPermissions(String... permissions) {
		requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
	}

	@Override
	public void onLocationChange(Location location) {
		String latitude = "" + location.getLatitude();
		String longitude = "" + location.getLongitude();

		myLatitude.setText(latitude);
		myLongitude.setText(longitude);
	}

	@Override
	public void onLatitudeSet(float latitude) {
		if (latitude >= -90 && latitude <= 90) {
			latitudeSet = true;
			orientationManager.setBearingLatitude(latitude);
			String sLatitude = "" + latitude;
			destLatitude.setText(sLatitude);
		} else
			Toast.makeText(this, R.string.permissible_latitude, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onLongitudeSet(float longitude) {
		if (longitude >= -180 && longitude <= 180) {
			longitudeSet = true;
			orientationManager.setBearingLongitude(longitude);
			String sLongitude = "" + longitude;
			destLongitude.setText(sLongitude);
		} else
			Toast.makeText(this, R.string.permissible_longitude, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View v) {
		if (v == latitudeButton) {
			MyDialogFragment myDialogFragment = MyDialogFragment.newInstance(MyDialogFragment.LATITUDE);
			myDialogFragment.show(getSupportFragmentManager(), "inputDialog");
		} else if (v == longitudeButton) {
			MyDialogFragment myDialogFragment = MyDialogFragment.newInstance(MyDialogFragment.LONGITUDE);
			myDialogFragment.show(getSupportFragmentManager(), "inputDialog");
		}
	}
}

