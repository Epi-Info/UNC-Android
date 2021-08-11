package com.epiinfo.unc;


public class Constants {

	public static final String INTENT_ACTION_DISCLAIMER      = "com.epiinfo.unc.DISCLAIMER";
    public static final String INTENT_ACTION_LOGIN_USER      = "com.epiinfo.unc.LOGIN_USER";
    public static final String INTENT_ACTION_SURVEY_LIST     = "com.epiinfo.unc.SURVEY_LIST";
    public static final String INTENT_ACTION_SURVEY_DOWNLOAD = "com.epiinfo.unc.SURVEY_DOWNLOAD";
    public static final String INTENT_ACTION_EDIT_SETTINGS   = "com.epiinfo.unc.EDIT_SETTINGS";
	public static final String INTENT_ACTION_FAQ             = "com.epiinfo.unc.FAQ";
	public static final String INTENT_ACTION_SURVEYUPLOAD    = "com.epiinfo.unc.SURVEYUPLOAD";
	public static final String INTENT_ACTION_NOSURVEYUPLOAD  = "com.epiinfo.unc.NOSURVEYUPLOAD";
    public static final String INTENT_ACTION_HELP            = "com.epiinfo.unc.HELP";
    public static final String INTENT_ACTION_PHONEINFO       = "com.epiinfo.unc.PHONEINFO";
    public static final String INTENT_ACTION_MAPS_MAIN       = "com.epiinfo.unc.MAPS_MAIN";  // Google Maps API v2
    
    public static final String LOGTAG = "EPI_INFO";
    public static final String PREFS  = "RCP";
	
    public static final int APP_NOT_RUNNING     = 0;
    public static final int APP_ALREADY_RUNNING = 1;
    public static final int APP_TIME_TO_EXIT    = 2;
    
    // EPI API Interface
    // public static final String EPI_API_PREFIX = "http://50.16.195.50/epiinfo/api/epi_api.php?";
    // public static final String EPI_API_PREFIX = "http://www.collectsmartdata.com/phase3/api/epi_api3.php?";
    public static final String EPI_API_PREFIX = "https://collectsmartdata.org/phase32/api/epi_api3.php?";
    
    // KRC v0.9.47 - Use new php command file on server for file uploads
    // public static final String EPI_FILE_UPLOAD_URI   = "http://50.16.195.50/epiinfo/upload/upload.php";
    // public static final String EPI_FILE_UPLOAD_URI   = "http://50.16.195.50/epiinfo/upload/upload2.php";
    // public static final String EPI_FILE_UPLOAD_URI   = "http://www.collectsmartdata.com/phase3/upload/upload2.php";
    public static final String EPI_FILE_UPLOAD_URI   = "https://collectsmartdata.org/phase32/upload/upload2.php";
    // public static final String EPI_FILE_DOWNLOAD_URI = "http://50.16.195.50/epiinfo/Surveys/";
    // public static final String EPI_FILE_DOWNLOAD_URI = "http://www.collectsmartdata.com/phase3";
    public static final String EPI_FILE_DOWNLOAD_URI = "https://collectsmartdata.org/phase32";
    
    public static final String EPI_FILE_QUESTIONAIRE_FOLDER = "/sdcard/Download/Epiinfo/Questionnaires";
    public static final String EPI_FILE_CLUSTERS_FOLDER     = "/sdcard/Download/Epiinfo/Questionnaires";
    public static final String EPI_FILE_POINTS_FOLDER       = "/sdcard/Download/Epiinfo/Questionnaires";

    public static final int MIN_USERNAME_LENGTH    = 3;
    public static final int MIN_PASSWORD_LENGTH    = 3;
    public static final int MIN_COORDINATOR_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH    = 16;
    public static final int MAX_PASSWORD_LENGTH    = 16;
    public static final int MAX_COORDINATOR_LENGTH = 16;
    
    // Point Status
    public static final int POINT_STATUS_COMPLETED  = 0;
    public static final int POINT_STATUS_INPROGRESS = 1;
    public static final int POINT_STATUS_NOTSTARTED = 2;
    public static final int POINT_STATUS_PAUSED     = 3;
    public static final int POINT_STATUS_ERROR      = 4;
    public static final int POINT_STATUS_UNKNOWN    = 5;
    
