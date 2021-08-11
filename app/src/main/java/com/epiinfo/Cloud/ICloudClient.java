package com.epiinfo.Cloud;

import android.content.ContentValues;

import com.epiinfo.droid.EpiDbHelper;

import org.json.JSONArray;

/**
 * Created by asad on 4/12/18.
 */

public interface ICloudClient {

    JSONArray getData(boolean downloadImages, boolean downloadMedia, EpiDbHelper dbHelper);

    boolean insertRecord(ContentValues values);

    boolean deleteRecord(String recordId);

    boolean updateRecord(String recordId, ContentValues values);

}
