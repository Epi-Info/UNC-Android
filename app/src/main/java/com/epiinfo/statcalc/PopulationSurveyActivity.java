package com.epiinfo.statcalc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.epiinfo.droid.DeviceManager;
import com.epiinfo.droid.R;
import com.epiinfo.statcalc.calculators.PopulationSurvey;

public class PopulationSurveyActivity extends Activity implements SeekBar.OnSeekBarChangeListener{
	
	EditText txtPopSize;
	SeekBar skbExpFreq;
	SeekBar skbWorst;
	TextView txtExpFreq;
	TextView txtWorst;
	TextView txt80;
	TextView txt90;
	TextView txt95;
	TextView txt97;
	TextView txt99;
	TextView txt999;
	TextView txt9999;
	InputMethodManager imm;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DeviceManager.SetOrientation(this,false);
		this.setTheme(android.R.style.Theme_Holo_Light);
        
        setContentView(R.layout.statcalc_population_survey);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        txtPopSize = (EditText) findViewById(R.id.txtPopSiz);
        txtPopSize.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Calculate();
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
        skbExpFreq = (SeekBar) findViewById(R.id.skbExpFreq);
        skbExpFreq.setOnSeekBarChangeListener(this);
        skbWorst = (SeekBar) findViewById(R.id.skbWorst);
        skbWorst.setOnSeekBarChangeListener(this);
        txtExpFreq = (TextView) findViewById(R.id.lblExpFreq);
        txtWorst = (TextView) findViewById(R.id.lblWorst);
        txt80 = (TextView) findViewById(R.id.txt80);
        txt90 = (TextView) findViewById(R.id.txt90);
        txt95 = (TextView) findViewById(R.id.txt95);
        txt97 = (TextView) findViewById(R.id.txt97);
        txt99 = (TextView) findViewById(R.id.txt99);
        txt999 = (TextView) findViewById(R.id.txt999);
        txt9999 = (TextView) findViewById(R.id.txt9999);
        Calculate();
        //imm.hideSoftInputFromWindow(txtPopSize.getWindowToken(), 0);
    }
    
    private void Calculate()
    {
    	PopulationSurvey calc = new PopulationSurvey();
    	if (txtPopSize.getText().toString().length() > 0)
    	{
    		int popSize = Integer.parseInt(txtPopSize.getText().toString());
    		double expFreq = skbExpFreq.getProgress() / 100.0;
    		double worst = skbWorst.getProgress() / 100.0;
    		int[] results = calc.CalculateSampleSizes(popSize, expFreq, worst);
    		txt80.setText(results[0] + "");
    		txt90.setText(results[1] + "");
    		txt95.setText(results[2] + "");
    		txt97.setText(results[3] + "");
    		txt99.setText(results[4] + "");
    		txt999.setText(results[5] + "");
    		txt9999.setText(results[6] + "");
    	}
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar.getId() == R.id.skbExpFreq)
		{
			txtExpFreq.setText(((progress / 10) / 10.0) + "%");
		}
		else
		{
			txtWorst.setText(((progress / 10) / 10.0) + "%");
			
		}		
		Calculate();
		imm.hideSoftInputFromWindow(txtPopSize.getWindowToken(), 0);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}