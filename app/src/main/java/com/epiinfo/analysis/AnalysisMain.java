package com.epiinfo.analysis;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.epiinfo.droid.AppManager;
import com.epiinfo.droid.DeviceManager;
import com.epiinfo.droid.EpiDbHelper;
import com.epiinfo.droid.FormMetadata;
import com.epiinfo.droid.R;

public class AnalysisMain extends AppCompatActivity {

	private EpiDbHelper dbHelper;
	private Bundle state;
	private FormMetadata view;
	private String viewName;

	private void LoadActivity(Class c)
	{
		startActivity(new Intent(this, c));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			openOptionsMenu();
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public void openOptionsMenu()
	{
		Configuration config = getResources().getConfiguration();

		if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE)
		{
			int originalScreenLayout = config.screenLayout;
			config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
			super.openOptionsMenu();
			config.screenLayout = originalScreenLayout;
		}
		else
		{
			super.openOptionsMenu();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem mnuFreq = menu.add(0, 0, 0, R.string.analysis_add_freq);
		mnuFreq.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuMeans = menu.add(0, 1,1, R.string.analysis_add_means);
		mnuMeans.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnu2x2 = menu.add(0, 2,2, R.string.analysis_add_2x2);
		mnu2x2.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuMap = menu.add(0, 3,3, R.string.analysis_add_map);
		mnuMap.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuChart1 = menu.add(0, 4,4, R.string.analysis_add_chart_pie);
		mnuChart1.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuList = menu.add(0, 5,5, R.string.analysis_view_list);
		mnuList.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		PrepareCanvas();

		switch(item.getItemId()) {
			case android.R.id.home:
				this.onBackPressed();
				return true;
			case 0:
				AddFrequencyGadget();
				GoToBottom();
				return true;
			case 1:
				AddMeansGadget();
				GoToBottom();
				return true;
			case 2:
				Add2x2Gadget();
				GoToBottom();
				return true;
			case 3:
				AddMapGadget();
				GoToBottom();
				return true;
			case 4:
				AddPieChartGadget();
				GoToBottom();
				return true;
			case 5:
				new CsvFileGenerator().Generate(this, dbHelper, view, viewName);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void GoToBottom()
	{
		final ScrollView scroller = (ScrollView) findViewById(R.id.analysis_scroller);
		scroller.setBackgroundColor(0xFFCBD1DF);
		scroller.post(new Runnable() {
			@Override
			public void run() {
				scroller.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	private void PrepareCanvas()
	{
		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		layout.setVisibility(View.VISIBLE);

		LinearLayout logo = (LinearLayout) findViewById(R.id.analysis_logo);
		logo.setVisibility(View.GONE);

		ScrollView scroller = (ScrollView) findViewById(R.id.analysis_scroller);
		scroller.setBackgroundColor(0xFFCBD1DF);
	}

	private void AddMapGadget()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		MapViewer gadget = new MapViewer(this, view, dbHelper, state, (ScrollView) findViewById(R.id.analysis_scroller));
		gadget.setLayoutParams(params);
		layout.addView(gadget);
	}

	private void AddMeansGadget()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		MeansView gadget = new MeansView(this, view, dbHelper);
		gadget.setLayoutParams(params);
		layout.addView(gadget);
	}

	private void Add2x2Gadget()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		TwoByTwoView gadget = new TwoByTwoView(this, view, dbHelper);
		gadget.setLayoutParams(params);
		layout.addView(gadget);
	}

	private void AddFrequencyGadget()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		FrequencyView gadget = new FrequencyView(this, view, dbHelper);
		gadget.setLayoutParams(params);
		layout.addView(gadget);
	}

	private void AddPieChartGadget()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

		LinearLayout layout = (LinearLayout) findViewById(R.id.analysis_layout);
		PieChartView gadget = new PieChartView(this, view, dbHelper);
		gadget.setLayoutParams(params);
		layout.addView(gadget);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DeviceManager.SetOrientation(this, false);
		this.setTheme(R.style.AppTheme);

		setContentView(R.layout.analysis);

		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			viewName = extras.getString("ViewName");
			if (viewName.startsWith("_"))
			{
				viewName = viewName.toLowerCase();
			}
			view = new FormMetadata("EpiInfo/Questionnaires/"+ viewName +".xml", this);
			AppManager.AddFormMetadata(viewName, view);
			dbHelper = new EpiDbHelper(this, view, viewName);
			dbHelper.open();

		}

		ScrollView scroller = (ScrollView) findViewById(R.id.analysis_scroller);
		scroller.setBackgroundColor(0xFFFFFFFF);

		LinearLayout logo = (LinearLayout) findViewById(R.id.analysis_logo);
		logo.setBackgroundColor(0xFFFFFFFF);

	}


}