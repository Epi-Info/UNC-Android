/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epiinfo.droid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.epiinfo.analysis.AnalysisMain;
import com.epiinfo.etc.Base64;
import com.epiinfo.etc.CustomListAdapter;
import com.epiinfo.etc.PBKDF2;
import com.epiinfo.etc.TextUtils;
import com.epiinfo.interpreter.CheckCodeEngine;
import com.epiinfo.unc.Constants;
import com.epiinfo.unc.PointItem;
import com.epiinfo.unc.UncEpiSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class RecordList extends AppCompatActivity {
	private static final String CLASSTAG = RecordList.class.getSimpleName();
	
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT   = 1;
    private static final int ACTIVITY_UPLOAD_FILE = 2;  // v0.9.61

	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int SYNC_ID = Menu.FIRST + 2;
	private static final int ANALYSIS_ID = Menu.FIRST + 4;
	private static final int CLOUD_ID = Menu.FIRST + 5;
	private static final int DELETE_ALL_ID = Menu.FIRST + 6;
	private static final int HELP_ID = Menu.FIRST + 7;
	private static final int SET_DEFAULT_ID = Menu.FIRST + 8;
	private static final int EXIT_DEFAULT_MODE_ID = Menu.FIRST + 9;
	private static final int BOX_SIGNIN_ID = Menu.FIRST + 10;
	private static final int BOX_SIGNOUT_ID = Menu.FIRST + 11;
	private static final int SEARCH_ID = Menu.FIRST + 13;
	private static final int QR_ID = Menu.FIRST + 14;

    public EpiDbHelper mDbHelper;
    private Cursor mNotesCursor;
    private String viewName;
    
    // #UNC - Start
    private String uncClusterPoint = "";  // passed to intent from UNC Map activity
    private String uncTitleName = "";     // V0.9.58 passed to intent from UNC Map activity
    private String uploadResultMessage = "";  // v0.9.61
    private String filename = "";  // v0.9.61
    private String nowString = "";  // v0.9.61
    // #UNC - End
    private Dialog locationDialog;
    private Dialog passwordDialog;
    private Dialog analysisDialog;
    private ProgressDialog progressDialog;
	private LineListFragment lineListFragment;
    private Spinner latSpinner;
    private Spinner longSpinner;
    private EditText txtPassword;
    private Cipher _aesCipher;
    private RecordList self;
    private ProgressDialog waitDialog;
    private FormMetadata formMetadata;
    private String fkeyGuid;
	private SearchView searchView;
	private MenuItem mnuSearch;
	private Boolean listOnly;
    
    // 09Jun2015 v0.9.52 Add support for database
  	private SQLiteDatabase epiDB= null;
  	
  	// 29Jul2015 v0.9.60
  	private boolean bCreateSyncFileInProgress = false;

  	// 09Oct2015 v0.9.63
  	private boolean bSyncFileNeeded = false;
  	
  	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Constants.LOGS_ENABLED_RECORDLIST) {
    		Log.d(Constants.LOGTAG, " RecordList onCreate - enter");
        }
        
        // #UNC Start 22May2015
		if (DeviceManager.IsPhone())
		{
			DeviceManager.SetOrientation(this, false);
		}
		else
		{
			DeviceManager.SetOrientation(this, true);
		}
		this.setTheme(R.style.AppTheme);
     	// #UNC End
        // DeviceManager.SetOrientation(this);
        
		// v0.9.60
		bCreateSyncFileInProgress = false;
		bSyncFileNeeded = false;  // v0.9.63
        listOnly = false;
		
        self = this;
        setContentView(R.layout.record_list);
		lineListFragment = (LineListFragment)getFragmentManager().findFragmentById(R.id.listFragment);
        AppManager.Started(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
        	viewName = extras.getString("ViewName");
        	if (extras.containsKey("ListOnly"))
            {
                listOnly = extras.getBoolean("ListOnly");
            }
        	// #UNC - Start
        	int index = viewName.lastIndexOf("/");
        	if (index != -1) {
    			viewName = viewName.substring(index+1);
        	}
        	
        	if (Constants.LOGS_ENABLED_RECORDLIST) {
        		Log.d(Constants.LOGTAG, " RecordList onCreate - viewname = " + viewName);
        		Log.d(Constants.LOGTAG, " selectedSurveyItem.surveyFilesFolderName = " + UncEpiSettings.selectedSurveyItem.surveyFilesFolderName);
        		Log.d(Constants.LOGTAG, " selectedSurveyItem.clustersFilename      = " + UncEpiSettings.selectedSurveyItem.clustersFilename);
        		Log.d(Constants.LOGTAG, " selectedSurveyItem.pointsFilename        = " + UncEpiSettings.selectedSurveyItem.pointsFilename);
        		Log.d(Constants.LOGTAG, " selectedSurveyItem.formFilename          = " + UncEpiSettings.selectedSurveyItem.formFilename);
        	}
        	
        	if (viewName.contains(".xml")) {
        		index = viewName.indexOf(".xml");
        		viewName = viewName.substring(0, index);
        		if (Constants.LOGS_ENABLED_RECORDLIST) {
        			Log.d(Constants.LOGTAG, " RecordList onCreate - viewname no extension = " + viewName);
        		}
        	}
        	
        	uncClusterPoint = extras.getString("UncClusterPoint");

			if (uncClusterPoint != null && !uncClusterPoint.equals("")) {
				UncEpiSettings.selectedPointItem = new PointItem();
				UncEpiSettings.selectedPointItem.name = uncClusterPoint;
			}
        	if (uncClusterPoint == null) {
        		uncClusterPoint = "";
        	}
        	extras.putString("UncClusterPoint",  "");  // reset bundle value
        	
        	// v0.9.58 - This will enable a different db table for each survey title to store the completed xml files
        	uncTitleName = extras.getString("UncTitleName");
        	if ((uncTitleName == null) || (uncTitleName.equals(""))) {
        		uncTitleName = UncEpiSettings.selectedSurveyItem.title;
        		if ((uncTitleName == null) || (uncTitleName.equals(""))) {
        			uncTitleName = UncEpiSettings.previousSurveyTitle;
        			if ((uncTitleName == null) || (uncTitleName.equals(""))) {
        				uncTitleName = "DefaultTitle";  // should never happen
        			}
        		}
        	}
        	
        	// **********  22Apr2015 - change folder location
        	// v0.9.58
        	// int i1 = UncEpiSettings.selectedSurveyItem.surveyFilesFolderName.indexOf("Epi");
        	// String s1 = UncEpiSettings.selectedSurveyItem.surveyFilesFolderName.substring(i1) + "/";
    		String s1 = "EpiInfo/Questionnaires/Surveys/" + uncTitleName + "/";
        	
    		if (Constants.LOGS_ENABLED_RECORDLIST) {
    			Log.d(Constants.LOGTAG, " *********** s1 = " + s1);
    		}

        	formMetadata = new FormMetadata(s1 + viewName + ".xml",this);
        	////FormMetadata view = new FormMetadata(UncEpiSettings.selectedSurveyItem.surveyFilesFolderName + viewName + ".xml");
        	// FormMetadata view = new FormMetadata("Epiinfo/Questionnaires/Surveys/K9/"+ viewName +".xml");
        	//// FormMetadata view = new FormMetadata("EpiInfo/Questionnaires/"+ viewName +".xml");
        	// #UNC - End
        	
        	// v0.9.58 - Use title name instead of view name
        	// mDbHelper = new EpiDbHelper(this, viewName);
			AppManager.AddFormMetadata(viewName, formMetadata);
        	mDbHelper = new EpiDbHelper(this,formMetadata, uncTitleName);
			AppManager.SetCurrentDatabase(mDbHelper);
        	mDbHelper.open();
        	try
        	{
        		fillData();
        	}
        	catch (Exception ex)
        	{
        		PromptUserForDelete("Data table no longer matches the form definition. Delete data table?");
        	}

        }
        
		LinearLayout bg = (LinearLayout) findViewById(R.id.lineListContainer);
		//bg.setBackgroundColor(0xFFFFFFFF);
		
		// #UNC - Start
		if (Constants.LOGS_ENABLED_RECORDLIST) {
			Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " onCreate - viewName = " + viewName);
			Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " onCreate - uncClusterPoint = " + uncClusterPoint);
			Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " onCreate - uncTitleName = " + uncTitleName);
		}
		if (!uncClusterPoint.equals("")) {
			// Log.d(Constants.LOGTAG, " RecordList onCreate call createNote *****************");
			//createNote();
			// finish();
		}
		// 23Apr2015 - Do this to prevent onCreate from getting called twice
		//             onCreate is called twice in activities that create Thread or Runnable, AsyncTask
		// v0.9.58 - add check for uncClusterPoint
		// if (savedInstanceState == null) {
        if (!listOnly) {
            if ((savedInstanceState == null) && (!uncClusterPoint.equals(""))) {
                createNote();
            }
        }
		
		// 10Jun2015 v0.9.52 - Add long click to delete a record

    }
    
    public void promptToDeleteRecord(final long recordId) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Record List");
    	builder.setMessage("Delete Record ID: " + recordId + "?")       
    	.setCancelable(false)       
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
    	{           
    		public void onClick(DialogInterface dialog, int id) {                
    			mDbHelper.deleteRecord(recordId);
				fillData();
    		}       
    	})       
    	.setNegativeButton("No", new DialogInterface.OnClickListener() 
    	{           
    		public void onClick(DialogInterface dialog, int id) {
    			dialog.cancel();           
    		}       
    	});
    	builder.create();
    	builder.show();
    }

    
    // #UNC - Start
    /********************************** causes error in progressDialog.dismiss with window manager */
    @Override
    protected void onResume() {
    	if (Constants.LOGS_ENABLED_RECORDLIST) {
    		Log.d(Constants.LOGTAG, " RecordList onResume - Enter ****************************");
    	}
        super.onResume();
        if (!uncClusterPoint.equals("")) {
			// createNote();
		}
        
        // v0.9.63
        if (Constants.LOGS_ENABLED_RECORDLIST) {
    		Log.d(Constants.LOGTAG, " RecordList onResume - bSyncFile = " + bSyncFileNeeded);
    	}
        if (bSyncFileNeeded) {
        	bSyncFileNeeded = false;
        	if (!bCreateSyncFileInProgress) {
				bCreateSyncFileInProgress = true;
				showDialog(7);
			}
        }
        
        if (Constants.LOGS_ENABLED_RECORDLIST) {
        	Log.d(Constants.LOGTAG, " RecordList onResume - Exit ****************************");
        }
    }
    /***********************************/
    // #UNC - End
    
    // #UNC Start 22May2015
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		if (Constants.LOGS_ENABLED_RECORDLIST) {
 			Log.d(Constants.LOGTAG, " " + "RecordList onConfigurationChanged");
 		}
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 	}
 	// #UNC End
 	
    private void SubscribeButtonEvents()
    {
    	/*Button btnAddRecord = (Button) findViewById(R.id.btnAddRecord);
    	// #UNC - Start
    	if (btnAddRecord != null) {
    		btnAddRecord.setOnClickListener(new View.OnClickListener() {
			
    			@Override
    			public void onClick(View v) {
    				if (uncClusterPoint.equals(""))  // v0.9.54
    					createNote();				
    			}
    		});
    	}
    	
    	Button btnGenerateMap = (Button) findViewById(R.id.btnGenerateMap);
    	// #UNC - Start
    	if (btnGenerateMap != null) {
    		btnGenerateMap.setOnClickListener(new View.OnClickListener() {
			
    			@Override
    			public void onClick(View v) {
    				showDialog(5);				
    			}
    		});
    	}
    	
    	Button btnCreateSyncFile = (Button) findViewById(R.id.btnCreateSyncFile);
    	// #UNC - Start
    	if (btnCreateSyncFile != null) {
    		btnCreateSyncFile.setOnClickListener(new View.OnClickListener() {
			
    			@Override
    			public void onClick(View v) {
    				// UNC - v0.9.60
    				if (!bCreateSyncFileInProgress) {
    					bCreateSyncFileInProgress = true;
    					showDialog(7);
    				}
    			}
			});
    	}*/
    }
    
    @Override
    public void onRestart()
    {
    	super.onRestart();
    	AppManager.Started(this);
    }
    
    @Override
    public void onStop()
    {
    	if (progressDialog != null)
    	{
    		progressDialog.dismiss();
    		removeDialog(6);
    	}
    	AppManager.Closed(this);
    	super.onStop();
    }

	public void fillData() {

		mDbHelper.fetchTopOne();

		String fieldName1;
		String fieldName2;
		String fieldName3;
		String[] from = new String[1];
		int[] to = new int[1];

		if (formMetadata.DataFields.size() > 2)
		{

			fieldName1 = formMetadata.DataFields.get(3).getName();
			fieldName2 = formMetadata.DataFields.get(4).getName();
			fieldName3 = formMetadata.DataFields.get(2).getName();
			if (fkeyGuid != null && fkeyGuid.length() > 0)
			{
				mNotesCursor = mDbHelper.fetchWhere(fieldName1, fieldName2, fieldName3, "FKEY = '" + fkeyGuid + "'");
			}
			else
			{
				mNotesCursor = mDbHelper.fetchLineListing(fieldName1, fieldName2, fieldName3);
			}
			from = new String[]{"_id", "columnName1", fieldName1, "columnName2", fieldName2, "columnName3", fieldName3, "_syncStatus"};
			to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.header3, R.id.text3, R.id.header4, R.id.text4, R.id.hiddenText};
		}
		else if (formMetadata.DataFields.size() == 2)
		{
			fieldName1 = formMetadata.DataFields.get(3).getName();
			fieldName2 = formMetadata.DataFields.get(4).getName();
			if (fkeyGuid != null && fkeyGuid.length() > 0)
			{
				mNotesCursor = mDbHelper.fetchWhere(fieldName1, fieldName2, "FKEY = '" + fkeyGuid + "'");
			}
			else
			{
				mNotesCursor = mDbHelper.fetchLineListing(fieldName1, fieldName2);
			}
			from = new String[]{"_id", "columnName1", fieldName1, "columnName2", fieldName2, "_syncStatus"};
			to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.header3, R.id.text3, R.id.hiddenText};
		}
		else if (formMetadata.DataFields.size() == 1)
		{
			fieldName1 = formMetadata.DataFields.get(3).getName();
			if (fkeyGuid != null && fkeyGuid.length() > 0)
			{
				mNotesCursor = mDbHelper.fetchWhere(fieldName1, "FKEY = '" + fkeyGuid + "'");
			}
			else
			{
				mNotesCursor = mDbHelper.fetchLineListing(fieldName1);
			}
			from = new String[]{"_id", "columnName1", fieldName1, "_syncStatus"};
			to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.hiddenText};
		}
		else
		{
			//Alert(getString(R.string.no_fields));
			this.finish();
		}

		startManagingCursor(mNotesCursor);
		CustomListAdapter notes = new CustomListAdapter(this, R.layout.line_list_row, mNotesCursor, from, to);
		lineListFragment.setListAdapter(notes);
		this.setTitle(viewName.replace("_", "").toUpperCase() + " - " + String.format(getString(R.string.record_count), mNotesCursor.getCount()));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView = new SearchView(this);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String searchTerm) {

				try
				{
					mDbHelper.fetchTopOne();

					String query = BuildQuery(searchTerm);


					String fieldName1;
					String fieldName2;
					String fieldName3;
					String[] from = new String[1];
					int[] to = new int[1];

					if (formMetadata.DataFields.size() > 2)
					{

						fieldName1 = formMetadata.DataFields.get(3).getName();
						fieldName2 = formMetadata.DataFields.get(4).getName();
						fieldName3 = formMetadata.DataFields.get(2).getName();
						mNotesCursor = mDbHelper.fetchWhere(fieldName1, fieldName2, fieldName3, query);
						from = new String[]{"_id", "columnName1", fieldName1, "columnName2", fieldName2, "columnName3", fieldName3, "_syncStatus"};
						to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.header3, R.id.text3, R.id.header4, R.id.text4, R.id.hiddenText};
					}
					else if (formMetadata.DataFields.size() == 2)
					{
						fieldName1 = formMetadata.DataFields.get(3).getName();
						fieldName2 = formMetadata.DataFields.get(4).getName();
						mNotesCursor = mDbHelper.fetchWhere(fieldName1, fieldName2, query);
						from = new String[]{"_id", "columnName1", fieldName1, "columnName2", fieldName2, "_syncStatus"};
						to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.header3, R.id.text3, R.id.hiddenText};
					}
					else if (formMetadata.DataFields.size() == 1)
					{
						fieldName1 = formMetadata.DataFields.get(3).getName();
						mNotesCursor = mDbHelper.fetchWhere(fieldName1, query);
						from = new String[]{"_id", "columnName1", fieldName1, "_syncStatus"};
						to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.hiddenText};
					}

					startManagingCursor(mNotesCursor);
					CustomListAdapter notes = new CustomListAdapter(self, R.layout.line_list_row, mNotesCursor, from, to);
					lineListFragment.setListAdapter(notes);


				}
				catch (Exception ex)
				{
					fillData();
				}

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

				if (newText.equals(""))
				{
					fillData();
				}

				return false;
			}
		});


		mnuSearch = menu.add(0, SEARCH_ID, 0, R.string.menu_search);
		mnuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		mnuSearch.setActionView(searchView);
		mnuSearch.setIcon(com.epiinfo.droid.R.drawable.action_search);

		/*MenuItem mnuQR = menu.add(0, QR_ID, 1, R.string.menu_barcode);
		mnuQR.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		mnuQR.setIcon(gov.cdc.epiinfo.R.drawable.qrcode_scan);*/

		MenuItem mnuCloud = menu.add(0, CLOUD_ID,2, R.string.menu_cloud_sync);
		mnuCloud.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuSync = menu.add(0, SYNC_ID,3, R.string.menu_sync_file);
		mnuSync.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuDeleteAll = menu.add(0, DELETE_ALL_ID,4, R.string.menu_delete_all);
		mnuDeleteAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuHelp = menu.add(1, HELP_ID,7, R.string.menu_help);
		mnuHelp.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	private String BuildQuery(String searchTerm)
	{
		String query = "";
		if (fkeyGuid != null && fkeyGuid.length() > 0)
		{
			query += "FKEY = '" + fkeyGuid + "' AND (";
		}
		if (searchTerm.contains("=") || searchTerm.contains("%"))
		{
			query += searchTerm;
		}
		else
		{
			for (int x = 0; x < formMetadata.DataFields.size(); x++)
			{
				if (x > 0)
				{
					query += " OR ";
				}
				query += formMetadata.DataFields.get(x).getName() + " like '%" + searchTerm + "%' ";
			}
		}
		if (fkeyGuid != null && fkeyGuid.length() > 0)
		{
			query += ")";
		}
		return query;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case INSERT_ID:
            createNote();
            return true;
        case CLOUD_ID:
        	doCloudSync();
        	return true;
        case SYNC_ID:
        	showDialog(7);
        	return true;
        case ANALYSIS_ID:
        	LoadAnalysis();
        	return true;
        }

		return super.onOptionsItemSelected(item);
    }
    
    private void doCloudSync()
    {
    	waitDialog = ProgressDialog.show(this, "Cloud Sync", "Synchronizing with Amazon Cloud...", true);
    	new CloudSynchronizer().execute(true);
    }
    
    private class CloudSynchronizer extends AsyncTask<Boolean,Void, Integer>
    {
		@Override
		protected Integer doInBackground(Boolean... params) {

			mDbHelper.SyncWithCloud(this);
			return 0;
		}
		
		@Override
        protected void onPostExecute(Integer result) {
            
			waitDialog.dismiss();
	    	fillData();
        }
    }
    
    private void LoadAnalysis()
    {
    	final Intent analysis = new Intent(this, AnalysisMain.class);
    	analysis.putExtra("ViewName", viewName);  // v0.9.58 @ToDo keep viewName or use UncTitleName ???
		startActivity(analysis);
    }
    
	@Override
	protected Dialog onCreateDialog(int id)
	{
		if (id == 5)
		{
			return showLocationDialog();
		}
		else if (id == 6)
		{
        	progressDialog = new ProgressDialog(this);
        	progressDialog.setTitle("Generating Map");
        	progressDialog.setMessage("Please wait while the app generates a map based on your data...");
        	progressDialog.setIndeterminate(true);
        	progressDialog.setCancelable(false);
            return progressDialog;
		}
		else if (id == 7)
		{
			// UNC - v0.9.60
			Toast toast= Toast.makeText(RecordList.this, "Please Wait - Creating Sync File...", Toast.LENGTH_LONG);
			
			// v0.9.61
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
			// toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
			
			toast.show();
			
			return showPasswordDialog();
		}
		return null;
	}

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// #UNC - Start
		// menu.add(0, DELETE_ID, 0, "Delete Record")
		// .setIcon(android.R.drawable.ic_menu_delete);
		// #UNC - End
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) 
		{
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			mDbHelper.deleteRecord(info.id);
			fillData();
			return true;
		}
    	return super.onContextItemSelected(item);
	}
    
    private Dialog showLocationDialog()
	{		
		locationDialog = new Dialog(this);
        locationDialog.setTitle("Map Settings");
        locationDialog.setContentView(R.layout.loc_dialog);
        locationDialog.setCancelable(true);
        
        latSpinner = (Spinner) locationDialog.findViewById(R.id.cbxLatitude);
    	latSpinner.setPrompt("Please select a field that represents latitude");
    	
    	String[] stringValues = new String[formMetadata.NumericFields.size()];
    	for (int x=0;x<formMetadata.NumericFields.size();x++)
    	{
    		stringValues[x] = formMetadata.NumericFields.get(x).getName();
    	}
    	
    	ArrayAdapter<CharSequence> latAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, stringValues);
        latAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	latSpinner.setAdapter(latAdapter);        
    	
    	longSpinner = (Spinner) locationDialog.findViewById(R.id.cbxLongitude);
    	longSpinner.setPrompt("Please select a field that represents longitude");
    	
    	ArrayAdapter<CharSequence> longAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, stringValues);
        longAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	longSpinner.setAdapter(longAdapter);
    	
    	Button btnSet = (Button) locationDialog.findViewById(R.id.btnSet);
    	btnSet.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String latFieldName = latSpinner.getSelectedItem().toString();
				String longFieldName = longSpinner.getSelectedItem().toString();
				showDialog(6);
				createMap(latFieldName, longFieldName);
				
				locationDialog.dismiss();
				
			}
		});
        
        return locationDialog;
	}
    
    private void createMap(String latField, String longField)
    {
    	Cursor c = mDbHelper.fetchAllRecords();
    	LinkedList<Double> latitudes = new LinkedList<Double>();
    	LinkedList<Double> longitudes = new LinkedList<Double>();
    	if (c.moveToFirst())
    	{
    		do
    		{
    			double testLat = c.getDouble(c.getColumnIndexOrThrow(latField));
    			double testLng = c.getDouble(c.getColumnIndexOrThrow(longField));
    			
    			if (testLat < Double.POSITIVE_INFINITY && testLng < Double.POSITIVE_INFINITY)
    			{
    				latitudes.add(testLat);
    				longitudes.add(testLng);
    			}
    		}while (c.moveToNext());
    	}
    	
    	Intent i = new Intent(this, MapViewer.class);
    	Double[] latArray = new Double[latitudes.size()];
    	Double[] longArray = new Double[longitudes.size()];
    	latitudes.toArray(latArray);
    	longitudes.toArray(longArray);
    	i.putExtra("Latitudes", latArray);
    	i.putExtra("Longitudes", longArray);
        startActivity(i);
    }
    
    private Dialog showPasswordDialog()
	{
    	// v0.9.53 UNC
    	new SyncGenerator().execute("a");  // use "a" as default password
    	return null;
    	
    	/**********************************************
		passwordDialog = new Dialog(this);
        passwordDialog.setTitle("Set Password");
        passwordDialog.setContentView(R.layout.password_dialog);
        passwordDialog.setCancelable(false);
        
        txtPassword = (EditText) passwordDialog.findViewById(R.id.txtPassword);
    	    	
    	Button btnSet = (Button) passwordDialog.findViewById(R.id.btnSet);
    	btnSet.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				new SyncGenerator().execute(txtPassword.getText().toString());
				passwordDialog.dismiss();
				waitDialog = ProgressDialog.show(self, "Creating Encrypted Sync File", "Please wait...", true);
				
			}
		});
        
        return passwordDialog;
        ************************************************/
	}
    
        
    private boolean setupAesCipher(String password) {
    	// #UNC - Start
    	// String initVector = "000102030405060708090A0B0C0D0E0F"; // orig CDC Key 
        // String salt = "00010203040506070809";                   // orig CDC Key
    	String initVector = "0D1C120FF40C091708090B0A5A0D0E0F";    // UNC specific Key
    	String salt       = "06090501380901017309";                // UNC specific key
    	// #UNC - End
        
        int RFC = 1000;
        String _providerName = "BC";
        int _keyLengthInBits = 128;
        
        try
        {
            _aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", _providerName);
            int iterations = RFC;
            byte[] keyBytes= PBKDF2.deriveKey(password.getBytes(),
                                              TextUtils.HexStringToByteArray(salt),
                                              iterations,
                                              _keyLengthInBits/8);

            String keyString = TextUtils.ByteArrayToHexString(keyBytes);
            
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = TextUtils.HexStringToByteArray(initVector); 
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            _aesCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        }
        catch (Exception ex1)
        {
            return false; 
        }
        
        return true;
    }
    
    private String doEncrypt(String xml, String password) {
        
    	setupAesCipher(password);
    	
        byte[] plainText = xml.getBytes();
        
        try {
            
            byte[] result= _aesCipher.doFinal(plainText);
            return Base64.encode(result);
        }
        catch(Exception ex) {
            return "";
        }
    }
    
    private String createXml(String password)
    {
    	// #UNC - Start
   		filename  = "";
   		// String filename2 = "";
   		// String nowString;
   		// #UNC - End
   		
    	try
    	{
    	StringBuilder xml = new StringBuilder();
    	xml.append("<SurveyResponses>");
    	Cursor c = mDbHelper.fetchAllRecords();
    	if (c.moveToFirst())
    	{
    		do
    		{
    			xml.append("<SurveyResponse SurveyResponseId=\"" + c.getString(c.getColumnIndexOrThrow(EpiDbHelper.GUID)) + "\">");
    			int pageId = -99;
    			for (int x=0;x<formMetadata.DataFields.size();x++)
    			{
    				if (formMetadata.DataFields.get(x).getPageId() > pageId)
    				{
    					pageId = formMetadata.DataFields.get(x).getPageId();
    					if (x!=0)
    					{
    						xml.append("</Page>");
    					}
    					xml.append("<Page PageId=\"" + pageId + "\">");
    				}
    				if (formMetadata.DataFields.get(x).getType().equals("5"))
    				{
    					if (c.getDouble(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) == Double.POSITIVE_INFINITY)
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\"/>");
    					}
    					else
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">" + c.getDouble(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) + "</ResponseDetail>");
    					}
    				}
    				else if (formMetadata.DataFields.get(x).getType().equals("7"))
    				{
    					if (c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())).equals(""))
    					{    						
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\"/>");
    					}
    					else
    					{
    						String strDate = c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName()));
    						try
    						{
    							strDate = DateFormat.getDateInstance().format(new SimpleDateFormat("dd/MM/yyyy").parse(strDate));
    						}
    						catch (Exception ex)
    						{
    							
    						}
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">"+ strDate +"</ResponseDetail>");
    					}
    				}
    				else if (formMetadata.DataFields.get(x).getType().equals("8"))
    				{
    					if (c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())).equals(""))
    					{    						
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\"/>");
    					}
    					else
    					{
    						String strTime = c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName()));
    						if (!strTime.toLowerCase().contains("m"))
    						{
    							try
    							{
    								strTime = new SimpleDateFormat("h:mm a").format(new SimpleDateFormat("HH:mm").parse(strTime));
    							}
    							catch (Exception ex)
    							{
    							
    							}
    						}
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">"+ strTime +"</ResponseDetail>");
    					}
    				}
    				else if (formMetadata.DataFields.get(x).getType().equals("10"))
    				{
    					if (c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) == 0)
    					{    						
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">No</ResponseDetail>");
    					}
    					else
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">Yes</ResponseDetail>");
    					}
    				}
    				else if (formMetadata.DataFields.get(x).getType().equals("11"))
    				{
    					if (c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) > 0)
    					{
    						if (c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) == 1)
    						{
    							xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">1</ResponseDetail>");
    						}
    						else
    						{
    							xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">0</ResponseDetail>");
    						}
    					}
    					else
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\"/>");
    					}
    				}
    				else if (formMetadata.DataFields.get(x).getType().equals("12"))
    				{
    					if (c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) > 0)
    					{
    						int rawRadioVal = c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName()));
    						int radioVal = rawRadioVal % 1000;
   							xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">" + radioVal + "</ResponseDetail>");
    					}
    					else
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\"/>");
    					}
    				}
    				else if (formMetadata.DataFields.get(x).getType().equals("17") || formMetadata.DataFields.get(x).getType().equals("19"))
    				{
    					if (c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) > 0)
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">" + formMetadata.DataFields.get(x).getListValues().get(c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName()))) + "</ResponseDetail>");
    					}
    					else
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\"/>");
    					}
    				}
    				else
    				{
    					if (c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())).equals(""))
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\"/>");
    					}
    					else
    					{
    						xml.append("<ResponseDetail QuestionName=\"" + formMetadata.DataFields.get(x).getName() + "\">" + c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</ResponseDetail>");
    					}
    				}
    			}
    			xml.append("</Page></SurveyResponse>");
    		} while (c.moveToNext());
    	}
    	xml.append("</SurveyResponses>");
    	
    		Calendar cal = Calendar.getInstance();
    		nowString = cal.get(Calendar.YEAR) + String.format("%02d", cal.get(Calendar.MONTH) + 1) + String.format("%02d", cal.get(Calendar.DATE)) + String.format("%02d", cal.get(Calendar.HOUR)) + String.format("%02d", cal.get(Calendar.MINUTE));
    		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    		path.mkdirs();
    		// #UNC - Start
    		// File file = new File(path, "/EpiInfo/SyncFiles/" + viewName + "_" + nowString + ".epi7");
    		File file  = new File(path, "/EpiInfo/SyncFiles/" + viewName + "_" + UncEpiSettings.username + "_" + nowString + ".epi7");
    		File file2 = new File(path, "/EpiInfo/SyncFiles/" + viewName + "_" + UncEpiSettings.username + "_" + nowString + ".xml");
    		filename  = viewName + "_" + UncEpiSettings.username + "_" + nowString + ".epi7";
    		// filename2 = viewName + "_" + UncEpiSettings.username + "_" + nowString + ".xml";
    		// #UNC - End
    		FileWriter fileWriter  = new FileWriter(file);
    		FileWriter fileWriter2 = new FileWriter(file2);
			BufferedWriter out  = new BufferedWriter(fileWriter);
			BufferedWriter out2 = new BufferedWriter(fileWriter2);   
			// #UNC - Start
			// out.write(xml.toString());  // UNC stub when we don't want to encrypt or do not have keys to match the PC IE7 app
			out.write(doEncrypt(xml.toString(), password));
			out2.write(xml.toString());    // no encryption
			// #UNC - End
			out.close();
			out2.close();
			
			// #UNC - start
			if (UncEpiSettings.IsNetworkAvailable(getApplicationContext())) {
				UncEpiSettings.uploadFileResultSuccess = true;   // v0.9.61
				if (Constants.LOGS_ENABLED_RECORDLIST) {
					Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " createXml - &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
					Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " createXml - Network Is Available");
				}
				Intent fileUploadIntent = new Intent(Constants.INTENT_ACTION_SURVEYUPLOAD);
				fileUploadIntent.putExtra("Filename",  viewName + "_" + UncEpiSettings.username + "_" + nowString + ".epi7");  // e.g. "DurhamCHOS_Hispanic_FINAL_201309090116.epi7");
				fileUploadIntent.putExtra("Filename2", viewName + "_" + UncEpiSettings.username + "_" + nowString + ".xml");
				fileUploadIntent.putExtra("uncClusterPoint", uncClusterPoint);  // v0.9.53, needed???
				fileUploadIntent.putExtra("Formname", UncEpiSettings.selectedSurveyItem.formFilename);  // v0.9.65
				// startActivity(fileUploadIntent);
				startActivityForResult(fileUploadIntent, ACTIVITY_UPLOAD_FILE);  // v0.9.61
			}
			else {
				UncEpiSettings.uploadFileResultSuccess = false;   // v0.9.61
				if (Constants.LOGS_ENABLED_RECORDLIST) {
					Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " createXml - &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
					Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " createXml - Network NOT Available!");
				}
				// v0.9.66 -add these 3 lines
				Intent fileUploadIntent = new Intent(Constants.INTENT_ACTION_NOSURVEYUPLOAD);
				startActivityForResult(fileUploadIntent, ACTIVITY_UPLOAD_FILE);
				// insertSyncFilenamesToDb(viewName + "_" + UncEpiSettings.username + "_" + nowString);
				// Alert("Tablet is offline! Survey files for " + viewName + "_" + UncEpiSettings.username + "_" + nowString + " will be transferred to server when tablet is online.");
    	    	// readSyncFilenamesFromDb();  // v0.9.53 TEMP CODE FOR UNIT TEST
			}
			// #UNC - End
    	}
    	catch (Exception ex)
    	{
    		// return "Error occurred: " + ex.toString();
    		// #UNC - Start
    		if (Constants.LOGS_ENABLED_RECORDLIST) {
    			Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " createXml - " + filename + " Exception &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    			Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + ex.toString());
    		}
    		return ("XML Format Error for " + filename + "!");
    		// #UNC - End
    	}
    	
    	// return "Data is ready to transfer into Epi Info 7. Please connect your Android device to your PC.";
    	// #UNC - Start
    	// v0.9.61
    	// if (UncEpiSettings.IsNetworkAvailable(getApplicationContext()))
    	if (Constants.LOGS_ENABLED_RECORDLIST) {
    		Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " CHECKING FILE UPLOAD STATUS #############################################");
    	}
    	return ("");
    	/*********************************** v0.9.61
    	if (UncEpiSettings.uploadFileResultSuccess) {
    		return ("Survey file " + filename + " has been transferred to server.");
    	}
    	else { // v0.9.53
    		insertSyncFilenamesToDb(viewName + "_" + UncEpiSettings.username + "_" + nowString);
    		return ("Tablet is offline! Survey files for " + viewName + "_" + UncEpiSettings.username + "_" + nowString + " will be transferred to server when tablet is online.");
    	}
    	******************************************/
    	// #UNC - End
    }

    private void createNote() {
    	if (Constants.LOGS_ENABLED_RECORDLIST) {
    		Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " createNote - Enter ************");
    	}
    	
    	// #UNC - start
    	String tt;
    	if (!uncClusterPoint.equals("")) {
    		tt = "Loading Form for Cluster-Point " + uncClusterPoint;
    	}
    	else {
    		tt = "Loading Form";	
    	}
    	waitDialog = ProgressDialog.show(this, tt, "Please wait...", true);
    	// #UNC - end
    	
    	// #UNC - start v0.9.48

    	if (!uncClusterPoint.equals("")) {
			UncEpiSettings.selectedPointItem = new PointItem();
    		UncEpiSettings.selectedPointItem.name = uncClusterPoint;
    		uncClusterPoint = "";  // reset
    	}
    	// #UNC - end
    	
    	Intent i = new Intent(this, RecordEditor.class);
    	new PreCompiledLoader().execute(i);
    }
    
    //@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);
        
        if (Constants.LOGS_ENABLED_RECORDLIST) {
        	Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " onListItemClick - Enter");
        }
        
        Cursor c = mDbHelper.fetchRecord(id);
        
        c.moveToPosition(0);
        waitDialog = ProgressDialog.show(this, "Loading Form", "Please wait...", true);
        Intent i = new Intent(this, RecordEditor.class);
        i.putExtra(EpiDbHelper.KEY_ROWID, id);
		i.putExtra(EpiDbHelper.GUID, c.getString(c.getColumnIndexOrThrow(EpiDbHelper.GUID)));
        
        for (int x=0;x<formMetadata.DataFields.size();x++)
		{
			if (formMetadata.DataFields.get(x).getType().equals("10") || formMetadata.DataFields.get(x).getType().equals("11") || formMetadata.DataFields.get(x).getType().equals("12") || formMetadata.DataFields.get(x).getType().equals("17") || formMetadata.DataFields.get(x).getType().equals("19"))
			{
				String fieldName = formMetadata.DataFields.get(x).getName();
				int columnIndex = c.getColumnIndexOrThrow(fieldName);
				int value = c.getInt(columnIndex);
				i.putExtra(fieldName, value);
			}
			else if (formMetadata.DataFields.get(x).getType().equals("5"))
			{
				String fieldName = formMetadata.DataFields.get(x).getName();
				int columnIndex = c.getColumnIndexOrThrow(fieldName);
				double value = c.getDouble(columnIndex);
				i.putExtra(fieldName, value);
			}
			else
			{
				String fieldName = formMetadata.DataFields.get(x).getName();
				int columnIndex = c.getColumnIndexOrThrow(fieldName);
				String value = c.getString(columnIndex);
				i.putExtra(fieldName, value);				
			}
		}
        new PreCompiledLoader().execute(i);        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	if (intent != null)
    	{
    		if (Constants.LOGS_ENABLED_RECORDLIST) {
    			Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " onActivityResult - Enter ^^^^^^^^^^^^^^^^^^^^^^^^^");
    			Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " onActivityResult - requestCode=" + requestCode + " , resultCode=" + resultCode);
    		}
    		
    		if (requestCode == ACTIVITY_UPLOAD_FILE) {
    			boolean result = false;
    			if (resultCode == RESULT_OK) {
            		intent.getBooleanExtra("result", result);
    	        }
    			if (UncEpiSettings.uploadFileResultSuccess) {
    	    		Alert("Survey file " + filename + " has been transferred to server.");
    	    	}
    	    	else { // v0.9.53
    	    		insertSyncFilenamesToDb(viewName + "_" + UncEpiSettings.username + "_" + nowString);
    	    		Alert("Tablet is offline! Survey files for " + viewName + "_" + UncEpiSettings.username + "_" + nowString + " will be transferred to server when tablet is online.");
    	    	}
    			
    		}
    		else { // must be ACTIVITY_CREATE or ACTIVITY_EDIT from RecordEditor activity
    			Bundle extras = intent.getExtras();
    			if (extras != null) {
    				ContentValues initialValues = new ContentValues();
    				for (int x=0;x<formMetadata.DataFields.size();x++) {
    					if (formMetadata.DataFields.get(x).getType().equals("10") || formMetadata.DataFields.get(x).getType().equals("11") || formMetadata.DataFields.get(x).getType().equals("12") || formMetadata.DataFields.get(x).getType().equals("17") || formMetadata.DataFields.get(x).getType().equals("19")) {
    						initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getInt(formMetadata.DataFields.get(x).getName()));
    					}
    					else if (formMetadata.DataFields.get(x).getType().equals("5")) {
    						initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getDouble(formMetadata.DataFields.get(x).getName()));
    					}
    					else {
    						initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getString(formMetadata.DataFields.get(x).getName()));
    					}
    				}
    			
    				switch (requestCode) {
    					case ACTIVITY_CREATE:
    						//mDbHelper.createRecord(initialValues, true, null,null);
    						fillData();
    						// v0.9.63
    						if (resultCode == RESULT_OK)
    							bSyncFileNeeded = true;
    						break;
    					case ACTIVITY_EDIT:
    						Long mRowId = extras.getLong(EpiDbHelper.KEY_ROWID);
    						String mRowGuid = extras.getString(EpiDbHelper.GUID);
    						if (mRowId != null) {
    							mDbHelper.updateRecord(mRowId, initialValues, true);
    						}
    						fillData();
    						// v0.9.63
    						if (resultCode == RESULT_OK)
    							bSyncFileNeeded = true;
    						break;
    				}
    			}
    		}
    	}
    }
    
    public void PromptUserForDelete(String message)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message)       
    	.setCancelable(false)       
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
    	{           
    		public void onClick(DialogInterface dialog, int id) 
    		{    
    			// v0.9.58 - Use Title name instead of View name to support different db tables for each survey
    			// mDbHelper.DropDatabase(viewName);
    			mDbHelper.DropDatabase(uncTitleName);
    			mDbHelper.close();
    			finish();
    		}       
    	})       
    	.setNegativeButton("No", new DialogInterface.OnClickListener() 
    	{           
    		public void onClick(DialogInterface dialog, int id) 
    		{                
    			dialog.cancel();           
    			finish();
    		}       
    	});
    	builder.create();
    	builder.show();
    }
    
    public void Alert(String message)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	// #UNC - Start
    	builder.setTitle("EPI Info Survey");
    	builder.setIcon(R.drawable.epi_icon);
    	// #UNC - End
    	builder.setMessage(message)       
    	.setCancelable(false)       
    	.setPositiveButton("OK", new DialogInterface.OnClickListener() 
    	{           
    		public void onClick(DialogInterface dialog, int id) 
    		{                
    			dialog.cancel();  
    			// #UNC - Start
	        	finish();
	        	// #UNC - End
    		}       
    		});
    	builder.create();
    	builder.show();
    }
    
    private class PreCompiledLoader extends AsyncTask<Intent,Void, Intent>
    {

		@Override
		protected Intent doInBackground(Intent... params) {
			
			if (formMetadata.Context == null)
				formMetadata.Context = new CheckCodeEngine(getAssets()).PreCompile(formMetadata.CheckCode);
			AppManager.AddFormMetadata(viewName, formMetadata);
			return params[0];
			
		}
		
		@Override
        protected void onPostExecute(Intent i) {
            
			//#UNC - Start
			if (waitDialog != null)
			// #UNC - End
				waitDialog.dismiss();
			
			if (i.getExtras() != null)
			{
				i.putExtra("ViewName", viewName);
				startActivityForResult(i, ACTIVITY_EDIT);
			}
			else
			{
				i.putExtra("ViewName", viewName);
				i.putExtra("NewGuid", UUID.randomUUID().toString());
				startActivityForResult(i, ACTIVITY_CREATE);
			}
        }
    	
    }
    
    private class SyncGenerator extends AsyncTask<String,Void, String>
    {

		@Override
		protected String doInBackground(String... params) {

			return createXml(params[0]);
		}
		
		@Override
        protected void onPostExecute(String message) {
            
			if (waitDialog != null)  // v0.9.53 UNC
				waitDialog.dismiss();
			bCreateSyncFileInProgress = false;  // UNC v0.9.60
			// Alert(message);  // v0.9.61
        }
    	
    }
    
    // v0.9.53 - UNC
    private int insertSyncFilenamesToDb(final String savedFileName) {
		if (Constants.LOGS_ENABLED_DATABASE) {
			Log.v(Constants.LOGTAG, " " + RecordList.CLASSTAG + " insertSyncFilenamesToDb - Enter");
		}
		
		// Create or Open the Database
		try {
			epiDB = openOrCreateDatabase(Constants.EPI_DB_NAME, getApplicationContext().MODE_PRIVATE, null);

			// Create the Points Table in the Database
			epiDB.execSQL("CREATE TABLE IF NOT EXISTS "
					+ Constants.SYNC_FILES_TABLE_NAME
					// v0.9.65 add a formFilename field
					// + " (ClusterField1 VARCHAR, PointField2 VARCHAR, LatitudeField3 VARCHAR, LongitudeField4 VARCHAR, FilenameField5 VARCHAR);");
					+ " (ClusterField1 VARCHAR, PointField2 VARCHAR, LatitudeField3 VARCHAR, LongitudeField4 VARCHAR, FilenameField5 VARCHAR, FormnameField6 VARCHAR);");

			String pointName = "";
			if (!uncClusterPoint.equals(""))
			{
				pointName = uncClusterPoint;
			}
			else if (!UncEpiSettings.selectedPointItem.name.equals(""))
			{
				pointName = UncEpiSettings.selectedPointItem.name;
			}
			else if (!UncEpiSettings.tempPointName.equals(""))
			{
				pointName = UncEpiSettings.tempPointName;
			}

			String cluster = UncEpiSettings.getClusterId(pointName);//.selectedPointItem.name);// uncClusterPoint);
			String point   = UncEpiSettings.getPointId(pointName);//.selectedPointItem.name);// uncClusterPoint);
			String lat     = Double.toString(UncEpiSettings.latitude);
			String lon     = Double.toString(UncEpiSettings.longitude);

			UncEpiSettings.selectedPointItem.name="";
			
			if (Constants.LOGS_ENABLED_DATABASE) {
				Log.v(Constants.LOGTAG, " " + RecordList.CLASSTAG + " insertSyncFilenamesToDb - cluster = " + cluster);
				Log.v(Constants.LOGTAG, " " + RecordList.CLASSTAG + " insertSyncFilenamesToDb - point = " + point);
				Log.v(Constants.LOGTAG, " " + RecordList.CLASSTAG + " insertSyncFilenamesToDb - latitude = " + lat);
				Log.v(Constants.LOGTAG, " " + RecordList.CLASSTAG + " insertSyncFilenamesToDb - longitude = " + lon);
				Log.v(Constants.LOGTAG, " " + RecordList.CLASSTAG + " insertSyncFilenamesToDb - filename = " + savedFileName);
				Log.v(Constants.LOGTAG, " " + RecordList.CLASSTAG + " insertSyncFilenamesToDb - formname = " + UncEpiSettings.selectedSurveyItem.formFilename);
			}

			// insert both sync filename into the database
			epiDB.execSQL("INSERT INTO " 
				            + Constants.SYNC_FILES_TABLE_NAME
				            + " (ClusterField1, PointField2, LatitudeField3, LongitudeField4, FilenameField5, FormnameField6)"
				            // v0.9.65 add field for formname
				            // + " VALUES ('" + cluster + "', '" + point + "', '" + lat + "', '" + lon + "', '" + savedFileName + "');");
				               + " VALUES ('" + cluster + "', '" + point + "', '" + lat + "', '" + lon + "', '" + savedFileName + "', '" + UncEpiSettings.selectedSurveyItem.formFilename + "');");
			
			if (Constants.LOGS_ENABLED_DATABASE) {
				Log.d(Constants.LOGTAG, " " + RecordList.CLASSTAG + " insertSyncFilenamesToDb - record Inserted");
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

}
