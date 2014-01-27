package com.example.twitterrssnotifier.layout;

import twitter4j.TwitterException;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.example.twitterrssnotifier.R;
import com.example.twitterrssnotifier.database.MessageFilter;
import com.example.twitterrssnotifier.database.RssContentProvider;
import com.example.twitterrssnotifier.database.RssMessageTable;
import com.example.twitterrssnotifier.layout.MessageFilterDialog.MessageFilterDialogListener;
import com.example.twitterrssnotifier.layout.TweetMessageDialog.TweetMessageInterface;
import com.example.twitterrssnotifier.miscellaneous.Miscellaneous;
import com.example.twitterrssnotifier.miscellaneous.Settings;
import com.example.twitterrssnotifier.miscellaneous.ToastHelper;
import com.example.twitterrssnotifier.miscellaneous.TwitterHelper;

public class RssMessageActivity extends FragmentActivity
		implements LoaderManager.LoaderCallbacks<Cursor>
{
	//UI elements
	private ListView rssMessageListView;

	//Adapter to our SQLite database
	private SimpleCursorAdapter adapter;

	//Reference to TwitterHelper
	private TwitterHelper twitter;
	
	//Link that we want messages from it
	private String rssLink;
	
	//Our filter dialog
	private MessageFilterDialog messageFilterDialogActivity = new MessageFilterDialog();
	
	//Our default message filter
	private MessageFilter messageFilter = new MessageFilter();
	
	//Our TweetMessage dialog
	private TweetMessageDialog tweetMessageDialog = new TweetMessageDialog();
	
	//Listener to messageFilterDialogActivity
	private MessageFilterDialogListener messageFilterDialogListener =
			new MessageFilterDialogListener()
	{
		@Override
		public void onConfirmButtonClick(MessageFilter filter)
		{
			//Just change our messageFilter
			messageFilter = filter;
			//And reset cursor manager
			getLoaderManager().restartLoader(0, null, RssMessageActivity.this);
		}
		@Override
		public void onCancelButtonClick()
		{}
	};
	//Listener to tweetMessageDialog
	private TweetMessageInterface tweetMessageInterface =
			new TweetMessageInterface()
	{
		@Override
		public void onSuccessfulTweet()
		{
			ToastHelper.showToast(RssMessageActivity.this,
					getString(R.string.successful_tweet_message));
		}
		@Override
		public void onException(TwitterException e)
		{
			ToastHelper.showToast(RssMessageActivity.this,
					getString(R.string.unsuccessful_tweet_message));
			Log.e("TwitterError", e.getMessage());
		}
	};
	
	//We want to have only one WebView visible so here we hold a reference
	//to hold previous WebView
	private WebView previousWebView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rss_message);
		
		//Gets settings
		Settings settings = Settings.getSettings(MainActivity.getSharedPreferences());
		//Here we set our filter according to settings
		messageFilter.setAsLastDays(settings.getHowOldMessages());
		
		//Prepares UI elements
		rssMessageListView = (ListView) findViewById(R.id.rssMessageListView);
		
		//Gets link to the rss channel
		Bundle extras = getIntent().getExtras();
		rssLink = extras.getString(MainActivity.FEED_LINK);
		
		//Set the title of our activity
		setTitle(extras.getString(MainActivity.FEED_TITLE));
		
		if (rssLink == null)
		{
			throw new NullPointerException("Link cannot be null");
		}
		twitter = TwitterHelper.getInstance();
		if (twitter == null)
		{
			throw new NullPointerException("MainActvity: twiter object is null");
		}
		
		//Prepares loader
		adapter = new SimpleCursorAdapter(this, R.layout.rss_message,
				null,
				new String[]
						{RssMessageTable.COLUMN_TITLE, RssMessageTable.COLUMN_PUBDATE,
							RssMessageTable.COLUMN_DESCRIPTION, RssMessageTable.COLUMN_LINK},
				new int[] {R.id.rssMessageTitleTextView, R.id.rssMessagePubdateTextView,
							R.id.rssMessageDescriptionTextView, R.id.rssMessageLinkTextView},
				0);
		rssMessageListView.setAdapter(adapter);
		//Registers contex menu
		registerForContextMenu(rssMessageListView);
		
		//Defines the way how we display data
		adapter.setViewBinder(new ViewBinder()
		{
			public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex)
			{
				//We have to convert data into deadible format (beacuse it's saved as milliseconds)
				if (aColumnIndex == aCursor.getColumnIndex(RssMessageTable.COLUMN_PUBDATE))
				{
					Long time_in_milliseconds = aCursor.getLong(aColumnIndex);
					String time_as_string = Miscellaneous
							.getDateAsString(time_in_milliseconds, Miscellaneous.RFC822);

					TextView textView = (TextView) aView;
					textView.setText(time_as_string);
					return true;
				}

				return false;
			}
		});
		
		rssMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				WebView webView = (WebView) view.findViewById(R.id.rssMessageWebView);
				
				//Open full article in WebBrowser if and only if we see descriptnio (WebView)
				if (webView.getVisibility() == View.VISIBLE && previousWebView == webView)
				{
					previousWebView = null;
					webView.setVisibility(View.GONE);
					//Get webpage that we want to open
					String webpageAdress = ((TextView) view.findViewById(R.id.rssMessageLinkTextView)).getText().toString();

					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webpageAdress));
					startActivity(browserIntent);
				}
				//Otherwise show description (WebView) and maybe hide previous
				else
				{
					if (previousWebView != null)
					{
						previousWebView.setVisibility(View.GONE);
					}
					previousWebView = webView;
					TextView textView = (TextView) view.findViewById(R.id.rssMessageDescriptionTextView);
					String text = textView.getText().toString();
					webView.setVisibility(View.VISIBLE);
					webView.loadData(text, "text/html; charset=UTF-8", null);
				}
			}
		});
		
		//Set listener to our messageFilterDialog
		messageFilterDialogActivity.setListener(messageFilterDialogListener);
		//Set listener to our tweetMessageInterface
		tweetMessageDialog.setListener(tweetMessageInterface);
		getLoaderManager().restartLoader(0, null, RssMessageActivity.this);
	}
	
	/***********/
	//Context Menu
	/**********/
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.rssMessageListView)
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.rss_message_long_clicked, menu);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
	      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	      switch(item.getItemId())
	      {
	    	  case R.id.action_tweet_message:
	    		  Bundle args = new Bundle();
	    		  
	    		  //Get view of seleected item
	    		  View v = rssMessageListView.getAdapter().getView(info.position, null, rssMessageListView);
	    		  TextView titleTextView = (TextView) v.findViewById(R.id.rssMessageTitleTextView);
	    		  TextView linkTextView = (TextView) v.findViewById(R.id.rssMessageLinkTextView);
	    		  String message = titleTextView.getText().toString() + " " 
	    				  + linkTextView.getText().toString();
	    		  args.putString(TweetMessageDialog.MESSAGE, message);
	    		  
	    		  //Just set arguments and open dialog
	    		  tweetMessageDialog.setArguments(args);
	    		  tweetMessageDialog.show(getSupportFragmentManager(), "tweetMessageDialog");
	    		  return true;
	    	  default:
	    		  return super.onContextItemSelected(item);
	      }
	}

	/**********/
	//MENU
	/**********/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rss_message, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_message_filter:
				messageFilterDialogActivity.show(getSupportFragmentManager(), "messageFilterDialog");
				break;
		}
		return true;
	}

	//Implementation of LoaderManager.LoaderCallbacks<Cursor>
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		if (twitter.getUser() == null)
		{
			//TEST
			//throw new NullPointerException("You have to be logged in");
		}
		//TEST
		//String where = RssMessageTable.COLUMN_USER + " = " + "'" + "RssNotifierTest" + "' and "
				//+ RssMessageTable.COLUMN_RSS_LINK + " = " + "'" + rssLink + "'";
		String whereClause = RssMessageTable.COLUMN_USER + " = " + "'" + twitter.getUser().getScreenName() + "' and "
				+ RssMessageTable.COLUMN_RSS_LINK + " = " + "'" + rssLink + "'";
		
		if (messageFilter != null)
		{
			whereClause += " and " + messageFilter.getWhereClause();
		}
		
		Log.i("WHERECLAUSE", whereClause);
		
		String orderBy = RssMessageTable.COLUMN_PUBDATE + " DESC";
		//We display only messages for a specific user and link
		return new CursorLoader(this, RssContentProvider.CONTENT_URI_MESSAGE, null,
				whereClause,
				null, orderBy);
	}
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		@SuppressWarnings("unused")
		int a = data.getCount();
		adapter.swapCursor(data);
	}
	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
	}
}