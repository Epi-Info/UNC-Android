package com.epiinfo.unc;


/**
 * The class FaqData contains a static array of FaqItem items.
 * These values are NOT stored persistently and are not dynamic.
 * The FAQ list is built statically inside each app release. 
 * This file should be re-usable for different companies.  
 * 
 * @author keithcollins
 */
public class FaqData {
	
	public final static int maxItems = 20;
	public static int activeCount = 0;
	
    public static FaqItem faqList[] = {
    	new FaqItem(), // 1
    	new FaqItem(), // 2
    	new FaqItem(), // 3
    	new FaqItem(), // 4
    	new FaqItem(), // 5
    	new FaqItem(), // 6
    	new FaqItem(), // 7
    	new FaqItem(), // 8
    	new FaqItem(), // 9
    	new FaqItem(), // 10
    	new FaqItem(), // 11
    	new FaqItem(), // 12
    	new FaqItem(), // 13
    	new FaqItem(), // 14
    	new FaqItem(), // 15
    	new FaqItem(), // 16
    	new FaqItem(), // 17
    	new FaqItem(), // 18
    	new FaqItem(), // 19
    	new FaqItem(), // 20
    };

	public static void clearFaqList() {
		activeCount = 0;
		for (int i=0; i<maxItems; i++) {
			faqList[i].title = "";
			faqList[i].text = "";
			}
		}

}