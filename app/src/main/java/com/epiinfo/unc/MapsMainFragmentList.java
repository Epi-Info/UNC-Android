package com.epiinfo.unc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.epiinfo.droid.R;
import com.epiinfo.droid.RecordList;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

// import android.support.v4.app.FragmentActivity;
// import android.os.Vibrator;
// import com.google.android.gms.maps.GoogleMap.OnMapClickListener;

public class MapsMainFragmentList extends ListFragment implements OnMapReadyCallback {

	private static final String CLASSTAG = MapsMainFragmentList.class.getSimpleName();

	private static final int MENU_CLUSTERS_ID = Menu.FIRST;
	private static final int MENU_POINTS_ID = Menu.FIRST + 1;
	private static final int MENU_MAPTYPE_ID = Menu.FIRST + 2;
	private static final int MENU_RESETMAP_ID = Menu.FIRST + 3;
	private static final int MENU_SETTINGS_ID = Menu.FIRST + 4;
	private static final int MENU_HELP_ID = Menu.FIRST + 5;
	private static final int MENU_SUBMIT_ID = Menu.FIRST + 6;

	private static final int MENU_CURRENTLOCATION_ID = Menu.FIRST + 8;

	private int mainViewId = MENU_CLUSTERS_ID;
	private int prevMainViewId = MENU_CLUSTERS_ID;
	private TextView dataItemsHeaderTextView;

	Button mClustersButton;
	Button mPointsButton;
	Button mMapTypeButton;
	Button mBeginInterviewButton;    // V0.9.63 m ResetMapButton;

	private int displayOrientation;

	private GoogleMap googleMap;
	private MapView mapView;
	private Marker stagingAreaLocationMarker = null;
	private MarkerOptions stagingAreaMarkerOptions;

	private Marker clusterMarker = null;
	private MarkerOptions clusterMarkerOptions;

	private boolean bDefaultLatLan = false;

	private static final float defaultMapZoomLevel = 17;
	private static final float stagingAreaMapZoomLevel = 12;
	private static final float clusterMapZoomLevel = 14;
	private static final float pointMapZoomLevel = 16;

	// private static final double stagingAreaLatitude = 35.9818810; //
	// 35.9940329 Durham downtown
	// private static final double stagingAreaLongitude = -78.8783960; //
	// -78.898619 Durham downtown
	// private static final String stagingAreaAddress =
	// "410 South Driver St, Durham, NC 27703";

	private ListView mDataListView;

	LayoutInflater vi; // KRC save from onCreateView

	// private SurveyListAdapter mSurveyListAdapter;

	private ArrayList<ClusterItem> mClusterList = null;
	private ClusterListAdapter mClusterListAdapter;

	private ArrayList<PointItem> mPointList = null;
	private PointListAdapter mPointListAdapter;

	// private ArrayList<Polyline> mRoutePolylineItems = null; // Route path
	// consisting of Polylines
	private ArrayList<Polygon> mClusterPolygonList = null;

	// 06May2016 v0.9.66 - add support for holes
	public ArrayList<String>   mHolePolylineCoordinates;
	public ArrayList<Polyline> mHolePolylineList;
	
	private ArrayList<PointItem> mClusterPointsList = null;

	private String mClusterSelected = "";
	private String mPointSelected = "";
	private boolean pointSurveyActive = false;

	// private boolean bUpdateUserStatusActive   = false;    // v0.9.63 30Sep2015  it was true;
	// private boolean bGetAllPointsStatusActive = false;

	private static final String notFoundString = "NotFound";

	private static final float MIN_ZOOM_LEVEL_FOR_STOP_ICONS = 15;
	private boolean pointIconsEnabled = true;

	private boolean bFirstTimeStartup = true;

	private String mApiErrorMsg = "";

	// private ProgressDialog waitDialog = null;
	private PointItem surveyPointItem = null;

