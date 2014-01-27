package com.example.twitterrssnotifier.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.twitterrssnotifier.R;
import com.example.twitterrssnotifier.database.RssContentProvider;
import com.example.twitterrssnotifier.database.RssFeedTable;
import com.example.twitterrssnotifier.database.RssMessageTable;
import com.example.twitterrssnotifier.miscellaneous.ToastHelper;
import com.example.twitterrssnotifier.rsslibrary.BadRssFromUrlException;
import com.example.twitterrssnotifier.rsslibrary.RssFeed;
import com.example.twitterrssnotifier.rsslibrary.RssMessage;

/*
 * Class that represents service that works in background to download
 * new feeds that users follows.
 */
public class RssService extends IntentService
{
	public static final String SERVICE_NAME = "RssService";
	public static final String NOTIFICATION = "com.vogella.android.service.receiver";
	
	public RssService()
	{
		super(SERVICE_NAME);
	}
	
	public static void initializeService(Context context)
	{
		final long REPEAT_TIME = 1000 * 1000;
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, RssStartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0,
				i, PendingIntent.FLAG_CANCEL_CURRENT);
		
		//How often we refresh our service
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 1);
		
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				REPEAT_TIME, pending);
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		//We want to update every rss feed
		//Get all rows from our db
		String[] projection = { RssFeedTable.COLUMN_USER, RssFeedTable.COLUMN_RSS_LINK };
		Cursor cursor = getContentResolver().query(RssContentProvider.CONTENT_URI_FEED,
				projection, null, null, null);
		//It's wise to keep only unique rss feeds (link -> list of users)
		HashMap<String, List<String>> uniqueLinks = new HashMap<String, List<String>>();
		
		//Just break a service when there is no data
		if (!cursor.moveToFirst())
		{
			return;
		}
		while (!cursor.isAfterLast())
		{
			String rssLink = cursor.getString( cursor.getColumnIndex(RssFeedTable.COLUMN_RSS_LINK) );
			String user = cursor.getString( cursor.getColumnIndex(RssFeedTable.COLUMN_USER) );
			
			List<String> usersList = uniqueLinks.get(rssLink);
			if (usersList == null)
			{
				usersList = new ArrayList<String>();
				uniqueLinks.put(rssLink, usersList);
			}
			//Add a new user
			usersList.add(user);
			cursor.moveToNext();
		}
		cursor.close();
		
		//TODO this is very simple algorithm
		//It's wise to execute it in parallel
		for (Entry<String, List<String>> entry: uniqueLinks.entrySet())
		{
			RssFeed feed = null;
			try
			{
				feed = RssFeed.Create(entry.getKey());
			}
			catch (Exception e)
			{
				Log.e("RssService", e.getMessage());
				continue;
			}
			
			List<RssMessage> messages = feed.getMessageList();
			
			//Process every message (the newest ones are earlier)
			//CAUTION! I assume that if this message exist in user's messages
			//I ignore rest of them!
			for (RssMessage message : messages)
			{
				List<String> removalList = new ArrayList<String>();
				
				for (String user: entry.getValue())
				{
					//Prepare data to insert
					ContentValues values = new ContentValues();
					
					//Prepare all data of RssMessage to insert into RssMessageTable
					values.put(RssMessageTable.COLUMN_USER, user);
					values.put(RssMessageTable.COLUMN_DESCRIPTION, message.getDescription());
					values.put(RssMessageTable.COLUMN_LINK, message.getLink());
					values.put(RssMessageTable.COLUMN_PUBDATE, message.getPubdata());
					values.put(RssMessageTable.COLUMN_RSS_LINK, message.getRssLink());
					values.put(RssMessageTable.COLUMN_TITLE, message.getTitle());
					
					//Try to insert a new message
					try
					{
						getContentResolver().insert(RssContentProvider.CONTENT_URI_MESSAGE, values);
						Log.i("NewMessage", message.getTitle());
					}
					catch (SQLiteException e)
					{
						//Just delete user not to perform it again
						removalList.add(user);
						Log.i("RssServiceDuplicate", user + " currently has " + message.getLink());
					}
				}
				
				//Remove users
				entry.getValue().removeAll(removalList);
			}
		}
	}
	
	//Helper method just to update one Rss channel (we call it after clicking one channel)
	public static void updateRssFeed(final Activity activity, final String rssLink, final String user)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				RssFeed feed = null;
				try
				{
					feed = RssFeed.Create(rssLink);
				}
				catch (IOException e)
				{
					ToastHelper.showToast(activity, activity.getString(R.string.update_feed)
							+ activity.getString(R.string.io_error));
					return;
				}
				catch (BadRssFromUrlException e)
				{
					ToastHelper.showToast(activity, activity.getString(R.string.update_feed)
							+ activity.getString(R.string.bad_rss_from_url));
					return;
				}
				catch (XmlPullParserException e)
				{
					ToastHelper.showToast(activity, activity.getString(R.string.update_feed)
							+ activity.getString(R.string.xml_parsing_error));
					return;
				}
				
				List<RssMessage> messages = feed.getMessageList();

				//Process every message (the newest ones are earlier)
				//CAUTION! I assume that if this message exist in user's messages
				//I ignore rest of them!
				for (RssMessage message : messages)
				{
					List<String> removalList = new ArrayList<String>();

					//Prepare data to insert
					ContentValues values = new ContentValues();

					//Prepare all data of RssMessage to insert into RssMessageTable
					values.put(RssMessageTable.COLUMN_USER, user);
					values.put(RssMessageTable.COLUMN_DESCRIPTION, message.getDescription());
					values.put(RssMessageTable.COLUMN_LINK, message.getLink());
					values.put(RssMessageTable.COLUMN_PUBDATE, message.getPubdata());
					values.put(RssMessageTable.COLUMN_RSS_LINK, message.getRssLink());
					values.put(RssMessageTable.COLUMN_TITLE, message.getTitle());

					//Try to insert a new message
					try
					{
						activity.getContentResolver().insert(RssContentProvider.CONTENT_URI_MESSAGE, values);
						Log.i("NewMessage", message.getTitle());
					}
					catch (SQLiteException e)
					{
						//Just delete user not to perform it again
						removalList.add(user);
						Log.i("RssServiceDuplicate", user + " currently has " + message.getLink());
					}
				}
			}
		}).start();
	}
}
