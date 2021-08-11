package com.epiinfo.unc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * The class UncEpiSettings contains the EPI Info App settings, 
 * including user preferences. These values are stored persistently 
 * in the file system using the Android Shared Preferences feature.
 * This file should be re-usable for different transit companies.  
 * 
 * A separate class, SettingsActivity is used to view and edit 
 * these values from the Settings menu.
 * 
 * @author keithcollins
 */

public class UncEpiSettings {

	private static final String CLASSTAG = UncEpiSettings.class.getSimpleName();
	
	public static boolean exitAppFlag;  // used to force activities to close
	
	public static boolean logoutRequest = false;  // used to force Main Menu to exit/finish
	
	// public static boolean acceptDisclaimer = false;
	// public static boolean acceptEula = false;
	
	public static String  username;         // minimum 3 alphanumeric characters
	public static String  password;         // minimum 3 alphanumeric characters
	public static String  coordinator;      // minimum 3 alphanumeric characters
	public static boolean autoLogin;        // auto login when app starts
	public static boolean loginVerified;    // true if user has previously logged in using the username/password/coordinator values
	
	public static boolean speechEnabled;     // control for text-to-speech features
	
	// public static boolean screenPowerSaveEnabled;
	// public static boolean locationPowerSaveEnabled;
	
	// v0.9.53 17Jun2015 - Add to control location accuracy and battery usage
	public static boolean locationFineEnabled;
	public static boolean locationCoarseEnabled;
	
	// Nexus 7 is 1280x800
	public static int screenWidth;   // physical screen width in pixels
    public static int screenHeight;  // physical screen height in pixels
    
    // public static String twitterUsername;  // e.g. keith@skyhighways.com
	// public static String twitterPassword;    // minimum 8 alphanumeric characters
    
    public static double latitude  = 0;
    public static double longitude = 0;
    
    private static boolean bPointStatusEntriesAllocated = false;
    private static ArrayList<PointStatusItem> mPointStatusList = null;
    private static final int MAX_SAVED_POINT_STATUS_ENTRIES = 20;

    public static ArrayList<SurveyItem> mSurveyList = null;
    public static SurveyItem selectedSurveyItem;
    
    // v0.9.64
    public static String origSurveyCreateDate = "";
    
    // v0.9.48
    public static PointItem selectedPointItem;
    public static String tempPointName = "";
    
    // v0.9.57
    public static String previousSurveyTitle;
    public static boolean pointQuestionnaireSaved = false;
    
    // v0.9.61
    public static boolean uploadFileResultSuccess = false;
    