	// 09Jun2015 v0.9.52 Add support for database
	private SQLiteDatabase epiDB = null;
	private boolean onCreateFlag = false;
	private boolean refreshPointsRequest = false;
	  
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " onCreateView - Enter");
		}
		vi = inflater; // KRC temp code???
		View v = inflater.inflate(R.layout.maps_main_fragmentlist, container,
				false);

		// Hook up button presses to the appropriate event handler.
		mClustersButton = (Button) v.findViewById(R.id.clusters_button_id);
		mPointsButton = (Button) v.findViewById(R.id.points_button_id);
		mMapTypeButton = (Button) v.findViewById(R.id.maptype_button_id);
		mBeginInterviewButton = (Button) v.findViewById(R.id.begininterview_button_id);

		mClustersButton.setOnClickListener(mClustersButtonListener);
		mPointsButton.setOnClickListener(mPointsButtonListener);
		mMapTypeButton.setOnClickListener(mMapTypeButtonListener);
		mBeginInterviewButton.setOnClickListener(mBeginInterviewButtonListener);

		dataItemsHeaderTextView = (TextView) v.findViewById(R.id.listview_header_id);



		return (v);
	}

	@Override
	public void onViewCreated(View v, Bundle b)
	{
		mapView =  this.getActivity().findViewById(R.id.map_fragment2_id);

		// setSurveyListItems(); // already read from server

		// Get a reference to the map
		mapView.onCreate(b);
		mapView.onResume();
		mapView.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap gMap) {

		try {
			googleMap = gMap;
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			googleMap.setMapStyle(
					MapStyleOptions.loadRawResourceStyle(
							this.getActivity(), R.raw.style_json));


			googleMap.getUiSettings().setZoomControlsEnabled(false); // this will
			// remove
			// the zoom
			// "+" and
			// "-" icons
			// on map
			googleMap.getUiSettings().setCompassEnabled(true); // display the
			// compass when
			// camera is tilted
			// or rotated from
			// default
			// orientation
			googleMap.getUiSettings().setAllGesturesEnabled(true);

			// The myLocationButton icon is the GPS symbol that appears on the
			// Google Map by default.
			// When pressing this icon the map is centered to the user�s current
			// position.
			// #1 Issue - The myLocationButton will center the map outside of SF if
			// the user is not in SF.
			// #2 Issue - The location of the MyLocationButton icon in Android is in
			// the top right corner of screen,
			// while in iOS it in the bottom left corner.
			// Android does not support a listener for this button so I can�t
			// override the lat/lon that is set.
			// The recommended workaround is to remove the Google myLocationButton
			// icon from the map
			// (which can be done with a Map UI setting), replace it with a user
			// supplied icon/image
			// that can then be placed anywhere on the screen (i.e. bottom left
			// similar to iOS) and
			// implement a listener for when it gets pressed. The listener would
			// normally just use the
			// current location, but if the user was out of range of the map or
			// location services were not
			// available then the default SFMTA Office address could be used.
			// Android also does not support moving the Google Map supplied
			// myLocationButton icon.

			googleMap.setMyLocationEnabled(true); // enable standard blue circle for
			// current location

			googleMap.setOnCameraChangeListener(mCameraChangeListener);
			googleMap.setOnInfoWindowClickListener(mInfoWindowClickListener);
			googleMap.setOnMapClickListener(mMapClickListener);
			googleMap.setOnMapLongClickListener(mMapLongClickListener);
			googleMap.setOnMarkerClickListener(mMarkerClickListener);
			googleMap.setOnMarkerDragListener(mMarkerDragListener);

			// setInitialMapCoordinates(); // KRC 16Dec2013 wait until staging area
			// coordinates are read from Points.kml file

			// googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

			clusterMarkerOptions = new MarkerOptions();

			ReadClusterPointFilesAsyncTask task = new ReadClusterPointFilesAsyncTask();
			task.execute(new String[] { "" });

			startUpdateUserStatusTimer();

			onCreateFlag = true;

			if (Constants.LOGS_ENABLED) {
				int val = GooglePlayServicesUtil
						.isGooglePlayServicesAvailable(getActivity());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " onActivityCreated - isGooglePlayServicesAvailable="
						+ val);
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " onActivityCreated - Exit");
			}

		} catch (Exception ex) {

		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " onActivityCreated - Enter");
			Log.d(Constants.LOGTAG,
					" "
							+ MapsMainFragmentList.CLASSTAG
							+ " onActivityCreated - isGooglePlayServicesAvailable="
							+ GooglePlayServicesUtil
									.isGooglePlayServicesAvailable(getActivity()));
		}

		mClusterList = new ArrayList<ClusterItem>();
		mPointList = new ArrayList<PointItem>();
		mClusterPolygonList = new ArrayList<Polygon>();
		mClusterPolygonList.clear();
		mClusterPointsList = new ArrayList<PointItem>();

		/**************************
		 * SupportMapFragment supportMapFragment =
		 * (SupportMapFragment)getSupportFragmentManager
		 * ().findFragmentById(R.id.map_fragment_id);
		 ***************************/

	}

	@Override
	public void onPause() {
		super.onPause();
		if (googleMap != null) {
			mapView.onPause();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (googleMap != null) {
			mapView.onSaveInstanceState(outState);
		}
	}
	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		if (googleMap != null) {
			mapView.onLowMemory();
		}
	}


	/*
	 * This method is called during creation after onActivityCreated() and also
	 * when the activity is resumed.
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (googleMap != null) {
			mapView.onResume();
		}
		if (Constants.LOGS_ENABLED_DATABASE) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " onResume - Enter");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}

		if (surveyPointItem != null) {
			surveyPointItem.marker.remove();
			// v0.9.57
			if (UncEpiSettings.pointQuestionnaireSaved) {
				surveyPointItem.setSurveyStatus(Constants.POINT_STATUS_COMPLETED);
			}
			else {
				surveyPointItem.setSurveyStatus(Constants.POINT_STATUS_NOTSTARTED);
			}
			surveyPointItem.marker = googleMap.addMarker(surveyPointItem.mo);
			setPointStatusOnServerAsync(surveyPointItem);
			surveyPointItem.marker.showInfoWindow();

			if (mainViewId == MENU_POINTS_ID) {
				setPointList();
			}
			surveyPointItem = null;
		}
		
		// v0.9.53 update all points from server when re-entering this activity
		if (!onCreateFlag) {
			if (Constants.LOGS_ENABLED_DATABASE) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " onResume - call getAllPointsStatsuAsync");
			}
			getAllPointsStatusAsync();
		}
		else {
			onCreateFlag = false;
		}
	}

	/*
	 * This method is called during creation after onActivityCreated() and also
	 * when the activity is resumed.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (googleMap != null) {
			mapView.onDestroy();
		}
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " onDestroy - Enter");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " #############################");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " #############################");
		}
		
		// v0.9.54
		writeAllPointsToDb();
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Do something with the data

	}

	private void setInitialMapCoordinates() {
		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " setInitialMapCoordinates");
		}
		setStagingAreaMapCoordinates();

		// inform user of their current address
		// new
		// ReverseGeocodingAsyncTask(getActivity().getBaseContext()).execute(latLng);
	}

	private void setStagingAreaMapCoordinates() {
		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " setStagingAreaMapCoordinates - Enter");
		}
		// Toast toast= Toast.makeText(getActivity(),
		// "Reseting Map to Staging Area Location", Toast.LENGTH_LONG);
		// toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL,
		// 0, 0);
		// toast.show();

		LatLng myLatLng = new LatLng(
				UncEpiSettings.selectedSurveyItem.stagingAreaLatitude,
				UncEpiSettings.selectedSurveyItem.stagingAreaLongitude);

		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " setStagingAreaMapCoordinates - #1");
		}
		
		// animate to the position
		// googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, stagingAreaMapZoomLevel));
		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " setStagingAreaMapCoordinates - #2");
		}
		
		// remove current marker from map
		// if (stagingAreaLocationMarker != null)
		// stagingAreaLocationMarker.remove();

		// Create a marker for default position and add to Map
		// if (stagingAreaMarkerOptions == null)
		stagingAreaMarkerOptions = new MarkerOptions();
		stagingAreaMarkerOptions.position(myLatLng);
		stagingAreaMarkerOptions.title("Staging Area @ " + UncEpiSettings.selectedSurveyItem.stagingAreaAddr);
		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " setStagingAreaMapCoordinates - #3");
		}
		
		// v0.9.60 KRC 22Feb2015 - get point png files from drawable folder, not assets folder
		// stagingAreaMarkerOptions.icon(BitmapDescriptorFactory.fromAsset("point_marker_black2.png"));
		// stagingAreaMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_black2));
		stagingAreaMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.staging_area_marker_black));
		
		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " setStagingAreaMapCoordinates - #4");
		}
		stagingAreaLocationMarker = googleMap.addMarker(stagingAreaMarkerOptions);
		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " setStagingAreaMapCoordinates - #5");
		}
		
		stagingAreaLocationMarker.showInfoWindow();
		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " setStagingAreaMapCoordinates - Exit");
		}
	}

	/******************************************************
	 * private void setCurrentMapCoordinates() { if (Constants.LOGS_ENABLED) {
	 * Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG +
	 * " setCurrentMapCoordinates - Use location listener coordinates");
	 * Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG +
	 * " setCurrentMapCoordinates - latitude=" + UncEpiSettings.latitude +
	 * " longitude=" + UncEpiSettings.longitude); }
	 * 
	 * LocationManager locMan = (LocationManager)
	 * getActivity().getSystemService(Context.LOCATION_SERVICE); Criteria crit =
	 * new Criteria(); Location loc =
	 * locMan.getLastKnownLocation(locMan.getBestProvider(crit, false));
	 * CameraPosition camPos = new CameraPosition.Builder() .target(new
	 * LatLng(loc.getLatitude(), loc.getLongitude())) .zoom(12.8f) .build();
	 * CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
	 * googleMap.moveCamera(camUpdate); //
	 * mapFragment.getMap().moveCamera(camUpdate);
	 * 
	 * 
	 * // use Location Listener values // LatLng myLatLng = new
	 * LatLng(UncEpiSettings.latitude, UncEpiSettings.longitude);
	 * 
	 * // animate to the position //////////////
	 * googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng)); //
	 * googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,
	 * defaultMapZoomLevel)); }
	 *******************************************************/

	/**
	 * Called when your activity's options menu needs to be created.
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		// We are going to create two menus. Note that we assign them
		// unique integer IDs, labels from our string resources, and
		// given them shortcuts.
		// We are going to create two menus. Note that we assign them
		// unique integer IDs, labels from our string resources, and
		// given them shortcuts.
		setOptionsMenuItems(menu);
		// return true;
	}

	/**
	 * Called right before the activity's option menu is displayed.
	 */
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Before showing the menu, we need to decide whether the clear
		// item is enabled depending on whether there is text to clear.
		// menu.findItem(LOGIN_ID).setVisible(mAlert.getText().length() > 0);

		menu.clear();
		setOptionsMenuItems(menu);
		// return true;
	}

	private void setOptionsMenuItems(Menu menu) {
		menu.add(0, MENU_CLUSTERS_ID, 0, R.string.clusters_button_label)
				.setShortcut('0', 'c');
		menu.add(0, MENU_POINTS_ID, 0, R.string.points_button_label)
				.setShortcut('1', 'p');
		menu.add(0, MENU_MAPTYPE_ID, 0, R.string.maptype_button_label)
				.setShortcut('2', 'm');
		menu.add(0, MENU_RESETMAP_ID, 0, R.string.resetmap_button_label)
				.setShortcut('3', 'r');
		menu.add(0, MENU_SETTINGS_ID, 0, R.string.settings_button_label)
				.setShortcut('4', 'e');
		menu.add(0, MENU_HELP_ID, 0, R.string.help_button_label)
		        .setShortcut('5', 'h');
		menu.add(0, MENU_SUBMIT_ID, 0, R.string.submit_button_label)
				.setShortcut('6', 'u');

		// menu.add(0, MENU_CURRENTLOCATION_ID, 0,
		// R.string.currentlocation_button_label).setShortcut('8', 'c');
	}

	/**
	 * Called when a menu item is selected.
	 */
	@Override
	// public boolean onMenuItemSelected(FeatureId, MenuItem item) {
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case MENU_CLUSTERS_ID:
			handleClustersView();
			return true;

		case MENU_POINTS_ID:
			handlePointsView();
			return true;

		case MENU_MAPTYPE_ID:
			handleMapTypeView();
			return true;

		case MENU_RESETMAP_ID:
			handleResetMapView();
			return true;

		case MENU_SETTINGS_ID:
			handleSettingsView();
			return true;

		case MENU_HELP_ID:
			handleHelpView();
			return true;

		case MENU_SUBMIT_ID:
			handleSubmitView();
			return true;

		case MENU_CURRENTLOCATION_ID:
			handleCurrentLocationRequest();
			return true;
		}
		// return super.onMenuItemSelected(feature_id, item);
		return super.onOptionsItemSelected(item);
	}

	/***********************************
	 * @Override public void onBackPressed() { if (Constants.LOGS_ENABLED) {
	 *           Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG +
	 *           " onBackPressed"); } handleBackKey(); }
	 ************************************/

	/**
	 * A call-back for when the user presses the "Back" button.
	 */
	OnClickListener mBackListener = new OnClickListener() {
		public void onClick(View v) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " mBackListener");
			}
			handleBackKey();
		}
	};

	/**
	 * A call-back for when the user presses the "Clusters" button.
	 */
	OnClickListener mClustersButtonListener = new OnClickListener() {
		public void onClick(View v) {
			handleClustersView();
		}
	};

	/**
	 * A call-back for when the user presses the "Points" button.
	 */
	OnClickListener mPointsButtonListener = new OnClickListener() {
		public void onClick(View v) {
			handlePointsView();
		}
	};

	/**
	 * A call-back for when the user presses the "Map Type" button.
	 */
	OnClickListener mMapTypeButtonListener = new OnClickListener() {
		public void onClick(View v) {
			handleMapTypeView();
		}
	};

	/**
	 * A call-back for when the user presses the "Begin Interview" button.
	 */
	OnClickListener mBeginInterviewButtonListener = new OnClickListener() {
		public void onClick(View v) {
			handleBeginInterviewRequest();   // V0.9.63
		}
	};

	void setSelectButton() {
		if (mainViewId == MENU_CLUSTERS_ID) {
			mClustersButton.setBackgroundColor(getResources().getColor(R.color.unc_button_selected));
			mClustersButton.setTextColor(getResources().getColor(R.color.yellow));
			mPointsButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mPointsButton.setTextColor(getResources().getColor(R.color.black3));
			mMapTypeButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mMapTypeButton.setTextColor(getResources().getColor(R.color.black3));
			mBeginInterviewButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mBeginInterviewButton.setTextColor(getResources().getColor(R.color.black3));
		} else if (mainViewId == MENU_POINTS_ID) {
			mClustersButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mClustersButton.setTextColor(getResources().getColor(R.color.black3));
			mPointsButton.setBackgroundColor(getResources().getColor(R.color.unc_button_selected));
			mPointsButton.setTextColor(getResources().getColor(R.color.yellow));
			mMapTypeButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mMapTypeButton.setTextColor(getResources().getColor(R.color.black3));
			mBeginInterviewButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mBeginInterviewButton.setTextColor(getResources().getColor(R.color.black3));
		} else if (mainViewId == MENU_MAPTYPE_ID) {
			mClustersButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mClustersButton.setTextColor(getResources().getColor(R.color.black3));
			mPointsButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mPointsButton.setTextColor(getResources().getColor(R.color.black3));
			mMapTypeButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mMapTypeButton.setTextColor(getResources().getColor(R.color.black3));
			mBeginInterviewButton.setBackgroundColor(getResources().getColor(R.color.unc_button_selected));
			mBeginInterviewButton.setTextColor(getResources().getColor(R.color.yellow));
		} else if (mainViewId == MENU_RESETMAP_ID) {
			mClustersButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mClustersButton.setTextColor(getResources().getColor(R.color.black3));
			mPointsButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mPointsButton.setTextColor(getResources().getColor(R.color.black3));
			mMapTypeButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mMapTypeButton.setTextColor(getResources().getColor(R.color.black3));
			mBeginInterviewButton.setBackgroundColor(getResources().getColor(R.color.unc_button_selected));
			mBeginInterviewButton.setTextColor(getResources().getColor(R.color.yellow));
		} else {
			// default to cluster view
			mClustersButton.setBackgroundColor(getResources().getColor(R.color.unc_button_selected));
			mClustersButton.setTextColor(getResources().getColor(R.color.yellow));
			mPointsButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mPointsButton.setTextColor(getResources().getColor(R.color.black3));
			mMapTypeButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mMapTypeButton.setTextColor(getResources().getColor(R.color.black3));
			mBeginInterviewButton.setBackgroundColor(getResources().getColor(R.color.unc_button_normal));
			mBeginInterviewButton.setTextColor(getResources().getColor(R.color.black3));
		}
	}

	/**
	 * A call-back for when the map's Camera changes.
	 */
	GoogleMap.OnCameraChangeListener mCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
		public void onCameraChange(CameraPosition arg0) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " onCameraChangeListener - Enter");
			}

			// determine if we are zooming and what the zoom level is
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " onCameraChangeListener - zoomLevel = " + arg0.zoom);
			}
			// KRC - V2.0.0 Fix to support Search Stop
			// if (mainViewId != MENU_ROUTEPATH_ID) {

			/*************************************
			 * if ((arg0.zoom < MIN_ZOOM_LEVEL_FOR_STOP_ICONS) &&
			 * (pointIconsEnabled)) { pointIconsEnabled = false;
			 * setPointListMarkersVisible(false); } else if ((arg0.zoom >=
			 * MIN_ZOOM_LEVEL_FOR_STOP_ICONS) && (!pointIconsEnabled)) {
			 * pointIconsEnabled = true; setPointListMarkersVisible(true); }
			 *****************************************/
			// }
		}
	};

	/**
	 * A call-back for when the map's marker info window is clicked.
	 */
	GoogleMap.OnInfoWindowClickListener mInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
		public void onInfoWindowClick(Marker arg0) {
			if (Constants.LOGS_ENABLED_MAPMARKERS) {
				Log.d(Constants.LOGTAG,
						" "
								+ MapsMainFragmentList.CLASSTAG
								+ " mInfoWindowClickListener OnInfoWindowClickListener - id="
								+ arg0.getId() + "  title=" + arg0.getTitle());
			}

			// @todo KRC - get handle to Cluster item or Point item depending on
			// app context
			PointItem item = null;
			String title = arg0.getTitle();
			if (title.contains("Point")) {
				int startIndex = title.indexOf(" ") + 1;
				int endIndex = title.indexOf(" ", startIndex);
				String pointId = title.substring(startIndex, endIndex);
				item = findPointItem(pointId);
				if (item != null) {
					// item.marker.remove();
					// item.setSurveyStatus(Constants.POINT_STATUS_INPROGRESS);
					// item.marker = googleMap.addMarker(item.mo); // Place the
					// marker on the map

					displayStartSurveyDialog(item, true);  // V0.9.64 add param
					// setPointStatusOnServerAsync(item);
				}
			} else if (title.contains("Cluster")) {
				// @todo KRC - Display Cluster info using an InfoWindow?
			}

			// @toDo KRC - Update Point List if it being displayed
			/**************************
			 * if (mainViewId == MENU_POINTS_ID) { setPointList(); } else
			 ****/
			if (mainViewId == MENU_CLUSTERS_ID) {
				setClusterPointsView(mClusterSelected);
			}

			// inform the server of the new Point Status
			// setPointStatusOnServerAsync(item);
		}
	};

	private void displayStartSurveyDialog(final PointItem item, final boolean promptUser) {

		// KRC v0.9.46 - Disable check for a completed point until we implement a feature
		//               to only set the point status to completed once a completed survey
		//               has been uploaded to server.
		// V0.9.63 - Add check back in
		if (item.surveyStatus == Constants.POINT_STATUS_COMPLETED) {
			String text1 = "Survey is already completed for Point " + item.name + " . Please choose another point.";
			// Toast toast = Toast.makeText(getActivity(), "Survey is already completed for this Point! Please choose another point.", Toast.LENGTH_LONG);
			Toast toast = Toast.makeText(getActivity(), text1, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
			Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
			if (v != null) {
				v.vibrate(1000); // 1 second
			}
		}

		else {
			if (promptUser) {  // V0.9.64
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

				alertDialog.setTitle("EPI Info Survey");
				String msg = "Begin a Survey Interview for Point " + item.name + " or Cancel";
				alertDialog.setMessage(msg);
				// alertDialog.setMessage("Begin a Survey Interview or Cancel");
				alertDialog.setIcon(R.drawable.epi_icon);

				alertDialog.setPositiveButton("Begin Interview",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {	
							item.marker.remove();
							item.setSurveyStatus(Constants.POINT_STATUS_INPROGRESS);
							item.marker = googleMap.addMarker(item.mo);
							setPointStatusOnServerAsync(item);
							if (mainViewId == MENU_POINTS_ID) {
								setPointList();
							}
							// dialog.cancel();
							startSurvey(item);
							dialog.cancel();

						}
					});

				alertDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// setPointList();  // KRC v0.9.49 - Fix a defect when app crashes if canceling a "begin survey" before list adapter is initialized
							dialog.cancel();
						}
					});

				alertDialog.show();
			}
			else {
				// V0.9.64 skip the extra user prompt to start a survey
				item.marker.remove();
				item.setSurveyStatus(Constants.POINT_STATUS_INPROGRESS);
				item.marker = googleMap.addMarker(item.mo);
				setPointStatusOnServerAsync(item);
				if (mainViewId == MENU_POINTS_ID) {
					setPointList();
				}
				startSurvey(item);
			}
		}
	}

	void startSurvey(PointItem item) {
		// if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " StartSurvey &&&&&&&&&&&&&&&&&&&&&");
		// }
		
		// set Intent params for survey filename and auto start
		final Intent recordList = new Intent(getActivity(), RecordList.class);

		recordList.putExtra("ViewName", UncEpiSettings.selectedSurveyItem.formFilename);
		recordList.putExtra("UncClusterPoint", item.name);
		// v0.9.58 - Add survey title since all surveys now have the same for filename (i.e. questionaire.xml)
		//           This will enable RecordList to have different db table for each survey title to store the completed xml files
		recordList.putExtra("UncTitleName", UncEpiSettings.selectedSurveyItem.title);
		
		// waitDialog = ProgressDialog.show(getActivity(), "Starting Survey",
		// "Please wait...", true);
		// Toast toast= Toast.makeText(getActivity(),
		// "Starting Survey. Please wait...", Toast.LENGTH_LONG);
		// toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL,
		// 0, 0);
		// toast.show();
		surveyPointItem = item;
		startActivity(recordList);
	}

	void uploadSurveyToServer(PointItem item) {

	}

	/**
	 * A call-back for when the map is tapped.
	 */
	GoogleMap.OnMapClickListener mMapClickListener = new GoogleMap.OnMapClickListener() {

		public void onMapClick(LatLng arg0) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " mMapClickListener onMapClick");
			}

			// KRC V2.0.0
			// hideStopMarkerInfo();
			// stopScheduleActive = false;
		}
	};

	/**
	 * A call-back for when the map is long pressed.
	 */
	GoogleMap.OnMapLongClickListener mMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
		public void onMapLongClick(LatLng arg0) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " mMapLongClickListener onMapLongClick");
			}
		}
	};

	/**
	 * A call-back for when a map marker is tapped.
	 */
	GoogleMap.OnMarkerClickListener mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
		public boolean onMarkerClick(Marker arg0) {
			if (Constants.LOGS_ENABLED3) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " mMarkerClickListener onMarkerClick - Enter");
				Log.d(Constants.LOGTAG,
						" " + MapsMainFragmentList.CLASSTAG
								+ " mMarkerClickListener onMarkerClick - id="
								+ arg0.getId() + "  title=" + arg0.getTitle());
			}

			mPointSelected = arg0.getTitle();
			pointSurveyActive = false;
			return false; // default behavior is desired, which displays the
							// registered info window with name & move camera to
							// marker coordinates
		}
	};

	/**
	 * A call-back for when a map marker is dragged.
	 */
	GoogleMap.OnMarkerDragListener mMarkerDragListener = new GoogleMap.OnMarkerDragListener() {

		public void onMarkerDrag(Marker arg0) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " mMarkerDragListener onMarkerDrag");
			}
		}

		public void onMarkerDragEnd(Marker arg0) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " mMarkerDragListener onMarkerDragEnd");
			}
		}

		public void onMarkerDragStart(Marker arg0) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " mMarkerDragListener onMarkerDragStart");
			}
		}
	};

	private void handleClustersView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handleClustersView");
		}
		prevMainViewId = mainViewId;
		mainViewId = MENU_CLUSTERS_ID;

		dataItemsHeaderTextView.setText(UncEpiSettings.selectedSurveyItem.title
				+ " - Clusters");

		setSelectButton();
		setClustersListView();
	}

	private void handlePointsView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handlePointsView");
		}
		prevMainViewId = mainViewId;
		mainViewId = MENU_POINTS_ID;

		dataItemsHeaderTextView.setText(UncEpiSettings.selectedSurveyItem.title
				+ " - Points");

		setSelectButton();
		setPointsListView();
	}

	public void handleMapTypeView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handleMapTypeView");
		}
		prevMainViewId = mainViewId;
		mainViewId = MENU_MAPTYPE_ID;
		setSelectButton();

		int mapType = googleMap.getMapType();
		if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		} else {
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}

		mainViewId = prevMainViewId;
		prevMainViewId = MENU_MAPTYPE_ID;
		setSelectButton();
	}

	public void handleResetMapView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handleResetMapView");
		}
		Toast toast = Toast.makeText(getActivity(),
				"Reseting Map to Staging Area Location", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,
				0, 0);
		toast.show();

		prevMainViewId = mainViewId;
		mainViewId = MENU_RESETMAP_ID;
		setSelectButton();

		setStagingAreaMapCoordinates();

		mainViewId = prevMainViewId;
		prevMainViewId = MENU_RESETMAP_ID;
		setSelectButton();
	}

	public void handleSubmitView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " handleSearchView");
		}
		// Toast.makeText(getActivity(),
		// "Not Implemented - Available in future app version",
		// Toast.LENGTH_SHORT).show();

		AlertDialog.Builder editAlert = new AlertDialog.Builder(getActivity());

		editAlert.setTitle("Submit Survey Form for a Point");
		editAlert.setMessage("Enter the Point Id");
		editAlert.setIcon(R.drawable.epi_icon);

		final EditText input = new EditText(getActivity());

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);
		editAlert.setView(input);
		input.requestFocus();

		editAlert.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (input.getText().toString().length() > 0) {
							// submitSurveyForm(input.getText().toString());
						} else {
							// getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
						}
					}
				});

		editAlert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// do nothing
						// getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					}
				});

		editAlert.show();
	}

	private void handleSettingsView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handleSettingsView");
		}
		Intent intent = new Intent(Constants.INTENT_ACTION_EDIT_SETTINGS);
		startActivity(intent);
	}

	private void handleHelpView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handleHelpView");
		}
		Intent intent = new Intent(Constants.INTENT_ACTION_FAQ);
		startActivity(intent);
	}

	public void handleCurrentLocationRequest() {
		if (Constants.LOGS_ENABLED_LOCATION) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handleCurrentLocationRequest");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " ##################################");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " ##################################");
		}
		PointItem item = getPointItemFromList(mPointSelected);
		if (item != null) {
			if (item.marker.isInfoWindowShown()) {
				item.marker.hideInfoWindow();
				// pointSurveyActive = false;
			}
		}

		// use Location Listener values
		LatLng myLatLng = new LatLng(UncEpiSettings.latitude, UncEpiSettings.longitude);
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, defaultMapZoomLevel)); // animate to the position
		new ReverseGeocodingAsyncTask(getActivity().getBaseContext()).execute(myLatLng); // inform user of their current address

		// KRC - V2.0.0 Re-draw Stop markers
		// setClusterPathMarkersVisible(false);
		pointIconsEnabled = true;
		// setPointListMarkersVisible(true);
	}

	public void handleRefreshPointsRequest() {
		if (Constants.LOGS_ENABLED2) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " handleRefreshPointsRequest");
		}

		// @todo KRC - call async task to query all points status, or do in background???
		refreshPointsRequest = true;
		getAllPointsStatusAsyncTimer();
	}
	
	// V0.9.63
	public void handleBeginInterviewRequest() {
		if (Constants.LOGS_ENABLED2) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " handleBeginInterviewRequest");
		}

		// Prompt user for Cluster # and Point # to begin an interview
		// Note that Cluster # & Point # may not be in the KML files
		AlertDialog.Builder pointClusterDialog = new AlertDialog.Builder(getActivity());
		
		// V0.9.64 Convenience Survey mode (i.e. # Clusters == 1) then only prompt for Point #
		// V0.9.65 Also check if Cluster #2 has no points
		if ((mClusterList.size() == 1) || (findPointItemForBeginSurvey("2-1") == null)) {
			pointClusterDialog.setTitle("Start a Survey for a Point");
			pointClusterDialog.setMessage("Enter the Point #");
		}
		else {
			pointClusterDialog.setTitle("Start a Survey for a Cluster-Point");
			pointClusterDialog.setMessage("Enter the Cluster-Point #");
		}
		pointClusterDialog.setIcon(R.drawable.epi_icon);

		final EditText input = new EditText(getActivity());
		input.setRawInputType(Configuration.KEYBOARD_QWERTY);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);
		pointClusterDialog.setView(input);
		input.requestFocus();

		pointClusterDialog.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (input.getText().toString().length() > 0) {
							// v0.9.65 hide the virtual keyboard when user presses ok
							InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			                
							PointItem item = null;
							// V0.9.64 Add the Cluster prefix of "1-" for Convenience Survey mode (i.e. # Clusters == 1)
							// V0.9.65 Also check if Cluster #2 has no points
							String value = input.getText().toString();
							if ((mClusterList.size() == 1) || (findPointItemForBeginSurvey("2-1") == null)) {
								value = "1-" + value;
							}
							item = findPointItemForBeginSurvey(value);
							if (item != null) {
								displayStartSurveyDialog(item, false);  // V0.9.64 add param
							}
							else {
								Toast toast = Toast.makeText(getActivity(), "INVALID Cluster-Point # !!", Toast.LENGTH_LONG);
								toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
								toast.show();
							}
						} else {
							Toast toast = Toast.makeText(getActivity(), "INVALID Cluster-Point # !!", Toast.LENGTH_LONG);
							toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
							toast.show();
							// getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
						}
					}
				});

		pointClusterDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// do nothing
						// getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					}
				});

		pointClusterDialog.show();
	}

	public boolean handleBackKey() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handleBackKey - Enter");
		}

		boolean retVal = hidePointMarkerInfo();

		if ((retVal == false) && (mainViewId == MENU_POINTS_ID)) {
			handleClustersView();
			return true;
		}

		if (clusterMarker != null) {
			if (clusterMarker.isInfoWindowShown()) {
				clusterMarker.hideInfoWindow();
				retVal = true;
			}
		}

		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " handleBackKey - return = " + retVal);
		}
		return retVal;
	}

	private boolean hidePointMarkerInfo() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " hidePointMarkerInfo - Enter");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " hidePointMarkerInfo - mPointSelected = "
					+ mPointSelected);
		}

		PointItem item = getPointItemFromList(mPointSelected);
		if (item != null) {
			if (item.marker.isInfoWindowShown()) {
				item.marker.hideInfoWindow();
				pointSurveyActive = false;
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " hidePointMarkerInfo - return true");
				}
				return true;
			}
		}

		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " hidePointMarkerInfo - return false");
		}
		return false;
	}

	

	private void setClustersListView() {
		// Cluster

		mDataListView = (ListView) getListView(); // @ToDo KRC - Needed???

		mClusterListAdapter = new ClusterListAdapter(getActivity(),
				R.layout.mapsmaindatalist_item, mClusterList);
		setListAdapter(mClusterListAdapter);

		setClusterList();

		hidePointMarkerInfo();

		// setClusterOutlinesVisibleState(false);
		// setPointMarkersVisibleState(true);

		/**********************************************************
		 * mDataListView.setOnScrollListener(new OnScrollListener() { // @Override
		 * public void onScroll(AbsListView view, int firstVisibleItem, int
		 * visibleItemCount, int totalItemCount) { // You can determine first
		 * and last visible items here // final int lastVisibleItem =
		 * firstVisibleItem + visibleItemCount - 1; // if (firstVisibleItem < 6)
		 * { dataItemsHeaderTextView.setText("Clusters"); // } }
		 * 
		 * // @Override public void onScrollStateChanged(AbsListView arg0, int
		 * arg1) { // TODO Auto-generated method stub } });
		 *********************************************************/
	}

	private void setPointsListView() {
		// Cluster

		mDataListView = (ListView) getListView(); // @ToDo KRC - Needed???

		mPointListAdapter = new PointListAdapter(getActivity(),
				R.layout.mapsmaindatalist_item, mPointList);
		setListAdapter(mPointListAdapter);

		setPointList();

		hidePointMarkerInfo();

		// setClusterOutlinesVisibleState(false);
		// setPointMarkersVisibleState(true);

		/*********************************************************
		 * mDataListView.setOnScrollListener(new OnScrollListener() { // @Override
		 * public void onScroll(AbsListView view, int firstVisibleItem, int
		 * visibleItemCount, int totalItemCount) { // You can determine first
		 * and last visible items here // final int lastVisibleItem =
		 * firstVisibleItem + visibleItemCount - 1; // if (firstVisibleItem < 6)
		 * { dataItemsHeaderTextView.setText("Points"); // } }
		 * 
		 * // @Override public void onScrollStateChanged(AbsListView arg0, int
		 * arg1) { // TODO Auto-generated method stub } });
		 **********************************************************/
	}

