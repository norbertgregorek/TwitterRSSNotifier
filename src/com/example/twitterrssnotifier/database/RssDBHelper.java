package com.example.twitterrssnotifier.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.twitterrssnotifier.miscellaneous.TwitterHelper;

/* Class that represents our database.
 * Extends from standard Android helper for SQLite.
 * To interact with this class I use RssContentProvider class.
 */
public class RssDBHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "rss_feed.db";
	private static final int DATABASE_VERSION = 1;
	
	public RssDBHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		//TODO
		//onUpgrade(getWritableDatabase(), DATABASE_VERSION, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		/* Creates our two tables.
		 * I suggested a trip from http://www.vogella.com/tutorials/AndroidSQLite/article.html
		 * to keep each table as a separate class
		 */
		RssFeedTable.onCreate(database);
		RssMessageTable.onCreate(database);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		RssFeedTable.onUpgrade(database, oldVersion, newVersion);
		RssMessageTable.onUpgrade(database, oldVersion, newVersion);
	}
	
	//Convenient function for deleting feeds
	public static void deleteFeedFromDB(String rssLink, ContentResolver resolver)
	{
		String selection = RssFeedTable.COLUMN_RSS_LINK + " = " + "'" + rssLink + "' and "
				+ RssFeedTable.COLUMN_USER + " = " + "'"
				+ TwitterHelper.getInstance().getUser().getScreenName() + "'";
		//Deletes feed
		resolver.delete(RssContentProvider.CONTENT_URI_FEED, selection, null);
		//Deletes messages
		resolver.delete(RssContentProvider.CONTENT_URI_MESSAGE, selection, null);
	}
}