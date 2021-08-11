package com.epiinfo.unc;

import com.google.android.gms.maps.model.Polyline;

import android.util.Log;


/**
 * The class ClusterItem defines an instance of a Cluster item that is drawn on
 * the  map as a PolyLine. 
 * 
 * @author keithcollins
 */

public class ClusterItem {
	
	private static final String CLASSTAG = ClusterItem.class.getSimpleName();
	
	public String centerLatitude;
	public String centerLongitude;
	public String polygonCoordinates;  // multiple comma separate latitude and longitude values
	public String placemarkId;   // Placemark Id
	public String name;
	
	// public String fid;
	// public String dissField;
	public String occupied;
	public String whiteOnly;
	public String blackOnly;
	public String asianOnly;
	public String hispanic;
	// public String samples;
	// public String cluster;
	// public String team;
	//
	// public String fips;
	// public String geoId10;
	public Polyline polyline;
	
	
	public ClusterItem() {
		centerLatitude = "";
		centerLongitude = "";
		polygonCoordinates = "";
		placemarkId = "";
		name = "";
		
		// fid = "";
		// dissField = "";
		occupied = "";
		whiteOnly = "";
		blackOnly = "";
		// asianOnly = "";
		hispanic = "";
		// samples = "";
		// cluster = "";
		// team = "";
		//
		// FIPS = "";
		// GEOID10 = "":
	}
	
	public void Dump1(final int itemNum) {
		if (Constants.LOGS_ENABLED) {
	    	Log.d(Constants.LOGTAG, " " + " Count=" + itemNum + "  name=" + name + " centerlatitude=" + centerLatitude + " centerLongitude=" + centerLongitude);
	    }
	}
	
	public void Dump2(final int itemNum) {
		if (Constants.LOGS_ENABLED) {
	    	Log.d(Constants.LOGTAG, " " + " Count=" + itemNum + "  name=" + name + " centerlatitude=" + centerLatitude + " centerLongitude=" + centerLongitude);
	    	Log.d(Constants.LOGTAG, " " + "         polygonCoordinates=" + polygonCoordinates);
	    }
	}
	
	public void Dump(final int itemNum) {
		if (Constants.LOGS_ENABLED) {
	    	Log.d(Constants.LOGTAG, " " + " Count=" + itemNum + "  placemarkId=" + placemarkId + "  name=" + name);
	    	Log.d(Constants.LOGTAG, " " + "         occupied=" + occupied + "  whiteOnly=" + whiteOnly + "  blackOnly=" + blackOnly + "  asianOnly=" + asianOnly + " hispanic=" + hispanic);
	    	// Log.d(Constants.LOGTAG, " " + "         hispanic=" + hispanic + "  samples=" + samples + "  cluster=" + cluster + "  team=" + team);
	    	Log.d(Constants.LOGTAG, " " + "         centerlatitude=" + centerLatitude + " centerLongitude=" + centerLongitude);
	    	Log.d(Constants.LOGTAG, " " + "         polygonCoordinates=" + polygonCoordinates);
	    }
	}
}