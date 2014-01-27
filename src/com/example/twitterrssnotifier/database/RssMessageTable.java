package com.example.twitterrssnotifier.database;

import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

/* An assistant class that represents table with messages
 */
public class RssMessageTable
{
	//Database table
	public static final String TABLE_NAME = "rss_message";
	//Column names
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_RSS_LINK = "rss_link";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_PUBDATE = "pubdate";
	public static final String COLUMN_USER = "user";
	
	private static final HashSet<String> availableColumns = new HashSet<String>();
	static
	{
		availableColumns.add(COLUMN_ID);
		availableColumns.add(COLUMN_RSS_LINK);
		availableColumns.add(COLUMN_LINK);
		availableColumns.add(COLUMN_TITLE);
		availableColumns.add(COLUMN_DESCRIPTION);
		availableColumns.add(COLUMN_PUBDATE);
		availableColumns.add(COLUMN_USER);
	}
	
	//Check column correctness with projection
	public static void checkColumnCorrectness(String[] projection)
	{
		if (projection != null)
		{
			for (String column: projection)	
			{
				if (!availableColumns.contains(column))
					throw new IllegalArgumentException("Unknown column " + column);
			}
		}
	}
	
	//SQL create statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE_NAME
			+ "(\n"
			+ COLUMN_ID + " integer primary key autoincrement,\n"
			+ COLUMN_USER + " text not null,\n"
			+ COLUMN_RSS_LINK + " text not null,\n"
			+ COLUMN_LINK + " text not null,\n"
			+ COLUMN_TITLE + " text not null,\n"
			+ COLUMN_DESCRIPTION + " text,\n"
			+ COLUMN_PUBDATE + " integer not null,\n"
			+ "unique(" + COLUMN_USER + ", " + COLUMN_RSS_LINK + ", " + COLUMN_LINK + ")\n"
			+ ")";
	
	public static void onCreate(SQLiteDatabase database)
	{
		database.execSQL(DATABASE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	                             int newVersion)
	{
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}
}