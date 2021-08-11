package com.epiinfo.unc;


import android.util.Log;


/**
 * The class PointStatusItem defines an instance of an item that is stored locally by
 * the app to represent the survey status for a surverId-ClusterId-PointId.
 * A single string is formed to represent a SurveyId-ClusterId-PointId-SurveyStatus
 * 
 * @author keithcollins
 */

public class PointStatusItem {
	
	private static final String CLASSTAG = PointStatusItem.class.getSimpleName();
	
	public String pointStatus;
	
	public PointStatusItem() {
		pointStatus = "";
	}
	
	public String getSurveyId() {
		int endIndex = pointStatus.indexOf("-");
		String SurveyId = pointStatus.substring(0, endIndex);
		if (Constants.LOGS_ENABLED) {
	    	Log.d(Constants.LOGTAG, " PointStatusItem getSurveyId - return=" + SurveyId);
		}
		return SurveyId;
	}
	
	public String getClusterId() {
		int startIndex = pointStatus.indexOf("-") + 1;
		int endIndex = pointStatus.indexOf("-", startIndex);
		String ClusterId = pointStatus.substring(startIndex, endIndex);
		if (Constants.LOGS_ENABLED) {
	    	Log.d(Constants.LOGTAG, " PointStatusItem getClusterId - return=" + ClusterId);
		}
		return ClusterId;
	}
	
	public String getPointId() {
		int startIndex = pointStatus.indexOf("-") + 1;
		startIndex = pointStatus.indexOf("-", startIndex) + 1;
		int endIndex = pointStatus.indexOf("-", startIndex);
		String PointId = pointStatus.substring(startIndex, endIndex);
		if (Constants.LOGS_ENABLED) {
	    	Log.d(Constants.LOGTAG, " PointStatusItem getPointId - return=" + PointId);
		}
		return PointId;
	}
	
	public String getStatus() {
		int startIndex = pointStatus.indexOf("-") + 1;
		startIndex = pointStatus.indexOf("-", startIndex) + 1;
		startIndex = pointStatus.indexOf("-", startIndex) + 1;
		String Status = pointStatus.substring(startIndex);
		if (Constants.LOGS_ENABLED) {
	    	Log.d(Constants.LOGTAG, " PointStatusItem getPointId - return=" + Status);
		}
		return Status;
	}
	
	
	public void Dump() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " PointStatusItem Dump - String=" + pointStatus + " Survey=" + getSurveyId() + " Cluster=" + getClusterId() + " Point=" + getPointId() + " Status=" + getStatus());
	    }
	}
}