package com.epiinfo.unc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
//import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.epiinfo.droid.R; 

/**
 * "Edit Settings" for user preferences and then store using the shared preferences 
 * feature of Android.  Settings include:
 *    - Username
 *    - Password
 *    - Auto Login
 *    - Speech Enabled
 *    - etc
 * 
 * @author keithcollins
 */
public class EditSettingsActivity extends Activity {

    private static final String CLASSTAG = EditSettingsActivity.class.getSimpleName();
    
    private static final int MENU_SAVE_SETTINGS_ID = Menu.FIRST;
    private static final int MENU_REVERT_SETTINGS_ID = Menu.FIRST + 1;
    private static final int MENU_EXIT_SETTINGS_ID = Menu.FIRST + 2;
	
    private static final int MIN_LEN_USERNAME = 3;
    private static final int MAX_LEN_USERNAME = 16;
    private static final int MIN_LEN_PASSWORD = 3;
    private static final int MAX_LEN_PASSWORD = 16;
    
    private TextView mUsernameLabel;
    private EditText mUsername;
    private TextView mPasswordLabel;
    private EditText mPassword;
    private TextView mPassword2Label;
    private EditText mPassword2;
    private TextView mCoordinatorLabel;
    private EditText mCoordinator;
    
    private CheckBox mAutoLoginCheckBox;
    private CheckBox mPasswordVisibleCheckBox;
    private CheckBox mSpeechEnabledCheckBox;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " onCreate");
        }

        this.setContentView(R.layout.editsettings_activity);
        
       
        // Find the label and text fields
        mUsernameLabel    = (TextView) findViewById(R.id.username_label_id);
        mUsername         = (EditText) findViewById(R.id.username_text_id);
        mPasswordLabel    = (TextView) findViewById(R.id.password_label_id);
        mPassword         = (EditText) findViewById(R.id.enterpassword_text_id);
        mPassword2Label   = (TextView) findViewById(R.id.password2_label_id);
        mPassword2        = (EditText) findViewById(R.id.enterpassword2_text_id);
        mCoordinatorLabel = (TextView) findViewById(R.id.coordinator_label_id);
        mCoordinator      = (EditText) findViewById(R.id.entercoordinator_text_id);
        
        // Find the Check Box fields
        mAutoLoginCheckBox       = (CheckBox) findViewById(R.id.autologin_checkbox_id);
        mPasswordVisibleCheckBox = (CheckBox) findViewById(R.id.passwordvisible_checkbox_id);
        mSpeechEnabledCheckBox   = (CheckBox) findViewById(R.id.speechenabled_checkbox_id);
        
        // Hook up button presses to the appropriate event handler
        ((Button) findViewById(R.id.save_settings_changes_button)).setOnClickListener(mSaveListener);
        ((Button) findViewById(R.id.restore_settings_changes_button)).setOnClickListener(mRestoreListener);
        ((Button) findViewById(R.id.quit_settings_changes_button)).setOnClickListener(mExitListener);

        
        // display labels and set settings to fields to current values
        mUsernameLabel.setText(" Username:");          // getText(R.string.username_label));
        mPasswordLabel.setText(" Password:");          // getText(R.string.password_label));
        mPassword2Label.setText(" Verify Password:");  // getText(R.string.password2_label));
        mCoordinatorLabel.setText(" Coordinator:");    // getText(R.string.coordinator_label));
        setSettingsToCurrentValues();
        
        mPasswordVisibleCheckBox.setChecked(false);
        mPasswordVisibleCheckBox.setOnClickListener(new PasswordVisibleCheckBoxClickListener());
    }   
    
    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " onResume");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " onPause");
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " onConfigurationChanged");
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
        menu.add(0, MENU_SAVE_SETTINGS_ID, 0, R.string.save_button_label).setShortcut('0', 's');
        menu.add(0, MENU_REVERT_SETTINGS_ID, 0, R.string.revert_button_label).setShortcut('1', 'r');
        menu.add(0, MENU_EXIT_SETTINGS_ID, 0, R.string.exit_button_label).setShortcut('2', 'e');
        
        return true;
    }
    
    /**
     * Called right before the activity's option menu is displayed.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Before showing the menu, we need to decide whether the clear
        // item is enabled depending on whether there is text to clear.
        // menu.findItem(LOGIN_ID).setVisible(mAlert.getText().length() > 0);

        return true;
    }
    
    /**
     * Called when a menu item is selected.
     */
    @Override
    // public boolean onMenuItemSelected(FeatureId, MenuItem item) {
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE_SETTINGS_ID:
            	handleSaveSettingsChanges();
                return true;
            case MENU_REVERT_SETTINGS_ID:
            	handleRevertSettingsChanges();
                return true;
            case MENU_EXIT_SETTINGS_ID:
            	handleExitSettingsChanges();
                return true;
        }
        // return super.onMenuItemSelected(feature_id, item);
        return super.onOptionsItemSelected(item);
    }
   
    /**
     * A call-back for when the user presses the "Back" button.
     */
    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
    
    /**
     * A call-back for when the user presses the "Save" button.
     */
    OnClickListener mSaveListener = new OnClickListener() {
        public void onClick(View v) {
        	handleSaveSettingsChanges();
        }
    };
    
    /**
     * A call-back for when the user presses the "Restore" button.
     */
    OnClickListener mRestoreListener = new OnClickListener() {
        public void onClick(View v) {
        	handleRevertSettingsChanges();
        }
    };
    
    /**
     * A call-back for when the user presses the "Quit" button.
     */
    OnClickListener mExitListener = new OnClickListener() {
        public void onClick(View v) {
        	handleExitSettingsChanges();
        }
    };

	/**
	 * revert back to settings values currently in persistent storage
	 * any changes made by the user that were not saved are lost
	 */
	private void handleRevertSettingsChanges() {
		Toast.makeText(getApplicationContext(), "Settings Changes Reverted", Toast.LENGTH_SHORT).show();
		setSettingsToCurrentValues();
	}
	
	/**
	 * Exit the Settings screen. Any settings not previously saved will be lost.
	 */
	private void handleExitSettingsChanges() {
		finish();
	}
	
	/**
	 * Validate values entered by user
     *    Card Number: length 8-12 chars
     *    Email Address: length 6-40 chars, including "@"
     *    Password: length 7-20 chars, and password = password2 
     * Copy values from EditText fields to Sky Settings strings
     * Then use Shared Preferences to store them persistently
	 */
    private void handleSaveSettingsChanges() {
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " handleSaveSettings() - Enter");
    	}
    	
    	if ((this.mUsername.length() < MIN_LEN_USERNAME) ||
    	    (this.mUsername.length() > MAX_LEN_USERNAME)) {
    		StringBuilder validationText = new StringBuilder();
    		new AlertDialog.Builder(EditSettingsActivity.this).setTitle(getResources().getString(R.string.invalidusername_msg)).setMessage(
                    validationText.toString()).setPositiveButton("Continue",
                    new android.content.DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            // in this case, don't need to do anything other than close alert
                        }
                    }).show();
                validationText = null;
                this.mUsername.setText("");
                this.mUsername.requestFocus();
                return;
    	}
    	
    	if (this.mPassword.length() < MIN_LEN_PASSWORD) {
        		StringBuilder validationText = new StringBuilder();
        		new AlertDialog.Builder(EditSettingsActivity.this).setTitle(getResources().getString(R.string.invalidpasswordtooshort_msg)).setMessage(
                        validationText.toString()).setPositiveButton("Continue",
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                // in this case, don't need to do anything other than close alert
                            }
                        }).show();
                    validationText = null;
                    this.mPassword.setText("");
                    this.mPassword2.setText("");
                    this.mPassword.requestFocus();
                    return;
        }    	
    	
    	if ((this.mCoordinator.length() < MIN_LEN_USERNAME) ||
        	    (this.mUsername.length() > MAX_LEN_USERNAME)) {
        		StringBuilder validationText = new StringBuilder();
        		new AlertDialog.Builder(EditSettingsActivity.this).setTitle(getResources().getString(R.string.invalidcoordinator_msg)).setMessage(
                        validationText.toString()).setPositiveButton("Continue",
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                // in this case, don't need to do anything other than close alert
                            }
                        }).show();
                    validationText = null;
                    this.mCoordinator.setText("");
                    this.mCoordinator.requestFocus();
                    return;
        }

    	if (this.mPassword.length() > MAX_LEN_PASSWORD) {
            		StringBuilder validationText = new StringBuilder();
            		new AlertDialog.Builder(EditSettingsActivity.this).setTitle(getResources().getString(R.string.invalidpasswordtoolong_msg)).setMessage(
                            validationText.toString()).setPositiveButton("Continue",
                            new android.content.DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int arg1) {
                                    // in this case, don't need to do anything other than close alert
                                }
                            }).show();
                        validationText = null;
                        this.mPassword.setText("");
                        this.mPassword2.setText("");
                        this.mPassword.requestFocus();
                        return;
            }
    	
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " UncEpiSettings: Password " + this.mPassword.getText().toString());
    		Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " UncEpiSettings: Password2 " + this.mPassword2.getText().toString());
    	}
    	
    	if (this.mPassword.getText().toString().equals(this.mPassword2.getText().toString()) == false) {
            		StringBuilder validationText = new StringBuilder();
            		new AlertDialog.Builder(EditSettingsActivity.this).setTitle(getResources().getString(R.string.mismatchpassword_msg)).setMessage(
                            validationText.toString()).setPositiveButton("Continue",
                            new android.content.DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int arg1) {
                                    // in this case, don't need to do anything other than close alert
                                }
                            }).show();
                        validationText = null;
                        this.mPassword.setText("");
                        this.mPassword2.setText("");
                        this.mPassword.requestFocus();
                        return;
        }    	
    	
    	UncEpiSettings.username = this.mUsername.getText().toString();
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " UncEpiSettings Username:" + UncEpiSettings.username);
    	}
        
    	UncEpiSettings.password = this.mPassword.getText().toString();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " UncEpiSettings Password: " + UncEpiSettings.password);
        }
		
    	UncEpiSettings.coordinator = this.mCoordinator.getText().toString();
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " UncEpiSettings Coordinator:" + UncEpiSettings.coordinator);
    	}
    	
        UncEpiSettings.autoLogin = this.mAutoLoginCheckBox.isChecked();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " UncEpiSettings AutoLogin: " + UncEpiSettings.autoLogin);
        }
        
        UncEpiSettings.speechEnabled = this.mSpeechEnabledCheckBox.isChecked();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " UncEpiSettings SpeechEnabled: " + UncEpiSettings.speechEnabled);
        }
        
        // store settings persistently in file system using shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplication().getApplicationContext());
        Toast.makeText(getApplicationContext(), "Settings Changes Saved", Toast.LENGTH_SHORT).show();
        UncEpiSettings.SaveSettings(preferences);
        
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " handleSaveSettings() - Exit");
        }
    }

    /**
     * set settings to current values
     */
    private void setSettingsToCurrentValues() {
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " setSettingsToCurrentValues - Enter");
    	}
    	mUsername.setText(UncEpiSettings.username);
    	mPassword.setText(UncEpiSettings.password);
    	mPassword2.setText(UncEpiSettings.password);
    	mCoordinator.setText(UncEpiSettings.coordinator);
    	mAutoLoginCheckBox.setChecked(UncEpiSettings.autoLogin);
		mSpeechEnabledCheckBox.setChecked(UncEpiSettings.speechEnabled);
    	
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + EditSettingsActivity.CLASSTAG + " setSettingsToCurrentValues - Exit");
    	}
    }
    
    class PasswordVisibleCheckBoxClickListener implements OnClickListener {

    	public PasswordVisibleCheckBoxClickListener() {}
    	
    	public void onClick(View v) {
    		if (mPasswordVisibleCheckBox.isChecked() == true) {
    			if (Constants.LOGS_ENABLED) {
    				Log.d(Constants.LOGTAG, " " + "PasswordVisible CheckBox is CHECKED");
    			}
    			mPassword.setTransformationMethod(null);
    			mPassword2.setTransformationMethod(null);
    		}
    		else {
    			if (Constants.LOGS_ENABLED) {
    				Log.d(Constants.LOGTAG, " " + "PasswordVisible CheckBox is NOT CHECKED");
    			}
    			mPassword.setTransformationMethod(new PasswordTransformationMethod());
    			mPassword2.setTransformationMethod(new PasswordTransformationMethod());
    		}
    	}
    }

}