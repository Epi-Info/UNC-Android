package com.epiinfo.unc;


import android.util.Log;

import com.epiinfo.droid.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * The class PointItem defines an instance of a Point item that can be used
 * inside a Cluster.  A Point represents a home or apartment.
 * 
 * @author keithcollins
 */

public class PointItem {
	
	private static final String CLASSTAG = PointItem.class.getSimpleName();
	
	public String latitude;
	public String longitude;
	// public String placemarkId;
	public String name;
	public String whiteOnly;
	public String blackOnly;
	public String asianOnly;
	public String hispanic;
	
	public String streetAddr;
	public String city;
	// public String poName;
	// public String zipCode;
	
	// public String fid;
	// public String dissField;
	// public String cluster;
	// public String team;
	// public String label;
	// public String pntNumb;
	
	public MarkerOptions mo;
	public Marker marker;
	
	public int surveyStatus;
	
	public PointItem() {
		latitude = "";
		longitude = "";
		// placemarkId = "";
		name      = "";
		
		// whiteOnly = "";
		// blackOnly = "";
		// asianOnly = "";
		// hispanic  = "";
		
		// poName     = "";
		streetAddr = "";
		city       = "";
		// zipCode    = "";
		
		// fid = "";
		// dissField = "";
		// cluster = "";
		// team = "";
		// label = "";
		// pntNumb = "";
		
		mo = new MarkerOptions();
		
		surveyStatus = Constants.POINT_STATUS_UNKNOWN;
	}
	
	public void Dump(final int itemNum) {
		if (Constants.LOGS_ENABLED2) {
	    	Log.d(Constants.LOGTAG, " " + " Count=" + itemNum + " name=" + name + " Street=" + streetAddr + "City=" + city);
	    	Log.d(Constants.LOGTAG, " " + "         whiteOnly=" + whiteOnly + "  blackOnly=" + blackOnly + "  asianOnly=" + asianOnly + " hispanic=" + hispanic);
	    	Log.d(Constants.LOGTAG, " " + "         latitude=" + latitude + "  longitude=" + longitude);
	    }
	}
	
	public String getPointId() {
		if (name == "") return "";  // v0.9.48
		int startIndex = name.indexOf("-") + 1;
		return (name.substring(startIndex));
	}
	
	public String getClusterId() {
		if (name == "") return "";  // v0.9.48
		int endIndex = name.indexOf("-");
		if (Constants.LOGS_ENABLED5) {
	    	Log.d(Constants.LOGTAG, " " + " ++++++++++++++ getClusterId name=" + name);
	    }
		return (name.substring(0, endIndex));
	}
	
	public void setSurveyStatus(final int pStatus) {
		if (Constants.LOGS_ENABLED3) {
	    	Log.d(Constants.LOGTAG, " " + "setSurveyStatus point=" + name + " status=" + pStatus);
		}
		surveyStatus = pStatus;
		
		// v0.9.60 KRC 22Feb2015 - get point png files from drawable folder, not assets folder
		if (surveyStatus == Constants.POINT_STATUS_COMPLETED) {
			// mo.icon(BitmapDescriptorFactory.fromAsset("point_marker_green.png"));
			// mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_green));
			mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_greyx32x32));
		}
		else if (surveyStatus == Constants.POINT_STATUS_INPROGRESS) {
			if (Constants.LOGS_ENABLED3) {
		    	Log.d(Constants.LOGTAG, " " + "setSurveyStatus YELLOW");
			}
			// mo.icon(BitmapDescriptorFactory.fromAsset("point_marker_yellow.png"));
			mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_yellow));
		}
		else if (surveyStatus == Constants.POINT_STATUS_NOTSTARTED) {
			// mo.icon(BitmapDescriptorFactory.fromAsset("point_marker_blue.png"));
			// mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_blue));
			mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_green));
		}
		else if (surveyStatus == Constants.POINT_STATUS_PAUSED) {
			// mo.icon(BitmapDescriptorFactory.fromAsset("point_marker_yellow.png"));
			mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_yellow));
		}
		else if (surveyStatus == Constants.POINT_STATUS_ERROR) {
			// mo.icon(BitmapDescriptorFactory.fromAsset("point_marker_red.png"));
			mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_red));
		}
		else {  // assume POINT_STATUS_UNKNOWN
			// mo.icon(BitmapDescriptorFactory.fromAsset("point_marker_blue.png"));
			mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker_blue));
		}
	}
	
	/**************************************************
	public void setSurveyStatus(final int pStatus, GoogleMap map) {
		if (Constants.LOGS_ENABLED3) {
	    	Log.d(Constants.LOGTAG, " " + "setSurveyStatus with Map, point=" + name + " status=" + pStatus);
		}
		marker.remove();
		setSurveyStatus(pStatus);
	}
	****************************************************/
}