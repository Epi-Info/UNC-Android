package com.epiinfo.unc;


/**
 * The class FaqItem defines an instance of a FAQ item
 * with fields for Title and Text details. 
 * This class is used by the custom array adapter class.
 * This file should be re-usable for different companies.  
 * 
 * @author keithcollins
 */

public class FaqItem {
	
	public FaqItem() {
		title = "";
		text = "";
	}
	
	public String title;
	public String text;

}