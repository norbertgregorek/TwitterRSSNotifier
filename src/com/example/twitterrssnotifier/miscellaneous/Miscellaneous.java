package com.example.twitterrssnotifier.miscellaneous;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.widget.DatePicker;

/*
 * Keeps some useful functions that are not associated with objects.
 */
public class Miscellaneous
{
	//Date format
	public static final String RFC822 = "EEE, dd MMM yyyy HH:mm:ss Z";
	
	/*
	 * This function comes from the following link:
	 * http://stackoverflow.com/questions/7953725/how-to-convert-milliseconds-to-date-format-in-android
	 */
	/**
	 * Return date in specified format.
	 * @param milliSeconds Date in milliseconds
	 * @param dateFormat Date format 
	 * @return String representing date in specified format
	 */
	public static String getDateAsString(long milliSeconds, String dateFormat)
	{
		// Create a DateFormatter object for displaying date in specified format.
		DateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);

		// Create a calendar object that will convert the date and time value in milliseconds to date. 
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}
	
	
	/*
	 * It's used when we filter messages because we hold time as long in database.
	 */
	public static long getDateInMilliseconds(DatePicker datePicker, int hour)
	{
		return getDateInMilliseconds(datePicker.getDayOfMonth(), datePicker.getMonth(),
				datePicker.getYear(), hour);
	}
	public static long getDateInMilliseconds(int day, int month, int year, int hour)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);
		return cal.getTimeInMillis();
	}
}
