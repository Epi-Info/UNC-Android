package com.epiinfo.unc;

import java.util.List;

import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * The class UncEpiPhoneState contains the phone status of static and dynamic values. 
 * The include device type, phone number, connection status, SIM statuc, etc.
 * These values are NOT stored persistently, but are updated by registering a
 * listener for changes in the Telephony Service.
 * 
 * Phone information retrieved from TelephonyManager via 
 * Context.getSystemService(Context.TELEPHONY_SERVICE);
 *
 * Register a listener by invoking listen(PhoneStateListener listener, int events);
 * to receive notification of changes in specified telephony states.
 * 
 * This file is re-usable for different applications.  
 * 
 * @author keithcollins
 */
public class UncEpiPhoneState {
	
	private static final String CLASSTAG = UncEpiPhoneState.class.getSimpleName();
	
	public static int callState;  // Idle, Offhook, Ringing, Dormant, Activity In, Activity None, Activity Out, Data Connected, Data Connecting, Data Disconnected, Data Suspended, etc 
	public static CellLocation cellLocation;
	public static int dataActivity;
	public static int dataState;
	public static String deviceId;
	public static String deviceSoftwareVersion;
	public static String line1Number;  // MSISDN for GSM
	public static List<NeighboringCellInfo> neighboringCellInfo;
	public static String networkCountryIso;  // MCC - Mobile Country Code
	public static String networkOperator;  // MCC + MNC of registered operator
	public static String networkOperatorName;  //alphabetic name of current registered operator
	public static int networkType; // 1xRTT, CDMA, EDGE, eHRPD, EVDO Rev 0, EVDO Rev A, EVDO Rev B, GPRS, HSDPA, HSPA, HSUPA, IDEN, LTE, UMTS, Unknown
	public static int phoneType;  // 
	public static String simCountryIso;  //  ISO country code equivalent for the SIM provider's country code
	public static String simOperator;  // mcc = MNC of the SIM provider
	public static String simOperatorName;  // Service Provider Name (SPN) for the SIM
	public static String simSerialNumber;  // SIM serial # if applicable
	public static int simState;  // CDMA, GSM, NONE, SIP
	public static String subscriberId;  // IMSI for GSM
	public static String voiceMailAlphaTag;  // alphabetic id associated with the voice mail number
	public static String voiceMailNumber;
	public static boolean hasIccCard;  // true if ICC/SIM is present and functional
	public static boolean isNetworkRoaming;  // true if device is roaming on the current registered network
	public static String signalStrength;   // string form
	public static boolean cfi;     // call forward indicator
	public static boolean mwi;     // message waiting indicator
	public static ServiceState serviceState;  // emergency only, in service, out of service, power off
	
	// GPS location
	
	public static void GetCurrentPhoneState(TelephonyManager telMgr)
	{
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " GetCurrentPhoneState - Enter");
		}
		callState = telMgr.getCallState();
		cellLocation = telMgr.getCellLocation();
		dataActivity = telMgr.getDataActivity();
		dataState = telMgr.getDataState();
		deviceId = telMgr.getDeviceId();
		deviceSoftwareVersion = telMgr.getDeviceSoftwareVersion();
		line1Number = telMgr.getLine1Number();
		neighboringCellInfo = telMgr.getNeighboringCellInfo();
		networkCountryIso = telMgr.getNetworkCountryIso();
		networkOperator = telMgr.getNetworkOperator();
		networkOperatorName = telMgr.getNetworkOperatorName();
		networkType = telMgr.getNetworkType();
		phoneType = telMgr.getPhoneType();
		simCountryIso = telMgr.getSimCountryIso();
		simOperator = telMgr.getSimOperator();
		simOperatorName = telMgr.getSimOperatorName();
		simSerialNumber = telMgr.getSimSerialNumber();
		simState = telMgr.getSimState();
		subscriberId = telMgr.getSubscriberId();
		voiceMailAlphaTag = telMgr.getVoiceMailAlphaTag();
		voiceMailNumber = telMgr.getVoiceMailNumber();
		hasIccCard = telMgr.hasIccCard();
		isNetworkRoaming = telMgr.isNetworkRoaming();
		// signalStrength = telMgr.signalStrength();  // not supported by TelMgr
		
		DumpCurrentPhoneState();
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " GetCurrentPhoneState - Exit");
		}
	}
	
	public static void DumpCurrentPhoneState() {
		if (Constants.LOGS_ENABLED) {
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " DumpCurrentPhoneState - Enter");
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " callState = " + callState); 
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " cellLocation = " + cellLocation);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " dataActivity = " + dataActivity);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " dataState = " + dataState);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " deviceId = " + deviceId);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " deviceSoftwareVersion = " + deviceSoftwareVersion);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " line1Number = " + line1Number);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " neighboringCellInfo = " + neighboringCellInfo);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " networkCountryIso = " + networkCountryIso);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " networkOperator = " + networkOperator);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " networkOperatorName = " + networkOperatorName);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " networkType = " + networkType);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " phoneType = " + phoneType);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " simCountryIso = " + simCountryIso);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " simOperator = " + simOperator);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " simOperatorName = " + simOperatorName);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " simSerialNumber = " + simSerialNumber);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " simState = " + simState);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " subscriberId = " + subscriberId);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " voiceMailAlphaTag = " + voiceMailAlphaTag);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " voiceMailNumber = " + voiceMailNumber);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " hasIccCard = " + hasIccCard);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " isNetworkRoaming = " + isNetworkRoaming);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " signalStrength = " + signalStrength);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " Call Fwd Indicator = " + cfi);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " Msg Wait Indicator = " + mwi);
			Log.d(Constants.LOGTAG, " " + UncEpiPhoneState.CLASSTAG + " DumpCurrentPhoneState - Exit");
		}
	}
	
	public static String getPhoneNumber() { 
		if (line1Number == null) {
			return("Unknown");
		}
		else {
			if (line1Number.startsWith("1") == true) {
				return(line1Number.substring(1));
			}
			else {
				return(UncEpiPhoneState.line1Number);
			}
		}
	}
}