	/**
     * Read stored user settings from shared preferences.
     */
    public static void readSettings(SharedPreferences preferences) {
    	if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
    		Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " readSettings - Enter");
    	}
    	
    	// UncEpiSettings.acceptDisclaimer = preferences.getBoolean("acceptDisclaimer", false);
		// UncEpiSettings.acceptEula = preferences.getBoolean("acceptEula", false);
    	UncEpiSettings.username = preferences.getString("username", "");
    	UncEpiSettings.password = preferences.getString("password", "");
    	UncEpiSettings.coordinator = preferences.getString("coordinator", "");
    	UncEpiSettings.autoLogin = preferences.getBoolean("autoLogin", false);

    	UncEpiSettings.speechEnabled = preferences.getBoolean("speechEnabled", true);
    	// UncEpiSettings.screenPowerSaveEnabled = preferences.getBoolean("screenPowerSaveEnabled", true);
    	// UncEpiSettings.locationPowerSaveEnabled = preferences.getBoolean("locationPowerSaveEnabled", false);

    	UncEpiSettings.locationFineEnabled   = preferences.getBoolean("locationFineEnabled", true);
    	UncEpiSettings.locationCoarseEnabled = preferences.getBoolean("locationCoarseEnabled", true);
    	
    	// UncEpiSettings.twitterUsername = preferences.getString("twitterUsername", "");
    	// UncEpiSettings.twitterPassword = preferences.getString("twitterPassword", "");
    	
    	selectedSurveyItem = new SurveyItem();
    	selectedSurveyItem.id = "0";
    	selectedSurveyItem.surveyFilesFolderName = preferences.getString("surveyFilesFolderName", "");
    	
    	// V0.9.64
    	selectedSurveyItem.stagingAreaAddr = "";
    	selectedSurveyItem.stagingAreaLatitude = 0;
    	selectedSurveyItem.stagingAreaLongitude = 0;
    	
    	// 08Jun2015 v0.9.52 add more fields to storage to support offline mode
    	UncEpiSettings.loginVerified                       = preferences.getBoolean("loginVerified", false);
		UncEpiSettings.selectedSurveyItem.id               = preferences.getString("surveyItemId", "");
		UncEpiSettings.selectedSurveyItem.title            = preferences.getString("surveyItemTitle", "");
		UncEpiSettings.selectedSurveyItem.createDate       = preferences.getString("surveyItemCreateDate", "");
		UncEpiSettings.selectedSurveyItem.clustersFilename = preferences.getString("surveyItemClustersFilename", "");
		UncEpiSettings.selectedSurveyItem.pointsFilename   = preferences.getString("surveyItemPointsFilename", "");
		UncEpiSettings.selectedSurveyItem.formFilename     = preferences.getString("surveyItemFormFilename", "");
		
		UncEpiSettings.origSurveyCreateDate = preferences.getString("surveyItemCreateDate", "");  // v0.9.64

		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			// Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " acceptDisclaimer: " + UncEpiSettings.acceptDisclaimer);
			// Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " acceptEula: " + UncEpiSettings.acceptEula);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " username: " + UncEpiSettings.username);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " password: " + UncEpiSettings.password);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " coordinator: " + UncEpiSettings.coordinator);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " autoLogin: " + UncEpiSettings.autoLogin);
			
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " locationFineEnabled: " + UncEpiSettings.locationFineEnabled);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " locationCoarseEnabled: " + UncEpiSettings.locationCoarseEnabled);
			
			// 08Jun2015 v0.9.52 add more fields to storage to support offline mode
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " loginVerified: " + UncEpiSettings.loginVerified);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " surveyItem.id: " + UncEpiSettings.selectedSurveyItem.id);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " surveyitem.title: " + UncEpiSettings.selectedSurveyItem.title);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " surveyitem.createDate: " + UncEpiSettings.selectedSurveyItem.createDate);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " surveyitem.clustersFilename: " + UncEpiSettings.selectedSurveyItem.clustersFilename);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " surveyItem.pointsFilename: " + UncEpiSettings.selectedSurveyItem.pointsFilename);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " surveyItem.formFilename: " + UncEpiSettings.selectedSurveyItem.formFilename);
			
			// Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " speechEnabled: " + UncEpiSettings.speechEnabled);
			// Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " screenPowerSaveEnabled: " + UncEpiSettings.screenPowerSaveEnabled);
			// Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " locationPowerSaveEnabled: " + UncEpiSettings.locationPowerSaveEnabled);

			// Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " twitterUsername: " + UncEpiSettings.twitterUsername);
			// Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " twitterPassword: " + UncEpiSettings.twitterPassword);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " screenHeight: " + UncEpiSettings.screenHeight);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " screenWidth: " + UncEpiSettings.screenWidth);
			Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " readSettings - Exit");
		}
    }
    
    /*
     * Write user settings to shared preferences.
     */
	public static void SaveSettings(SharedPreferences preferences) {
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveSettings - Enter");
		}
		SharedPreferences.Editor editor = preferences.edit();
		// editor.putBoolean("acceptDisclaimer", acceptDisclaimer);
		// editor.putBoolean("acceptEula", acceptEula);
		editor.putString("username", username);
		editor.putString("password", password);
		editor.putString("coordinator", coordinator);
		editor.putBoolean("autoLogin", autoLogin);
		
		editor.putBoolean("locationFineEnabled", locationFineEnabled);
		editor.putBoolean("locationCoarseEnabled", locationCoarseEnabled);
		
		// 08Jun2015 v0.9.52 add more fields to storage to support offline mode
		editor.putBoolean("loginVerified", loginVerified);
		editor.putString("surveyItemId", UncEpiSettings.selectedSurveyItem.id);
		editor.putString("surveyItemTitle", UncEpiSettings.selectedSurveyItem.title);
		editor.putString("surveyItemCreateDate", UncEpiSettings.selectedSurveyItem.createDate);
		editor.putString("surveyItemClustersFilename", UncEpiSettings.selectedSurveyItem.clustersFilename);
		editor.putString("surveyItemPointsFilename", UncEpiSettings.selectedSurveyItem.pointsFilename);
		editor.putString("surveyItemFormFilename", UncEpiSettings.selectedSurveyItem.formFilename);
		
		editor.putBoolean("speechEnabled", speechEnabled);
		
		// editor.putBoolean("screenPowerSaveEnabled", screenPowerSaveEnabled);
		// editor.putBoolean("locationPowerSaveEnabled", locationPowerSaveEnabled);
		
		editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveSettings - Exit");
		}
    }
	
	public static void SaveLoginVerifiedSetting(SharedPreferences preferences, Boolean value) {
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveLoginVerifiedSetting - Enter");
		}
		loginVerified = value;
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("loginVerified", loginVerified);
		editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveLoginVerifiedSetting - Exit");
		}
    }
	
	/**************************************************************************************
	public static void SaveDisclaimerSetting(SharedPreferences preferences) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveDisclaimerSetting - Enter");
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("acceptDisclaimer", acceptDisclaimer);
		editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveDisclaimerSetting - Exit");
		}
    }
    ***************************************************************************************/

	/**************************************************************************************
	public static void SaveEulaSetting(SharedPreferences preferences) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveEulaSetting - Enter");
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("acceptEula", acceptEula);
		editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveDisclaimer511Setting - Exit");
		}
    }
    ***************************************************************************************/
	
	/**************************************************************************************
	public static void SaveTwitterSettings(SharedPreferences preferences) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveTwitterSettings - Enter");
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("twitterUsername", twitterUsername);
		editor.putString("twitterPassword", twitterPassword);
		editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveTwitterSettings - Exit");
		}
	}
	***************************************************************************************/
	
	public static void SaveSurveyFilesFolderNameSetting(SharedPreferences preferences) {
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveSurveyFilesFolderNameSetting - Enter");
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("surveyFilesFolderName", selectedSurveyItem.surveyFilesFolderName);
		editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveSurveyFilesFolderNameSetting - Exit");
		}
	}
    
    
	/****************************************
	public static void SaveSelectedSurveyName(SharedPreferences preferences) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveSelectedSurveyName - Enter");
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("surveyName", surveyName);
		editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SaveSelectedSurveyName - Exit");
		}
    }
    ******************************************/
	
	/*
	 * This method is called by the Location Service Manager when a location change is detected.
	 * The coordinates can be used to update the server of the user's location.
	 */
	public static void UopdateCurrentLocation(Location location) {
    	if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
    		Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " UpdateCurrentLocation - Provider  = " + location.getProvider());
    		Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " UpdateCurrentLocation - Latitude  = " + location.getLatitude());
    		Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " UpdateCurrentLocation - Longitude = " + location.getLongitude());
    	}
    	
    	// @todo KRC - could check if lastProvider == GPS then do not accept Network location update ???
    	
    	latitude = location.getLatitude();
    	longitude = location.getLongitude();
	}
	
	
	
	
	
	private static void AllocatePointStatusEntries() {
		if (!bPointStatusEntriesAllocated) {
			for (int i=0; i<MAX_SAVED_POINT_STATUS_ENTRIES; i++) {
				PointStatusItem ps = new PointStatusItem();
				mPointStatusList.add(ps);
			}
			bPointStatusEntriesAllocated = true;
		}
	}
	
	public static void ResetPointStatusEntries(SharedPreferences preferences) {
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " ResetPointStatusEntries - Enter");
		}
		
		AllocatePointStatusEntries();
		
		SharedPreferences.Editor editor = preferences.edit();
		for (int i=0; i<MAX_SAVED_POINT_STATUS_ENTRIES; i++) {
			String psKey = "ps" + i;
			editor.putString(psKey, "");
		}
		editor.apply();  // do not use commit() to avoid disk/file writes on main thread
	}
	

	public static void ReadPointStatus(SharedPreferences preferences) {
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " ReadPointStatus - Enter");
		}
		
		int i = 0;
		Iterator<PointStatusItem> it = mPointStatusList.iterator();
		while(it.hasNext()) {
			String psKey = "ps" + i;
			++i;
		    it.next().pointStatus = preferences.getString(psKey, "");
		    if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
				Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " ReadPointStatus - psKey=" + psKey + " value=" + preferences.getString(psKey, ""));
			}
		}
	}
	
	
	/**********************************
	private static void UpdatePointStatus(SharedPreferences preferences, String surveyId, String clusterId, String pointId, int status) {
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " UpdatePointStatus - Enter");
		}
		String entryKey = surveyId + "-" + clusterId + "-" + pointId;
		String entryVal = entryKey + "-" + status;
		if (Constants.LOGS_ENABLED_UNCEPISETTINGS) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " UpdatePointStatus - entryKey=" + entryKey + " entryVal=" + entryVal);
		}
		
		// if entry exists then update its value, else update an empty entry
		int i=0;
		Iterator<PointStatusItem> it = mPointStatusList.iterator();
		while(it.hasNext()) {
			String psKey = "ps" + i;
			++i;
		    if (it.next().pointStatus.startsWith(entryKey)) {
		    	SharedPreferences.Editor editor = preferences.edit();
		    	editor.putString(psKey, entryVal);
		    	editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		    	return;
		    }
		}
		
		// if we made it this far then the entry was not found
		i = 0;
		while(it.hasNext()) {
		    if (it.next().pointStatus.length() == 0) {
		    	String psKey = "ps" + i;
		    	SharedPreferences.Editor editor = preferences.edit();
		    	editor.putString(psKey, entryVal);
		    	editor.apply();  // do not use commit() to avoid disk/file writes on main thread
		    	
		    	// @todo KRC - Update the array, or just re-write it using ReadPointStatus() ???
		    	ReadPointStatus(preferences);
		    	return;
		    }
		    ++i;
		}
	}
	

	public static void SetPointStatusNotStarted(SharedPreferences preferences, String surveyId, String clusterId, String pointId) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SetPointStatusNotStarted - Enter");
		}
		UpdatePointStatus(preferences, surveyId, clusterId, pointId, Constants.POINT_STATUS_NOTSTARTED);
	}
	
	public static void SetPointStatusInProgress(SharedPreferences preferences, String surveyId, String clusterId, String pointId) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SetPointStatusInProgress - Enter");
		}
		UpdatePointStatus(preferences, surveyId, clusterId, pointId, Constants.POINT_STATUS_INPROGRESS);
	}
	
	public static void SetPointStatusPaused(SharedPreferences preferences, String surveyId, String clusterId, String pointId) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SetPointStatusPaused - Enter");
		}
		UpdatePointStatus(preferences, surveyId, clusterId, pointId, Constants.POINT_STATUS_PAUSED);
	}
	
	public static void SetPointStatusCompleted(SharedPreferences preferences, String surveyId, String clusterId, String pointId) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SetPointStatusCompleted - Enter");
		}
		UpdatePointStatus(preferences, surveyId, clusterId, pointId, Constants.POINT_STATUS_COMPLETED);
	}
	
	public static void SetPointStatusError(SharedPreferences preferences, String surveyId, String clusterId, String pointId) {
		if (Constants.LOGS_ENABLED) {
			Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SetPointStatusError - Enter");
		}
		UpdatePointStatus(preferences, surveyId, clusterId, pointId, Constants.POINT_STATUS_ERROR);
	}
	*********************************************/
	
	public static SurveyItem findSurveyItem(final String pName) {
    	if (Constants.LOGS_ENABLED5) {
    		Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " findSurveyItem - Enter");
    		Log.v(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " name = " + pName);
    	}
    	
    	Iterator<SurveyItem> it = UncEpiSettings.mSurveyList.iterator();
		while(it.hasNext())
		{
		    SurveyItem item = it.next();
		    if (item.title.equals(pName)) {
		    	if (Constants.LOGS_ENABLED5) {
					Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SurveyListActivity - survey item found");
					Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SurveyListActivity - formFilename = " + item.formFilename);
					Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SurveyListActivity - clustersFilename = " + item.clustersFilename);
					Log.d(Constants.LOGTAG, " " + UncEpiSettings.CLASSTAG + " SurveyListActivity - pointsFilename = " + item.pointsFilename);
				}
		    	return (item);
		    }
		}
		
    	return null;
    }
	
	// v0.9.53
	public static boolean IsNetworkAvailable(Context context) {
	    ConnectivityManager mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (mConnectivityManager != null) {
	    	NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
	    	if (mNetworkInfo != null) {
	    		// v0.9.60
	    		// if (mNetworkInfo.isConnectedOrConnecting()) {
	    		if (mNetworkInfo.isConnected()) {
	    			return true;
	    		}
	    	}
	    }
	    return false;
	    
	    // return (mConnectivityManager != null && mConnectivityManager.getActiveNetworkInfo().isConnectedOrConnecting()) ? true : false;
	}
	
	// v0.9.53
	public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	
	// v0.9.53
	public static String getPointId(final String clusterPointName) {
		if (clusterPointName == "") return "";
		int startIndex = clusterPointName.indexOf("-") + 1;
		return (clusterPointName.substring(startIndex));
	}
	
	// v0.9.53
	public static String getClusterId(final String clusterPointName) {
		if (clusterPointName == "") return "";
		int endIndex = clusterPointName.indexOf("-");
		return (clusterPointName.substring(0, endIndex));
	}
	
}