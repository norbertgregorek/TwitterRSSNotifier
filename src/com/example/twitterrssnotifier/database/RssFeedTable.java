package com.example.twitterrssnotifier.database;

import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

/* An assistant class that represents table with feeds that we follow
 */
public class RssFeedTable
{
	//Database table
	public static final String TABLE_NAME = "rss_feed";
	
	//Column names
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USER = "user";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_RSS_LINK = "rss_link";
	
	private static final HashSet<String> availableColumns = new HashSet<String>();
	static
	{
		availableColumns.add(COLUMN_ID);
		availableColumns.add(COLUMN_USER);
		availableColumns.add(COLUMN_LINK);
		availableColumns.add(COLUMN_TITLE);
		availableColumns.add(COLUMN_DESCRIPTION);
		availableColumns.add(COLUMN_RSS_LINK);
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
			+ "("
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_USER + " text not null,"
			+ COLUMN_LINK + " text not null, "
			+ COLUMN_TITLE + " text not null, "
			+ COLUMN_DESCRIPTION + " text not null, "
			+ COLUMN_RSS_LINK + " text not null, "
			+ "unique(" + COLUMN_USER + ", " + COLUMN_RSS_LINK + ") "
			+ ")";
	
	public static void onCreate(SQLiteDatabase database)
	{
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		database.execSQL(DATABASE_CREATE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	                             int newVersion)
	{
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}
}