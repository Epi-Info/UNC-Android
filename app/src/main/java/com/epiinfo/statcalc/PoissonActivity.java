package com.epiinfo.statcalc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.epiinfo.droid.DeviceManager;
import com.epiinfo.droid.R;
import com.epiinfo.statcalc.calculators.Poisson;

import java.text.DecimalFormat;

public class PoissonActivity extends Activity {
	EditText txtObservedEvents;
	EditText txtExpectedEvents;
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
	InputMethodManager imm;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DeviceManager.SetOrientation(this,false);
		this.setTheme(android.R.style.Theme_Holo_Light);
        
        setContentView(R.layout.statcalc_poisson);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        txtObservedEvents = (EditText) findViewById(R.id.txtObservedEvents);
        txtObservedEvents.addTextChangedListener(new TextWatcher() {
			
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
        
        txtExpectedEvents = (EditText) findViewById(R.id.txtExpectedEvents);
        txtExpectedEvents.addTextChangedListener(new TextWatcher() {
			
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
        
        Calculate();
    }
    
    public void Calculate()
    {
    	Poisson calc = new Poisson();
		DecimalFormat formatter = new DecimalFormat("#.########");
    	if (txtObservedEvents.getText().toString().length() > 0 &&
    			txtExpectedEvents.getText().toString().length() > 0)
    	{
    		int observed = Integer.parseInt(txtObservedEvents.getText().toString());
    		int expected = Integer.parseInt(txtExpectedEvents.getText().toString());
    		double[] results = calc.CalculatePoisson(observed, expected);
    		txtLTLabel.setText("  < " + observed);
    		txtLELabel.setText("<= " + observed);
    		txtEQLabel.setText("  = " + observed);
    		txtGELabel.setText(">= " + observed);
    		txtGTLabel.setText("  > " + observed);
    		txtLTValue.setText(formatter.format(results[0]));
    		txtLEValue.setText(formatter.format(results[1]));
    		txtEQValue.setText(formatter.format(results[2]));
    		txtGEValue.setText(formatter.format(results[3]));
    		txtGTValue.setText(formatter.format(results[4]));
    	}
    }
}
