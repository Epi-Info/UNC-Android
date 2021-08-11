package com.epiinfo.unc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.epiinfo.droid.R;


/**
 * The class SurveyDownloadActivity contains the activity that is used to
 * download the selected survey contents from the server.
 * 
 * @author keithcollins
 */
public class SurveyDownloadActivity extends Activity {

    private static final String CLASSTAG = SurveyDownloadActivity.class.getSimpleName();
	
	private static final int MENU_CANCEL   = Menu.FIRST;
	private static final int MENU_SETTINGS = Menu.FIRST + 1;
	private static final int MENU_HELP     = Menu.FIRST + 2;

	private boolean mSurveyRetrievalSuccess = false;
	private String mSurveysRetrievalErrorMsg;    
    
    // SurveyItem selectedSurveyItem;
    private static final int FILETYPE_UNKNOWN_ID       = 0;
    private static final int FILETYPE_CLUSTERS_ID      = 1;
    private static final int FILETYPE_POINTS_ID        = 2;
    private static final int FILETYPE_QUESTIONNAIRE_ID = 3;
    private int currentDownloadFileType = FILETYPE_UNKNOWN_ID;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
        	Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " onCreate - Enter");
        }

        this.setContentView(R.layout.surveydownload_activity);
		
        // 08Jun2015 v0.9.52 Check if survey files have already been downloaded
        if (IsSurveyFilesExist()) {
        	// start the Map activity
			Intent intent = new Intent(Constants.INTENT_ACTION_MAPS_MAIN);
			startActivity(intent);
			finish();
        }
        else {
        	// download the survey files from the server via async task
        	// Toast.makeText(getApplicationContext(), "Please Wait - Downloading Survey Files...", Toast.LENGTH_SHORT).show();
        	downloadSurveyFiles();
        }
        
    	if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
    		Log.d(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " Survey Name=" + UncEpiSettings.selectedSurveyItem.title);
    	}
    	
        if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
        	Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " onCreate - Exit");
        }
    }   
    
    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
        	Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " onResume");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
        	Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " onPause");
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
        	Log.d(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " onConfigurationChanged");
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_CANCEL,   0, R.string.cancel_button_label).setShortcut('0', 'c');
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
			case MENU_CANCEL:
                handleCancel();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * A call-back for when the user presses the "Back" button.
     */
    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
        	handleCancel();
        }
    };

    /**
     * A call-back for when the user presses the "Cancel" button.
     */
    OnClickListener mCancelListener = new OnClickListener() {
        public void onClick(View v) {
        	handleCancel();
        }
    };
    
	private void handleCancel() {
        finish();
	}
        	
    
    private void downloadSurveyFiles() {
    	if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " downloadSurveyFiles - Enter");
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " name = " + UncEpiSettings.selectedSurveyItem.title);
    	}
    		
    	// download and store on the phone's filesystem the Form, Clusters and Points files
    	// directory structure on the server is:
    	//     http://www.collectsmartdata.com/phase3/Surveys/<SurveyName>/<clustersFile.kml>
    	//     http://www.collectsmartdata.com/phase3/Surveys/<SurveyName>/<pointsFile.kml>
    	//     http://www.collectsmartdata.com/phase3/Surveys/<SurveyName>/<questionaireFile.xml>

    	Toast.makeText(getApplicationContext(), "Please Wait - Downloading Survey Files...", Toast.LENGTH_SHORT).show();
    	currentDownloadFileType = FILETYPE_CLUSTERS_ID;
    	DownloadSurveyFileAsyncTask task = new DownloadSurveyFileAsyncTask();
    	task.execute(UncEpiSettings.selectedSurveyItem.title, UncEpiSettings.selectedSurveyItem.clustersFilename);
    }
    
    
    /**
     * 
     * Use this task to download the Survey, Clusters and Points files from the server 
     * while avoiding the use of the main thread
     *
     */
    private class DownloadSurveyFileAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... arg0) {
			// runs in non-UI thread
			if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
				Log.d(Constants.LOGTAG, " " + "DownloadSurveyFile:doInBackground - Enter");
			}
			String response = performDownloadSurveyFile(arg0[0], arg0[1]);
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// update screen in UI thread
			if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
				Log.d(Constants.LOGTAG, " " + "DownloadSurveyFile:onPostExecute - Enter");
				Log.d(Constants.LOGTAG, " " + "DownloadSurveyFile:onPostExecute - Result = " + result);
			}
			
			// if command is successful then go to next file to download or quit if all files have been downloaded
			// else display error dialog
			if (result.equals("")) {
				if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
					Log.d(Constants.LOGTAG, " " + "DownloadSurveyFile:onPostExecute - Success");
				}
				if (currentDownloadFileType == FILETYPE_CLUSTERS_ID) {
					currentDownloadFileType = FILETYPE_POINTS_ID;
					DownloadSurveyFileAsyncTask task = new DownloadSurveyFileAsyncTask();
					task.execute(UncEpiSettings.selectedSurveyItem.title, UncEpiSettings.selectedSurveyItem.pointsFilename);
				}
				else if (currentDownloadFileType == FILETYPE_POINTS_ID) {
					currentDownloadFileType = FILETYPE_QUESTIONNAIRE_ID;
					DownloadSurveyFileAsyncTask task = new DownloadSurveyFileAsyncTask();
					task.execute(UncEpiSettings.selectedSurveyItem.title, UncEpiSettings.selectedSurveyItem.formFilename);
				}
				else {
					// start the Map activity
					Intent intent = new Intent(Constants.INTENT_ACTION_MAPS_MAIN);
	    			startActivity(intent);
	    			finish();
				}
        	}
        	else {
    			StringBuilder validationText1 = new StringBuilder();
    			// new AlertDialog.Builder(LoginUserActivity.this).setTitle(getResources().getString(R.string.invalidusernamepassword_msg)).setMessage(
    			new AlertDialog.Builder(SurveyDownloadActivity.this).setTitle(result).setMessage(
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
    
    
    String performDownloadSurveyFile(final String surveyName, final String filename) {
    	if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " performDownloadSurveyFile - Enter");
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " filename = " + filename);
    	}
    	
    	try {
    		// URL url= new URL(Constants.EPI_FILE_DOWNLOAD_URI + surveyName + "/" + filename);
    		URL url= new URL(Constants.EPI_FILE_DOWNLOAD_URI + filename);
    		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
				Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " URL = " + url.toString());
			}
    		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    		
    		// 30Aug2015 v0.9.62
    		connection.setConnectTimeout(Constants.URL_CONNECTION_CONNECT_TIMEOUT);
			connection.setReadTimeout(Constants.URL_CONNECTION_READ_TIMEOUT);
			
    		connection.connect();

    		// expect HTTP 200 OK, so we don't mistakenly save error report 
    		// instead of the file
    		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
    			if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
    				Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
    			}
    			return ("Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
    		}

    		// this will be useful to display download percentage
    		// might be -1 if server did not report the length
    		// int fileLength = connection.getContentLength();

    		// download the file
	        InputStream input = connection.getInputStream();
	        
	        // extract the short filename without the folders from long filename
	        // convert "/Surveys/Durham_Disaster/clusterFile.kml" to "clusterFile.kml"
	        int startIndex = filename.indexOf("/");
	        startIndex = filename.indexOf("/", startIndex + 2);
    		startIndex = filename.indexOf("/", startIndex + 2);
    		String shortFilename = filename.substring(startIndex + 1);
    		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
				Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " shortFilename = " + shortFilename);
			}
    		
    		// create a File object for the parent directory
    		UncEpiSettings.selectedSurveyItem.surveyFilesFolderName = "/sdcard/Download/Epiinfo/Questionnaires/Surveys/" + UncEpiSettings.selectedSurveyItem.title;
    		// **** 26Apr2015 - store setting persistently by using shared preferences
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplication().getApplicationContext());
			UncEpiSettings.SaveSurveyFilesFolderNameSetting(preferences);
    		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
				Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " folderName = " + UncEpiSettings.selectedSurveyItem.surveyFilesFolderName);
    		}
	        // File fileDirectory = new File("/sdcard/Download/Epiinfo/Questionnaires/Surveys/" + UncEpiSettings.selectedSurveyItem.title);
    		File fileDirectory = new File(UncEpiSettings.selectedSurveyItem.surveyFilesFolderName);
	        
	        // have the object build the directory structure, if needed.
	        fileDirectory.mkdirs();
	        
	        // v0.9.58 - force a refresh of the directly so that the new subfolder shows up in the picker list of the MainActivity when selected a Survey for CollectData Activity
	        // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()))); 
	        // Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " ********************************************************");
	        
	        
	        // create a File object for the output file
	        File outputFile = new File(fileDirectory, shortFilename);
	        // now attach the OutputStream to the file object, instead of a String representation
	        FileOutputStream output = new FileOutputStream(outputFile);
	        // FileOutputStream output = new FileOutputStream("/sdcard/Download/Epiinfo/ClusterPoints/" + filename);
	        /////// FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Download/Epiinfo/Surveys/" + filename);

	        byte data[] = new byte[4096];
	        long total = 0;
	        int count;
	        while ((count = input.read(data)) != -1) {
	            // allow canceling with back button
	        	// if (isCancelled())
	            //    return null;
	            //
	            total += count;
	            // publishing the progress....
	            // if (fileLength > 0) // only if total length is known
	            //    publishProgress((int) (total * 100 / fileLength));
	            output.write(data, 0, count);
	            if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
	        		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " total = " + total + ", count = " + count);
	        	}
	        }
	        
	        if (input  != null) { input.close(); }
	        if (output != null) { output.close(); }
	        
	        return "";
    	}
    	catch (Exception e) {
    		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
    			Log.d(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " exception cause: " + e.getCause());
    			Log.d(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " exception message: " + e.getMessage());
    			Log.d(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " exception desc: " + e.toString());
    		
    			Log.d(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " Connectivity Issue");
    		}
    		return getResources().getString(R.string.checknetworkconnection_msg);
    	}
    }

    
    // 08Jun2015 v0.9.52 Support offline mode by checking if survey files have already been downloaded
    boolean IsSurveyFilesExist() {
    	if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " IsSurveyFilesExist - Enter");
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " survey title = " + UncEpiSettings.selectedSurveyItem.title);
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " points filename " + UncEpiSettings.selectedSurveyItem.pointsFilename);
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " clusters filename = " + UncEpiSettings.selectedSurveyItem.clustersFilename);
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " form filename = " + UncEpiSettings.selectedSurveyItem.formFilename);
    	}
    	
    	// v0.9.64
    	if ((UncEpiSettings.origSurveyCreateDate.equals("")) ||
    		(!UncEpiSettings.origSurveyCreateDate.equals(UncEpiSettings.selectedSurveyItem.createDate))) {
    		return false;
    	}
    	
    	// v0.9.57
    	if ((UncEpiSettings.selectedSurveyItem.title.equals("")) ||
    		(!UncEpiSettings.selectedSurveyItem.title.equals(UncEpiSettings.previousSurveyTitle))) {
    		return false;
    	}

    	String filePath;
    	
    	// check if Points file exists
    	if (!UncEpiSettings.selectedSurveyItem.pointsFilename.equals("")) {
    		filePath = "/sdcard/Download/Epiinfo/Questionnaires/Surveys/" + 
    	               UncEpiSettings.selectedSurveyItem.title + "/" +
    	               "pointsfile.kml";  // UncEpiSettings.selectedSurveyItem.pointsFilename;
    		File file = new File(filePath);
        	if (!file.exists()) {
        		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
	        		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " " + filePath + " does not exist!");
	        	}
        		return false;
        	}
        	else {
        		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
	        		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " IsSurveyFilesExist - Points File EXISTS!");
	        	}
        	}
    	}
    	
    	// check if Clusters file exists
    	if (!UncEpiSettings.selectedSurveyItem.clustersFilename.equals("")) {
    		filePath = "/sdcard/Download/Epiinfo/Questionnaires/Surveys/" + 
    	               UncEpiSettings.selectedSurveyItem.title + "/" +
    	               "clusterfile.kml";  // UncEpiSettings.selectedSurveyItem.clustersFilename;
    		File file = new File(filePath);
        	if (!file.exists()) {
        		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
	        		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " " + filePath + " does not exist!");
	        	}
        		return false;
        	}
        	else {
        		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
	        		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " IsSurveyFilesExist - Clusters File EXISTS!");
	        	}
        	}
    	}
    	
    	// check if Questionnaire Form file exists
    	if (!UncEpiSettings.selectedSurveyItem.formFilename.equals("")) {
    		filePath = "/sdcard/Download/Epiinfo/Questionnaires/Surveys/" + 
    	               UncEpiSettings.selectedSurveyItem.title + "/" +
    	               "questionaire.xml";  // UncEpiSettings.selectedSurveyItem.formFilename;
    		File file = new File(filePath);
        	if (!file.exists()) {
        		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
	        		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " " + filePath + " does not exist!");
	        	}
        		return false;
        	}
        	else {
        		if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
	        		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " IsSurveyFilesExist - Form File EXISTS!");
	        	}
        	}
    	}
    	
    	if (Constants.LOGS_ENABLED_SURVEYDOWNLOAD) {
    		Log.v(Constants.LOGTAG, " " + SurveyDownloadActivity.CLASSTAG + " IsSurveyFilesExist - All Files EXISTS!");
    	}
    	return true;
    	
    }
}