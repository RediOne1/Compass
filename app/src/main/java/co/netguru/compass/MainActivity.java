package co.netguru.compass;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OrientationListener {

	private static final int REQUEST_PERMISSIONS_CODE = 123;
	private View arrow;
	private View bearingArrow;
	private OrientationManager orientationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		arrow = findViewById(R.id.needle);
		bearingArrow = findViewById(R.id.bearing_needle);

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
}