    // TEST Only: used in API calls to server to identify the current survey
    // public static final String SURVEY_NAME_ENGLISH    = "pointsDurhamCHOS_County_FINAL";
    // public static final String SURVEY_NAME_HISPANIC   = "pointsDurhamCHOS_Hispanic_FINAL";
    // public static final String SURVEY_NAME_NEWHANOVER = "pointsNewHanover_FINAL";
    
    // GPS coordinates of staging areas
    // public static final double SURVEY_STAGING_AREA_LATITUDE_ENGLISH     = 35.9818810;         // 35.9940329 Durham downtown
    // public static final double SURVEY_STAGING_AREA_LONGITUDE_ENGLISH    = -78.8783960;
    // public static final double SURVEY_STAGING_AREA_LATITUDE_HISPANIC    = 35.9818810;         // 35.9940329 Durham downtown
    // public static final double SURVEY_STAGING_AREA_LONGITUDE_HISPANIC   = -78.8783960;
    // public static final double SURVEY_STAGING_AREA_LATITUDE_NEWHANOVER  = 34.2451143145109;
    // public static final double SURVEY_STAGING_AREA_LONGITUDE_NEWHANOVER = -77.8648355195489;
    	
    // Address of staging areas
    // public static final String SURVEY_STAGING_AREA_ADDR_ENGLISH    = "410 South Driver St, Durham, NC 27703";
    // public static final String SURVEY_STAGING_AREA_ADDR_HISPANIC   = "410 South Driver St, Durham, NC 27703";
    // public static final String SURVEY_STAGING_AREA_ADDR_NEWHANOVER = "106 Old Eastwood Rd, Wilmington, NC 28403";
    
    // v0.9.52 17Jun2015 - Database
    public static final String EPI_DB_NAME           = "epiDB";
	public static final String POINTS_TABLE_NAME     = "PointsTable";
	public static final String SYNC_FILES_TABLE_NAME = "SyncFilesTable";
	
	// v0.9.61 20Aug2015 - Sync File Upload retries
	public static final int UPLOAD_FILE_RETRY_LIMIT  = 1;
	
	// v0.9.62 30Aug2015 - Add timeouts to HTTP Get Operations using 
	// class HttpURLConnection instead of deprecated class HttpClientSync File
	public static final int	URL_CONNECTION_CONNECT_TIMEOUT = 3000;  // 3 seconds
	public static final int	URL_CONNECTION_READ_TIMEOUT    = 3000;  // 3 seconds
    
    // Flurry Analytics - application "MuniApp Android"
    public static final String FLURRY_APP_KEY = "DKVSMHKPHTS2NJFW6TYQ"; 
    
    // Logs
    public static final boolean LOGS_ENABLED  = false;
    public static final boolean LOGS_ENABLED2 = false;
    public static final boolean LOGS_ENABLED3 = false;
    public static final boolean LOGS_ENABLED4 = false;
    public static final boolean LOGS_ENABLED5 = false;
    public static final boolean LOGS_ENABLED6 = false;
    public static final boolean LOGS_ENABLED7 = false;
    public static final boolean LOGS_ENABLED_HTTP = false;
    public static final boolean LOGS_ENABLED_READCLUSTERFILE = false;
    public static final boolean LOGS_ENABLED_UPLOAD_SURVEY_FILE = false;
    public static final boolean LOGS_ENABLED_CLUSTERPOLYGON = false;
    public static final boolean LOGS_ENABLED_GETALLPOINTSSTATUS = false;
    public static final boolean LOGS_ENABLED_LOGINUSER = false;
    public static final boolean LOGS_ENABLED_SURVEYDOWNLOAD = false;
    public static final boolean LOGS_ENABLED_UNCEPISETTINGS = false;
    public static final boolean LOGS_ENABLED_LOCATION = false;
    public static final boolean LOGS_ENABLED_NETWORKMONITOR = false;
    public static final boolean LOGS_ENABLED_DATABASE = false;
    public static final boolean LOGS_ENABLED_RECORDLIST = false;
    public static final boolean LOGS_ENABLED_SYNCFILESINTENTSERVICE = false;
    public static final boolean LOGS_ENABLED_MAPMARKERS = false;
    public static final boolean LOGS_ENABLED_SORT = false;
    public static final boolean LOGS_ENABLED_TIMER = false;
    public static final boolean LOGS_ENABLED_HOLES = false;
}