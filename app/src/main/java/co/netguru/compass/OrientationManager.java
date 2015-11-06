package co.netguru.compass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author:  Adrian Kuta
 * Date:    05.11.2015
 * Index:   204423
 */
public class OrientationManager implements SensorEventListener {

	private static final long MAX_LOCATION_AGE_MILLIS = TimeUnit.MINUTES.toMillis(30);

	private static final long METERS_BETWEEN_LOCATIONS = 2;

	private static final long MILLIS_BETWEEN_LOCATIONS = TimeUnit.SECONDS.toMillis(3);

	private static final int MAX_NORTH_LIST_SIZE = 50;

	private final Sensor accelerometer;
	private final Sensor magnetometer;
	private SensorManager sensorManager;
	private LocationManager locationManager;
	private Location mLocation;
	private float[] gravity;
	private float[] geomagnetic;
	private GeomagneticField geomagneticField;
	private OrientationListener orientationListener;
	private LinkedList<Float> northList = new LinkedList<>();
	private float bearingLatitude = 0f;
	private float bearingLongitude = 0f;
	private LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			mLocation = location;
			updateGeomagneticField();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d("DEBUG_TAG", "DUPA1");
			setupLocationManager();
		}

		@Override
		public void onProviderDisabled(String provider) {

		}
	};

	public OrientationManager(Context context, SensorManager sensorManager, LocationManager locationManager) {
		this.sensorManager = sensorManager;
		this.locationManager = locationManager;
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		orientationListener = (OrientationListener) context;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ActivityCompat.checkSelfPermission((Context) orientationListener, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Context) orientationListener, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				orientationListener.mRequestPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
				return;
			}
		}
		this.locationManager.addGpsStatusListener(new GpsStatus.Listener() {
			@Override
			public void onGpsStatusChanged(int event) {
				setupLocationManager();
			}
		});
	}

	public void setBearingLatitude(float latitude) {
		bearingLatitude = latitude;
	}

	public void setBearingLongitude(float longitude) {
		bearingLongitude = longitude;
	}

	public void start() {
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
		setupLocationManager();
	}

	public void setupLocationManager() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ActivityCompat.checkSelfPermission((Context) orientationListener, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Context) orientationListener, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				orientationListener.mRequestPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
				return;
			}
		}
		Location lastLocation = locationManager
				.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		if (lastLocation != null) {
			long locationAge = lastLocation.getTime() - System.currentTimeMillis();
			if (locationAge < MAX_LOCATION_AGE_MILLIS) {
				mLocation = lastLocation;
				updateGeomagneticField();
			}
		}
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);

		List<String> providers =
				locationManager.getProviders(criteria, true /* enabledOnly */);
		for (String provider : providers) {
			locationManager.requestLocationUpdates(provider,
					MILLIS_BETWEEN_LOCATIONS, METERS_BETWEEN_LOCATIONS, locationListener,
					Looper.getMainLooper());
		}
	}

	public void stop() {
		sensorManager.unregisterListener(this);
		if (ActivityCompat.checkSelfPermission((Context) orientationListener, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Context) orientationListener, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			orientationListener.mRequestPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
			return;
		}
		locationManager.removeUpdates(locationListener);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			gravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			geomagnetic = event.values;
		if (gravity != null && geomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float azimut = orientation[0];
				float north = computeTrueNorth(-azimut * 360 / (2 * 3.14159f));
				north = calculateAverage(north);
				float bearingDegrees = 0f;
				if (mLocation != null) {
					Location destLocation = new Location(mLocation);
					destLocation.setLatitude(bearingLatitude);
					destLocation.setLongitude(bearingLongitude);
					bearingDegrees = mLocation.bearingTo(destLocation);
				}
				if (orientationListener != null)
					orientationListener.onOrientationChange(north, north + bearingDegrees);
			}
		}
	}

	private float calculateAverage(float north) {
		northList.add(north);
		if (northList.size() > MAX_NORTH_LIST_SIZE)
			northList.remove(0);
		float avgNorth = 0f;
		for (float northItem : northList)
			avgNorth += northItem;
		return avgNorth / northList.size();
	}

	private float computeTrueNorth(float heading) {
		if (geomagneticField != null) {
			return heading + geomagneticField.getDeclination();
		} else {
			return heading;
		}
	}

	private void updateGeomagneticField() {
		geomagneticField = new GeomagneticField((float) mLocation.getLatitude(),
				(float) mLocation.getLongitude(), (float) mLocation.getAltitude(),
				mLocation.getTime());
	}
}
