package co.netguru.compass;

/**
 * Author:  Adrian Kuta
 * Date:    05.11.2015
 * Index:   204423
 */
public interface OrientationListener {
	void onOrientationChange(float north, float bearingDegrees);
	void mRequestPermissions(String... permissions);
}
