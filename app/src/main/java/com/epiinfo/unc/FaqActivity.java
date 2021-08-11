package com.epiinfo.unc;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.epiinfo.droid.R;

/**
 * Class FaqActivity is used to display the FAQ list.
 * The list is scrollable, but static.
 * This view can be used to access Online Help webpage for more details.
 * 
 * @author keithcollins
 */
public class FaqActivity extends ListActivity {

    private static final String CLASSTAG = FaqActivity.class.getSimpleName();
    
	private ListView mFaqListView;
	private ArrayList<FaqItem> m_faqList = null;
    private FaqListAdapter m_adapter;
    
    int displayOrientation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + FaqActivity.CLASSTAG + " onCreate");
        }
        this.setContentView(R.layout.faq_activity);
        
        setTitle("Help FAQ - App Version " + getAppVersionName());
        
		mFaqListView = (ListView) getListView();
        m_faqList = new ArrayList<FaqItem>();
        m_adapter = new FaqListAdapter(this, R.layout.faqlist_item, m_faqList);
        setListAdapter(this.m_adapter);
    }   

    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + FaqActivity.CLASSTAG + " onResume KC");
        }

        getFaqData();
        setFaqListData();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Constants.LOGS_ENABLED) {
        	Log.d(Constants.LOGTAG, " " + FaqActivity.CLASSTAG + " onPause");
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Constants.LOGS_ENABLED) {
        	Log.i(Constants.LOGTAG, " " + FaqActivity.CLASSTAG + " onConfigurationChanged");
        }
        // prevent landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

	/** 
     * Get current version number. 
     * 
     * @return 
     */ 
    private String getAppVersionName() { 
            String version = "?"; 
            try { 
            	PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            	version = pi.versionName; 
            } 
            catch (PackageManager.NameNotFoundException e) {
            	Log.d(Constants.LOGTAG, "Package name not found ", e); 
            }; 
            return version; 
    }
	
    private void getFaqData() {
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + FaqActivity.CLASSTAG + " getFaqData - Enter");
    	}
    	
    	int i = 0;
    	
    	// #1
    	FaqData.faqList[i].title = "What are the new EPI Info app features added by UNC?";
    	FaqData.faqList[i].text = "The MAP VIEW contains the new features.  Use this to see a map of Clusters and Points";
    	++i;
    	
    	// #2
    	FaqData.faqList[i].title = "What is the purpose of the username and password?";
    	FaqData.faqList[i].text = "These are used to validate the Android device and user with the server to prevent fraud and unauthorized access to sensitive personal information.";
    	++i;
    	
    	// #3
    	FaqData.faqList[i].title = "What do the colored circles on the Map represent";
    	FaqData.faqList[i].text = "The colored circles represent the Staging Area, Cluster Center coordinates and Points.";
    	++i;
    	
    	// #4
    	FaqData.faqList[i].title = "What do the enclosed Red line areas on the Map represent";
    	FaqData.faqList[i].text = "The Red line areas on the Map each represent a Cluster. Once a Cluster is selected the red lines change to blue lines.";
    	++i;
    	
    	// #5
    	FaqData.faqList[i].title = "What is the Staging Area?";
    	FaqData.faqList[i].text = "This is the starting point for the survey team. Map View defaults to this map location when it first starts. A large Black circle represents the staging area. Press the 'Staging Area' button to move the Map to the Staging Area location.";
    	++i;
    	
    	// #6
    	FaqData.faqList[i].title = "What is a Cluster?";
    	FaqData.faqList[i].text = "A cluster is a census block or block group and contains 7-28 Survey Points. A typical two-stage cluster-sample is a 30/7 where 30 clusters are selected and 7 points per cluster. A Cluster is assigned to 1 or 2 people to conduct Surveys for each Point in the Cluster area. Select the 'Clusters' button in the App to see the list of all Points in the Cluster and their Survey status, the Map will also be updated to show the selected Cluster with a Blue line and a Black circle that represents the center coordinates.";
    	++i;
    	
    	// #7
    	FaqData.faqList[i].title = "What is a Point and what do the different Point colors represent in the Map View?";
    	FaqData.faqList[i].text = "A point is a location for a home or apartment where survey teams should start their attempt at an interview. The Point circle colors represent the Survey Status. Blue = Not Started, Yellow = In Progress, Green = Completed, Red = Error."; 
    	++i;
    	
    	// #8
    	FaqData.faqList[i].title = "How do I start a Survey";
    	FaqData.faqList[i].text = "Use Map View. Select a Point on the Map or from the List by selecting your Cluster Id and then the Point Id. The Survey form will then be opened.  The Survey forms can then be filled in.";
    	++i;
    	
    	// #9
    	FaqData.faqList[i].title = "Is Spanish language supported?";
    	FaqData.faqList[i].text = "Yes, when the user selects a Point to start a survey a dialog will prompt the user to select either English or Spanish. However, only use the Spanish version of the questionnaire if you are taking part in the Spanish-speaking survey, not for the County-wide survey."; 
    	++i;
    	
    	// #10
    	FaqData.faqList[i].title = "Where are Surveys stored?";
    	FaqData.faqList[i].text = "There is a survey file for each In Progress and Completed survey that is stored on the Android tablet under Record List. Completed surveys are also sent to the server for storage, where they can be viewed by the system administrator.";
    	++i;
    	
    	// #11
    	FaqData.faqList[i].title = "What is the purpose of the GPS symbol in the top right corner of the Map View screen?";
    	FaqData.faqList[i].text = "Press this button to move the map to your current location.";
    	++i;
    	
    	// #12
    	FaqData.faqList[i].title = "What if I need more help?  Whom can I contact?";
    	FaqData.faqList[i].text = "Please contact your survey coordinator, Matt Simon at 919-928-4477, if you need additional help in conducting the survey.";
    	++i;

    	FaqData.activeCount = i;
    	
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + FaqActivity.CLASSTAG + " setFaqListData count=" + FaqData.activeCount);
    	}
    }
    
    private void setFaqListData() {
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + FaqActivity.CLASSTAG + " setFaqListData - Enter");
    	}
    	
    	m_faqList.clear();
    	m_adapter.notifyDataSetChanged();  // needed ???
    	for (int i=0; i< FaqData.activeCount; i++) {
    		FaqItem faqItem = new FaqItem();
    		faqItem.title = "\n";
    		faqItem.title = faqItem.title.concat(FaqData.faqList[i].title.toString());
    		faqItem.text = FaqData.faqList[i].text.toString();
    		m_faqList.add(faqItem);
    	}
    	m_adapter.notifyDataSetChanged();
    	mFaqListView.setClickable(true);
    	mFaqListView.setHorizontalScrollBarEnabled(true);
    	mFaqListView.setSelection(0);
    	if (Constants.LOGS_ENABLED) {
    		Log.d(Constants.LOGTAG, " " + FaqActivity.CLASSTAG + " setFaqListData Count=" + FaqData.activeCount);
    	}
    }

      
	private class FaqListAdapter extends ArrayAdapter<FaqItem> {
		
        public ArrayList<FaqItem> items;
 
        public FaqListAdapter(Context context, int textViewResourceId, ArrayList<FaqItem> items) {
        	super(context, textViewResourceId, items);
        	this.items = items;
        	if (Constants.LOGS_ENABLED) {
        		Log.d(Constants.LOGTAG, " " + "FaqListAdapter" + " constructor");
        	}
         }
 
        @Override
         public View getView(int position, View convertView, ViewGroup parent) {
        	if (Constants.LOGS_ENABLED) {
        		Log.d(Constants.LOGTAG, " " + "FaqListAdapter" + " getView position=" + position);
        	}
        	View v = convertView;
        	//if (v == null) {
        		if (Constants.LOGS_ENABLED) {
            		Log.d(Constants.LOGTAG, " " + "FaqListAdapter" + " v == null so inflate");
            	}
        		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.faqlist_item, null);
        	//}
        	
        	FaqItem faqItem = items.get(position);
        	if (faqItem != null) {
        		TextView faqTitle = (TextView) v.findViewById(R.id.faqtitle_id);
                TextView faqText  = (TextView) v.findViewById(R.id.faqtext_id);
                
                if (faqTitle != null) {
                	faqTitle.setText(faqItem.title);
                }
                if (faqText != null) {
                	faqText.setText(faqItem.text);
                }
            }
        	return v;
        }
	}
}