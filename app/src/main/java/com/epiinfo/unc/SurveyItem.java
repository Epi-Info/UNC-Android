package com.epiinfo.unc;

import android.util.Log;


/**
 * The class SurveyItem defines an instance of a Survey item that can be used
 * to conduct an interview.
 * 
 * @author keithcollins
 */

public class SurveyItem {
	
	public String id;  // parsed, but not used
	public String title;
	public String createDate;
	public double stagingAreaLatitude;
	public double stagingAreaLongitude;
	public String stagingAreaAddr;
	public String apiParam;
	public String formFilename;
	public String clustersFilename;
	public String pointsFilename;
	public String surveyFilesFolderName;
	
	public SurveyItem() {
		id = "";
		title = "";
		stagingAreaAddr = "";
		apiParam = "";
		formFilename = "";
		clustersFilename = "";
		pointsFilename = "";
		
		//20Apr2015
		stagingAreaAddr = "";
	}
	
	public void Dump() {
		if (Constants.LOGS_ENABLED) {
	    	Log.d(Constants.LOGTAG, " " + " id=" + id);
	    	Log.d(Constants.LOGTAG, " " + "    Title = " + title);
	    	Log.d(Constants.LOGTAG, " " + "    Create Date = " + createDate);
	    	Log.d(Constants.LOGTAG, " " + "    QuestionnaireFilename = " + formFilename);
	    	Log.d(Constants.LOGTAG, " " + "    ClustersFilename = " + clustersFilename);
	    	Log.d(Constants.LOGTAG, " " + "    PointsFilename = " + pointsFilename);
	    }
	}
	
}