package com.epiinfo.unc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;


/**
 * The class UncNetworkMonitorReceiver is an instance of Broadcast Receiver
 * That is used to register for network connection changes.  If a change is
 * detected then an action is performed. Actions include checking the database
 * to see if there are any sync files that need to be sent to the server.
 * 
 * @author keithcollins
 */

public class UncNetworkMonitorReceiver extends BroadcastReceiver {

	private static final String CLASSTAG = UncNetworkMonitorReceiver.class.getSimpleName();
	
	private boolean networkWasConnected = false;
	
	@Override
    public void onReceive(Context context, Intent intent) {
		
		/**** check intent action???
		if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			
		}
		************/
		
		if (Constants.LOGS_ENABLED_NETWORKMONITOR) {
			Log.d(Constants.LOGTAG, " " + UncNetworkMonitorReceiver.CLASSTAG + " +++++++++++++++++");
			Log.d(Constants.LOGTAG, " " + UncNetworkMonitorReceiver.CLASSTAG + " onReceive - Enter");
			Log.d(Constants.LOGTAG, " " + UncNetworkMonitorReceiver.CLASSTAG + " +++++++++++++++++");
		}
		
		// boolean networkConnected = intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
		boolean networkConnected = UncEpiSettings.IsNetworkAvailable(context);
		
		if (Constants.LOGS_ENABLED_NETWORKMONITOR) {
			Log.d(Constants.LOGTAG, " " + UncNetworkMonitorReceiver.CLASSTAG + "EXTRA_NO_CONNECTIVITY = " + intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY));
			Log.d(Constants.LOGTAG, " " + UncNetworkMonitorReceiver.CLASSTAG + "IsNetworkAvailable = " + networkConnected);
		}

		if ((!networkWasConnected) && (networkConnected)) {
			// process the network connection change, but use a service since a Broadcast Receiver runs on the main thread
			// 1. Read entries from database table syncFiles
			// 2. For each entry in database
			//     a) Send sync file to server
			//     b) Invoke updatePointStatus(Completed) server API
			//     c) Delete entry from database
			// 3. InvokeGetAllPointsStatus() server API
			
			Intent syncFilesIntent = new Intent(context, SyncFilesIntentService.class);
            // msgIntent.putExtra(MyWebRequestService.REQUEST_STRING, "http://www.amazon.com");

            context.startService(syncFilesIntent);

		}
		
		networkWasConnected = networkConnected;
		
		if (Constants.LOGS_ENABLED_NETWORKMONITOR) {
			Log.d(Constants.LOGTAG, " " + UncNetworkMonitorReceiver.CLASSTAG + " ----------------");
			Log.d(Constants.LOGTAG, " " + UncNetworkMonitorReceiver.CLASSTAG + " onReceive - Exit");
			Log.d(Constants.LOGTAG, " " + UncNetworkMonitorReceiver.CLASSTAG + " ----------------");
		}
    }

}