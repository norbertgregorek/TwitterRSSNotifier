package com.example.twitterrssnotifier.layout;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.example.twitterrssnotifier.R;
import com.example.twitterrssnotifier.database.RssContentProvider;
import com.example.twitterrssnotifier.database.RssDBHelper;
import com.example.twitterrssnotifier.database.RssFeedTable;
import com.example.twitterrssnotifier.layout.AddFeedDialog.AddFeedDialogListener;
import com.example.twitterrssnotifier.miscellaneous.ToastHelper;
import com.example.twitterrssnotifier.miscellaneous.TwitterHelper;
import com.example.twitterrssnotifier.miscellaneous.TwitterHelper.TwitterHelperListener;
import com.example.twitterrssnotifier.rsslibrary.BadRssFromUrlException;
import com.example.twitterrssnotifier.rsslibrary.RssFeed;
import com.example.twitterrssnotifier.service.RssService;

/*
 * Represents main activity. It appears after application was started.
 * Implements LoaderManager to adjust data from our SQLite database
 */
public class MainActivity extends FragmentActivity
					implements LoaderManager.LoaderCallbacks<Cursor>
{
	//This is key that we pass to bundle object
	private static final String IS_LOGGED = "IS_LOGGED";
	
	//Use extra value when starting RssMessageActivity to transfer item link that was pressed
	static final String FEED_LINK = "feed_link";
	//To transer title of clicked item (we set the title in next activity as title of feed)
	static final String FEED_TITLE = "feed_title";
	//Name of preference file
	static final String PREFERENCE_FILE = "MainActivityPreference";
    
	//Reference to shared preferences that this app uses.
    private static SharedPreferences sharedPreferences;
	
	//References to GUI
	private AddFeedDialog addFeedDialog = new AddFeedDialog();
	private TextView helloWorldTextView;
	private ListView rssFeedListView;
	
	//Adapter to our SQLite database
	private SimpleCursorAdapter adapter;
	
	//Reference to TwitterHelper
	private TwitterHelper twitter;
	
	//Listeners
	private AddFeedDialogListener addFeedDialogListener = new AddFeedDialogListener()
	{
		@Override
		public void onConfirmButtonClick(String url)
		{
			new AsyncTask<String, Void, RssFeed>()
			{
				@Override
				protected RssFeed doInBackground(String... params)
				{
					String url = params[0];
					RssFeed feed = null;
					try
					{
						feed = RssFeed.Create(url);
					}
					catch (IOException e)
					{
						ToastHelper.showToast(MainActivity.this, getString(R.string.io_error));
					}
					catch (BadRssFromUrlException e)
					{
						ToastHelper.showToast(MainActivity.this,getString(R.string.bad_rss_from_url));
					}
					catch (XmlPullParserException e)
					{
						ToastHelper.showToast(MainActivity.this, getString(R.string.xml_parsing_error));
					}
					return feed;
				}
				
				@Override
				protected void onPostExecute(RssFeed feed)
				{
					if (feed != null)
					{
						ContentValues values = new ContentValues();
					    values.put(RssFeedTable.COLUMN_USER, twitter.getUser().getScreenName());
					    values.put(RssFeedTable.COLUMN_LINK, feed.getLink());
					    values.put(RssFeedTable.COLUMN_TITLE, feed.getTitle());
					    values.put(RssFeedTable.COLUMN_DESCRIPTION, feed.getDescription());
					    values.put(RssFeedTable.COLUMN_RSS_LINK, feed.getRssLink());
					    
					    //Add it to our SQL database
					    try
					    {
					    	getContentResolver().insert(RssContentProvider.CONTENT_URI_FEED, values);
					    }
					    catch (SQLException e)
					    {
					    	if (e.getMessage().contains("(code 19)"))
					    	{
					    		ToastHelper.showToast(MainActivity.this, getString(R.string.rss_feed_duplicate));
					    	}
					    }
					}
				}
			}.execute(url);
		}
		@Override
		public void onCancelButtonClick()
		{
		}
	};
	
	private TwitterHelperListener twitterHelperListener = new TwitterHelperListener()
	{
		@Override
		public void onUserAlreadyLoggedIn()
		{
			ToastHelper.showToast(MainActivity.this, getString(R.string.already_logged_in));
		}
		@Override
		public void onSuccessfulLogin()
		{
			refreshUI();
			ToastHelper.showToast(MainActivity.this, getString(R.string.successfully_logged_in));
			
			helloWorldTextView.setText(getString(R.string.welcome) + " " + twitter.getUser().getScreenName());
			//After successful login reset loader
			getLoaderManager().restartLoader(0, null, MainActivity.this);
		}
		@Override
		public void onExceptionLogin(String message)
		{
			ToastHelper.showToast(MainActivity.this, message);
		}
		@Override
		public void onSuccessfulLogout()
		{
			ToastHelper.showToast(MainActivity.this, getString(R.string.successfully_logged_out));
		}
	};
	
	private AdapterView.OnItemClickListener listViewItemClickListener =
			new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id)
		{
			String feedLink = ((TextView) view.findViewById(R.id.rssFeedRssLinkTextView)).getText().toString();
			String feedTitle =((TextView) view.findViewById(R.id.rssFeedTitleTextView)).getText().toString();
			
			//Update this rss feed after pressing it.
			RssService.updateRssFeed(MainActivity.this, feedLink, twitter.getUser().getScreenName());
			
			//Just run our second intent (send link and title as extra data)
			Intent intent = new Intent(MainActivity.this, RssMessageActivity.class);
			intent.putExtra(FEED_LINK, feedLink);
			intent.putExtra(FEED_TITLE, feedTitle);
			
			MainActivity.this.startActivity(intent);
		}
	};
	
    public static SharedPreferences getSharedPreferences()
    {
    	return sharedPreferences;
    }
	private void refreshUI()
	{
		MainActivity.this.setContentView(R.layout.activity_main);
		
		//Prepare loader
		adapter = new SimpleCursorAdapter(this, R.layout.rss_feed,
				null,
				new String[] {RssFeedTable.COLUMN_LINK, RssFeedTable.COLUMN_TITLE, RssFeedTable.COLUMN_RSS_LINK},
				new int[] {R.id.rssFeedLinkTextView, R.id.rssFeedTitleTextView, R.id.rssFeedRssLinkTextView},
				0);
		
		//Get UI controls
		helloWorldTextView = (TextView) findViewById(R.id.welcomeTitleTextView);
		rssFeedListView = (ListView) findViewById(R.id.rssFeedListView);
		rssFeedListView.setOnItemClickListener(listViewItemClickListener);
		rssFeedListView.setAdapter(adapter);
		//Registers contex menu
		registerForContextMenu(rssFeedListView);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		refreshUI();
		
		sharedPreferences = getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
		
		twitter = TwitterHelper.getInstance();
		if (twitter == null)
		{
			throw new NullPointerException("MainActvity: twiter object is null");
		}
		//Don't call it never ever again!
		twitter.setActivityOwner(this);
		
		if (savedInstanceState != null)
		{
			if (savedInstanceState.getBoolean(IS_LOGGED))
			{
				getLoaderManager().restartLoader(0, null, MainActivity.this);
				helloWorldTextView.setText(getString(R.string.welcome) + " " + twitter.getUser().getScreenName());
			}
		}
		
		//Change listeners
		addFeedDialog.setListener(addFeedDialogListener);
		twitter.setListener(twitterHelperListener);	
		
		//Initialize oour service
		RssService.initializeService(this);
		
		//Handle search query
		handleSearchQuery(getIntent());
		
		//TEST
		//getLoaderManager().restartLoader(0, null, MainActivity.this);
	}
	
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		//Handle search query
		handleSearchQuery(intent);
		setIntent(intent);
		twitter.checkLoginOnCreateActivity();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putBoolean(IS_LOGGED, twitter.isTwitterLoggedInAlready());
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		twitter = null;
	}
	
	/***********/
	//Context Menu
	/**********/
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.rssFeedListView)
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.rss_feed_long_clicked, menu);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
	      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	      switch(item.getItemId())
	      {
	    	  case R.id.action_delete_feed:
	    		  View v = rssFeedListView.getAdapter().getView(info.position, null, rssFeedListView);
	    		  TextView rssLinkTextView = (TextView) v.findViewById(R.id.rssFeedRssLinkTextView);
	    		  
	    		  String rssLink = rssLinkTextView.getText().toString();
	    		  RssDBHelper.deleteFeedFromDB(rssLink, getContentResolver());
	    		  return true;
	    	  default:
	    		  return super.onContextItemSelected(item);
	      }
	}

	/**********/
	//MENU
	/**********/
	
	//Action bar functions
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		/*TODO implementing search function
		// Associate searchable configuration with the SearchView
		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.action_search_feed).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));
				*/
		
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_twitter_login:
				new AsyncTask<Void, Void, Void>()
				{
					@Override
					protected Void doInBackground(Void... params)
					{
						twitter.login();
						return null;
					}
				}.execute();
				break;
			case R.id.action_twitter_logout:
				MainActivity.this.setContentView(R.layout.activity_main);
				twitter.logout();
				break;
			case R.id.action_add_new_rss_feed:
				if (twitter.isTwitterLoggedInAlready())
				{
					addFeedDialog.show(getSupportFragmentManager(), "addFeedDialog");
				}
				else
				{
					ToastHelper.showToast(MainActivity.this,
							getString(R.string.log_in_required));
				}
				break;
			case R.id.action_settings:	//Settings
				//We can open settings if and only if we are logged in
				if (twitter.isTwitterLoggedInAlready())
				{
					Intent intent = new Intent(this, SettingsActivity.class);
					startActivity(intent);
				}
				else
				{
					ToastHelper.showToast(MainActivity.this,
							getString(R.string.log_in_required));
				}
				break;
			case R.id.action_help:
				//Just build and display DialogBox
				AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
				popupBuilder.setMessage(R.string.help_description)
				.setTitle(R.string.help)
				.setIcon(R.drawable.ic_launcher);
				popupBuilder.show();
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
			//throw new NullPointerException(");
		}
		//TEST
		String whereClause = RssFeedTable.COLUMN_USER + " = " + "'" + "RssNotifierTest" + "'";
		//String whereClause = RssFeedTable.COLUMN_USER + " = " + "'" + twitter.getUser().getScreenName() + "'";
		//We display only feeds for a specific user
		
		//TODO maybe add some sort option ?
		//Default sort feeds by link
		String orderClause = RssFeedTable.COLUMN_LINK;
		return new CursorLoader(this, RssContentProvider.CONTENT_URI_FEED, null,
				whereClause,
				null, orderClause);
	}
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		adapter.swapCursor(data);
	}
	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		adapter.swapCursor(null);
	}
	private void handleSearchQuery(Intent intent)
	{
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            ToastHelper.showToast(this, "SDSAD " + query);
        }
    }
}