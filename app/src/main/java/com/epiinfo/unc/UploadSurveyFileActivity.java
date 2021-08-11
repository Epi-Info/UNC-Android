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
import android.widget.TextView;
import android.widget.Toast;

import com.epiinfo.droid.R;
// import com.epiinfo.droid.RecordList;

  
public class UploadSurveyFileActivity extends Activity {

	private static final String CLASSTAG = UploadSurveyFileActivity.class.getSimpleName();
	
    private TextView messageText;
    // private Button uploadButton;
    private int serverResponseCode = 0;
    private ProgressDialog dialog = null;
        
    final String uploadServerPhpUri = Constants.EPI_FILE_UPLOAD_URI;  // PHP script on server
     
    /**********  File Path *************/
    // final String uploadFilePath = "/mnt/sdcard/";
    final String uploadFilePath = "/mnt/sdcard/Download/EpiInfo/SyncFiles/";
    // final String uploadFileName = "XpressConnect.log";
    private String uploadFileName  = "";  // .epi7 encrypted file      e.g. DurhamCHOS_County_FINAL_201309050116.epi7;
    private String uploadFileName2 = "";  // .xml non-encrypted file   e.g. DurhamCHOS_County_FINAL_201309050116.xml;
    private String uploadFormName  = "";  // v0.9.65 extra param
 	
