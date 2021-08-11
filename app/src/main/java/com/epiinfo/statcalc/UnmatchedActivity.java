package com.epiinfo.statcalc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.epiinfo.droid.DeviceManager;
import com.epiinfo.droid.R;
import com.epiinfo.statcalc.calculators.Unmatched;
import com.epiinfo.statcalc.etc.CCResult;

import java.text.DecimalFormat;

public class UnmatchedActivity  extends Activity implements SeekBar.OnSeekBarChangeListener {
	
	Spinner ddlConfidence;
	TextView txtPower;
	SeekBar skbPower;
	EditText txtControlRatio;
	TextView txtPercentExposed;
	SeekBar skbPercentExposed;
	EditText txtOddsRatio;
	TextView txtPercentCasesExposure;
	SeekBar skbPercentCasesExposure;
	TextView txtKelseyCases;
	TextView txtKelseyControls;
	TextView txtKelseyTotal;
	TextView txtFleissCases;
	TextView txtFleissControls;
	TextView txtFleissTotal;
	TextView txtFleissCCCases;
	TextView txtFleissCCControls;
	TextView txtFleissCCTotal;
	InputMethodManager imm;
	Unmatched calc;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DeviceManager.SetOrientation(this,false);
		this.setTheme(android.R.style.Theme_Holo_Light);
        
