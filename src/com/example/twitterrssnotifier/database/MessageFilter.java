package com.example.twitterrssnotifier.database;

import java.util.Calendar;

import com.example.twitterrssnotifier.miscellaneous.Miscellaneous;

/* An assistant class that filter our rss messages.
/* At this point only date interval is implemented
 */
public class MessageFilter
{
	private long beginTime = Long.MIN_VALUE;
	private long endTime = Long.MAX_VALUE;
	
	public MessageFilter(long beginTime, long endTime)
	{
		this.beginTime = beginTime;
		this.endTime = endTime;
	}
	public MessageFilter()
	{
	}
	
	//It sets our time as interval [currentDay - howManyDay - 1; currentDay]
	public void setAsLastDays(int howManyDay)
	{
		Calendar cal = Calendar.getInstance();
		beginTime = Miscellaneous.getDateInMilliseconds(
				cal.get(Calendar.DAY_OF_MONTH) - howManyDay + 1 ,cal.get(Calendar.MONTH),
				cal.get(Calendar.YEAR), 0);
		endTime = Long.MAX_VALUE;
	}
		
	
	//Separete messages in SQLite databese (our where clause)
	public String getWhereClause()
	{
		String whereClause = "";
		whereClause = RssMessageTable.COLUMN_PUBDATE + " >= " + Long.toString(beginTime)
				+ " and " + RssMessageTable.COLUMN_PUBDATE + " <= " + Long.toString(endTime);
		return whereClause;
	}
}