/*********************************************************************
	private void readClusterListFile-OLD() {
		if (Constants.LOGS_ENABLED6) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readClusterPointListFile - Enter");
		}
		
		// clusters_durham_chos_b11.kml file is located in the assets folder
		// 
		// 1. read each Cluster <name> and center <coordinates>
		// 2. Read each Cluster's <Polygon> <coordinates>
		
		
		try {
			BufferedReader reader;
			
			// reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(UncEpiSettings.selectedSurveyItem.clustersFilename)));
			
			// create a File object for the parent directory
	        File fileDirectory = new File(Constants.EPI_FILE_CLUSTERS_FOLDER);
	        // create a File object for the input file
	        File inputFile = new File(fileDirectory, UncEpiSettings.selectedSurveyItem.clustersFilename);
	        // now attach the InputStream to the file object, instead of a String representation
	        FileInputStream input = new FileInputStream(inputFile);
	        InputStreamReader inputStreamReader = new InputStreamReader(input);
	        
			reader = new BufferedReader(inputStreamReader);
			
		    int startIndex = 0;
		    int endIndex = 0;
		    int i = 0;
		    int j = 0;
		    String line = ""; 
		    boolean eof = false;
		    boolean bParamFound = false;
		    // boolean bPolygonFound = false;
		    
		    String name = "";
		    String occupied = "";
		    String whiteOnly = "";
		    String blackOnly = "";
		    String asianOnly = "";
		    String hispanic = "";
		    
		    String coordinates = "";
		    String latitude = "";
		    String longitude = "";
		    
		    // boolean bMidFile = false;  // <KRC> marker that divides the cluster kml file
		    
		    ClusterItem item = new ClusterItem();
		    
		    while (!eof) {
		    	// find the start of a Cluster	
		    	bParamFound = false;
		    	// bPolygonFound = false;
		    	while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<Placemark id=")) {
		    			if (line.length() > 21) {
		    				eof = true;  // parse the 2nd half of file to get polygon coordinates
		    			}
		    			else {
			    			if (Constants.LOGS_ENABLED5) {
			    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " line=" + line);
			    			}
			    			bParamFound = true;
			    		}
		    		}
			    }
		    	
		    	// find the "<name>"
		    	bParamFound = false;
		    	while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<name>")) {
		    			startIndex = line.indexOf("<name>") + 6;
		    			endIndex   = line.indexOf("<", startIndex);
		    			name       = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " name=" + name);
		    			}
		    			bParamFound = true;
		    		}
		    	}
			   
		    	// find the line with <description>
		    	bParamFound = false;
		    	while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<description>")) {
		    			// find "OCCUPIED"
		    			startIndex = line.indexOf("OCCUPIED") + 30;
		    			endIndex   = line.indexOf("&", startIndex);
		    			occupied   = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " occupied=" + occupied);
		    			}
		    		
		    			// find "WHITE_ONLY"
		    			startIndex = line.indexOf("WHITE_ONLY") + 32;
		    			endIndex   = line.indexOf("&", startIndex);
		    			whiteOnly  = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " whiteOnly=" + whiteOnly);
		    			}
		    			
		    			// find "BLACK_ONLY"
		    			startIndex = line.indexOf("BLACK_ONLY") + 32;
		    			endIndex   = line.indexOf("&", startIndex);
		    			blackOnly  = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " blackOnly=" + blackOnly);
		    			}
		    			
		    			// find "ASIAN_ONLY"
		    			startIndex = line.indexOf("ASIAN_ONLY") + 32;
		    			endIndex   = line.indexOf("&", startIndex);
		    			asianOnly  = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " asianOnly=" + asianOnly);
		    			}
		    		
		    			// find "HISPANIC"
		    			startIndex = line.indexOf("HISPANIC") + 30;
		    			endIndex   = line.indexOf("&", startIndex);
		    			hispanic   = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " hispanic=" + hispanic);
		    			}

		    			bParamFound = true;
		    		}
		    	}
		    	
		    	// find the start of a Point and its "coordinates"	
		    	bParamFound = false;
		    	while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<coordinates>")) {
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " line=" + line);
		    			}
		    			
		    			if (line.length() < 100) {
		    				startIndex = line.indexOf("<coordinates>") + 13;
		    				endIndex   = line.indexOf(",", startIndex);
		    				longitude  = line.substring(startIndex, endIndex);
		    				if (Constants.LOGS_ENABLED5) {
		    					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " longitude=" + longitude);
		    				}
			    	
		    				startIndex = endIndex + 1;
		    				endIndex   = line.indexOf(",", startIndex);
		    				latitude   = line.substring(startIndex, endIndex);
		    				if (Constants.LOGS_ENABLED5) {
		    					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " latitude=" + latitude);
		    				}
		    			
		    				// item = new ClusterItem();
		    				item.name            = name;
		    				item.occupied        = occupied;	
		    				item.whiteOnly       = whiteOnly;
		    				item.blackOnly       = blackOnly;
		    				item.asianOnly       = asianOnly;
		    				item.hispanic        = hispanic;
		    				item.centerLatitude  = latitude;
		    				item.centerLongitude = longitude;
		    				
		    				mClusterList.add(item);
		    				item = new ClusterItem();
		    				if (Constants.LOGS_ENABLED5) {
				    			++i;
				    			item.Dump1(i);
				    		}
		    				
		    				bParamFound = true;
		    			}
		    		}
		    	}
		    	
       	    	if (line == null) { eof = true; }
		    	
		    }  // while !eof

		    
		    
		    // now parse 2nd half of cluster kml file to get the polyline coordinates
		    if (Constants.LOGS_ENABLED2) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " Start parse section #2");
			}
		    eof = false;
		    while (!eof) {
		    	// find the "<name>"
		    	bParamFound = false;
		    	while ((!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<name>")) {
		    			startIndex = line.indexOf("<name>") + 6;
		    			endIndex   = line.indexOf("<", startIndex);
		    			name       = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " name=" + name);
		    			}
		    			bParamFound = true;
		    		}
		    	}
		    
		    	ClusterItem item2 = findClusterItemFromTempList(name);
		    	
		    	// find the start of a Point and its "coordinates"	
		    	bParamFound = false;
		    	while ((!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<coordinates>")) {
		    			if (Constants.LOGS_ENABLED5) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " <coordinates> found line=" + line);
		    			}
		    			
		    			// go thru the line and parse each set of longitude/latitude values, then put into an array
		    			startIndex  = line.indexOf("<coordinates>") + 13;
		    			endIndex    = line.indexOf(",", startIndex);
		    			coordinates = line.substring(startIndex);
		    			item2.polygonCoordinates = coordinates;
		    			if (Constants.LOGS_ENABLED5) {
		    				++j;
		    				item2.Dump2(j);
		    			}
		    			// parse line for polygon coordinates
		    			// KRC - Move to end of AsyncTask
		    			// item2.polyline = createClusterPolygon(coordinates);
		    			bParamFound = true;
		    			// bPolygonFound = true;
		    		}
		    	}
		    	
		    	if (Constants.LOGS_ENABLED5) {
		    		if (line == null)
		    			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " line == null");
    			}
		    	
		    	if (line == null) { eof = true; }
		    }  // while !eof #2
		    
		    reader.close();
		} catch (IOException e) {
		    // log the exception
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readClusterListFile - Exception!!! " + e.toString());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}
	}
	*************************************************************************/

	private void readClusterListFile() {
		if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readClusterPointListFile - Enter");
			Log.d(Constants.LOGTAG, " " + "******************************************************************");
			Log.d(Constants.LOGTAG, " " + "******************************************************************");
		}

		// 1. read each Cluster <name> and center <coordinates>
		// 2. Read each Cluster's <Polygon> <coordinates>

		try {
			BufferedReader reader;

			// reader = new BufferedReader(new
			// InputStreamReader(getActivity().getAssets().open(UncEpiSettings.selectedSurveyItem.clustersFilename)));				

			// create a File object for the parent directory
			// File fileDirectory = new File(Constants.EPI_FILE_CLUSTERS_FOLDER);
			File fileDirectory = new File(UncEpiSettings.selectedSurveyItem.surveyFilesFolderName);
			// create a File object for the input file
			// File inputFile = new File(fileDirectory, UncEpiSettings.selectedSurveyItem.clustersFilename);
			// File inputFile = new File(fileDirectory, "clusterfile.kml");
			// strip off the unneeded leading chars
			int index = UncEpiSettings.selectedSurveyItem.clustersFilename.lastIndexOf("/");
			String filename1 = UncEpiSettings.selectedSurveyItem.clustersFilename.substring(index+1);
			File inputFile = new File(fileDirectory, filename1);
			
			// now attach the InputStream to the file object, instead of a
			// String representation
			FileInputStream input = new FileInputStream(inputFile);
			InputStreamReader inputStreamReader = new InputStreamReader(input);

			reader = new BufferedReader(inputStreamReader);

			int startIndex = 0;
			int endIndex = 0;
			int i = 0;
			int j = 0;
			String line = "";
			boolean eof = false;
			boolean bParamFound = false;

			ClusterItem item = new ClusterItem();
			
			// 06May2016 v0.9.66
			mHolePolylineCoordinates = new ArrayList<String>();
			mHolePolylineCoordinates.clear();
			

			while (!eof) {
				// "<Placemark id=" --> find the start of a Cluster
				bParamFound = false;
				while ((!eof) && (!bParamFound)
						&& ((line = reader.readLine()) != null)) {
					if ((line.contains("<Placemark id=")) || (line.contains("<PLACEMARK ID="))) {
						if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
							Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " line=" + line);
						}
						bParamFound = true;
					}
				}

				// find the "<name>", not "CLUSTER>"
				bParamFound = false;
				while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
					if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
						// Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " line = " + line);
						// Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " look for <name>");  // not <name>
					}
					// if (line.contains("<CLUSTER>")) {
					if ((line.contains("<name>")) || (line.contains("<NAME>"))) {
						// startIndex = line.indexOf("<CLUSTER>") + 9;
						startIndex = line.indexOf("<name>") + 6;
						endIndex = line.indexOf("<", startIndex);
						item.name = line.substring(startIndex, endIndex);
						if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
							Log.d(Constants.LOGTAG, " "
									+ MapsMainFragmentList.CLASSTAG
									+ " startIndex=" + startIndex
									+ " endIndex=" + endIndex + " name="
									+ item.name);
						}
						bParamFound = true;
					}
				}

				/****************** v0.9.61 - these params are no longer in .kml file ************
				
				// find the "OCCUPIED"
				bParamFound = false;
				while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("OCCUPIED")) || (line.contains("Occupied")) || (line.contains("occupied"))) {
						while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.contains("<td>")) {
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.occupied = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex
											+ " occupied=" + item.occupied);
								}
								bParamFound = true;
							}
						}
					}
				}

				// find the "WHITE_ONLY"
				bParamFound = false;
				while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("WHITE_ONLY")) || (line.contains("White_Only")) || (line.contains("white_only"))) {
						while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.contains("<td>")) {
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.whiteOnly = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex
											+ " whiteOnly=" + item.whiteOnly);
								}
								bParamFound = true;
							}
						}
					}
				}

				// find the "BLACK_ONLY"
				bParamFound = false;
				while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("BLACK_ONLY")) || (line.contains("Black_Only")) || (line.contains("black_only"))) {
						while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.contains("<td>")) {
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.blackOnly = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex
											+ " blackOnly=" + item.blackOnly);
								}
								bParamFound = true;
							}
						}
					}
				}

				// ********* This param has not been in .kml file for a while! *****
		    	// find the "ASIAN_ONLY"
		    	bParamFound = false;
		    	while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("ASIAN_ONLY")) {
		    			while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
		    				if (line.contains("<td>")) {
		    					startIndex = line.indexOf("<td>") + 4;
				    			endIndex   = line.indexOf("<", startIndex);
				    			item.asianOnly = line.substring(startIndex, endIndex);
				    			if (Constants.LOGS_ENABLED5) {
				    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " asianOnly=" + item.asianOnly);
				    			}
				    			bParamFound = true;
		    				}
		    			}
		    		}
		    	}

				// find the "HISPANIC"
				bParamFound = false;
				while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("HISPANIC")) || (line.contains("Hispanic")) || (line.contains("hispanic"))) {
						while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.contains("<td>")) {
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.hispanic = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex
											+ " hispanic=" + item.hispanic);
								}
								bParamFound = true;
							}
						}
					}
				}
				************************ end of v0.9.61 change *******************/

				// find the "REF_CENT_X" --> Center Longitude
				bParamFound = false;
				while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("REF_CENT_X")) || (line.contains("Ref_Cent_X")) || (line.contains("ref_cent_x"))) {
						while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.contains("<td>")) {
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.centerLongitude = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex
											+ " centerLongitude="
											+ item.centerLongitude);
								}
								bParamFound = true;
							}
						}
					}
					// v0.9.56 - or find the "REF_CENT_Y" --> Center Latitude
					else if ((line.contains("REF_CENT_Y")) || (line.contains("Ref_Cent_Y")) || (line.contains("ref_cent_y"))) {
						while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.contains("<td>")) {
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.centerLatitude = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex
											+ " centerLatitude="
											+ item.centerLatitude);
								}
								bParamFound = true;
							}
						}
					}
				}

				// find the "REF_CENT_Y" --> Center Latitude
				bParamFound = false;
				while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("REF_CENT_Y")) || (line.contains("Ref_Cent_Y")) || (line.contains("ref_cent_y"))) {
						while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.contains("<td>")) {
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.centerLatitude = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex
											+ " centerLatitude="
											+ item.centerLatitude);
								}
								bParamFound = true;
							}
						}
					}
					// v0.9.56 - or find the "REF_CENT_X" --> Center Longitude
					else if ((line.contains("REF_CENT_X")) || (line.contains("Ref_Cent_X")) || (line.contains("ref_cent_x"))) {
						while ((!eof) && (!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.contains("<td>")) {
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.centerLongitude = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex
											+ " centerLongitude="
											+ item.centerLongitude);
								}
								bParamFound = true;
							}
						}
					}
				}

				// find the "<outerBoundaryIs><LinearRing><coordinates> -->
				// Polygon
				bParamFound = false;
				while ((!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("<COORDINATES>")) || (line.contains("<Coordinates>")) || (line.contains("<coordinates>"))) {
						if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
							Log.d(Constants.LOGTAG, " "
									+ MapsMainFragmentList.CLASSTAG
									+ " <coordinates> found line=" + line);
						}

						// *****25Apr2015 - tags and values now on same line, even though GoogleMaps may expects the values to be on the next line
						// line = reader.readLine();
								
						// go thru the line and parse each set of longitude/latitude values, then put into an array
						// ********05May2015 - check if latitude/longitude values are on the same line as <coordinates> or on the next line
						if (line.length() < 50) {
							line = reader.readLine();
							startIndex = 0;
							endIndex = line.indexOf("<", startIndex);  // go until "</coordinates>" is found
							if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
								Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " Found lat/lon on next line =" + line.substring(startIndex));
							}
						}
						else {
							startIndex = line.indexOf("<coordinates>") + 13;   // or 14 if there is a space before the first coordinate value
							endIndex = line.indexOf("<", startIndex);
							if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
								Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " Found lat/lon on same line =" + line);
							}
						}
						
						item.polygonCoordinates = line.substring(startIndex);   // endIndex not needed since createClusterPolygon looks for ","
						if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
							++j;
							item.Dump2(j);
						}
						// parse line for polygon coordinates
						// KRC - Move to end of AsyncTask
						// item.polyline = createClusterPolygon(coordinates);
						bParamFound = true;
						// bPolygonFound = true;
					}
				}

				// 06May2016 V0.9.66 - add support for holes
				// find the "<innerBoundaryIs><LinearRing><coordinates> -->
				if (Constants.LOGS_ENABLED_HOLES) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " Look for Holes");
				}
				boolean bHoleParamFound = true;
				reader.mark(10000);
				while ((bHoleParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("<innerBoundaryIs><LinearRing><coordinates>"))) {
						if (Constants.LOGS_ENABLED_HOLES) {
							Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " <coordinates> found line=" + line);
						}
						startIndex = line.indexOf("<coordinates>") + 13;   // or 14 if there is a space before the first coordinate value
						endIndex = line.indexOf("<", startIndex);
						if (Constants.LOGS_ENABLED_HOLES) {
							Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " Found lat/lon on same line =" + line);
						}
						String holePolygonCoordinates = line.substring(startIndex);   // endIndex not needed since createClusterPolygon looks for ","
						mHolePolylineCoordinates.add(holePolygonCoordinates);
					}
					else {
						bHoleParamFound = false;
						reader.reset();
					}
				}
				
				
				
				if (bParamFound) {
					mClusterList.add(item);
					item = new ClusterItem();
					if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
						++i;
						item.Dump1(i);
					}
				}

				if (line == null) {
					eof = true;
				}

			} // while !eof

			if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
				if (line == null)
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " line == null");
			}

			if (line == null) {
				eof = true;
			}

			reader.close();
		} catch (IOException e) {
			// log the exception
			if (Constants.LOGS_ENABLED_READCLUSTERFILE) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " readClusterListFile - Exception!!! " + e.toString());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}

		Collections.sort(mClusterList, new SortClustersBasedOnName());
		Collections.sort(mClusterList, new SortClustersBasedOnLength());
	}

	private ClusterItem findClusterItemFromTempList(final String pName) {
		if (Constants.LOGS_ENABLED2) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " findClusterItemFromTempList - Enter - pName=" + pName);
		}

		Iterator<ClusterItem> it = mClusterList.iterator();
		while (it.hasNext()) {
			ClusterItem item = it.next();
			if (item.name.equals(pName)) {
				if (Constants.LOGS_ENABLED2) {
					Log.d(Constants.LOGTAG,
							" "
									+ MapsMainFragmentList.CLASSTAG
									+ " findClusterItemFromTempList - return item found");
				}
				return (item);
			}
		}
		if (Constants.LOGS_ENABLED2) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " findClusterItemFromTempList - return null");
		}
		return (null);
	}

	private void createAllClusterPolygons() {
		if (Constants.LOGS_ENABLED_CLUSTERPOLYGON) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " createAllClusterPolygons - Enter");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " *************************************");
		}

		ListIterator<ClusterItem> it = mClusterList.listIterator();
		ClusterItem item;
		while (it.hasNext()) {
			item = it.next();
			item.polyline = createClusterPolygon(item.polygonCoordinates);
			it.set(item); // now set the item back in the array list
		}
		
		createAllHolePolygons();
	}

	// 06May2016 v0.9.66 - - add support for holes
	private void createAllHolePolygons() {
		if (Constants.LOGS_ENABLED_HOLES) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " createAllHolePolygons - Enter");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " *************************************");
		}
		
		ListIterator<String> it = mHolePolylineCoordinates.listIterator();
		String item;
		while (it.hasNext()) {
			item = it.next();
			Polyline polyline = createClusterPolygon(item);
			it.set(item); // now set the item back in the array list
		}
	}
	
	private Polyline createClusterPolygon(final String coordinates) {
		if (Constants.LOGS_ENABLED_CLUSTERPOLYGON) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " createClusterPolygon - Enter");
			Log.d(Constants.LOGTAG,
					" " + MapsMainFragmentList.CLASSTAG
							+ " createClusterPolygon size="
							+ mClusterPolygonList.size());
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " createClusterPolygon - " + coordinates);

		}
		// parse the coordinates to retrieve the list of polygon coordinates
		List<LatLng> myLatLngList = new ArrayList<LatLng>();

		int startIndex = 0;
		int endIndex = 0;
		String longitude;
		String latitude;
		int lineLength = coordinates.length();
		int i = 0;

		// endIndex = coordinates.indexOf(",", startIndex);
		while ((endIndex < lineLength)
				&& ((endIndex = coordinates.indexOf(",", startIndex)) != -1)) {
			longitude = coordinates.substring(startIndex, endIndex);
			if (Constants.LOGS_ENABLED_CLUSTERPOLYGON) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " createClusterPolygon - longitude=" + longitude);
			}
			startIndex = endIndex + 1;
			endIndex = coordinates.indexOf(",", startIndex);
			latitude = coordinates.substring(startIndex, endIndex);
			if (Constants.LOGS_ENABLED_CLUSTERPOLYGON) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " createClusterPolygon - latitude=" + latitude);
			}
			LatLng ll = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
			myLatLngList.add(ll); // add to list used to build the polygon
			if (Constants.LOGS_ENABLED_CLUSTERPOLYGON) {
				++i;
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " createClusterPolygon - count=" + i);
			}

			// now skip over next "0 "
			startIndex = endIndex + 3;
			// endIndex = endIndex + 2;
		}
		
		// 06May2016 v0.9.66 - if Hole polyline then append the first entry to the end of list to close the polyline
		LatLng llfirst = myLatLngList.get(0);
		myLatLngList.add(llfirst);
		
		// @todo KRC - switch to Polygon?
		Polyline myPolyline = googleMap.addPolyline(new PolylineOptions()
		// .add(new LatLng(prevLat2, prevLon2), new LatLng(prevLat, prevLon),
		// new LatLng(lat, lon))
				.width(5).color(
						getActivity().getBaseContext().getResources()
								.getColor(R.color.sfmta_button_selected_red)));
		myPolyline.setPoints(myLatLngList);

		// mClusterPolylineList.add(myPolyline);
		myLatLngList.clear();

		return (myPolyline);
	}

