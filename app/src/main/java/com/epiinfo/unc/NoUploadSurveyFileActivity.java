package com.epiinfo.unc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.epiinfo.droid.R;
// import com.epiinfo.droid.RecordList;
import com.epiinfo.droid.RecordList;

  
public class NoUploadSurveyFileActivity extends Activity {

	private static final String CLASSTAG = NoUploadSurveyFileActivity.class.getSimpleName();
	
 	
    @Override
    public void onCreate(Bundle savedInstanceState) {
         
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.uploadsurveyfile_activity);
        setContentView(R.layout.uploadsurveyfile2_activity);
        
        Bundle extras = getIntent().getExtras();
        
        Intent returnIntent = new Intent();
        UncEpiSettings.uploadFileResultSuccess = false;
        returnIntent.putExtra("result", UncEpiSettings.uploadFileResultSuccess);
        setResult(RESULT_OK, returnIntent);
        finish();                 
    }
}