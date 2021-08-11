package com.epiinfo.droid;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapViewer extends Activity  implements OnMapReadyCallback {

	private double minLatitude;
	private double maxLatitude;
    private double minLongitude;
    private double maxLongitude;
	private Object[] latitudes;
	private Object[] longitudes;
    private LinearLayout mapLayout;
    private GoogleMap map;


	@Override
	public void onMapReady(GoogleMap googleMap) {

		try {
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			googleMap.setMyLocationEnabled(true);
			this.map = googleMap;
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			map.setMyLocationEnabled(true);


			minLatitude = 81.0;
			maxLatitude = -81.0;
			minLongitude  = 181.0;
			maxLongitude  = -181.0;

			MarkerOptions[] mapMarkers = new MarkerOptions[latitudes.length];
			for (int x=0;x<latitudes.length;x++)
			{
				double latitude = (Double)latitudes[x];
				double longitude = (Double)longitudes[x];

				minLatitude = (minLatitude > latitude) ? latitude : minLatitude;
				maxLatitude = (maxLatitude < latitude) ? latitude : maxLatitude;
				minLongitude = (minLongitude > longitude) ? longitude : minLongitude;
				maxLongitude = (maxLongitude < longitude) ? longitude : maxLongitude;

				mapMarkers[x] = new MarkerOptions()
						.position(new LatLng(latitude, longitude))
						.icon(BitmapDescriptorFactory.defaultMarker());

			}

			if (latitudes.length > 0)
			{
				new WaitAndRender().execute(mapMarkers);
			}
		}
		catch (Exception ex)
		{
			//
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try {
			super.onCreate(savedInstanceState);
			MapsInitializer.initialize(this);
			setContentView(R.layout.map_viewer);
//			AppManager.Started(this);

			Bundle bundle = getIntent().getExtras();
			latitudes = (Object[]) bundle.get("Latitudes");
			longitudes = (Object[]) bundle.get("Longitudes");

			mapLayout = (LinearLayout) findViewById(R.id.maplayout);
			MapFragment fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
			fragment.getMapAsync(this);
		}
		catch (Exception ex)
		{

		}
	}
	
    @Override
    public void onRestart()
    {
    	super.onRestart();
//    	AppManager.Started(this);
    }
    
    @Override
    public void onStop()
    {
//    	AppManager.Closed(this);
    	super.onStop();
    }
    
    private class WaitAndRender extends AsyncTask<MarkerOptions[],Void,MarkerOptions[]>
    {

		@Override
		protected MarkerOptions[] doInBackground(MarkerOptions[]... params) {
			try
			{
				Thread.sleep(3000);
			}
			catch (Exception ex)
			{
				
			}
			return params[0];
		}
		
		@Override
        protected void onPostExecute(MarkerOptions[] results) {
            
			for (int x=0; x<results.length; x++)
			{
				map.addMarker(results[x]);
			}
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(new LatLng(minLatitude,minLongitude),new LatLng(maxLatitude,maxLongitude)), 22));
		}
    	
    }
	
	
}