/************************************************************************
	private void readPointListFile_OLD_FILE_FORMAT() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readPointListFile - Enter");
		}
		
		try {
			BufferedReader reader;
			
			// reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(UncEpiSettings.selectedSurveyItem.pointsFilename)));
			
			// create a File object for the parent directory
	        File fileDirectory = new File(Constants.EPI_FILE_POINTS_FOLDER);
	        // create a File object for the input file
	        File inputFile = new File(fileDirectory, UncEpiSettings.selectedSurveyItem.pointsFilename);
	        // now attach the InputStream to the file object, instead of a String representation
	        FileInputStream input = new FileInputStream(inputFile);
	        InputStreamReader inputStreamReader = new InputStreamReader(input);
	        
			reader = new BufferedReader(inputStreamReader);
		    
		    int startIndex = 0;
		    int endIndex = 0;
		    int i = 0;
		    String line = ""; 
		    boolean eof = false;
		    boolean bParamFound = false;
		    
		    while (!eof) {
		    	PointItem item = new PointItem();
		    	
		    	// find the start of a Point
		    	bParamFound = false;
		    	while ((!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<Placemark id=")) {
		    			if (Constants.LOGS_ENABLED) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " line=" + line);
		    			}
		    			bParamFound = true;
		    		}
			    }
		    	
		    	// find the "<name>"
		    	bParamFound = false;
		    	while ((!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<name>")) {
		    			startIndex = line.indexOf("<name>") + 6;
		    			endIndex   = line.indexOf("<", startIndex);
		    			item.name  = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " name=" + item.name);
		    			}
		    			bParamFound = true;
		    		}
		    	}
			   
		    	// find the "PO_NAME", "STREETADD", "ZIP"
		    	bParamFound = false;
		    	while ((!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("ZIP")) {
		    			startIndex = line.indexOf("ZIP") + 25;  // skip over the table format text
		    			endIndex   = line.indexOf("&lt", startIndex);
		    			item.zipCode = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " zip=" + item.zipCode);
		    			}
		    			bParamFound = true;
		    		}
		    		if (line.contains("PO_NAME")) {
		    			startIndex = line.indexOf("PO_NAME") + 29;  // skip over the table format text
		    			endIndex   = line.indexOf("&lt", startIndex);
		    			item.poName = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " PO=" + item.poName);
		    			}
		    			bParamFound = true;
		    		}
		    		if (line.contains("STREETADD")) {
		    			startIndex = line.indexOf("STREETADD") + 31;  // skip over the table format text
		    			endIndex   = line.indexOf("&lt", startIndex);
		    			item.streetAddr = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " Street=" + item.streetAddr);
		    			}
		    			bParamFound = true;
		    		}
		    		if (Constants.LOGS_ENABLED3) {
	    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + "  Street=" + item.streetAddr + "  PO=" + item.poName + "  Zip=" + item.zipCode);
	    			}
		    	}
		    	
		    	// find the start of a Point and its "coordinates"	
		    	bParamFound = false;
		    	while ((!bParamFound) && ((line = reader.readLine()) != null)) {
		    		if (line.contains("<coordinates>")) {
		    			if (Constants.LOGS_ENABLED) {
		    				// Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " line=" + line);
		    			}
		    			startIndex     = line.indexOf("<coordinates>") + 13;
		    			endIndex       = line.indexOf(",", startIndex);
		    			item.longitude = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " longitude=" + item.longitude);
		    			}
			    	
		    			startIndex     = endIndex + 1;
		    			endIndex       = line.indexOf(",", startIndex);
		    			item.latitude  = line.substring(startIndex, endIndex);
		    			if (Constants.LOGS_ENABLED) {
		    				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startIndex=" + startIndex + " endIndex=" + endIndex + " latitude=" + item.latitude);
		    			}
		    			
		    			bParamFound = true;
		    		}
			    }
		    	
		    	if (Constants.LOGS_ENABLED) {
    		    	++i;
    		    	item.Dump(i);
    		    }
       	    	// add to list, store in database later
       	    	if (bParamFound) { mPointList.add(item); }
       	    	
       	    	if (line == null) { eof = true; }
		    	
		    }

		    reader.close();
		} catch (IOException e) {
		    // log the exception
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readPointListFile - Exception!!! " + e.toString());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}
	}
	******************************************************************************/

	// v0.9.57 - suppress warning genertaed by calls to collections calls when sorting points list
	@SuppressWarnings("unchecked")
	private void readPointListFile() {
		if (Constants.LOGS_ENABLED5) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " readPointListFile - Enter");
		}

		/******************
		 * <Placemark id=""> <Snippet maxLines="0"> </Snippet> <name>10-1</name>
		 * <description>&lt;br&gt;&lt;br&gt;&lt;table class='data'
		 * &gt;&lt;/table&gt;</description> <visibility>1</visibility> <Point>
		 * <extrude>1</extrude> <tessellate>0</tessellate>
		 * <altitudeMode>clampToGround</altitudeMode>
		 * <coordinates>-78.9440786896813,35.980061855736,0 </coordinates>
		 * </Point> <styleUrl>#default</styleUrl> </Placemark>
		 **************************/

		/***************************
		 * Staging area is defined with name = BASE <Placemark id="ID_00211">
		 * <name>BASE</name>
		 * 
		 */
		
		/*
		 * Placemark id
		 * name
		 * POINT_X (longitude)
		 * POINT_Y (latitude)
		 * STREETADD
		 * LABEL
		 * CLUSTER
		 * PNT_NUMB
		 * CITY
		 * 
		 */

		try {
			BufferedReader reader;

			// reader = new BufferedReader(new
			// InputStreamReader(getActivity().getAssets().open(UncEpiSettings.selectedSurveyItem.pointsFilename)));

			// create a File object for the parent directory
			// File fileDirectory = new File(Constants.EPI_FILE_POINTS_FOLDER);
			File fileDirectory = new File(UncEpiSettings.selectedSurveyItem.surveyFilesFolderName);
			// create a File object for the input file
			// File inputFile = new File(fileDirectory, UncEpiSettings.selectedSurveyItem.pointsFilename);
			// File inputFile = new File(fileDirectory, "pointsfile.kml");
			int index = UncEpiSettings.selectedSurveyItem.pointsFilename.lastIndexOf("/");
			String filename1 = UncEpiSettings.selectedSurveyItem.pointsFilename.substring(index+1);
			File inputFile = new File(fileDirectory, filename1);
			
			// now attach the InputStream to the file object, instead of a
			// String representation
			FileInputStream input = new FileInputStream(inputFile);
			InputStreamReader inputStreamReader = new InputStreamReader(input);

			reader = new BufferedReader(inputStreamReader);

			int startIndex = 0;
			int endIndex = 0;
			int i = 0;
			String line = "";
			boolean eof = false;
			boolean bParamFound = false;

			/************** 20Apr2015 ***************/
			UncEpiSettings.selectedSurveyItem.stagingAreaAddr = "";
			
			while (!eof) {
				PointItem item = new PointItem();

				// find the start of a Point
				bParamFound = false;
				while ((!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("<PLACEMARK ID=")) || (line.contains("<Placemark id=")) || (line.contains("<placemark id="))) {
						if (Constants.LOGS_ENABLED5) {
							Log.d(Constants.LOGTAG, " "
									+ MapsMainFragmentList.CLASSTAG + " line="
									+ line);
						}
						bParamFound = true;
					}
				}

				// find the "<name>17-5</name>" line --> This is the cluster-point ids
				// e.g. 10-1 or BASE or HOTEL
				bParamFound = false;
				while ((!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("<NAME>")) || (line.contains("<Name>")) || (line.contains("<name>"))) {
						startIndex = line.indexOf("<name>") + 6;
						endIndex = line.indexOf("<", startIndex);
						item.name = line.substring(startIndex, endIndex);
						if (Constants.LOGS_ENABLED5) {
							Log.d(Constants.LOGTAG, " "
									+ MapsMainFragmentList.CLASSTAG
									+ " startIndex=" + startIndex
									+ " endIndex=" + endIndex + " name="
									+ item.name);
						}
						bParamFound = true;
					}
				}


				// *********** 25Apr2015 - change in kml format...the tag and value are now on separate lines
				// find the "POINT_X" --> This this the longitude
				//     <td>POINT_X</td>
				//
				//     <td>-78.899101</td>
				bParamFound = false;
				while ((!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("POINT_X")) || (line.contains("Point_X")) || (line.contains("point_x"))) {
						// Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + "POINT_X tag found");
						// now get the value
						while ((!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.length() > 0) {  // skip any blank lines
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.longitude = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED5) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex + " POINT_X="
											+ item.longitude);
								}
								bParamFound = true;
							}
						}
					}
				}

				// *********** 25Apr2015 - change in kml format...the tag and value are now on separate lines
				// find the "POINT_Y" --> This this the longitude
				//     <td>POINT_Y</td>
				//     <td>35.980003</td>
				bParamFound = false;
				while ((!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("POINT_Y")) || (line.contains("Point_Y")) || (line.contains("point_y"))) {
						// Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + "POINT_Y tag found");
						// now get the value
						while ((!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.length() > 0) {  // skip any blank lines
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.latitude = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED5) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex + " POINT_Y="
											+ item.latitude);
								}
								bParamFound = true;
							}
						}
					}
				}

				
				// *********** 25Apr2015 - change in kml format...the tag and value are now on separate lines
				// find the "STREETADD" --> This this the street address
				//     <td>STREETADD</td>
				//     <td>123 Main Street</td>
				bParamFound = false;
				while ((!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("STREETADD")) || (line.contains("Streetadd")) || (line.contains("streetadd"))) {
						// now get the value
						while ((!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.length() > 0) {  // skip any blank lines
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.streetAddr = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED5) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex + " POINT_X="
											+ item.streetAddr);
								}
								bParamFound = true;
							}
						}
					}
				}
				
				
				// *********** 25Apr2015 - change in kml format...the tag and value are now on separate lines
				// find the "CITY" --> This this the city name
				//     <td>CITY</td>
				//     <td>Durham</td>
				bParamFound = false;
				while ((!bParamFound) && ((line = reader.readLine()) != null)) {
					if ((line.contains("CITY")) || (line.contains("City")) || (line.contains("city"))) {
						// now get the value
						while ((!bParamFound) && ((line = reader.readLine()) != null)) {
							if (line.length() > 0) {  // skip any blank lines
								startIndex = line.indexOf("<td>") + 4;
								endIndex = line.indexOf("<", startIndex);
								item.city = line.substring(startIndex, endIndex);
								if (Constants.LOGS_ENABLED5) {
									Log.d(Constants.LOGTAG, " "
											+ MapsMainFragmentList.CLASSTAG
											+ " startIndex=" + startIndex
											+ " endIndex=" + endIndex + " POINT_X="
											+ item.city);
								}
								bParamFound = true;
							}
						}
					}
				}
				
				
				if (Constants.LOGS_ENABLED3) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ "  Street=" + item.streetAddr + "  City=" + item.city);
				}
				

				if (Constants.LOGS_ENABLED5) {
					++i;
					item.Dump(i);
				}
				// add to list
				if (bParamFound) {
					if ((item.name.equals("HOTEL")) || (item.name.equals("BASE"))) {
						// @ToDo KRC - Update Survey Staging Area coordinates
						// and address (streetAddr, city)
						UncEpiSettings.selectedSurveyItem.stagingAreaAddr      = item.streetAddr + "," + item.city;
						UncEpiSettings.selectedSurveyItem.stagingAreaLatitude  = Double.parseDouble(item.latitude);
						UncEpiSettings.selectedSurveyItem.stagingAreaLongitude = Double.parseDouble(item.longitude);
					} else {
						mPointList.add(item);
					}
				}

				if (line == null) {
					eof = true;
				}

			}

			reader.close();
		} catch (IOException e) {
			// log the exception
			if (Constants.LOGS_ENABLED5) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " readPointListFile - Exception!!! " + e.toString());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}

		if (Constants.LOGS_ENABLED_SORT) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readPointListFile - SORT POINTS **************");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readPointListFile - SORT POINTS **************");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readPointListFile - SORT POINTS **************");
		}
		Collections.sort(mPointList, new SortPointsBasedOnCluster());
		Collections.sort(mPointList, new SortPointsBasedOnClusterLength());
		Collections.sort(mPointList, new SortPointsBasedOnPoint());
		Collections.sort(mPointList, new SortPointsBasedOnPointLength());
		
		/**** 20Apr2015 - if staging area coordinates are not set then set to first point in list */
		// V0.9.64 - add checks for latitude and longitude == 0
		if ((UncEpiSettings.selectedSurveyItem.stagingAreaAddr.equals("")) ||
			(UncEpiSettings.selectedSurveyItem.stagingAreaLatitude == 0)   ||
			(UncEpiSettings.selectedSurveyItem.stagingAreaLongitude == 0)) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readPointListFile - stagingAreaAddr empty");
			if (!mPointList.isEmpty()) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readPointListFile - mPointList not empty");
				PointItem firstPoint = mPointList.get(0);
				UncEpiSettings.selectedSurveyItem.stagingAreaAddr      = firstPoint.streetAddr + "," + firstPoint.city;
				UncEpiSettings.selectedSurveyItem.stagingAreaLatitude  = Double.parseDouble(firstPoint.latitude);
				UncEpiSettings.selectedSurveyItem.stagingAreaLongitude = Double.parseDouble(firstPoint.longitude);
			}
		}
	}

	private void setPointListMarkersInit() {
		if (Constants.LOGS_ENABLED3) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setPointListMarkers - Enter");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setPointListMarkers - NumPoints=" + mPointList.size());
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " &&&&&&&&&&&&&&&&&&&&&&&&&&");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " &&&&&&&&&&&&&&&&&&&&&&&&&&");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " &&&&&&&&&&&&&&&&&&&&&&&&&&");
		}

		/**************************************
		 * Toast toast= Toast.makeText(getActivity(),
		 * "Initializing map with list of Points...Please wait",
		 * Toast.LENGTH_SHORT);
		 * toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
		 * toast.show();
		 **************************************/
		// Toast.makeText(getActivity(),
		// "Initializing map with list of Points...Please wait",
		// Toast.LENGTH_SHORT).show();

		Iterator<PointItem> it = mPointList.iterator();
		while (it.hasNext()) {
			PointItem item = it.next();
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " name=" + item.name + " latitude=" + item.latitude
						+ " longitude=" + item.longitude);
			}
			double lati = Double.parseDouble(item.latitude);
			double lngi = Double.parseDouble(item.longitude);
			LatLng ll = new LatLng(lati, lngi);

			// Setting the position and title for the marker using the marker
			// option
			item.mo.position(ll);
			// item.mo.title("Point " + item.name.toString());
			item.mo.title("Point " + item.name + " @ " + item.streetAddr + ", "
					+ item.city);
			// v0.9.53 16Jun2015 - set anchor point of marker to the middle of my icon
			item.mo.anchor(0.5f,  0.5f);

			// use icon based on screen pixel width
			// e.g. Nexus S 480x800, Galaxy S3 720x1280, Galaxy S4 1080x1920
			// screen width <= 540 then use small icon, else <=720 use medium
			// icon, else use large icon
			/***********************************************************************
			 * if (UncEpiSettings.screenWidth <= 540) {
			 * item.mo.icon(BitmapDescriptorFactory
			 * .fromAsset("point_marker_blue.png")); // 1 } else if
			 * (UncEpiSettings.screenWidth <= 720) {
			 * item.mo.icon(BitmapDescriptorFactory
			 * .fromAsset("point_marker_blue.png")); // 2 } else {
			 * item.mo.icon(BitmapDescriptorFactory
			 * .fromAsset("point_marker_blue.png")); // 3 }
			 *****************************************************/
			item.setSurveyStatus(Constants.POINT_STATUS_NOTSTARTED);
			// temp comment item.marker = googleMap.addMarker(item.mo); // Place the marker on
														// the map
			// it.set(item); // KKKK - Put changed item back in the list
		}
	}

	private void setPointListMarkersVisible(final boolean state) {
		if (Constants.LOGS_ENABLED4) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setPointListMarkersVisible - Enter, state =  " + state);
		}
		Iterator<PointItem> it = mPointList.iterator();
		while (it.hasNext()) {
			PointItem item = it.next();
			// v0.9.60 KRC 22Feb2015
			if (item.marker == null) {
				if (Constants.LOGS_ENABLED4) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " setPointListMarkersVisible - item.marker is null!!");
				}
				item.marker = googleMap.addMarker(item.mo);
			}
			item.marker.setVisible(state);
			// it.set(item);
		}
	}

	private void setClusterOutlineMarkersVisibleState(final boolean state) {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG,
					" "
							+ MapsMainFragmentList.CLASSTAG
							+ " setClusterOutlineMarkersVisibleState - Enter, state =  "
							+ state);
		}

		/*******************************************
		 * Iterator<ClusterItem> it = mClusterList.iterator();
		 * while(it.hasNext()) { it.next().marker.setVisible(state); }
		 * 
		 * if (state == false) { Iterator<Polyline> it2 =
		 * mRoutePolylineItems.iterator(); while (it2.hasNext()) { Polyline item
		 * = it2.next(); item.remove(); } mRoutePolylineItems.clear(); }
		 ********************************************/
	}

	private PointItem getPointItemFromList(String pName) {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " getPointItemFromList - Enter");
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " getPointItemFromList - pName=" + pName);
		}

		Iterator<PointItem> it = mPointList.iterator();
		while (it.hasNext()) {
			PointItem item = it.next();
			if (item.name.equals(pName)) {
				return item;
			}
		}

		return null;
	}

	private void addReverseGeoLocationMarker(LatLng markLatLng) {
		// Add Marker on the touched location with address

		new ReverseGeocodingAsyncTask(getActivity().getBaseContext())
				.execute(markLatLng);
		// new ReverseGeocodingTask(getBaseContext()).execute(markLatLng);
	}

	private class ReverseGeocodingAsyncTask extends AsyncTask<LatLng, Void, String> {
		Context mContext;

		private String mGeolocationAddr;
		private TextToSpeech mTTS = null;
		private OnInitListener mTTSInitListener = new OnInitListener() {
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					if (Constants.LOGS_ENABLED_LOCATION) {
						Log.d(Constants.LOGTAG, " " + "ReverseGeocodingAsyncTask onInit - " + mGeolocationAddr);
						Log.d(Constants.LOGTAG, " " + "*******************************************************");
						Log.d(Constants.LOGTAG, " " + "*******************************************************");
						Log.d(Constants.LOGTAG, " " + "*******************************************************");
						Log.d(Constants.LOGTAG, " " + "*******************************************************");
					}
					if (bDefaultLatLan) {
						mTTS.speak(
								getString(R.string.using_default_unc_office_addr, mGeolocationAddr),
								TextToSpeech.QUEUE_ADD, null);
					} else {
						mTTS.speak(getString(R.string.current_location_speech, mGeolocationAddr),
								TextToSpeech.QUEUE_FLUSH, null);
					}
					// defaultMarkerOptions.title(mGeolocationAddr);
				}
			}
		};

		public ReverseGeocodingAsyncTask(Context context) {
			super();
			mContext = context;
		}

		// Find the address using Reverse Geocoding
		@Override
		protected String doInBackground(LatLng... params) {
			Geocoder geocoder = new Geocoder(mContext);
			double latitude = params[0].latitude;
			double longitude = params[0].longitude;

			List<Address> addresses = null;
			// String addressText = "";

			try {
				addresses = geocoder.getFromLocation(latitude, longitude, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}

			mGeolocationAddr = "Unknown";
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				/**********************************************
				 * addressText = String.format("%s, %s, %s",
				 * address.getMaxAddressLineIndex() > 0 ?
				 * address.getAddressLine(0) : "", address.getLocality(),
				 * address.getCountryName());
				 **********************************************/
				mGeolocationAddr = String.format(
						"%s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address.getLocality());

				if (Constants.LOGS_ENABLED_LOCATION) {
					Log.d(Constants.LOGTAG, " "
							+ "ReverseGeocodingAsyncTask doInBackground - "
							+ "mGeolocationAddr = " + mGeolocationAddr);
					Log.d(Constants.LOGTAG, " "
							+ "ReverseGeocodingAsyncTask doInBackground - "
							+ "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
					Log.d(Constants.LOGTAG, " "
							+ "ReverseGeocodingAsyncTask doInBackground - "
							+ "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				}
			} else {
				if (Constants.LOGS_ENABLED_LOCATION) {
					Log.d(Constants.LOGTAG, " "
							+ "ReverseGeocodingAsyncTask doInBackground - "
							+ "mGeolocationAddr = null");
					Log.d(Constants.LOGTAG, " "
							+ "ReverseGeocodingAsyncTask doInBackground - "
							+ "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
					Log.d(Constants.LOGTAG, " "
							+ "ReverseGeocodingAsyncTask doInBackground - "
							+ "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				}
			}

			// return addressText;
			return mGeolocationAddr;
		}

		@Override
		protected void onPostExecute(String addressText) {
			if (mGeolocationAddr != null) {
				if ((!mGeolocationAddr.equals("null")) && (!mGeolocationAddr.equals("Unknown")) && (mGeolocationAddr.length() > 5)) {
					if (UncEpiSettings.speechEnabled) {
						mTTS = new TextToSpeech(getActivity(), mTTSInitListener);
						if (!Build.VERSION.RELEASE.startsWith("2.")) {
							if (Constants.LOGS_ENABLED_LOCATION) {
								Log.d(Constants.LOGTAG, " " + "mTTS.shutdown due to Build.VERSION = " + Build.VERSION.RELEASE);
							}
							// mTTS.shutdown(); // v0.9.53 17Jun2015 - Remove check that was previously added in an earlier release
						}
					}

					String tt;
					if (bDefaultLatLan) {
						tt = "You are outside of Map range, using Staging Area address at "
								+ mGeolocationAddr + " as current location";
					} else {
						tt = "Your current address is " + mGeolocationAddr;
					}
					Toast toast = Toast.makeText(getActivity(), tt.toString(), Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
				} else if (!mGeolocationAddr.equals("Unknown")) {
					Toast toast = Toast
							.makeText(
									getActivity(),
									"Device GeoLocation service has stopped. Please turn off phone & remove battery. Then re-install batter & power on phone.",
									Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER_VERTICAL
							| Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
				}
			} else {
				Vibrator v = (Vibrator) getActivity().getSystemService(
						Context.VIBRATOR_SERVICE);
				if (v != null) {
					v.vibrate(500); // 1/2 second
				}
				if (mGeolocationAddr.equals("Unknown")) {
					Toast toast = Toast
							.makeText(
									getActivity(),
									"Please check the phone's internet settings",
									Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
						toast.show();
				}
				else {
					Toast toast = Toast
						.makeText(
								getActivity(),
								"Please check the phone's location settings, or turn phone off & on",
								Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
				}
			}

			if (bFirstTimeStartup) {
				bFirstTimeStartup = false;
				// Toast.makeText(getActivity(),
				// "Select a Stop to get name, and select again for Route & Time information",
				// Toast.LENGTH_LONG).show();
			}
		}
	} // ReverseGeocodingAsyncTask

	
	private class ClusterListAdapter extends ArrayAdapter<ClusterItem> {

		private ArrayList<ClusterItem> items; // needed???

		public ClusterListAdapter(Context context, int textViewResourceId,
				ArrayList<ClusterItem> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + "MapsMainFragmentList"
						+ " ClusterListAdapter constructor");
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " "
						+ "MapsMainFragmentList ClusterListAdapter"
						+ " getView");
			}
			View v = convertView;
			// KRC v0.9.44 only re-inflate if view is null
			if (v == null) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + "ClusterListAdapter"
							+ " v == null so inflate");
				}
				// @ToDo KRC may need this --> LayoutInflater vi =
				// (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.mapsmaindatalist_item, null);
			}

			// @ToDo KRC - Look at which data type (alerts, routes, etc) is
			// being displayed
			ClusterItem clusterItem = items.get(position);
			if (clusterItem != null) {
				TextView clusterText = (TextView) v
						.findViewById(R.id.alerttext_id);
				if (clusterText != null) {
					clusterText.setOnClickListener(new kcTextViewClickListener(
							position));
					clusterText
							.setOnLongClickListener(new kcTextViewLongClickListener(
									position));
					clusterText.setText(clusterItem.name);
				}
			}
			return v;
		}

		class kcTextViewClickListener implements OnClickListener {
			int position;

			public kcTextViewClickListener(int pos) {
				this.position = pos;
			}

			public void onClick(View v) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " kcTextViewClickListener onClick position="
							+ this.position);
				}

				ClusterItem clusterItem = items.get(position);

				if (Constants.LOGS_ENABLED) {
					// Log.d(Constants.LOGTAG, " " +
					// " kcTextLongViewClickListener onClick ClusterItem id = "
					// + clusterItem.placemarkId);
					Log.d(Constants.LOGTAG,
							" "
									+ " kcTextLongViewClickListener onClick ClusterItem shortName = "
									+ clusterItem.name);
					// Log.d(Constants.LOGTAG, " " +
					// " kcTextLongViewClickListener onClick ClusterItem longName = "
					// + clusterItem.cluster);
				}

				mClusterSelected = clusterItem.name;

				dataItemsHeaderTextView
						.setText(UncEpiSettings.selectedSurveyItem.title
								+ " - Cluster #" + clusterItem.name
								+ " - Points");

				setClusterPointsView(clusterItem.name);

				Toast toast = Toast.makeText(getActivity(),
						"Please wait. Updating map for Cluster "
								+ mClusterSelected, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();

				// animate map to the cluster center position
				setClustersToDefaultColor();
				ClusterItem item = findClusterItem(clusterItem.name);
				item.polyline.setColor(getResources().getColor(
						R.color.unc_button_selected));
				LatLng myLatLng = new LatLng(
						Double.parseDouble(item.centerLatitude),
						Double.parseDouble(item.centerLongitude));
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						myLatLng, clusterMapZoomLevel));

				// add a location marker to map
				// Create a marker for default position and add to Map
				// clusterMarkerOptions = new MarkerOptions();
				if (clusterMarker != null) {
					clusterMarker.remove();
				}
				clusterMarkerOptions.position(myLatLng);
				clusterMarkerOptions.title("Cluster " + clusterItem.name);
				// clusterMarkerOptions.icon(BitmapDescriptorFactory.fromAsset("point_marker_black.png"));
				// v0.9.53 16Jun2015 - Do not display the black dot for cluster center point. 
				//                     Maybe display the Cluster # inside a transparent circle???
				// clusterMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_black));
				clusterMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_marker_blank));
				clusterMarker = googleMap.addMarker(clusterMarkerOptions);

				clusterMarker.showInfoWindow();
			}
		}

		class kcTextViewLongClickListener implements OnLongClickListener {
			int position;

			public kcTextViewLongClickListener(int pos) {
				this.position = pos;
			}

			public boolean onLongClick(View v) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " "
							+ " kcTextLongViewClickListener onClick position="
							+ this.position);
				}

				return true;
			}
		}

	}

	private void setClusterList() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setClusterList - Enter");
		}

		mClusterListAdapter.notifyDataSetChanged();
		mDataListView.setClickable(true);
		mDataListView.setHorizontalScrollBarEnabled(true);
		mDataListView.setVerticalScrollBarEnabled(true);
		mDataListView.setSelection(0);
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setClusterList Cluster Count=" + mClusterList.size());
		}
	}

	private class PointListAdapter extends ArrayAdapter<PointItem> {

		private ArrayList<PointItem> items; // needed???

		public PointListAdapter(Context context, int textViewResourceId,
				ArrayList<PointItem> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + "MapsMainFragmentList"
						+ " PointListAdapter constructor");
			}
		}

		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	if (Constants.LOGS_ENABLED) {
        		Log.d(Constants.LOGTAG, " " + "MapsMainFragmentList PointListAdapter" + " getView");
        	}
        	View v = convertView;
        	// KRC v0.9.44 only re-inflate if view is null
        	if (v == null) {
        		if (Constants.LOGS_ENABLED) {
            		Log.d(Constants.LOGTAG, " " + "PointListAdapter" + " v == null so inflate");
            	}
        		// @ToDo KRC may need this --> LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.mapsmainpointlist_item, null);
        	}
        	
        	// @ToDo KRC - Look at which data type (alerts, routes, etc) is being displayed
        	PointItem pointItem = items.get(position);
        	if (pointItem != null) {
                TextView text = (TextView) v.findViewById(R.id.alerttext_id);
                if (text != null) {
                	text.setOnClickListener(new kcTextViewClickListener(position));
                	text.setOnLongClickListener(new kcTextViewLongClickListener(position));
                	// KRC v0.9.46 Truncate the street string to 20 chars so it fits in screen view
                	String street;
                	if (pointItem.streetAddr.length() > 20) {
                		street = pointItem.streetAddr.substring(0, 20);
                	}
                	else {
                		street = pointItem.streetAddr;
                	}
                	if (pointItem.surveyStatus == Constants.POINT_STATUS_COMPLETED) {
                		text.setText(pointItem.name + " @ " + street + ", " + pointItem.city + " - COMPLETED");
                		// text.setText(pointItem.name + " @ " + pointItem.streetAddr + ", " + pointItem.city + "  --  Survey COMPLETED");
                		// text.setText(pointItem.name + " - Survey Completed");
                	}
                	else if (pointItem.surveyStatus == Constants.POINT_STATUS_INPROGRESS) {
                		text.setText(pointItem.name + " @ " + street + ", " + pointItem.city + " - IN PROGRESS");
                		// text.setText(pointItem.name + " @ " + pointItem.streetAddr + ", " + pointItem.city + "  --  Survey IN PROGRESS");
                		// text.setText(pointItem.name + " - Survey In Progress");
                	}
                	else if (pointItem.surveyStatus == Constants.POINT_STATUS_NOTSTARTED) {
                		text.setText(pointItem.name + " @ " + street + ", " + pointItem.city + " - NOT STARTED");
                		// text.setText(pointItem.name + " @ " + pointItem.streetAddr + ", " + pointItem.city + "  --  Survey NOT STARTED");
                		// text.setText(pointItem.name + " - Survey Not Started");
                	}
                	else if (pointItem.surveyStatus == Constants.POINT_STATUS_PAUSED) {
                		text.setText(pointItem.name + " @ " + street + ", " + pointItem.city + " - PAUSED");
                		// text.setText(pointItem.name + " @ " + pointItem.streetAddr + ", " + pointItem.city + "  --  Survey PAUSED");
                		// text.setText(pointItem.name + " - Survey Paused");
                	}
                	else if (pointItem.surveyStatus == Constants.POINT_STATUS_ERROR) {
                		text.setText(pointItem.name + " @ " + street + ", " + pointItem.city + " - ERROR");
                		// text.setText(pointItem.name + " @ " + pointItem.streetAddr + ", " + pointItem.city + "  --  Survey ERROR");
                		// text.setText(pointItem.name + " - Survey Error");
                	}
                	else {
                		text.setText(pointItem.name + " @ " + street + ", " + pointItem.city + " - UNKNOWN");
                		// text.setText(pointItem.name + " @ " + pointItem.streetAddr + ", " + pointItem.city + "  --  Survey UNKNOWN");
                		// text.setText(pointItem.name + " - Unknown");
                	}
                }
            }
        	return v;
        }

		class kcTextViewClickListener implements OnClickListener {
			int position;

			public kcTextViewClickListener(int pos) {
				this.position = pos;
			}

			public void onClick(View v) {
				if (Constants.LOGS_ENABLED_MAPMARKERS) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " kcTextViewClickListener onClick position="
							+ this.position);
				}

				PointItem pointItem = items.get(position);
				if (Constants.LOGS_ENABLED_MAPMARKERS) {
					Log.d(Constants.LOGTAG, " "
									+ " kcTextLongViewClickListener onClick routeItem shortName = "
									+ pointItem.name);
				}

				mPointSelected = pointItem.name;

				Toast toast = Toast
						.makeText(getActivity(),
								"Please wait. Updating map for Point "
										+ mPointSelected, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();

				setMapToPointLocation(pointItem);
				pointItem.marker.showInfoWindow();
			}
		}

		// 11Jun2015 v0.9.52 - Enable long click to start a survey
		class kcTextViewLongClickListener implements OnLongClickListener {
			int position;

			public kcTextViewLongClickListener(int pos) {
				this.position = pos;
			}

			public boolean onLongClick(View v) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " "
							+ " kcTextLongViewClickListener onClick position="
							+ this.position);
				}

				PointItem pointItem = items.get(position);
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " "
									+ " kcTextLongViewClickListener onClick routeItem shortName = "
									+ pointItem.name);
				}

				mPointSelected = pointItem.name;
				setMapToPointLocation(pointItem);
				displayStartSurveyDialog(pointItem, true);  // V0.9.64 add param
				return true;
			}
		}

	}

	private void setPointList() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setPointList - Enter");
		}

		mPointListAdapter.notifyDataSetChanged();
		mDataListView.setClickable(true);
		mDataListView.setHorizontalScrollBarEnabled(true);
		mDataListView.setVerticalScrollBarEnabled(true);
		mDataListView.setSelection(0);
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setPointList Point Count=" + mPointList.size());
		}
	}

	private void setMapToPointLocation(final PointItem pointItem) {
		LatLng myLatLng = new LatLng(Double.parseDouble(pointItem.latitude),
				Double.parseDouble(pointItem.longitude));

		// animate to the position
		// googleMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLng));
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,
				pointMapZoomLevel));
	}

	/*******************************************************************************
	 * private void updateMarkerWithPointInfo() { if (Constants.LOGS_ENABLED) {
	 * Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG +
	 * " updateMarkerWithPointInfo - Enter"); }
	 * 
	 * PointItem item = getPointItemFromList(mPointSelected);
	 * item.marker.showInfoWindow(); }
	 * 
	 * class MyInfoWindowAdapter implements InfoWindowAdapter {
	 * 
	 * private final View v;
	 * 
	 * MyInfoWindowAdapter() { // Note This is a NextBus request so use a custom
	 * layout, else return null to use default layout // This object gets
	 * created at startup, which is before we know how many routes there are.
	 * 
	 * if (Constants.LOGS_ENABLED) { Log.d(Constants.LOGTAG, " " +
	 * MapsMainFragmentList.CLASSTAG + " MyInfoWindowAdapter() items.size = " +
	 * mStopScheduleItems.size()); Log.d(Constants.LOGTAG, " " +
	 * MapsMainFragmentList.CLASSTAG +
	 * " ###########################################################");
	 * Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG +
	 * " ###########################################################"); }
	 * 
	 * v = getActivity().getLayoutInflater().inflate(R.layout.
	 * stopmarker_infowindow3, null); }
	 * 
	 * // @Override public View getInfoWindow(Marker marker) { // If null is
	 * returned then getInfoContents() is then called
	 * 
	 * if (Constants.LOGS_ENABLED) { Log.d(Constants.LOGTAG, " " +
	 * MapsMainFragmentList.CLASSTAG +
	 * " MyInfoWindowAdapter:getInfoWindow - stopScheduleActive = " +
	 * stopScheduleActive); Log.d(Constants.LOGTAG, " " +
	 * MapsMainFragmentList.CLASSTAG +
	 * " ###########################################################");
	 * Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG +
	 * " ###########################################################"); }
	 * 
	 * Iterator<StopScheduleInfoItem> it = mStopScheduleItems.iterator();
	 * StopScheduleInfoItem item;
	 * 
	 * TextView titleTextView; TextView dirTextView; TextView eta1TextView;
	 * TextView eta2TextView;
	 * 
	 * if (stopScheduleActive) { if (mStopScheduleItems.size() >= 1) { TextView
	 * stopTitleTextView = (TextView) v.findViewById(R.id.stop_title_id);
	 * 
	 * titleTextView = (TextView) v.findViewById(R.id.route1title_text_id);
	 * dirTextView = (TextView) v.findViewById(R.id.route1dir_text_id);
	 * eta1TextView = (TextView) v.findViewById(R.id.route1eta1_text_id);
	 * eta2TextView = (TextView) v.findViewById(R.id.route1eta2_text_id);
	 * 
	 * titleTextView.setVisibility(View.VISIBLE);
	 * dirTextView.setVisibility(View.VISIBLE);
	 * eta1TextView.setVisibility(View.VISIBLE);
	 * eta2TextView.setVisibility(View.VISIBLE);
	 * 
	 * // Route #1 item = it.next(); if (item.title.length() > 1) {
	 * stopTitleTextView.setText(currentStopTitle.toString());
	 * 
	 * // Route #1 if (item.title.length() > 1) {
	 * titleTextView.setText(item.title.toString()); } else {
	 * titleTextView.setText("No Route Name available"); }
	 * 
	 * if (item.eta.length() > 0) {
	 * eta1TextView.setText(convertSecondsToMinutesSeconds
	 * (item.eta).toString()); } else { eta1TextView.setText("Unknown"); }
	 * 
	 * if (item.eta2.length() > 0) {
	 * eta2TextView.setText(convertSecondsToMinutesSeconds
	 * (item.eta2).toString()); } else { eta2TextView.setText("Unknown"); }
	 * 
	 * if (item.dir.length() > 1) { dirTextView.setText(item.dir.toString()); }
	 * else { dirTextView.setText("No info available for route"); } } else { //
	 * KRC - V2.0.0 // stopTitleTextView.setText("No Routes Available");
	 * stopTitleTextView.setText(currentStopTitle.toString());
	 * titleTextView.setText("No route information");
	 * dirTextView.setText("is available"); eta1TextView.setText("");
	 * eta2TextView.setText(""); } }
	 * 
	 * 
	 * // Route #2 titleTextView = (TextView)
	 * v.findViewById(R.id.route2title_text_id); dirTextView = (TextView)
	 * v.findViewById(R.id.route2dir_text_id); eta1TextView = (TextView)
	 * v.findViewById(R.id.route2eta1_text_id); eta2TextView = (TextView)
	 * v.findViewById(R.id.route2eta2_text_id);
	 * 
	 * if (mStopScheduleItems.size() >= 2) {
	 * titleTextView.setVisibility(View.VISIBLE);
	 * dirTextView.setVisibility(View.VISIBLE);
	 * eta1TextView.setVisibility(View.VISIBLE);
	 * eta2TextView.setVisibility(View.VISIBLE);
	 * 
	 * item = it.next(); if (item.title.length() > 1) {
	 * titleTextView.setText(item.title.toString()); } else {
	 * titleTextView.setText("No Route Name available"); }
	 * 
	 * if (item.eta.length() > 0) {
	 * eta1TextView.setText(convertSecondsToMinutesSeconds
	 * (item.eta).toString()); } else { eta1TextView.setText("Unknown"); }
	 * 
	 * if (item.eta2.length() > 0) {
	 * eta2TextView.setText(convertSecondsToMinutesSeconds
	 * (item.eta2).toString()); } else { eta2TextView.setText("Unknown"); }
	 * 
	 * if (item.dir.length() > 1) { dirTextView.setText(item.dir.toString()); }
	 * else { dirTextView.setText("No info available for route"); } } else {
	 * titleTextView.setText(""); dirTextView.setText("");
	 * eta1TextView.setText(""); eta2TextView.setText("");
	 * titleTextView.setVisibility(View.GONE);
	 * dirTextView.setVisibility(View.GONE);
	 * eta1TextView.setVisibility(View.GONE);
	 * eta2TextView.setVisibility(View.GONE); }
	 * 
	 * 
	 * // Route #3 titleTextView = (TextView)
	 * v.findViewById(R.id.route3title_text_id); dirTextView = (TextView)
	 * v.findViewById(R.id.route3dir_text_id); eta1TextView = (TextView)
	 * v.findViewById(R.id.route3eta1_text_id); eta2TextView = (TextView)
	 * v.findViewById(R.id.route3eta2_text_id);
	 * 
	 * if (mStopScheduleItems.size() >= 3) {
	 * titleTextView.setVisibility(View.VISIBLE);
	 * dirTextView.setVisibility(View.VISIBLE);
	 * eta1TextView.setVisibility(View.VISIBLE);
	 * eta2TextView.setVisibility(View.VISIBLE);
	 * 
	 * item = it.next(); if (item.title.length() > 1) {
	 * titleTextView.setText(item.title.toString()); } else {
	 * titleTextView.setText("No Route Name available"); }
	 * 
	 * if (item.eta.length() > 0) {
	 * eta1TextView.setText(convertSecondsToMinutesSeconds
	 * (item.eta).toString()); } else { eta1TextView.setText("Unknown"); }
	 * 
	 * if (item.eta2.length() > 0) {
	 * eta2TextView.setText(convertSecondsToMinutesSeconds
	 * (item.eta2).toString()); } else { eta2TextView.setText("Unknown"); }
	 * 
	 * if (item.dir.length() > 1) { dirTextView.setText(item.dir.toString()); }
	 * else { dirTextView.setText("No info available for route"); } } else {
	 * titleTextView.setText(""); dirTextView.setText("");
	 * eta1TextView.setText(""); eta2TextView.setText("");
	 * titleTextView.setVisibility(View.GONE);
	 * dirTextView.setVisibility(View.GONE);
	 * eta1TextView.setVisibility(View.GONE);
	 * eta2TextView.setVisibility(View.GONE); }
	 * 
	 * startInfoWindowUpdateTimer(); return v; } // stopScheduleActive else {
	 * return null; } }
	 * 
	 * // @Override public View getInfoContents(Marker marker) {
	 * 
	 * // If null is returned then the default window info is displayed
	 * 
	 * // ImageView icon = (ImageView) v.findViewById(R.id.icon); // set some
	 * bitmap to the imageview
	 * 
	 * return null; // v; } } // class MyInfoWindowAdapter
	 * 
	 * 
	 * private void startInfoWindowUpdateTimer() { if (Constants.LOGS_ENABLED) {
	 * Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG +
	 * " startInfoWindowUpdateTimer - Enter"); } int delay = 9800; // 10000; //
	 * delay for 10 seconds // int period = 10000; // repeat every 10 seconds
	 * Timer timer = new Timer(); // timer.scheduleAtFixedRate(new TimerTask() {
	 * timer.schedule(new TimerTask() { // one shot timer public void run() { if
	 * (stopScheduleActive) { if (Constants.LOGS_ENABLED) {
	 * Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG +
	 * " Timer run="); } String stopId = getStopIdFromList(currentStopTitle); if
	 * (!stopId.equals(notFoundString)) { // invoke NextBus api to retrieve the
	 * current stop schedule getStopScheduleAsync(stopId); } } } // }, delay,
	 * period); }, delay); }
	 **********************************************************************/

	private void submitSurveyForm(final String surveyName) {

	}

	private ClusterItem findClusterItem(final String name) {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " findClusterItem - enter - name=" + name);
		}
		Iterator<ClusterItem> it = mClusterList.iterator();
		while (it.hasNext()) {
			ClusterItem item = it.next();
			if (item.name.equals(name)) {
				if (Constants.LOGS_ENABLED) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " findClusterItem - return item found");
				}
				return (item);
			}
		}

		ClusterItem nullItem = new ClusterItem();
		return (nullItem);
	}

	private PointItem findPointItem(final String name) {
		if (Constants.LOGS_ENABLED3) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " findPointItem - enter - name=" + name);
		}
		Iterator<PointItem> it = mPointList.iterator();
		while (it.hasNext()) {
			PointItem item = it.next();
			if (item.name.equals(name)) {
				if (Constants.LOGS_ENABLED3) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " findPointItem - return item found");
				}
				return (item);
			}
		}

		PointItem nullItem = new PointItem();
		return (nullItem);
	}

	// V0.9.64 New method
	private PointItem findPointItemForBeginSurvey(final String name) {
		if (Constants.LOGS_ENABLED3) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " findPointItemForBeginSurvey - enter - name=" + name);
		}
		Iterator<PointItem> it = mPointList.iterator();
		while (it.hasNext()) {
			PointItem item = it.next();
			if (item.name.equals(name)) {
				if (Constants.LOGS_ENABLED3) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " findPointItem - return item found");
				}
				return (item);
			}
		}

		return (null);
	}
	
	private void setClustersToDefaultColor() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setClustersToDefaultColor - enter");
		}
		Iterator<ClusterItem> it = mClusterList.iterator();
		while (it.hasNext()) {
			it.next().polyline.setColor(getResources().getColor(
					R.color.sfmta_button_selected_red));
		}
	}

	private void setClusterPointsView(final String pCluster) {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setClusterPointsView - enter cluster=" + pCluster);
		}

		// find all points that belong to the specified cluster,
		// add the points to a new list adapter
		mClusterPointsList.clear();
		Iterator<PointItem> it = mPointList.iterator();
		while (it.hasNext()) {
			PointItem item = it.next();
			if (item.getClusterId().equals(pCluster)) {
				mClusterPointsList.add(item);
			}
		}
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG,
					" " + MapsMainFragmentList.CLASSTAG
							+ " setClusterPointsView - #points="
							+ mClusterPointsList.size());
		}

		prevMainViewId = mainViewId;
		mainViewId = MENU_POINTS_ID;
		// dataItemsHeaderTextView.setText("Cluster #" + pCluster);
		setSelectButton();

		mDataListView = (ListView) getListView(); // @ToDo KRC - Needed???
		mPointListAdapter = new PointListAdapter(getActivity(),
				R.layout.mapsmaindatalist_item, mClusterPointsList);
		setListAdapter(mPointListAdapter);
		setPointList();
		hidePointMarkerInfo();
	}

	private void getAllPointsStatusAsyncTimer() {
		if (Constants.LOGS_ENABLED_DATABASE) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " getAllPointsAsyncTimer - Enter");
		}
		int delay = 2000; // delay for 2 seconds
		// int period = 10000; // repeat every 10 seconds
		Timer timer = new Timer();
		// timer.scheduleAtFixedRate(new TimerTask() {
		timer.schedule(new TimerTask() { // one shot timer
					public void run() {
						getAllPointsStatusAsync();
					}
					// }, delay, period);
				}, delay);
	}

	private void getAllPointsStatusAsync() {
		// Create an AsyncTask class to set to perform the Get All Points Status
		// on the server and to handle the server response.
		// Toast.makeText(getApplicationContext(),
		// "Please Wait - Getting Survey Status of all Points from Server...",
		// Toast.LENGTH_SHORT).show();
		GetAllPointsStatusAsyncTask task = new GetAllPointsStatusAsyncTask();
		task.execute(new String[] { "" });
	}

	private class GetAllPointsStatusAsyncTask extends
			AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			// runs in non-UI thread
			if (Constants.LOGS_ENABLED_DATABASE) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " GetAllPointsStatusAsyncTask doInBackground - Enter");
			}
			// bGetAllPointsStatusActive = true;
			String response;
			int retVal = 0;
			
			// 09Jun2015 v0.9.52 Check if network connection is active before issuing http API
			if (UncEpiSettings.IsNetworkAvailable(getActivity().getApplicationContext())) {
			    retVal = getAllPointsStatusFromServer(); // should return 0 or an error code
			}
			// v0.9.54 - read the last point status for all points from the local database
			else if (!refreshPointsRequest) {
				retVal = readAllPointsFromDb();
			}
			else {
				refreshPointsRequest = false;
				retVal = -1;
			}
			
			if (retVal == 0) {
				response = "0";
			} else {
				response = "-1";
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// update screen in UI thread
			if (Constants.LOGS_ENABLED_DATABASE) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " GetAllPointsStatusAsyncTask onPostExecute - Enter - result = " + result);
			}
			// bGetAllPointsStatusActive = false;
			//v0.9.54
			if (result.equals("0"))
				updateAllPointsForStatus();
		}
	}

	private int getAllPointsStatusFromServer() {
		if (Constants.LOGS_ENABLED_DATABASE) {
			Log.v(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " getAllPointsStatusFromServer - Enter");
		}

		// Format CURL string to Get All Points Status
		BufferedReader in = null;

		try {
			/*********************** 30Aug2015 v0.9.62
			HttpClient client = new DefaultHttpClient();

			String httpGetCmd; = Constants.EPI_API_PREFIX
								+ "op=epiGetAllPointsStatus&p1=" + UncEpiSettings.username
								+ "&p2=" + UncEpiSettings.selectedSurveyItem.title
								+ "%7C" + UncEpiSettings.coordinator;
			***********************/
			
			URL getCmdUrl = new URL(Constants.EPI_API_PREFIX
					+ "op=epiGetAllPointsStatus&p1=" + UncEpiSettings.username
					+ "&p2=" + UncEpiSettings.selectedSurveyItem.title
					+ "%7C" + UncEpiSettings.coordinator);
			HttpURLConnection urlConnection = (HttpURLConnection) getCmdUrl.openConnection();
			urlConnection.setConnectTimeout(Constants.URL_CONNECTION_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(Constants.URL_CONNECTION_READ_TIMEOUT);


			if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " getAllPointsStatusFromServer httpGetCmd = "
						+ getCmdUrl.toString());
			}
			
			/***************************************** 30Aug2015 v0.9.62
			HttpGet method = new HttpGet(httpGetCmd);
			HttpResponse response = client.execute(method);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			*****************************************/
			in = new BufferedReader (new InputStreamReader(urlConnection.getInputStream()));
			
			String line = "";
			if ((line = in.readLine()) != null) {
				if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " in: " + line);
				}
				/*************************************************
				 * if (line.indexOf("retrieving failed") != -1) { mApiErrorMsg =
				 * getResources().getString(R.string.retrievingfailed_msg); }
				 * else { mApiErrorMsg =
				 * getResources().getString(R.string.networktimeout_msg); }
				 *************************************************/

				// parse string to retrieve sets of
				// clusterId-PointId;SurveyStatus
				// e.g. "22-1;2|21-1;2|10-1;0|10-2;2 etc...
				// e.g. "22-1;2;lat;lon|21-1;2|10-1;0|10-2;2 etc...
				boolean eol = false;
				int startIndex = 0;
				int endIndex = -1;
				String pointName = "";
				String pointStatus = "";
				String pointLatitude;
				String pointLongitude;

				while (!eol) {
					startIndex = endIndex + 1;
					endIndex = line.indexOf(";", startIndex);
					pointName = line.substring(startIndex, endIndex);
					if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
						Log.d(Constants.LOGTAG, " "
								+ MapsMainFragmentList.CLASSTAG
								+ " startIndex=" + startIndex + " endIndex="
								+ endIndex + " Name=" + pointName);
					}

					startIndex = endIndex + 1;
					endIndex = line.indexOf(";", startIndex);
					pointStatus = line.substring(startIndex, endIndex);
					if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
						Log.d(Constants.LOGTAG, " "
								+ MapsMainFragmentList.CLASSTAG
								+ " startIndex=" + startIndex + " endIndex="
								+ endIndex + " Status=" + pointStatus);
					}
					
					startIndex = endIndex + 1;
					endIndex = line.indexOf(";", startIndex);
					pointLatitude = line.substring(startIndex, endIndex);
					if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
						Log.d(Constants.LOGTAG, " "
								+ MapsMainFragmentList.CLASSTAG
								+ " startIndex=" + startIndex + " endIndex="
								+ endIndex + " Latitude=" + pointLatitude);
					}
					
					startIndex = endIndex + 1;
					endIndex = line.indexOf("|", startIndex);
					if (endIndex == -1) {
						pointLongitude = line.substring(startIndex);
						// pointLongitude = pointStatus.trim();
						eol = true;
					} else {
						pointLongitude = line.substring(startIndex, endIndex);
					}
					if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
						Log.d(Constants.LOGTAG, " "
								+ MapsMainFragmentList.CLASSTAG
								+ " startIndex=" + startIndex + " endIndex="
								+ endIndex + " Longitude=" + pointLongitude);
					}

					updatePointForStatus(pointName, pointStatus, pointLatitude, pointLongitude);

					if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
						Log.d(Constants.LOGTAG, " "
								+ MapsMainFragmentList.CLASSTAG
								+ " getAllPointsStatusFromServer name="
								+ pointName + " status=" + pointStatus);
					}

					if (startIndex == -1) {
						eol = true;
					}
				}
			}

			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " exception cause: " + e.getCause());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " exception message: " + e.getMessage());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " exception desc: " + e.toString());

				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " Connectivity Issue");
			}
			mApiErrorMsg = getResources().getString(
					R.string.checknetworkconnection_msg);
		}

		return 0;
	}
	
	private void updateAllPointsForStatus() {
		if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " updateAllPointsForStatus - enter");
		}

		Iterator<PointItem> it = mPointList.iterator();
		PointItem item;
		while (it.hasNext()) {
			item = it.next();
			// KRC v0.9.60 22Feb2015
			if (item.marker != null) {
				item.marker.remove();
			}
			else if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " updateAllPointsForStatus - item.marker is null!!");
			}
			
			// v0.9.54
			double lati = Double.parseDouble(item.latitude);
			double lngi = Double.parseDouble(item.longitude);
			item.mo = new MarkerOptions().position(new LatLng(lati, lngi));
			
			item.setSurveyStatus(item.surveyStatus);
			
			// v0.9.55 - set infowindow title and set anchor point of marker to middle of my icon
			item.mo.title("Point " + item.name + " @ " + item.streetAddr + ", " + item.city);
			item.mo.anchor(0.5f,  0.5f);
			
			item.marker = googleMap.addMarker(item.mo);
			
			// v0.9.54
			item.marker.setPosition(new LatLng(lati, lngi));
		}
	}

	private void updatePointForStatus(final String pointName, final String pointStatus,
									  final String pointLatitude, final String pointLongitude) {
		if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " updatePointForStatus enter - Name=" + pointName
					+ " Status='" + pointStatus + "'"
					+ " Latitude='" + pointLatitude + "'"
					+ " Longitude='" + pointLongitude + "'");
		}
			
		Iterator<PointItem> it = mPointList.iterator();
		PointItem item;
		while (it.hasNext()) {
			item = it.next();
			if (item.name.equals(pointName)) {
				if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " updatePointForStatus - item found, point="
							+ pointName + " status=" + pointStatus);
				}

				if (item.marker != null) {
					// item.marker.remove();
				}

				// 23Apr2015 - Update the point's location based on where the server thinks the survey was conducted
				// 10Jun2015 v0.9.52 - Check if Lat/Lon are null before updating
				if (!pointLatitude.equals("")) {
					item.latitude  = pointLatitude;
				}
				if (!pointLongitude.equals("")) {
					item.longitude = pointLongitude;
				}
				
				// v0.9.53 - tried this, but it causes an exception on main thread!!
				// @Todo use backend of async task to update map markers?
				/*********************
				if (item.marker != null) {
					double lati = Double.parseDouble(item.latitude);
					double lngi = Double.parseDouble(item.longitude);
					LatLng ll = new LatLng(lati, lngi);
					item.mo.position(ll);
					item.marker.setPosition(ll);
				}
				**********************/
				/******************************** exception, need to update Map on backside of asynctask
				double lati = Double.parseDouble(item.latitude);
				double lngi = Double.parseDouble(item.longitude);
				item.mo = new MarkerOptions().position(new LatLng(lati, lngi));
				item.marker = googleMap.addMarker(item.mo);
				item.marker.setPosition(new LatLng(lati, lngi));
				*********************************/
				
				
				if (pointStatus.equals("0")) {
					item.setSurveyStatus(Constants.POINT_STATUS_COMPLETED);
				} else if (pointStatus.equals("1")) {
					item.setSurveyStatus(Constants.POINT_STATUS_INPROGRESS);
				} else if (pointStatus.equals("2")) {
					item.setSurveyStatus(Constants.POINT_STATUS_NOTSTARTED);
				} else if (pointStatus.equals("3")) {
					item.setSurveyStatus(Constants.POINT_STATUS_PAUSED);
				} else if (pointStatus.equals("4")) {
					item.setSurveyStatus(Constants.POINT_STATUS_ERROR);
				} else {
					item.setSurveyStatus(Constants.POINT_STATUS_UNKNOWN);
				}
				
				// item.marker = googleMap.addMarker(item.mo); // Place the marker on the map
				// it.set(item); // now set the item back in the array list

				return;
			}
		}

		if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " updatePointForStatus - item NOT found!!");
		}
	}

	
	
	private void updatePointForStatus(final String pointName, final int pointStatus) {
		if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " updatePointForStatus enter - Name=" + pointName
					+ " Status='" + pointStatus);
		}
		
		Iterator<PointItem> it = mPointList.iterator();
		PointItem item;
		while (it.hasNext()) {
			item = it.next();
			if (item.name.equals(pointName)) {
				if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " updatePointForStatus - item found, point="
							+ pointName + " status=" + pointStatus);
				}

				if (item.marker != null) {
					// item.marker.remove();
				}

				item.setSurveyStatus(pointStatus);
				return;
			}
		}

		if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " updatePointForStatus - item NOT found!!");
		}
	}
	

	private int writeAllPointsToDb() {
		if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
			Log.v(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " writeAllPointsToDb - Enter");
		}
		
		// Create or Open the Database
		try {
			epiDB = getActivity().openOrCreateDatabase(Constants.EPI_DB_NAME, getActivity().getApplicationContext().MODE_PRIVATE, null);
		 

			// Create the Points Table in the Database
			epiDB.execSQL("CREATE TABLE IF NOT EXISTS "
					+ Constants.POINTS_TABLE_NAME
					+ " (ClusterField1 VARCHAR, PointField2 VARCHAR, StatusField3 INT(1), LatField4, LonField5);");
			
			// delete all entries
			epiDB.execSQL("delete from " + Constants.POINTS_TABLE_NAME);
 			epiDB.execSQL("vacuum");    // clear all allocated spaces
 			
 			// add allentries
			Iterator<PointItem> it = mPointList.iterator();
			PointItem item;
			while (it.hasNext()) {
				item = it.next();
				
				epiDB.execSQL("INSERT INTO " 
				              + Constants.POINTS_TABLE_NAME
				              + " (ClusterField1, PointField2, StatusField3, LatField4, LonField5)"
				              + " VALUES ('" + item.getClusterId() + "', '" + item.getPointId() + "', '" + item.surveyStatus + "', '" 
				                             + item.latitude + "', '" + item.longitude + "');");
				
				if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " writeAllPointsToDb - record Inserted");
				}
			}
		}
		catch(Exception e) {
			Log.e("Error", "Error", e);
		}
		finally {
			if (epiDB != null) {
				epiDB.close();
			}
		}

		return 0;
	}
	
	
	private int readAllPointsFromDb() {
		if (Constants.LOGS_ENABLED_DATABASE) {
			Log.v(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " readAllPointsFromDb - Enter");
			Log.v(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " ^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			Log.v(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " ^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
		
		// Create or Open the Database
		try {
			epiDB = getActivity().openOrCreateDatabase(Constants.EPI_DB_NAME, getActivity().getApplicationContext().MODE_PRIVATE, null);
		 

			// Create the Points Table in the Database
			epiDB.execSQL("CREATE TABLE IF NOT EXISTS "
							+ Constants.POINTS_TABLE_NAME
							+ " (ClusterField1 VARCHAR, PointField2 VARCHAR, StatusField3 INT(1), LatField4, LonField5);");
			
			// retrieve all Points from database
			Cursor c = epiDB.rawQuery("SELECT * FROM " + Constants.POINTS_TABLE_NAME , null);
			int Column1 = c.getColumnIndex("ClusterField1");
			int Column2 = c.getColumnIndex("PointField2");
			int Column3 = c.getColumnIndex("StatusField3");
			int Column4 = c.getColumnIndex("LatField4");
			int Column5 = c.getColumnIndex("LonField5");
			
			c.moveToFirst();
			if (c != null) {
				// Loop through all Results
				do {
					String clusterVal = c.getString(Column1);
					String pointVal   = c.getString(Column2);
					int    statusVal  = c.getInt(Column3);
					String latVal     = c.getString(Column4);
					String lonVal     = c.getString(Column5);
					
					if (Constants.LOGS_ENABLED_DATABASE) {
						Log.v(Constants.LOGTAG, " readAllPointsFromDb:"
								+ " Cluster = " + clusterVal
								+ " Point = "   + pointVal
								+ " Status = "  + statusVal
								+ " Lat = "     + latVal
								+ " Lon = "     + lonVal);
					}
					
					// find point item
					PointItem item = findPointItem(clusterVal + "-" + pointVal);
					if (item != null) {
						if (item.marker != null) {
							item.marker.remove();
						}
						
						item.surveyStatus = statusVal;
						item.latitude     = latVal;
						item.longitude    = lonVal;
					}
				} while(c.moveToNext());
			}
		}
		catch(Exception e) {
			Log.e("Error", "Error", e);
		}
		finally {
			if (epiDB != null) {
				epiDB.close();
			}
		}

		return 0;
	}
	
	
	private void startUpdateUserStatusTimer() {
		if (Constants.LOGS_ENABLED_TIMER) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startUpdateUserStatusTimer - Enter");
		}
		int delay = 120000;   // v0.9.63 dealy for 120 seconds  // 60000; // delay for 60 seconds
		// int period = 10000; // repeat every 10 seconds
		Timer timer = new Timer();
		// timer.scheduleAtFixedRate(new TimerTask() {
		timer.schedule(new TimerTask() { // one shot timer
			public void run() {
				if (Constants.LOGS_ENABLED_TIMER) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " startUpdateUserStatusTimer - run()");
				}
				updateUserStatusAsync();
			}
			// }, delay, period);
		}, delay);
	}

	public void stopUpdateUserStatusTimer() {
		// bUpdateUserStatusActive = false;
	}

	private void updateUserStatusAsync() {
		if (Constants.LOGS_ENABLED_TIMER) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " updateUserStatusAsync - Enter");
		}

		// Create an AsyncTask class to send the user's status to the server
		// with methods to override doInBackground() and onPostExecute()
		UploadUserStatusAsyncTask task = new UploadUserStatusAsyncTask();
		String httpGetCmd = "";
		task.execute(new String[] { httpGetCmd.toString() });
	}

	private class UploadUserStatusAsyncTask extends
			AsyncTask<String, Void, String> {
		
		private boolean bRestartTimer;
		
		@Override
		protected String doInBackground(String... urls) {
			// runs in non-UI thread
			if (Constants.LOGS_ENABLED_TIMER) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " UploadUserStatusAsyncTask:doInBackground - Enter");
			}
			String response;
			int retVal = 0;
			
			// v0.9.53
			// V0.9.63
			bRestartTimer = false;
			Activity activity = getActivity();
			if (activity != null) {
				Context context = activity.getApplicationContext();
				if (context != null) {
					bRestartTimer = true;  // Activity is still running
					if (UncEpiSettings.IsNetworkAvailable(getActivity().getApplicationContext())) {
						retVal = uploadUserStatusToServer();  // should return 0 or an error code
					}
					else {
						if (Constants.LOGS_ENABLED_TIMER) {
							Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " UploadUserStatusAsyncTask:doInBackground - Network Not Available");
						}
					}
				}
				else {
					if (Constants.LOGS_ENABLED_TIMER) {
						Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " UploadUserStatusAsyncTask:doInBackground - Context is null");
					}
				}
			}
			else {
				if (Constants.LOGS_ENABLED_TIMER) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " UploadUserStatusAsyncTask:doInBackground - Activity is null");
				}	
			}
			
			if (retVal == 0) {
				response = "0";
			} else {
				response = "-1";
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// update screen in UI thread
			if (Constants.LOGS_ENABLED_TIMER) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " UploadUserStatusAsyncTask:onPostExecute - Enter");
			}
			// v0.9.63 30Sep2015
			if (bRestartTimer) {
				startUpdateUserStatusTimer();
			}
		}
	}

	private int uploadUserStatusToServer() {
		if (Constants.LOGS_ENABLED_TIMER) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " uploadUserStatusToServer - Enter");
		}

		// Format CURL string to login the user
		BufferedReader in = null;

		try {
			String myCluster;
			String myPoint;

			if (mClusterSelected.length() < 1) {
				myCluster = "0";
			} else {
				myCluster = mClusterSelected;
			}
			if (mPointSelected.length() < 1) {
				myPoint = "0";
			} else {
				myPoint = mPointSelected;
			}

			// e.g.
			// http://50.16.195.50/epiinfo/api/epi_api.php?op=epiStatusUpdate&p1=one&p2=1|1|10|35.3245|-78.5612
			// p1=username
			// p2=SurveyId,ClusterId,PointId,Latitude,longitude

			/****************************************** 30Aug2015 v0.9.62
			HttpClient client = new DefaultHttpClient();
			String httpGetCmd = Constants.EPI_API_PREFIX + "op=epiStatusUpdate"
					+ "&p1=" + UncEpiSettings.username + "&p2="
					+ UncEpiSettings.selectedSurveyItem.title + "%7C"
					+ myCluster + "%7C" + myPoint + "%7C"
					+ UncEpiSettings.latitude + "%7C"
					+ UncEpiSettings.longitude + "%7C"
					//*********** 22Apr2015 - Add coordinator field
					+ UncEpiSettings.coordinator;
			******************************************/
			
			// v0.9.63 30Sep2015
			/****************** wrong command ******
			URL getCmdUrl = new URL(Constants.EPI_API_PREFIX
					+ "op=epiGetAllPointsStatus&p1=" + UncEpiSettings.username
					+ "&p2=" + UncEpiSettings.selectedSurveyItem.title
					+ "%7C" + UncEpiSettings.coordinator);
			*********************************/
			URL getCmdUrl = new URL(Constants.EPI_API_PREFIX
							+ "op=epiStatusUpdate"
							+ "&p1=" + UncEpiSettings.username + "&p2="
							+ UncEpiSettings.selectedSurveyItem.title + "%7C"
							+ myCluster + "%7C"
							+ myPoint + "%7C"
							+ UncEpiSettings.latitude + "%7C"
							+ UncEpiSettings.longitude + "%7C"
							+ UncEpiSettings.coordinator);
			
			HttpURLConnection urlConnection = (HttpURLConnection) getCmdUrl.openConnection();
			urlConnection.setConnectTimeout(Constants.URL_CONNECTION_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(Constants.URL_CONNECTION_READ_TIMEOUT);
			
			if (Constants.LOGS_ENABLED_TIMER) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + "httpGetCmd = " + getCmdUrl.toString());
			}
			
			/************************************** 30Aug2015 v0.9.62
			HttpGet method = new HttpGet(httpGetCmd);
			HttpResponse response = client.execute(method);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			***************************************/
			in = new BufferedReader (new InputStreamReader(urlConnection.getInputStream()));
			
			String line = "";
			if ((line = in.readLine()) != null) {
				if (Constants.LOGS_ENABLED_TIMER) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " in: " + line);
				}
				if (line.indexOf("0") != -1) {
					return 0;
				}
			}

			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			if (Constants.LOGS_ENABLED_TIMER) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " exception cause: " + e.getCause());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG	+ " exception message: " + e.getMessage());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " exception desc: " + e.toString());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + " Connectivity Issue");
			}
		}

		return -1; // error
	}

	private void setPointStatusOnServerAsync(final PointItem pItem) {
		if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setPointStatusOnServerAsync - Enter");
		}

		// Create an AsyncTask class to inform the server of the point status
		// change
		// with methods to override doInBackground() and onPostExecute()

		// V0.9.63 - Don't change a Point's Lat/Lon when starting Survey
		String httpGetCmd;
		if ((pItem.surveyStatus == Constants.POINT_STATUS_INPROGRESS) || 
		    (pItem.surveyStatus == Constants.POINT_STATUS_NOTSTARTED)) {
			httpGetCmd = Constants.EPI_API_PREFIX + "op=epiSetPointStatus"
					+ "&p1=" + UncEpiSettings.username + "&p2="
					+ UncEpiSettings.selectedSurveyItem.title + "%7C" + pItem.name
					+ "%7C" + pItem.surveyStatus
					// ************ 22Apr2015 - Add latitude, longitude, coordinator fields
					+ "%7C" + pItem.latitude  // V0.9.63
					+ "%7C" + pItem.longitude // V0.9.63
					+ "%7C" + UncEpiSettings.coordinator;
		}
		else {
			httpGetCmd = Constants.EPI_API_PREFIX + "op=epiSetPointStatus"
					+ "&p1=" + UncEpiSettings.username + "&p2="
					+ UncEpiSettings.selectedSurveyItem.title + "%7C" + pItem.name
					+ "%7C" + pItem.surveyStatus
					// ************ 22Apr2015 - Add latitude, longitude, coordinator fields
					+ "%7C" + String.valueOf(UncEpiSettings.latitude)
					+ "%7C" + String.valueOf(UncEpiSettings.longitude)
					+ "%7C" + UncEpiSettings.coordinator;
		}

		if (Constants.LOGS_ENABLED_GETALLPOINTSSTATUS) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " httpgetCmd = " + httpGetCmd);
		}
		
		// v0.9.54
		String ptStatus;
		if (pItem.surveyStatus == Constants.POINT_STATUS_INPROGRESS)
			ptStatus = "1";
		else if (pItem.surveyStatus == Constants.POINT_STATUS_NOTSTARTED)
			ptStatus = "2";
		else
			ptStatus = "0";  // COMPLETED
		    
		SetPointStatusOnServerAsyncTask task = new SetPointStatusOnServerAsyncTask();
		// V0.9.63 - Don't change a Point's Lat/Lon when starting Survey
		if ((pItem.surveyStatus == Constants.POINT_STATUS_INPROGRESS) ||
		    (pItem.surveyStatus == Constants.POINT_STATUS_NOTSTARTED)) {
			task.execute(new String[] { httpGetCmd.toString(), 
				                    	pItem.getClusterId(),
				                    	pItem.getPointId(),
				                    	ptStatus,
				                    	pItem.latitude,     // V0.9.63
				                    	pItem.longitude }); // V0.9.63
		}
		else {
			task.execute(new String[] { httpGetCmd.toString(), 
                						pItem.getClusterId(),
                						pItem.getPointId(),
                						ptStatus,
                						String.valueOf(UncEpiSettings.latitude),
                						String.valueOf(UncEpiSettings.longitude) });
		}
	}

	private class SetPointStatusOnServerAsyncTask extends
			AsyncTask<String, Void, String> {
		
		String clusterStr;
		String pointStr;
		String statusStr;
		String latStr;
		String lonStr;
		
		@Override
		protected String doInBackground(String... urls) {
			// runs in non-UI thread
			if (Constants.LOGS_ENABLED3) {
				Log.d(Constants.LOGTAG,
						" "
								+ MapsMainFragmentList.CLASSTAG
								+ " SetPointStatusOnServerAsyncTask:doInBackground - Enter");
			}

			// v0.9.54
			String cmd = urls[0];
			clusterStr = urls[1];
			pointStr   = urls[2];
			statusStr  = urls[3];
			latStr     = urls[4];
			lonStr     = urls[5];
			
			int retVal = 0;
			// 29Jul2015 v0.9.60 - Check if network connection is active before issuing http API
			if (UncEpiSettings.IsNetworkAvailable(getActivity().getApplicationContext())) {
				retVal = setPointStatusOnServer(cmd); // should return 0 or an error code
			}
			else {
				// @TODO - might need to write the point status to DB if it is completed????
			}
			if (retVal == 0) {
				return "0";
			}
			else {
				return "-1";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			// update screen in UI thread
			if (Constants.LOGS_ENABLED3) {
				Log.d(Constants.LOGTAG,
						" "
								+ MapsMainFragmentList.CLASSTAG
								+ " SetPointStatusOnServerAsyncTask:onPostExecute - Enter");
			}
			// v0.9.53 - @ToDo do we want this here??? It only belongs in user status, not point status
			// if (bUpdateUserStatusActive) {
			//	startUpdateUserStatusTimer(); // restart timer
			// }
			
			// v0.9.54
			PointItem item = findPointItem(clusterStr + "-" + pointStr);
			if (item != null) {
				if (item.marker != null) {
					item.marker.remove();
				}
				
				if (statusStr.equals("1"))
					item.surveyStatus = Constants.POINT_STATUS_INPROGRESS;
				else if (statusStr.equals("2"))
					item.surveyStatus = Constants.POINT_STATUS_NOTSTARTED;
				else
					item.surveyStatus = Constants.POINT_STATUS_COMPLETED;
					
				double lati = Double.parseDouble(latStr);
				double lngi = Double.parseDouble(lonStr);
				item.mo = new MarkerOptions().position(new LatLng(lati, lngi));		
				item.setSurveyStatus(item.surveyStatus);
				
				// v0.9.55 - set infowindow title and set anchor point of marker to middle of my icon
				item.mo.title("Point " + item.name + " @ " + item.streetAddr + ", " + item.city);
				item.mo.anchor(0.5f,  0.5f);
				
				item.marker = googleMap.addMarker(item.mo);
				item.marker.setPosition(new LatLng(lati, lngi));
			}
		}
	}

	private int setPointStatusOnServer(final String pHttpGetCmd) {
		if (Constants.LOGS_ENABLED3) {
			Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
					+ " setPointStatusOnServer - Enter");
		}

		// Format CURL string to set the Point Status on the user
		BufferedReader in = null;

		try {
			// e.g.
			// http://50.16.195.50/epiinfo/api/epi_api.php?op=epiStatusUpdate&p1=one&p2=1|1|10|35.3245|-78.5612
			// p1=username
			// p2=SurveyId,ClusterId,PointId,Latitude,longitude

			if (Constants.LOGS_ENABLED3) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG + "pHttpGetCmd = " + pHttpGetCmd);
			}
			
			/********************************** 30Aug2015 v0.9.62
			HttpClient client = new DefaultHttpClient();
			HttpGet method = new HttpGet(pHttpGetCmd);
			HttpResponse response = client.execute(method);
			
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			************************************/
			
			URL getCmdUrl = new URL(pHttpGetCmd);
			HttpURLConnection urlConnection = (HttpURLConnection) getCmdUrl.openConnection();
			urlConnection.setConnectTimeout(Constants.URL_CONNECTION_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(Constants.URL_CONNECTION_READ_TIMEOUT);
			
			in = new BufferedReader (new InputStreamReader(urlConnection.getInputStream()));
			
			String line = "";
			if ((line = in.readLine()) != null) {
				if (Constants.LOGS_ENABLED3) {
					Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
							+ " in: " + line);
				}
				if (line.indexOf("0") != -1) {
					return 0;
				}
			}

			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " exception cause: " + e.getCause());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " exception message: " + e.getMessage());
				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " exception desc: " + e.toString());

				Log.d(Constants.LOGTAG, " " + MapsMainFragmentList.CLASSTAG
						+ " Connectivity Issue");
			}
		}

		return -1; // error
	}


	private class ReadClusterPointFilesAsyncTask extends
			AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			// runs in non-UI thread
			if (Constants.LOGS_ENABLED) {
				Log.d(Constants.LOGTAG,
						" "
								+ MapsMainFragmentList.CLASSTAG
								+ " ReadClusterPointFilesAsyncTask:doInBackground - Enter");
			}
			String response = "0";

			readClusterListFile();
			readPointListFile();

			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// update screen in UI thread
			if ((Constants.LOGS_ENABLED) || (Constants.LOGS_ENABLED4)) {
				Log.d(Constants.LOGTAG,
						" "
								+ MapsMainFragmentList.CLASSTAG
								+ " ReadClusterPointFilesAsyncTask:onPostExecute - Enter");
			}
			setInitialMapCoordinates();
			setPointListMarkersInit();
			createAllClusterPolygons();
			setClustersListView();
			getAllPointsStatusAsync();
		}
	}

	// v0.9.57 - fix compiler warning
	public class SortClustersBasedOnName implements Comparator<Object> {
	// public class SortClustersBasedOnName implements Comparator {
		public int compare(Object o1, Object o2) {

			ClusterItem dd1 = (ClusterItem) o1; // where ClusterItem is your
												// object class
			ClusterItem dd2 = (ClusterItem) o2;
			return dd1.name.compareToIgnoreCase(dd2.name); // where name is
															// field name
		}
	}

	public class SortClustersBasedOnLength implements Comparator<Object> {
	// public class SortClustersBasedOnName implements Comparator {
		public int compare(Object o1, Object o2) {
			ClusterItem dd1 = (ClusterItem) o1; // where ClusterItem is your
												// object class
			ClusterItem dd2 = (ClusterItem) o2;
			if (dd1.name.length() == dd2.name.length())
				return 0;
			else if (dd1.name.length() < dd2.name.length())
				return -1;
			else
				return 1;
		}
	}

	public class SortPointsBasedOnCluster implements Comparator<Object> {
	// public class SortPointsBasedOnCluster implements Comparator {
		public int compare(Object o1, Object o2) {
			PointItem dd1 = (PointItem) o1; // where ClusterItem is your object
											// class
			PointItem dd2 = (PointItem) o2;
			return dd1.getClusterId().compareToIgnoreCase(dd2.getClusterId());
		}
	}

	public class SortPointsBasedOnClusterLength implements Comparator<Object> {
	// public class SortPointsBasedOnClusterLength implements Comparator {
		public int compare(Object o1, Object o2) {
			PointItem dd1 = (PointItem) o1; // where PointItem is your object
											// class
			PointItem dd2 = (PointItem) o2;
			if (dd1.getClusterId().length() == dd2.getClusterId().length())
				return 0;
			else if (dd1.getClusterId().length() < dd2.getClusterId().length())
				return -1;
			else
				return 1;
		}
	}

	public class SortPointsBasedOnPoint implements Comparator<Object> {
	// public class SortPointsBasedOnPoint implements Comparator {
		public int compare(Object o1, Object o2) {
			PointItem dd1 = (PointItem) o1;
			PointItem dd2 = (PointItem) o2;
			if (dd1.getClusterId().equals(dd2.getClusterId())) {
				return dd1.getPointId().compareToIgnoreCase(dd2.getPointId());
			} else
				return 0;
		}
	}

	public class SortPointsBasedOnPointLength implements Comparator<Object> {
	// public class SortPointsBasedOnPointLength implements Comparator {
		public int compare(Object o1, Object o2) {
			PointItem dd1 = (PointItem) o1; // where PointItem is your object
											// class
			PointItem dd2 = (PointItem) o2;
			if (dd1.getPointId().length() == dd2.getPointId().length())
				return 0;
			else if (dd1.getPointId().length() < dd2.getPointId().length())
				return -1;
			else
				return 1;
		}
	}
}