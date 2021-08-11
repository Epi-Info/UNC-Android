package com.epiinfo.unc;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.epiinfo.droid.R;
import com.epiinfo.droid.RecordList;

// import android.support.v4.app.ListFragment;
// import com.flurry.android.FlurryAgent;

public class MapsMainActivity extends FragmentActivity {

	private static final String CLASSTAG = MapsMainActivity.class.getSimpleName();
	
	// @ToDO KRC - Need to use Action Bar
	private static final int MENU_HELP_ID            = Menu.FIRST;
	private static final int MENU_SETTINGS_ID        = Menu.FIRST + 1;
	private static final int MENU_CURRENTLOCATION_ID = Menu.FIRST + 2;
	private static final int MENU_REFRESHPOINTS_ID   = Menu.FIRST + 3;
	// V0.9.63
	private static final int MENU_STAGINGAREA_ID     = Menu.FIRST + 4;

    // private int displayOrientation;
    
    private MapsMainFragmentList fragmentList;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " handleDisclaimer - Enter");
		}
        
        setContentView(R.layout.maps_main_activity);
        
        // start the GPS Service
        Intent intentGpsService = new Intent(getApplicationContext(), UncLocationService.class);     
        getApplicationContext().startService(intentGpsService);

        if (UncEpiSettings.selectedSurveyItem != null) {
            Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " selectedSurveyItem.title = " + UncEpiSettings.selectedSurveyItem.title);
            setTitle("Map View - " + UncEpiSettings.selectedSurveyItem.title + " Survey");
        }
        else
        {
            setTitle("Map View");
        }
		
    	Toast toast= Toast.makeText(MapsMainActivity.this, "Please Wait - Retrieving Map...", Toast.LENGTH_SHORT);  
		toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
		
    	// Create the list fragment and add it as our sole content
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            fragmentList = new MapsMainFragmentList();
            fm.beginTransaction().add(android.R.id.content, fragmentList).commit();
        }
    }
    
    // Called after onCreate(), and before onResume()
    @Override
    protected void onStart()
    {
    	super.onStart();
    	// FlurryAgent.onStartSession(this, Constants.FLURRY_APP_KEY);
    }

    // Called after onPause()
    @Override
    protected void onStop()
    {
    	super.onStop();		
    	// FlurryAgent.onEndSession(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " onResume - Enter");
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " onDestroy - Enter");
        }
        
        // stop the GPS service
        Intent intentGpsService2 = new Intent(getApplicationContext(), UncLocationService.class);     
        getApplicationContext().stopService(intentGpsService2);
        
        // stop the user status update time
        	fragmentList.stopUpdateUserStatusTimer();
        
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " onDestroy - Exit");
        }
    }
    
   
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " onConfigurationChanged");
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    /**
     * Called when your activity's options menu needs to be created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

    	// We are going to create two menus. Note that we assign them
    	// unique integer IDs, labels from our string resources, and
    	// given them shortcuts.
    	// We are going to create two menus. Note that we assign them
        // unique integer IDs, labels from our string resources, and
        // given them shortcuts.
        setOptionsMenuItems(menu);
        return true;
	}
    
    /**
     * Called right before the activity's option menu is displayed.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.clear();
        setOptionsMenuItems(menu);
        return true;
    }
    
    private void setOptionsMenuItems(Menu menu) {
    	menu.add(0, MENU_HELP_ID, 0, R.string.help_button_label).setShortcut('0', 'h');
    	menu.add(0, MENU_SETTINGS_ID, 0, R.string.settings_button_label).setShortcut('1', 's');
    	menu.add(0, MENU_CURRENTLOCATION_ID, 0, R.string.currentlocation_button_label).setShortcut('2', 'm');
    	menu.add(0, MENU_REFRESHPOINTS_ID, 0, R.string.refreshpoints_button_label).setShortcut('3', 'r');
    	// V0.9.63
    	menu.add(0, MENU_STAGINGAREA_ID, 0, R.string.resetmap_button_label).setShortcut('4', 'a');
    	menu.add(0,90000,0,"Record List").setShortcut('5','l');
    }
    
    /**
     * Called when a menu item is selected.
     */
    @Override
    // public boolean onMenuItemSelected(FeatureId, MenuItem item) {
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        		
        	case MENU_HELP_ID:
        		handleHelpView();
        		return true;
        		
        	case MENU_SETTINGS_ID:
        		handleSettingsView();
        		return true;
        	 
			case MENU_CURRENTLOCATION_ID:
                handleCurrentLocationRequest();
                return true;
                
			case MENU_REFRESHPOINTS_ID:
                handleRefreshPointsRequest();
                return true;
                
			case MENU_STAGINGAREA_ID:
                handleResetMapRequest();
                return true;
            case 90000:
                showRecordList();
                return true;
        }
        // return super.onMenuItemSelected(feature_id, item);
        return super.onOptionsItemSelected(item);
    }

    private void showRecordList()
    {
        final Intent recordList = new Intent(this, RecordList.class);

        recordList.putExtra("ViewName", UncEpiSettings.selectedSurveyItem.formFilename);
        recordList.putExtra("ListOnly",true);
        //recordList.putExtra("UncClusterPoint", item.name);
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
        //surveyPointItem = item;
        startActivity(recordList);
    }
   
    
    @Override
    public void onBackPressed() {
    	if (Constants.LOGS_ENABLED2) {
        	Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " onBackPressed");
        }
    	handleBackKey();
    }
    
    
    /**
     * A call-back for when the user presses the "Back" button.
     */
    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
        	if (Constants.LOGS_ENABLED2) {
            	Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " mBackListener");
            }
        	handleBackKey();
        }
    };
	
	private void handleSettingsView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " handleSettingsView");
		}
		Intent intent = new Intent(Constants.INTENT_ACTION_EDIT_SETTINGS);
		startActivity(intent);
	}
	
	private void handleHelpView() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " handleHelpView");
		}
		Intent intent = new Intent(Constants.INTENT_ACTION_FAQ);
		startActivity(intent);
	}
	
	private void handleCurrentLocationRequest() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " handleResetMapRequest");
		}
		fragmentList.handleCurrentLocationRequest();
	}

	private void handleRefreshPointsRequest() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " handleRefreshPointsRequest");
		}
		fragmentList.handleRefreshPointsRequest();
	}
	
	// V0.9.63
	private void handleResetMapRequest() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " handleResetMapRequest");
		}
		fragmentList.handleResetMapView();
	}
	
	private void handleBackKey() {
		if (Constants.LOGS_ENABLED2) {
			Log.d(Constants.LOGTAG, " " + MapsMainActivity.CLASSTAG + " handleBackKey - Enter");
		}
		if (!fragmentList.handleBackKey()) {
			finish();
		}
	}

}