    @Override
    public void onCreate(Bundle savedInstanceState) {
         
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.uploadsurveyfile_activity);
        setContentView(R.layout.uploadsurveyfile2_activity);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	uploadFileName  = extras.getString("Filename");
        	uploadFileName2 = extras.getString("Filename2");
        	uploadFormName  = extras.getString("Formname");
        	// uncClusterPoint = extras.getString("uncClusterPoint");
        }

        // uploadButton = (Button)findViewById(R.id.uploadButton);
        messageText  = (TextView)findViewById(R.id.messageText);
        
        String uploadFileNameStripped;
        int index = uploadFileName.indexOf(".epi7");
        if (index != -1)
        	uploadFileNameStripped = uploadFileName.substring(0, index);
        else
        	uploadFileNameStripped = uploadFileName;	
        
        messageText.setText("Uploading file path: '" + uploadFilePath + uploadFileNameStripped + "'");
        		
        dialog = ProgressDialog.show(UploadSurveyFileActivity.this, "", "Uploading " + uploadFileNameStripped, true);
        
        new Thread(new Runnable() {
        	public void run() {
        		runOnUiThread(new Runnable() {
        			public void run() {
        				messageText.setText("uploading started.....");
        			}
        		});
                
        		// v0.9.61 retry upload operation if it fails
        		int retVal = -1;
        		for (int i=0; i<Constants.UPLOAD_FILE_RETRY_LIMIT; i++) {
        			if (uploadFileName != null) {
        				retVal = uploadFile(uploadFilePath + "" + uploadFileName, uploadFileName);
        				if (retVal == 0)
        					i = Constants.UPLOAD_FILE_RETRY_LIMIT;
        			}
        		}
        		if (retVal == 0) {
        			for (int i=0; i<Constants.UPLOAD_FILE_RETRY_LIMIT; i++) {
        				if (uploadFileName2 != null) {
        					retVal = uploadFile(uploadFilePath + "" + uploadFileName2, uploadFileName2);
        					if (retVal == 0)
        						i = Constants.UPLOAD_FILE_RETRY_LIMIT;
        				}
        			}
        		}
        		
        		// v0.9.61
        		if (Constants.LOGS_ENABLED_UPLOAD_SURVEY_FILE) {
         		   Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " onCreate Exit retVal = " + retVal);
        		}
        		if (retVal == 0) UncEpiSettings.uploadFileResultSuccess = true;
        		else UncEpiSettings.uploadFileResultSuccess = false;
        		
        		dialog.dismiss();  // v0.9.61
        		Intent returnIntent = new Intent();
                returnIntent.putExtra("result", UncEpiSettings.uploadFileResultSuccess);
                setResult(RESULT_OK, returnIntent);
                finish();                 
        	}
        }).start();
        
        /***********************************
        dialog.dismiss();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", UncEpiSettings.uploadFileResultSuccess);
        setResult(RESULT_OK, returnIntent);
        finish();
        ***********************************/
    }
      
    private int uploadFile(String sourceFileUri, String destFileName) {
    	if (Constants.LOGS_ENABLED_UPLOAD_SURVEY_FILE) {
			Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " uploadFile - Enter");
			Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " sourceFileUri = " + sourceFileUri);
			Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " destFileName = " + destFileName);
			Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " uploadFormName = " + uploadFormName);
			Log.d(Constants.LOGTAG, "*************************************************************");
			Log.d(Constants.LOGTAG, "*************************************************************");
		}
    	
          String fileName = sourceFileUri;
  
          HttpURLConnection conn = null;
          DataOutputStream dos = null; 
          String lineEnd = "\r\n";
          String twoHyphens = "--";
          String boundary = "*****";
          int bytesRead, bytesAvailable, bufferSize;
          byte[] buffer;
          int maxBufferSize = 1 * 1024 * 1024;
          File sourceFile = new File(sourceFileUri);
          int retVal = -1; // v0.9.61, assume error
           
          if (!sourceFile.isFile()) {
               
               // dialog.dismiss();  // v0.9.61
                
               Log.e("uploadFile", "Source File not exist :"
                                   + uploadFilePath + "" + uploadFileName);
                
               runOnUiThread(new Runnable() {
                   public void run() {
                       messageText.setText("Source File not exist :"
                               + uploadFilePath + "" + uploadFileName);
                   }
               });
                
               return retVal;  // v0.9.61
          }
          else
          {
               try {
            	   if (Constants.LOGS_ENABLED_UPLOAD_SURVEY_FILE) {
            		   Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " uploadFile - try Enter");
            		   Log.d(Constants.LOGTAG, "*************************************************************");
            		   Log.d(Constants.LOGTAG, "*************************************************************");
           		   }
                    
                     // open a URL connection to the Server so we can stream the file to it
                   FileInputStream fileInputStream = new FileInputStream(sourceFile);
                   // KRC v0.9.47 - call new URI on server that uses a different folder location for upload files
                   //               and it also has a p1 param for survey name
                   // URL url = new URL(uploadServerPhpUri);
                   // URL url = new URL(uploadServerPhpUri + "?p1=" + UncEpiSettings.selectedSurveyItem.title);
                   
                   // v0.9.65
                   // int i1 = UncEpiSettings.selectedSurveyItem.formFilename.lastIndexOf("/");       // "/Surveys/Keith637_22-10-2015/questionaire.xml"
                   // String s1 = UncEpiSettings.selectedSurveyItem.formFilename.substring(0, i1+1);  // "/Surveys/Keith637_22-10-2015/"
                   int i1 = uploadFormName.lastIndexOf("/");       // "/Surveys/Keith637_22-10-2015/questionaire.xml"
                   String s1 = uploadFormName.substring(0, i1+1);  // "/Surveys/Keith637_22-10-2015/"
                   
                   URL url = new URL(uploadServerPhpUri + "?p1=" + s1 + destFileName);
                   if (Constants.LOGS_ENABLED_UPLOAD_SURVEY_FILE) {
            		   Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " url = " + url.toString());
           		   }
                   
                   
                   // Open a HTTP  connection to  the URL
                   if (Constants.LOGS_ENABLED_UPLOAD_SURVEY_FILE) {
            		   Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " uploadFile - url.openConnection start");
                   }
                   conn = (HttpURLConnection) url.openConnection();
                   if (Constants.LOGS_ENABLED_UPLOAD_SURVEY_FILE) {
            		   Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " uploadFile - url.openConnection finish");
                   }
                   conn.setDoInput(true); // Allow Inputs
                   conn.setDoOutput(true); // Allow Outputs
                   conn.setUseCaches(false); // Don't use a Cached Copy
                   conn.setRequestMethod("POST");
                   
                   conn.setRequestProperty("Connection", "Keep-Alive");
                   
                   // v0.9.66 increase timeout due to server taking long time to prcess multiple surveys in 1 upload file
                   // conn.setConnectTimeout(10000);  // wait 10 seconds
                   conn.setConnectTimeout(60000);  // wait 60 seconds
                   conn.setReadTimeout(10000);     // wait 10 seconds
                   
                   conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                   conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                   conn.setRequestProperty("uploaded_file", fileName);
                    
                   dos = new DataOutputStream(conn.getOutputStream());
          
                   dos.writeBytes(twoHyphens + boundary + lineEnd);
                   /************************
                   dos.writeBytes("Content-Disposition: form-data; name="uploaded_file";filename=""
                                             + fileName + """ + lineEnd);
                   ***************/
                   dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                           + fileName + "\"" + lineEnd);
                           	
                   
                   dos.writeBytes(lineEnd);
          
                   // create a buffer of  maximum size
                   bytesAvailable = fileInputStream.available();
          
                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
                   buffer = new byte[bufferSize];
          
                   // read file and write it into form...
                   bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
                      
                   while (bytesRead > 0) {     
                     dos.write(buffer, 0, bufferSize);
                     bytesAvailable = fileInputStream.available();
                     bufferSize = Math.min(bytesAvailable, maxBufferSize);
                     bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
                   }
          
                   // send multipart form data necessary after file data...
                   dos.writeBytes(lineEnd);
                   dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
          
                   // Responses from the server (code and message)
                   serverResponseCode = conn.getResponseCode();
                   final String serverResponseMessage = conn.getResponseMessage();
                     
                   // Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                   if (Constants.LOGS_ENABLED_UPLOAD_SURVEY_FILE) {
            		   Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " uploadFile - Server HTTP Resp Code = " + serverResponseCode);
            		   Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " uploadFile - Server HTTP Resp Msg = " + serverResponseMessage);
            		   Log.d(Constants.LOGTAG, "*************************************************************");
            		   Log.d(Constants.LOGTAG, "*************************************************************");
           		   }
                   
                   if (serverResponseCode == 200) {
                	   retVal = 0;  // v0.9.61 success
                	   
                       runOnUiThread(new Runnable() {
                            public void run() {
                                 
                                String msg = " File Upload Completed.\n\n See uploaded file here : \n\n"
                                			  + " http://50.16.195.50/epiinfo/upload/"
                                              + uploadFileName;
                                 
                                messageText.setText(msg);
                                Toast.makeText(UploadSurveyFileActivity.this, "File Upload Complete",
                                             Toast.LENGTH_SHORT).show();
                            }
                        });               
                   }
                   // KRC v0.9.47 If not success then display server response code and text
                   else {
                	   runOnUiThread(new Runnable() {
                           public void run() {
                                
                               String msg = " File Upload NOT Completed.\n\n Server Response Code =  " + serverResponseCode + "\n\n"
                               			  + " serverResponseMessage = " + serverResponseMessage;
                                
                               messageText.setText(msg);
                               Toast.makeText(UploadSurveyFileActivity.this, "File Upload NOT Complete",
                                            Toast.LENGTH_LONG).show();
                           }
                       });
                   }
                    
                   // close the streams
                   fileInputStream.close();
                   dos.flush();
                   dos.close();
                     
              } catch (MalformedURLException ex) {
                   
                  // dialog.dismiss();  // v0.9.61
                  ex.printStackTrace();
                   
                  runOnUiThread(new Runnable() {
                      public void run() {
                          messageText.setText("MalformedURLException Exception : check script url.");
                          Toast.makeText(UploadSurveyFileActivity.this, "MalformedURLException",
                                                              Toast.LENGTH_SHORT).show();
                      }
                  });
                   
                  Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
              } catch (Exception e) {
                   
                  // dialog.dismiss();  // v0.9.61
                  e.printStackTrace();
                   
                  runOnUiThread(new Runnable() {
                      public void run() {
                          messageText.setText("Got Exception : see logcat ");
                          Toast.makeText(UploadSurveyFileActivity.this, "Got Exception : see logcat ",
                                  Toast.LENGTH_SHORT).show();
                      }
                  });
                  Log.e("Upload file to server Exception", "Exception : "
                                                   + e.getMessage(), e); 
              }
              // dialog.dismiss();  // v0.9.61
              if (Constants.LOGS_ENABLED_UPLOAD_SURVEY_FILE) {
        		   Log.d(Constants.LOGTAG, " " + UploadSurveyFileActivity.CLASSTAG + " uploadFile retVal = " + retVal + "*******************");
              }
              return retVal;  // v0.9.61 use retVal, not serverResponseCode;
               
          } // End else block
      }

}