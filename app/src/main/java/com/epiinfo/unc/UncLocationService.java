package com.epiinfo.unc;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


/**
 * Class UncLocationService is a Service which runs in the background to collect
 * GPS coordinate changes from UncLocationListener.
 *
 * The Location Service should be started from the initial activity:
 *    Intent i = new Intent(context, MyPositioningService.class);     
 *    context.startService(i);
 *
 * The location listener is started by using:
 *    LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 *    LocationListener locationListener = new CTLocationListener();
 *    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1.0f, locationListener);
 *
 * Stop the GPS by using:
 *    locationManager.removeUpdates(locationListener);
 *
 * It is better to do the GPS start on the onCreate/onStart and the GPS remove on the onDestroy 
 * of a service and use that service. Otherwise once you stop the GPS the chance of starting the 
 * GPS again is less than 50% on some devices.
 * 
 * @author keithcollins
 */

public class UncLocationService extends Service {

	private static final String CLASSTAG = UncLocationService.class.getSimpleName();
	
	private LocationManager mLocationManager = null;
	
	private static final int SKY_LOCATION_LISTENER_GPS_ID = 0;
	private static final int SKY_LOCATION_LISTENER_CELLULAR_ID = 1;
	
	// v0.9.52 17Jun2015 - Change values that determine when a new fix is requested
	//                     Both must be true in order for a new fix to be requested
	//                     Most developers just use the time interval and set distance to 0
	private static final int   LOCATION_INTERVAL = 30000;   // 30 seconds
	private static final float LOCATION_DISTANCE = 1f;      // 1 meter
	
	private boolean isLocationListenerCellularOn = false;
	private boolean isLocationListenerGpsOn = false;

	// private String provider;
	
	private class SkyLocationListener implements LocationListener {
		Location mLastLocation;
		
