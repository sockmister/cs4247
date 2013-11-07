package com.cs4247;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;

public class CalendarContext {
	
	private Context context;
	
	public final String[] INSTANCE_PROJECTION = new String[] {
	    CalendarContract.Instances.EVENT_ID,      // 0
	    Instances.BEGIN,         // 1
	    Instances.TITLE          // 2
	    };
	
	final int PROJECTION_ID_INDEX = 0;
	final int PROJECTION_BEGIN_INDEX = 1;
	final int PROJECTION_TITLE_INDEX = 2;
	
	public CalendarContext(Context context){
		this.context = context;
	}
	
	public boolean isFree(){
    	
    	// Specify the date range you want to search for recurring
    	// event instances
		int hours = 3;
		long hoursInMillis = 3 * 60 * 60 * 100;
		
    	long startMillis = System.currentTimeMillis();
    	long endMillis = startMillis + hoursInMillis; 
    	
    	Cursor cur = null;
    	ContentResolver cr = context.getContentResolver();
    	
    	// this shouldn't be done on main thread.
    	cur = Instances.query(cr, INSTANCE_PROJECTION, startMillis, endMillis);
    	   
    	while (cur.moveToNext()) {
    	    String title = null;
    	    long eventID = 0;
    	    long beginVal = 0;    
    	    
    	    // Get the field values
    	    eventID = cur.getLong(PROJECTION_ID_INDEX);
    	    beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
    	    title = cur.getString(PROJECTION_TITLE_INDEX);
    	              
    	    // Do something with the values. 
    	    System.out.println("Event: " + title);
    	    Calendar calendar = Calendar.getInstance();
    	    calendar.setTimeInMillis(beginVal);  
    	    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    	    System.out.println("Date: "+ formatter.format(calendar.getTime()));        
    	}
    	
    	if(cur.getCount() == 0)
    		return true;
    	else 
    		return false;
	}

}
