package com.epiinfo.statcalc;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.epiinfo.droid.DeviceManager;
import com.epiinfo.droid.R;
import com.epiinfo.statcalc.calculators.Binomial;

import java.text.DecimalFormat;

public class BinomialActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	EditText txtNumerator;
	EditText txtTotalObservations;
	TextView txtLTLabel;
	TextView txtLTValue;
	TextView txtLELabel;
	TextView txtLEValue;
	TextView txtEQLabel;
	TextView txtEQValue;
	TextView txtGELabel;
	TextView txtGEValue;
	TextView txtGTLabel;
	TextView txtGTValue;
	TextView txtPValueValue;
	TextView txtCIValue;
	SeekBar skbExpPercent;
	TextView txtExpPercentValue;
	InputMethodManager imm;
	private ProgressBar ciWaitCursor;
	
	private LowerLimitProcessor llp;
	private UpperLimitProcessor ulp;
	
	String lowerLimit = "";
	String upperLimit = "";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DeviceManager.SetOrientation(this,false);
		this.setTheme(android.R.style.Theme_Holo_Light);
        
        setContentView(R.layout.statcalc_binomial);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ciWaitCursor = (ProgressBar) findViewById(R.id.waitCursor);
        
        txtNumerator = (EditText) findViewById(R.id.txtNumerator);
        txtNumerator.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Calculate();
				CalculateCI();
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
        
        txtTotalObservations = (EditText) findViewById(R.id.txtTotalObservations);
        txtTotalObservations.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Calculate();
				CalculateCI();
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

        txtExpPercentValue = (TextView) findViewById(R.id.txtExpPercentValue);
        skbExpPercent = (SeekBar) findViewById(R.id.skbExpPercent);
        skbExpPercent.setOnSeekBarChangeListener(this);
        txtLTLabel = (TextView) findViewById(R.id.txtLTLabel);
        txtLTValue = (TextView) findViewById(R.id.txtLTValue);
        txtLELabel = (TextView) findViewById(R.id.txtLELabel);
        txtLEValue = (TextView) findViewById(R.id.txtLEValue);
        txtEQLabel = (TextView) findViewById(R.id.txtEQLabel);
        txtEQValue = (TextView) findViewById(R.id.txtEQValue);
        txtGELabel = (TextView) findViewById(R.id.txtGELabel);
        txtGEValue = (TextView) findViewById(R.id.txtGEValue);
        txtGTLabel = (TextView) findViewById(R.id.txtGTLabel);
        txtGTValue = (TextView) findViewById(R.id.txtGTValue);
        txtPValueValue = (TextView) findViewById(R.id.txtPValueValue);
        txtCIValue = (TextView) findViewById(R.id.txtCIValue);
        
//        Calculate();
//        CalculateCI();
    }
    
    public void Calculate()
    {
		txtLTLabel.setText("  < ");
		txtLELabel.setText("<= ");
		txtEQLabel.setText("  = ");
		txtGELabel.setText(">= ");
		txtGTLabel.setText("  > ");
		txtLTValue.setText("");
		txtLEValue.setText("");
		txtEQValue.setText("");
		txtGEValue.setText("");
		txtGTValue.setText("");
		txtPValueValue.setText("");
    	Binomial calc = new Binomial();
		DecimalFormat formatter = new DecimalFormat("#.########");
    	if (txtNumerator.getText().toString().length() > 0 &&
    			txtTotalObservations.getText().toString().length() > 0)
    	{
    		int numerator = Integer.parseInt(txtNumerator.getText().toString());
    		int observations = Integer.parseInt(txtTotalObservations.getText().toString());
    		double prob = skbExpPercent.getProgress() / 10000.0;
    		if (numerator <= observations)
    		{
        		double[] results = calc.CalculateBinomialProbabilities(observations, numerator, prob);
        		txtLTLabel.setText("  < " + numerator);
        		txtLELabel.setText("<= " + numerator);
        		txtEQLabel.setText("  = " + numerator);
        		txtGELabel.setText(">= " + numerator);
        		txtGTLabel.setText("  > " + numerator);
        		txtLTValue.setText(formatter.format(results[0]));
        		txtLEValue.setText(formatter.format(results[1]));
        		txtEQValue.setText(formatter.format(results[2]));
        		txtGEValue.setText(formatter.format(results[3]));
        		txtGTValue.setText(formatter.format(results[4]));
        		txtPValueValue.setText(formatter.format(results[5]));
    		}
    		else
    		{
        		txtLTLabel.setText("  < " + numerator);
        		txtLELabel.setText("<= " + numerator);
        		txtEQLabel.setText("  = " + numerator);
        		txtGELabel.setText(">= " + numerator);
        		txtGTLabel.setText("  > " + numerator);
        		txtLTValue.setText(formatter.format(1.0));
        		txtLEValue.setText(formatter.format(1.0));
        		txtEQValue.setText(formatter.format(0.0));
        		txtGEValue.setText(formatter.format(0.0));
        		txtGTValue.setText(formatter.format(0.0));
        		txtPValueValue.setText("");
    		}
    	}
    }
    
    public void CalculateCI()
    {
    	this.lowerLimit = "";
    	this.upperLimit = "";
    	try
    	{
    		if (llp.cancel(true)) { }
    		if (ulp.cancel(true)) { }
    		while (!llp.isCancelled() || !ulp.isCancelled())
    		{
    			wait(10);
    		}
    	}
    	catch (Exception e)
    	{ }
		ciWaitCursor.setVisibility(View.GONE);
    	
		txtCIValue.setText(lowerLimit + " - " + upperLimit);
    	if (txtNumerator.getText().toString().length() > 0 &&
    			txtTotalObservations.getText().toString().length() > 0)
    	{
    		int numerator = Integer.parseInt(txtNumerator.getText().toString());
    		int observations = Integer.parseInt(txtTotalObservations.getText().toString());
    		if (numerator <= observations)
    		{
    			ciWaitCursor.setVisibility(View.VISIBLE);
    			llp = new LowerLimitProcessor();
    			llp.execute(observations, numerator);
    			ulp = new UpperLimitProcessor();
    	    	ulp.execute(observations, numerator);
    		}
    	}
    }
    
    public void CalculateLowerLimit(int numerator, int observations)
    {
    	Binomial calc = new Binomial();
    	int lcl = calc.CalculateBinomialLowerLimit(observations, numerator);
    	this.lowerLimit = lcl + "";
    }
    
    public void CalculateUpperLimit(int numerator, int observations)
    {
    	Binomial calc = new Binomial();
    	int ucl = calc.CalculateBinomialUpperLimit(observations, numerator);
    	this.upperLimit = ucl + "";
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		txtExpPercentValue.setText(((progress / 10) / 10.0) + "%");
		Calculate();
		imm.hideSoftInputFromWindow(txtNumerator.getWindowToken(), 0);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	private class LowerLimitProcessor extends AsyncTask<Integer,Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Integer... params) {
			int numerator = params[0];
			int observations = params[1];
			CalculateLowerLimit(observations, numerator);

			return true;
		}
		
		@Override
        protected void onPostExecute(Boolean result) {
			txtCIValue.setText(lowerLimit + " - " + upperLimit);
		}
	}
	
	private class UpperLimitProcessor extends AsyncTask<Integer,Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Integer... params) {
			int numerator = params[0];
			int observations = params[1];
			CalculateUpperLimit(observations, numerator);

			return true;
		}
		
		@Override
        protected void onPostExecute(Boolean result) {
			txtCIValue.setText(lowerLimit + " - " + upperLimit);
			ciWaitCursor.setVisibility(View.GONE);
		}
	}
}