		public SkyLocationListener(String provider) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " SkyLocationListener " + provider);
			}
			mLastLocation = new Location(provider);
		}
		
		/*************************
		@Override
		public IBinder onBind(Intent arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		**************************/
	
		// @Override
		public void onLocationChanged(Location location) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onLocationChanged " + location);
			}
		
			// update WeGeo ad framework with new GPS coordinates
			mLastLocation.set(location);
			UncEpiSettings.UopdateCurrentLocation(location);
		}

		// @Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onStatusChanged - provider=" + provider + " status=" + status);
			}

		}

		// @Override
		public void onProviderEnabled(String provider) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onProviderEnabled - " + provider);
			}
		}

		// @Override
		public void onProviderDisabled(String provider) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onProviderDisabled - " + provider);
			}
		}
	}

	SkyLocationListener[] mLocationListeners = new SkyLocationListener[] {
	        new SkyLocationListener(LocationManager.GPS_PROVIDER),
	        new SkyLocationListener(LocationManager.NETWORK_PROVIDER)
	};

	
	private BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, final Intent intent) {
		    String iAction = intent.getAction();
		    if (iAction.equals(Intent.ACTION_SCREEN_OFF)) {
		    	if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver - ACTION_SCREEN_OFF");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver -------------------------------------------");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver -------------------------------------------");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver -------------------------------------------");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver -------------------------------------------");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver -------------------------------------------");
				}
		    	stopLocationServicesGps();
		    }
		    else if (iAction.equals(Intent.ACTION_SCREEN_ON)) {
		    	if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver - ACTION_SCREEN_ON");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver +++++++++++++++++++++++++++++++++++++++++++");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver +++++++++++++++++++++++++++++++++++++++++++");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver +++++++++++++++++++++++++++++++++++++++++++");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver +++++++++++++++++++++++++++++++++++++++++++");
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " BroadcastReceiver +++++++++++++++++++++++++++++++++++++++++++");
				}
		    	startLocationServicesGps();
		    }
		}
	};
		
		
	
	@Override
	public IBinder onBind(Intent arg0) {
	    return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onStartCommand - Enter");
		}
	    super.onStartCommand(intent, flags, startId);       
	    return START_STICKY;
	}

	@Override
	public void onCreate() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onCreate - Enter");
    	}
		
		mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		startLocationServicesAll();
	    
	    // Catch screen on & off events
	    IntentFilter myFilter = new IntentFilter();
	    myFilter.addAction(Intent.ACTION_SCREEN_OFF);
	    myFilter.addAction(Intent.ACTION_SCREEN_ON);
	    registerReceiver(screenOnOffReceiver, myFilter);
	    
	    if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onCreate - Exit");
    	}
	}

	// @Override
	// Called when the service is stopped
	public void onDestroy(String provider, int status, Bundle extras) {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onDestroy - Enter");
    	}
		super.onDestroy();
		unregisterReceiver(screenOnOffReceiver);
		stopLocationServicesAll();
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onDestroy - Exit");
    	}
	}

	// @Override
	// Called when the service is stopped
	public void onDestroy() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onDestroy - Enter");
    	}
		super.onDestroy();
		unregisterReceiver(screenOnOffReceiver);
		stopLocationServicesAll();
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onDestroy - Exit");
    	}
	}
	
	// @Override
	public void onResume() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onResume - Enter");
    	}
	}
	
	// @Override
	public void onPause() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " onPause - Enter");
    	}
	}
	
	private void startLocationServicesAll() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " startLocationServicesAll");
    	}
		// v0.9.53 17Jun2015 - Check location settings
		if (UncEpiSettings.locationFineEnabled) {
			startLocationServicesGps();
		}
		if (UncEpiSettings.locationCoarseEnabled) {
			startLocationServicesCellular();
		}
		// v0.9.53 17Jun2015 - Add location criteria to force a more accurate measurement
		//                     Note this is currently not used in my requestLocationUpdates call
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
	}
	
	private void startLocationServicesGps() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " startLocationServicesGps");
    	}
		
		if (isLocationListenerGpsOn == false) {
			try {
				mLocationManager.requestLocationUpdates(
	                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[SKY_LOCATION_LISTENER_GPS_ID]);
				isLocationListenerGpsOn = true;
			} catch (java.lang.SecurityException ex) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " Fail to request GPS location update, ignore", ex);
				}
			} catch (IllegalArgumentException ex) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " GPS provider does not exist, " + ex.getMessage());
				}
			} catch (Exception e) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " exception cause: " + e.getCause());
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " exception message: " + e.getMessage());
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " exception desc: " + e.toString());
				}
			}
		}
	}
	
	private void startLocationServicesCellular() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " startLocationServicesCellular");
    	}
		
		if (isLocationListenerCellularOn == false) {
			try {
				mLocationManager.requestLocationUpdates(
	                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL * 6, LOCATION_DISTANCE, mLocationListeners[SKY_LOCATION_LISTENER_CELLULAR_ID]);
				isLocationListenerCellularOn = true;
			} catch (java.lang.SecurityException ex) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " Fail to request location update, ignore", ex);
				}
			} catch (IllegalArgumentException ex) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " Network provider does not exist, " + ex.getMessage());
				}
			} catch (Exception e) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " exception cause: " + e.getCause());
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " exception message: " + e.getMessage());
					Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " exception desc: " + e.toString());
				}
			}
		}
	}
	
	private void stopLocationServicesAll() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " stopLocationServicesAll");
    	}
		
		stopLocationServicesGps();
		stopLocationServicesCellular();
	}
	
	private void stopLocationServicesGps() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " stopLocationServicesGps");
    	}
		
		if (isLocationListenerGpsOn == true) {
			mLocationManager.removeUpdates(mLocationListeners[SKY_LOCATION_LISTENER_GPS_ID]);
			isLocationListenerGpsOn = false;
		}
	}
	
	private void stopLocationServicesCellular() {
		if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + UncLocationService.CLASSTAG + " stopLocationServicesCellular");
    	}
		
		if (isLocationListenerCellularOn == true) {
			mLocationManager.removeUpdates(mLocationListeners[SKY_LOCATION_LISTENER_CELLULAR_ID]);
			isLocationListenerCellularOn = false;
		}
	}

}