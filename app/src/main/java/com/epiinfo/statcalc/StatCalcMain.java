package com.epiinfo.statcalc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.epiinfo.droid.DeviceManager;
import com.epiinfo.droid.R;

public class StatCalcMain extends Activity {

	
	private void LoadActivity(Class c)
	{
		startActivity(new Intent(this, c));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DeviceManager.SetOrientation(this,false);
		this.setTheme(android.R.style.Theme_Holo_Light);
		
		setContentView(R.layout.statcalc_main);
		Button btnPS = (Button) findViewById(R.id.btnPS);
		btnPS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(PopulationSurveyActivity.class);				
			}
		});
		Button btnUCC = (Button) findViewById(R.id.btnUCC);
		btnUCC.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(UnmatchedActivity.class);
			}
		});
		Button btnCCS = (Button) findViewById(R.id.btnCCS);
		btnCCS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(CohortActivity.class);
			}
		});
		Button btn2x2 = (Button) findViewById(R.id.btn2x2);
		btn2x2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(TwoByTwoActivity.class);
			}
		});
		try
		{
			Button btnMatchedPair = (Button) findViewById(R.id.btnMatchedPair);
			btnMatchedPair.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					LoadActivity(MatchedPairActivity.class);
				}
			});
		}
		catch (Exception e) { }
		Button btnChiSq = (Button) findViewById(R.id.btnChiSq);
		btnChiSq.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(ChiSquareActivity.class);
			}
		});
		try
		{
			Button btnPoisson = (Button) findViewById(R.id.btnPoisson);
			btnPoisson.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					LoadActivity(PoissonActivity.class);
				}
			});
			Button btnBinomial = (Button) findViewById(R.id.btnBinomial);
			btnBinomial.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					LoadActivity(BinomialActivity.class);
				}
			});
		}
		catch (Exception e) { }
		Button btnOpenEpi = (Button) findViewById(R.id.btnOpenEpi);
		btnOpenEpi.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uriUrl = Uri.parse("http://www.openepi.com/");
				startActivity(new Intent(Intent.ACTION_VIEW, uriUrl));
			}
		});
	}


}