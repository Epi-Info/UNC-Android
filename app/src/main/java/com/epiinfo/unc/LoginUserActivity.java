package com.epiinfo.unc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.epiinfo.droid.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The class LoginUserActivity contains the activity that is used to
 * perform a user login with the server. The username/password must already
 * exist on the server.
 * 
 * @author keithcollins
 */
public class LoginUserActivity extends Activity {

    private static final String CLASSTAG = LoginUserActivity.class.getSimpleName();
	
    private static final int MENU_USER_LOGIN     = Menu.FIRST;
	private static final int MENU_CANCEL_LOGIN   = Menu.FIRST + 1;
	private static final int MENU_SETTINGS_LOGIN = Menu.FIRST + 2;
	private static final int MENU_HELP_LOGIN     = Menu.FIRST + 3;
	
    private EditText mUsername;
    private EditText mPassword;
    private EditText mCoordinator;
  
    private CheckBox mAutoLoginCheckBox;
    private CheckBox mPasswordVisibleCheckBox;
    
    private boolean mLoginInProgress = false;
    private boolean mLoginSuccess    = false;
    private String  mLoginErrorMsg = "No surveys found...";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.LOGS_ENABLED_LOGINUSER) {
        	Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " onCreate - Enter");
        	//Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " DeviceManager.isPhone = " + DeviceManager.isPhone);
        	//Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " DeviceManager.isLargeTablet = " + DeviceManager.isLargeTablet);
        	Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " UncEpiInfo.isTablet = " + UncEpiSettings.isTablet(getApplicationContext()) );
        }

        if (UncEpiSettings.isTablet(getApplicationContext())) {
        	this.setContentView(R.layout.loginuser_activity);
        }
        else {
        	this.setContentView(R.layout.loginuser_phone_activity);
        }

        // Find the edit text fields
        mUsername    = (EditText) findViewById(R.id.username_text_id);
        mPassword    = (EditText) findViewById(R.id.password_text_id);
        mCoordinator = (EditText) findViewById(R.id.coordinator_text_id);
        
        // Find the checkbox fields
        mAutoLoginCheckBox       = (CheckBox) findViewById(R.id.autologin_checkbox_id);
        mPasswordVisibleCheckBox = (CheckBox) findViewById(R.id.passwordvisible_checkbox_id);

        // Hook up button presses to the appropriate event handler
        ((Button) findViewById(R.id.login_button_id)).setOnClickListener(mLoginListener);
        ((Button) findViewById(R.id.cancel_button_id)).setOnClickListener(mCancelListener);

        if (Constants.LOGS_ENABLED_LOGINUSER) {
        	Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " onCreate - Exit");
        }
    }   
    
    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.LOGS_ENABLED_LOGINUSER) {
        	Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " onResume");
        }
        
        // Set input fields to current values
        mUsername.setText(UncEpiSettings.username);
        mPassword.setText(UncEpiSettings.password);
        mCoordinator.setText(UncEpiSettings.coordinator);
        mAutoLoginCheckBox.setChecked(UncEpiSettings.autoLogin);
        mPasswordVisibleCheckBox.setChecked(false);
        mPasswordVisibleCheckBox.setOnClickListener(new PasswordVisibleCheckBoxClickListener());
        
        mLoginInProgress = false;
        
        if ((UncEpiSettings.autoLogin) && (UncEpiSettings.username.length() > 0) && (UncEpiSettings.password.length() > 0)) {
        	handleLoginUser();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (Constants.LOGS_ENABLED_LOGINUSER) {
        	Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " onPause");
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Constants.LOGS_ENABLED_LOGINUSER) {
        	Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " onConfigurationChanged");
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_USER_LOGIN,   0, R.string.login_button_label).setShortcut('0', 'l');
        menu.add(0, MENU_CANCEL_LOGIN, 0, R.string.cancel_button_label).setShortcut('1', 'c');
        menu.add(0, MENU_SETTINGS_LOGIN, 0, R.string.settings_button_label).setShortcut('2', 's');
        menu.add(0, MENU_HELP_LOGIN, 0, R.string.help_button_label).setShortcut('3', 'h');
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
			case MENU_USER_LOGIN:
                handleLoginUser();
                return true;
			case MENU_CANCEL_LOGIN:
                handleCancelLogin();
                return true;
			case MENU_SETTINGS_LOGIN:
                handleEditSettings();
                return true;
			case MENU_HELP_LOGIN:
                handleHelp();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * A call-back for when the user presses the "Back" button.
     */
    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
        	handleCancelLogin();
        }
    };

    /**
     * A call-back for when the user presses the "Cancel" button.
     */
    OnClickListener mCancelListener = new OnClickListener() {
        public void onClick(View v) {
        	handleCancelLogin();
        }
    };
    
    /**
     * A call-back for when the user presses the "Login" button.
     */
    OnClickListener mLoginListener = new OnClickListener() {
        public void onClick(View v) {
        	handleLoginUser();
        }
    };
    
	private void handleCancelLogin() {
        finish();
	}
	

    private void handleLoginUser() {
    	if (Constants.LOGS_ENABLED_LOGINUSER) {
			Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " handleLoginUser - Enter");
		}
    	// prevent multiple login requests
    	if (mLoginInProgress == false) {
    		
    		// 07Jun2015 v0.9.52 - Support network offline mode
    		if (Constants.LOGS_ENABLED_LOGINUSER) {
    			Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " IsNetworkAvailable = " + UncEpiSettings.IsNetworkAvailable(getApplicationContext()));
    			Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " loginVerified = " + UncEpiSettings.loginVerified);
    		}
    		
    		// v0.9.57
    		UncEpiSettings.previousSurveyTitle = UncEpiSettings.selectedSurveyItem.title;
    		
    		if ((!UncEpiSettings.IsNetworkAvailable(getApplicationContext())) && (UncEpiSettings.loginVerified)) {
    			// check if login params are unchanged
    			if ((mUsername.getText().toString().equals(UncEpiSettings.username)) &&
    				(mPassword.getText().toString().equals(UncEpiSettings.password)) &&
    				(mCoordinator.getText().toString().equals(UncEpiSettings.coordinator))) {
    				if (Constants.LOGS_ENABLED_LOGINUSER) {
    					Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " handleLoginUser - offline and login verified");
    				}
    				// start the activity to download survey questionnaire, cluster & point files
    				Intent intent = new Intent(Constants.INTENT_ACTION_SURVEY_DOWNLOAD);
        			startActivity(intent);
        			finish();
    				return;
    			}
    		}
    		
    		// 08Jun2015 v.0.9.52 reset login response fields
    		UncEpiSettings.loginVerified = false;
    		UncEpiSettings.selectedSurveyItem.id = "";
    		UncEpiSettings.selectedSurveyItem.title = "";
    		UncEpiSettings.selectedSurveyItem.createDate = "";
    		UncEpiSettings.selectedSurveyItem.clustersFilename = "";
    		UncEpiSettings.selectedSurveyItem.pointsFilename = "";
    		UncEpiSettings.selectedSurveyItem.formFilename = "";

    		mLoginInProgress = true;
    		
    		if (saveUserLoginPreferences(true) == false) {
        		mLoginInProgress = false;
        		return;
    		}
    		
    		// Create an AsyncTask class to set to perform the user login on the server and to handle the server response.
    		Toast.makeText(getApplicationContext(), "Please Wait - Logging In...", Toast.LENGTH_SHORT).show();
    		LoginUserAsyncTask task = new LoginUserAsyncTask();
        	task.execute(new String[] { "" });
    	}
    	if (Constants.LOGS_ENABLED_LOGINUSER) {
			Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " handleLoginUser - Exit");
		}
    }
  

    private void handleEditSettings() {
    	if (Constants.LOGS_ENABLED_LOGINUSER) {
			Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " handleEditSettings - Enter");
		}
    	Intent intent = new Intent(Constants.INTENT_ACTION_EDIT_SETTINGS);
		startActivity(intent);
    }
    
    private void handleHelp() {
    	if (Constants.LOGS_ENABLED_LOGINUSER) {
			Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " handleHelp - Enter");
		}
    	Intent intent = new Intent(Constants.INTENT_ACTION_FAQ);
		startActivity(intent);
    }
    
    
	private void performLoginUser() {
    	if (Constants.LOGS_ENABLED_LOGINUSER) {
    		Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " performLoginUser - Enter");
    	}
    	
    	// This is an example response from server
    	/*
    	 * <?xml version ="1.0"?>
    	 * <Survey>
    	 *   <id>61</id>
    	 *   <SurveyName>TexasAM_FINAL_v3_2015</SurveyName>
    	 *   <CreateDate>2015-04-07</CreateDate>
    	 *   <Clusterfile>./Surveys/TexasAM_FINAL_v3_2015/clusterFile.kml</Clusterfile>
    	 *   <Pointsfile>./Surveys/TexasAM_FINAL_v3_2015/pointsFile.kml</Pointsfile>
    	 *   <SurveyFormfile>./Surveys/TexasAM_FINAL_v3_2015/questionaire.xml</SurveyFormfile>
    	 * </Survey>
    	 * 
    	 */

    	mLoginSuccess = false;  // assume
    	
    	// v0.9.65 - check if formFilename changes, and if it does then delete saved entries in db 
    	//           that could not be send because device was offline
    	String prevSurveyFormFileame = UncEpiSettings.selectedSurveyItem.formFilename;  // e.g. "/Surveys/Keith637_22-10-2015/questionaire.xml"
    	
    	
    	/***********TEMP CODE - 20Apr2015 - Bypass do to DB error *****
    	if (mLoginSuccess == false) {
    		mLoginSuccess = true;
    		UncEpiSettings.selectedSurveyItem.id = "61";
    		UncEpiSettings.selectedSurveyItem.title = "TexasAM_FINAL_v3_2015";
    		UncEpiSettings.selectedSurveyItem.createDate = "Apr 20 2015";
    		UncEpiSettings.selectedSurveyItem.clustersFilename = "/Surveys/TexasAM_FINAL_v3_2015/clusterFile.kml";
    		UncEpiSettings.selectedSurveyItem.pointsFilename = "/Surveys/TexasAM_FINAL_v3_2015/pointsFile.kml";
    		UncEpiSettings.selectedSurveyItem.formFilename = "/Surveys/TexasAM_FINAL_v3_2015/questionaire.xml";
    	    return;
    	}
    	***************************************************************/
    	
    	
		// Format CURL string to login the user
    	BufferedReader in = null;
    	
    	try {
			// 29Aug2015 v0.9.62
			// Need to add timeout to the HTTP Client Get operation.
			//
			// Android 'M' removes support for the Apache HTTP client. 
			// Use the HttpURLConnection class instead. This API is more efficient because 
			// it reduces network use through transparent compression and response caching, 
			// and minimizes power consumption. 
			//
			// To continue using the Apache HTTP APIs, you must first declare the 
			// following compile-time dependency in your build.gradle file:
			//
			//	android {
				// useLibrary 'org.apache.http.legacy'
			//	}
			
			/************************
            HttpClient client = new DefaultHttpClient();

            String httpGetCmd = Constants.EPI_API_PREFIX + "op=epiLogin&p1=" +
            	                UncEpiSettings.username + "&p2=" +
            	                UncEpiSettings.password + "%7C" +
            					UncEpiPhoneState.getPhoneNumber() + "%7C" +
            					UncEpiSettings.coordinator;
			************************/
			URL getCmdUrl = new URL(Constants.EPI_API_PREFIX + "op=epiLogin&p1=" +
            	                UncEpiSettings.username + "&p2=" +
            	                UncEpiSettings.password + "%7C" +
            					UncEpiPhoneState.getPhoneNumber() + "%7C" +
            					UncEpiSettings.coordinator);
			HttpURLConnection urlConnection = (HttpURLConnection) getCmdUrl.openConnection();
			urlConnection.setConnectTimeout(Constants.URL_CONNECTION_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(Constants.URL_CONNECTION_READ_TIMEOUT);
			
            if (Constants.LOGS_ENABLED_LOGINUSER) {
            	// Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " httpGetCmd = " + httpGetCmd);
            	Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " getCmdUrl = ");
            	Log.d(Constants.LOGTAG, " " + getCmdUrl.toString());
            }
			/***************
            HttpGet method = new HttpGet(httpGetCmd);
            HttpResponse response = client.execute(method);
            in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
			*****************/
            in = new BufferedReader (new InputStreamReader(urlConnection.getInputStream()));
			//////InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			
            String line = "";
            boolean finished = true;
            
            // check the first line for an error
            if ((line = in.readLine()) != null) {
            	if (Constants.LOGS_ENABLED_LOGINUSER) {
            		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " in: " + line);
            	}
            	if (line.indexOf("retrieving failed") != -1) {
            		mLoginErrorMsg = getResources().getString(R.string.retrievingfailed_msg);
            	}
            	else if (line.indexOf("DOCTYPE") != -1) {
            		mLoginErrorMsg = getResources().getString(R.string.loginfailed_msg);
            	}
            	else if ((line.indexOf("No Account Found") != -1) || (line.indexOf("-1") != -1)) {
            		mLoginErrorMsg = getResources().getString(R.string.invalidusername_msg);
            	}
            	else if ((line.indexOf("Bad Password") != -1) || (line.indexOf("-2") != -1)) {
            		mLoginErrorMsg = getResources().getString(R.string.invalidpassword_msg);
            	}
            	else if ((line.indexOf("Coordinator Not Found") != -1) || (line.indexOf("-3") != -1)) {
            		mLoginErrorMsg = getResources().getString(R.string.invalidcoordinator_msg);
            	}
            	else if (line.indexOf("DOCTYPE") != -1) {
            		mLoginErrorMsg = getResources().getString(R.string.retrievingfailed_msg);
            	}
            	else if (line.indexOf("xml") != -1) {
            		finished = false;
            		// UncEpiSettings.mSurveyList.clear();
            		// parse the strings, the survey will have 6 fields
                	// Survey Id, Survey Name, Create Date, Clusters Filename, Points Filename, Questionnaire Name
                }
            	else {
            		mLoginErrorMsg = getResources().getString(R.string.networktimeout_msg);
            	}
            }
            
            
            if ((line = in.readLine()) != null) {
            	if (Constants.LOGS_ENABLED_LOGINUSER) {
               		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " in: " + line);
               	}
            	// search for beginning of a survey section
            	if (line.indexOf("<Survey>") != -1) {
            		mLoginSuccess = true;
            		int startIndex;
            		int endIndex;
            			
            		// get Survey Id from "<id>3</id>"
            		//line = in.readLine();
            		startIndex = line.indexOf("<id>") + 4;
            		endIndex = line.indexOf("<", startIndex);
            		if (Constants.LOGS_ENABLED_LOGINUSER) {
                   		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " found id value @index " + startIndex + " " + endIndex);
                   	}
            		UncEpiSettings.selectedSurveyItem.id = line.substring(startIndex, endIndex);
            		if (Constants.LOGS_ENABLED_LOGINUSER) {
                   		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " id: " + UncEpiSettings.selectedSurveyItem.id);
                   	}
            			
            		// get Survey Name from "<SurveyName>DurhamCHOS_County_FINAL</SurveyName>"
            		//line = in.readLine();
            		startIndex = line.indexOf("<SurveyName>") + 12;
            		endIndex = line.indexOf("<", startIndex);
            		UncEpiSettings.selectedSurveyItem.title = line.substring(startIndex, endIndex);
            		if (Constants.LOGS_ENABLED_LOGINUSER) {
                   		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " title: " + UncEpiSettings.selectedSurveyItem.title);
                   	}
            			
            		// get Create Date from "<CreateDate>Wed, 13 Nov 13 13:10:53 -0500</CreateDate>"
            		//line = in.readLine();
            		startIndex = line.indexOf("<CreateDate>") + 12;
            		endIndex = line.indexOf("<", startIndex);
            		UncEpiSettings.selectedSurveyItem.createDate = line.substring(startIndex, endIndex);
            		if (Constants.LOGS_ENABLED_LOGINUSER) {
                   		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " createDate: " + UncEpiSettings.selectedSurveyItem.createDate);
                   	}
            			
            		// get Cluster Filename from "<Clusterfile>clustersDurhamCHOS_County_FINAL.kml</Clusterfile>"
            		//line = in.readLine();
            		startIndex = line.indexOf("<Clusterfile>") + 13;
            		endIndex = line.indexOf("<", startIndex);
            		// UncEpiSettings.selectedSurveyItem.clustersFilename = line.substring(startIndex, endIndex);
            		UncEpiSettings.selectedSurveyItem.clustersFilename = line.substring(startIndex+1, endIndex);  // strip off the leading "."
            		if (Constants.LOGS_ENABLED_LOGINUSER) {
                   		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " clustersFilename: " + UncEpiSettings.selectedSurveyItem.clustersFilename);
                   	}
            			
            		// get Points Filename from "<Pointsfile>pointsDurhamCHOS_County_FINAL</Pointsfile>"
            		//line = in.readLine();
            		startIndex = line.indexOf("<Pointsfile>") + 12;
            		endIndex = line.indexOf("<", startIndex);
            		// UncEpiSettings.selectedSurveyItem.pointsFilename = line.substring(startIndex, endIndex);
            		UncEpiSettings.selectedSurveyItem.pointsFilename = line.substring(startIndex+1, endIndex);  // strip off the leading "."
            		if (Constants.LOGS_ENABLED_LOGINUSER) {
                   		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " pointsFilename: " + UncEpiSettings.selectedSurveyItem.pointsFilename);
                   	}
            			
            		// get Questionnaire Filename "<SurveyFormfile>EMPTY.xml</SurveyFormfile>"
            		//line = in.readLine();
            		startIndex = line.indexOf("<SurveyFormfile>") + 16;
            		endIndex = line.indexOf("<", startIndex);
            		// UncEpiSettings.selectedSurveyItem.formFilename = line.substring(startIndex, endIndex);
            		UncEpiSettings.selectedSurveyItem.formFilename = line.substring(startIndex+1, endIndex);  // strip off the leading "."
            		if (Constants.LOGS_ENABLED_LOGINUSER) {
                   		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " formFilename: " + UncEpiSettings.selectedSurveyItem.formFilename);
                   	}
            		
            		// v0.9.65 if formFilename has changed then delete saved entries in db that could not be send because device was offline
            		if (!prevSurveyFormFileame.equals(UncEpiSettings.selectedSurveyItem.formFilename)) {
            			
            		}
            			
            		UncEpiSettings.selectedSurveyItem.Dump();
            	}
            	
            	while ((line = in.readLine()) != null) { }  // read and dump any garbage on line

                in.close();
            }
            else {   // response is null 
            	if (Constants.LOGS_ENABLED_LOGINUSER) {
            		Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " in: is null!!");
            	}
            }
        } 
    	catch (Exception e) {
    		if (Constants.LOGS_ENABLED_LOGINUSER) {
    			Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " exception cause: " + e.getCause());
    			Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " exception message: " + e.getMessage());
    			Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " exception desc: " + e.toString());
    		
    			Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " Connectivity Issue");
    		}
    		mLoginErrorMsg = getResources().getString(R.string.checknetworkconnection_msg);
    	}
    }
	
	/**
     * Save login preferences to the persistent settings 
     */
    private boolean saveUserLoginPreferences(boolean saveParams) {
    	// Set settings to fields to current values
    	if (Constants.LOGS_ENABLED_LOGINUSER) {
    		Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " saveUserLoginPreferences - Enter - checkParams=" + saveParams);
    	}
    	boolean retVal = false;  // assume
    	
    	if (mUsername.getText().toString().length() < Constants.MIN_USERNAME_LENGTH) {
    		StringBuilder validationText1 = new StringBuilder();
    		new AlertDialog.Builder(LoginUserActivity.this).setTitle(getResources().getString(R.string.usernametooshort_msg)).setMessage(
    			validationText1.toString()).setPositiveButton("Continue",
    			new android.content.DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int arg1) {
    					// in this case, don't need to do anything other than close alert
    				}
    			}).show();
    		validationText1 = null;
    	}
    	else if (mPassword.getText().toString().length() < Constants.MIN_PASSWORD_LENGTH) {
    		StringBuilder validationText1 = new StringBuilder();
    		new AlertDialog.Builder(LoginUserActivity.this).setTitle(getResources().getString(R.string.invalidpassword_msg)).setMessage(
    			validationText1.toString()).setPositiveButton("Continue",
    			new android.content.DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int arg1) {
    					// in this case, don't need to do anything other than close alert
    				}
    			}).show();
    		validationText1 = null;
    	}
    	else if (mCoordinator.getText().toString().length() < Constants.MIN_COORDINATOR_LENGTH) {
    		StringBuilder validationText1 = new StringBuilder();
    		new AlertDialog.Builder(LoginUserActivity.this).setTitle(getResources().getString(R.string.invalidcoordinator_msg)).setMessage(
    			validationText1.toString()).setPositiveButton("Continue",
    			new android.content.DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int arg1) {
    					// in this case, don't need to do anything other than close alert
    				}
    			}).show();
    		validationText1 = null;
    	}
    	else {
    		UncEpiSettings.username = mUsername.getText().toString();
    		UncEpiSettings.password = mPassword.getText().toString();
    		UncEpiSettings.coordinator = mCoordinator.getText().toString();
    	
    		if (mAutoLoginCheckBox.isChecked()) { UncEpiSettings.autoLogin = true; }
    		else { UncEpiSettings.autoLogin = false; }
		
    		if (saveParams) {
    			// store settings persistently in file system using shared preferences
    			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplication().getApplicationContext());
    			UncEpiSettings.SaveSettings(preferences);
    		}
    		retVal = true;
    	}
    	
        if (Constants.LOGS_ENABLED_LOGINUSER) {
        	Log.v(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " saveUserLoginPreferences - Exit retVal=" + retVal);
        }
        return (retVal);
    }

    
    /**
     * 
     * Use this task to send the Login request to the server to avoid using the main thread.
     * Provides the methods to override doInBackground() and onPostExecute().
     * Note that no UI changes are allowed in doInBackground().
     *
     */
    private class LoginUserAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			// runs in non-UI thread
			if (Constants.LOGS_ENABLED_LOGINUSER) {
				Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " doInBackground - Enter");
			}
			String response = "";
			performLoginUser();
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// update screen in UI thread
			if (Constants.LOGS_ENABLED_LOGINUSER) {
				Log.d(Constants.LOGTAG, " " + LoginUserActivity.CLASSTAG + " onPostExecute - Enter");
			}
			
			// save the user setting
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginUserActivity.this.getApplication().getApplicationContext());
			
			// if login successful then start Map View activity
			// else display error dialog
			if (mLoginSuccess) {
				// Enable AutoLogin by default once login is successful
				// UncEpiSettings.autoLogin = true;
		        
		        // save settings in persistent storage
				// 07Jun2015 v0.9.52 - Support network offline mode
	    		UncEpiSettings.loginVerified = true;
				UncEpiSettings.SaveSettings(preferences);

				/********************************
				Toast toast= Toast.makeText(LoginUserActivity.this, "Please Wait - Retrieving Map...", Toast.LENGTH_LONG);  
				toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
				********************************/
				
				// 21Nov2013 KRC - Add new activity to download survey questionnaire, cluster & point files
				// Intent intent = new Intent(Constants.INTENT_ACTION_MAPS_MAIN);
				Intent intent = new Intent(Constants.INTENT_ACTION_SURVEY_DOWNLOAD);
    			startActivity(intent);
    			finish();
        	}
        	else {
    			mLoginInProgress = false;
    			
    			StringBuilder validationText1 = new StringBuilder();
    			// new AlertDialog.Builder(LoginUserActivity.this).setTitle(getResources().getString(R.string.invalidusernamepassword_msg)).setMessage(
    			new AlertDialog.Builder(LoginUserActivity.this).setTitle(mLoginErrorMsg.toString()).setMessage(
                        validationText1.toString()).setPositiveButton("Continue",
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                // in this case, don't need to do anything other than close alert
                            }
                        }).show();
                    validationText1 = null;
    		}
		}
	}
    
    class PasswordVisibleCheckBoxClickListener implements OnClickListener {

    	public PasswordVisibleCheckBoxClickListener() {}
    	
    	public void onClick(View v) {
    		if (mPasswordVisibleCheckBox.isChecked() == true) {
    			if (Constants.LOGS_ENABLED_LOGINUSER) {
    				Log.d(Constants.LOGTAG, " " + "PasswordVisible CheckBox is CHECKED");
    			}
    			mPassword.setTransformationMethod(null);
    		}
    		else {
    			if (Constants.LOGS_ENABLED_LOGINUSER) {
    				Log.d(Constants.LOGTAG, " " + "PasswordVisible CheckBox is NOT CHECKED");
    			}
    			mPassword.setTransformationMethod(new PasswordTransformationMethod());
    		}
    	}
    }


}