        setContentView(R.layout.statcalc_unmatched);
        calc = new Unmatched();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ddlConfidence = (Spinner) findViewById(R.id.ddlConfidence);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.confidence_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ddlConfidence.setAdapter(adapter);
        ddlConfidence.setOnItemSelectedListener(
        		new OnItemSelectedListener() {
        				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        				{
        					Calculate();
        				}
        				
        				public void onNothingSelected(AdapterView<?> parent)
        				{
        					// do nothing
        				}
        		}
        );
        txtPower = (TextView) findViewById(R.id.txtPower);
        skbPower = (SeekBar) findViewById(R.id.skbPower);
        skbPower.setOnSeekBarChangeListener(this);
        txtControlRatio = (EditText) findViewById(R.id.txtControlRatio);
        txtControlRatio.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				if (txtControlRatio.getText().toString().length() > 0)
				{
					Calculate();
				}
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
        txtPercentExposed = (TextView) findViewById(R.id.txtPercentExposed);
        skbPercentExposed = (SeekBar) findViewById(R.id.skbPercentExposed);
        skbPercentExposed.setOnSeekBarChangeListener(this);
        txtOddsRatio = (EditText) findViewById(R.id.txtOddsRatio);
        txtOddsRatio.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (txtOddsRatio.getText().toString().length() > 0)
				{
					double oddsRatioRaw = Double.parseDouble(txtOddsRatio.getText().toString());
					double percentExposed = ((skbPercentExposed.getProgress() / 10) / 10.0)/100.0;
					double percentCases = calc.OddsToPercentCases(oddsRatioRaw, percentExposed);
				
					skbPercentCasesExposure.setProgress((int)Math.round(percentCases * 100.0));
				}
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
        txtPercentCasesExposure = (TextView) findViewById(R.id.txtPercentCasesExposure);
        skbPercentCasesExposure = (SeekBar) findViewById(R.id.skbPercentCasesExposure);
        skbPercentCasesExposure.setOnSeekBarChangeListener(this);
        txtKelseyCases = (TextView) findViewById(R.id.txtKelseyCases);
        txtKelseyControls = (TextView) findViewById(R.id.txtKelseyControls);
        txtKelseyTotal = (TextView) findViewById(R.id.txtKelseyTotal);
        txtFleissCases = (TextView) findViewById(R.id.txtFleissCases);
        txtFleissControls = (TextView) findViewById(R.id.txtFleissControls);
        txtFleissTotal = (TextView) findViewById(R.id.txtFleissTotal);
        txtFleissCCCases = (TextView) findViewById(R.id.txtFleissCCCases);
        txtFleissCCControls = (TextView) findViewById(R.id.txtFleissCCControls);
        txtFleissCCTotal = (TextView) findViewById(R.id.txtFleissCCTotal);
        Calculate();
		imm.hideSoftInputFromWindow(txtControlRatio.getWindowToken(), 0);
    }
    
    private void Calculate()
    {
    	calc = new Unmatched();
    	String confidenceRaw = ddlConfidence.getSelectedItem().toString().split("%")[0];
		double confidence = (100.0 - Double.parseDouble(confidenceRaw)) / 100.0;
		double power = (skbPower.getProgress() / 100.0) / 100.0;
		double controlRatio = 1;
		if (txtControlRatio.getText().toString().length() > 0)
		{
			controlRatio = Double.parseDouble(txtControlRatio.getText().toString());
		}
		else
		{
			txtControlRatio.setText("1");
		}
		double percentExposed = (skbPercentExposed.getProgress() / 100.0) / 100.0;
		double oddsRatio = 3;
		if (txtOddsRatio.getText().toString().length() > 0)
		{
			oddsRatio = Double.parseDouble(txtOddsRatio.getText().toString());
		}
		else
		{
			txtOddsRatio.setText("3");
		}
		double percentCasesExposure = (skbPercentCasesExposure.getProgress() / 100.0) / 100.0;
		CCResult results = calc.CalculateUnmatchedCaseControl(confidence, power, controlRatio, percentExposed, oddsRatio, percentCasesExposure);
		txtKelseyCases.setText(results.GetKelseyCases() + "");
		txtKelseyControls.setText(results.GetKelseyControls() + "");
		txtKelseyTotal.setText((results.GetKelseyCases() + results.GetKelseyControls()) + "");
		txtFleissCases.setText(results.GetFleissCases() + "");
		txtFleissControls.setText(results.GetFleissControls() + "");
		txtFleissTotal.setText((results.GetFleissCases() + results.GetFleissControls()) + "");
		txtFleissCCCases.setText(results.GetFleissCCCases() + "");
		txtFleissCCControls.setText(results.GetFleissCCControls() + "");
		txtFleissCCTotal.setText((results.GetFleissCCCases() + results.GetFleissCCControls()) + "");
		
		//scroller.fullScroll(ScrollView.FOCUS_DOWN);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar.getId() == R.id.skbPower)
		{
			txtPower.setText(((progress / 10) / 10.0) + "%");
		}
		else if (seekBar.getId() == R.id.skbPercentExposed)
		{
			double percentExposedRaw = (progress / 10) / 10.0;
			double percentExposed = percentExposedRaw / 100.0;
			double percentCases = ((skbPercentCasesExposure.getProgress() / 10) / 10.0)/100.0;
			double oddsRatioRaw = calc.PercentCasesToOdds(percentCases, percentExposed);
			
			txtPercentExposed.setText(percentExposedRaw + "%");
			if (oddsRatioRaw < 999)
			{
				txtOddsRatio.setText(new DecimalFormat("#.##").format(oddsRatioRaw));
			}
			else
			{
				txtOddsRatio.setText(new DecimalFormat("#").format(oddsRatioRaw));
			}
		}		
		else
		{
			double percentCasesRaw = (progress / 10) / 10.0;
			double percentCases = percentCasesRaw / 100.0;
			double percentExposed = ((skbPercentExposed.getProgress() / 10) / 10.0)/100.0;
			double oddsRatioRaw = calc.PercentCasesToOdds(percentCases, percentExposed);
			
			txtPercentCasesExposure.setText(percentCasesRaw + "%");
			if (fromUser)
			{
				if (oddsRatioRaw < 999)
				{
					txtOddsRatio.setText(new DecimalFormat("#.##").format(oddsRatioRaw));
				}
				else
				{
					txtOddsRatio.setText(new DecimalFormat("#").format(oddsRatioRaw));
				}
			}
		}
		if (fromUser)
		{
			imm.hideSoftInputFromWindow(txtOddsRatio.getWindowToken(), 0);
		}
		Calculate();
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