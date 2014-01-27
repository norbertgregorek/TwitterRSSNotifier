package com.example.twitterrssnotifier.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/*
 * Implementation of ContentProvider. Tutorial link :
 * http://www.vogella.com/tutorials/AndroidSQLite/article.html
 */
public class RssContentProvider extends ContentProvider
{
	//Our database
	private RssDBHelper database;
	
	//Useful static declarations
	private static final String AUTHORITY = "com.example.twitterrssnotifier.database.rsscontentprovider";
	private static final String PATH_FEED = "feed";
	private static final String PATH_MESSAGE = "message";
	public static final Uri CONTENT_URI_FEED = Uri.parse("content://" + AUTHORITY
			+ "/" + PATH_FEED);
	public static final Uri CONTENT_URI_MESSAGE = Uri.parse("content://" + AUTHORITY
			+ "/" + PATH_MESSAGE);
	
	//For UriMatcher
	private static final int FEED = 10;
	private static final int FEED_ID = 20;
	private static final int MESSAGE = 30;
	private static final int MESSAGE_ID = 40;
	
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static
	{
		uriMatcher.addURI(AUTHORITY, PATH_FEED, FEED);
		uriMatcher.addURI(AUTHORITY, PATH_FEED + "/#", FEED_ID);
		uriMatcher.addURI(AUTHORITY, PATH_MESSAGE, MESSAGE);
		uriMatcher.addURI(AUTHORITY, PATH_MESSAGE + "/#", MESSAGE_ID);
	}
	

	@Override
	public boolean onCreate()
	{
		database = new RssDBHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder)
	{
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		int uriType = uriMatcher.match(uri);
		
		if (uriType == FEED || uriType == FEED_ID)
		{
			//RssFeedTable.checkColumnCorrectness(projection);
			queryBuilder.setTables(RssFeedTable.TABLE_NAME);
			//Append WHERE clause when uriType is FEED_ID
			if (uriType == FEED_ID)
				queryBuilder.appendWhere(RssFeedTable.COLUMN_ID + "=" + uri.getLastPathSegment());
		}
		else if (uriType == MESSAGE || uriType == MESSAGE_ID)
		{
			//RssMessageTable.checkColumnCorrectness(projection);
			queryBuilder.setTables(RssMessageTable.TABLE_NAME);
			//Append WHEER clause when uriType is MESSAGE_ID
			if (uriType == MESSAGE_ID)
				queryBuilder.appendWhere(RssMessageTable.COLUMN_ID + "=" + uri.getLastPathSegment());
		}
		else
			throw new IllegalArgumentException("Unknown URI: " + uri);
		
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		//Notify potential listeners
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri)
	{
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		SQLiteDatabase db = database.getWritableDatabase();
		long id = 0;
		String base_path = null;
		switch(uriMatcher.match(uri))
		{
			case FEED:
				id = db.insertOrThrow(RssFeedTable.TABLE_NAME, null, values);
				base_path = PATH_FEED;
				break;
			case MESSAGE:
				id = db.insertOrThrow(RssMessageTable.TABLE_NAME, null, values);
				base_path = PATH_MESSAGE;
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(base_path + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		String table = null;
		SQLiteDatabase db = database.getWritableDatabase();
		int uriType = uriMatcher.match(uri);
		
		if (uriType == FEED || uriType == FEED_ID)
			table = RssFeedTable.TABLE_NAME;
		else if (uriType == MESSAGE || uriType == MESSAGE_ID)
			table = RssMessageTable.TABLE_NAME;
		else
			throw new IllegalArgumentException("Unknown URI: " + uri);
		
		if (uriType == FEED_ID || uriType == MESSAGE_ID)
		{
			String idName = null;
			switch (uriType)
			{
				case FEED_ID:
					idName = RssFeedTable.COLUMN_ID;
					break;
				case MESSAGE_ID:
					idName = RssMessageTable.COLUMN_ID;
					break;
			}
			if (TextUtils.isEmpty(selection))
				selection = idName + "=" + uri.getLastPathSegment();
			else
				selection = idName + "=" + uri.getLastPathSegment() 
							+ " and " + selection;
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return db.delete(table, selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
						String[] selectionArgs)
	{
		String table = null;
		SQLiteDatabase db = database.getWritableDatabase();
		int uriType = uriMatcher.match(uri);
		
		if (uriType == FEED || uriType == FEED_ID)
			table = RssFeedTable.TABLE_NAME;
		else if (uriType == MESSAGE || uriType == MESSAGE_ID)
			table = RssMessageTable.TABLE_NAME;
		else
			throw new IllegalArgumentException("Unknown URI: " + uri);
		
		if (uriType == FEED_ID || uriType == MESSAGE_ID)
		{
			String idName = null;
			switch (uriType)
			{
				case FEED_ID:
					idName = RssFeedTable.COLUMN_ID;
					break;
				case MESSAGE_ID:
					idName = RssMessageTable.COLUMN_ID;
					break;
			}
			if (TextUtils.isEmpty(selection))
				selection = idName + "=" + uri.getLastPathSegment();
			else
				selection = idName + "=" + uri.getLastPathSegment() 
							+ " and " + selection;
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return db.update(table, values, selection, selectionArgs);
	}
}