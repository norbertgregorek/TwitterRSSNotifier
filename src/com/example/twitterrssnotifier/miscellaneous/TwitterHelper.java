package com.example.twitterrssnotifier.miscellaneous;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.twitterrssnotifier.layout.MainActivity;
import com.example.twitterrssnotifier.layout.TweetMessageDialog.TweetMessageInterface;

//Subsidiary class that simplifies communication with twitter using twitter4j
//Implemented as singleton
public class TwitterHelper
{
	private static TwitterHelper singleton = null;
	
	public interface TwitterHelperListener
	{
		public void onSuccessfulLogin();
		public void onSuccessfulLogout();
		public void onExceptionLogin(String message);
		public void onUserAlreadyLoggedIn();
	}
	private TwitterHelperListener listener = null;
	
	//Who owns this object
	private MainActivity activityOwner = null;
	
	//Keys from https://dev.twitter.com/apps/new
	private static final String TWITTER_CONSUMER_KEY = "wFG3A3JONadA6XAthnMw";
	private static final String TWITTER_CONSUMER_SECRET = "453k0h0LOFv7AXaQerKTv3F8RNcC4IiLp3eZkpMNLhw";
	
	//References to twitter object
    private static Twitter twitter;
    private static RequestToken requestToken;
    private static AccessToken accessToken;
	private static User user = null;
	
	// Twitter oauth urls
    //private static final String URL_TWITTER_AUTH = "auth_url";
    private static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    //private static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
    private static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    
    
    public boolean isTwitterLoggedInAlready()
	{
    	SharedPreferences preferences = MainActivity.getSharedPreferences();
    	
    	if (preferences == null)
    	{
    		throw new NullPointerException("TwitterHelper: shared preferences cannot be null");
    	}
    	
        return user != null && preferences.getString(PREF_KEY_OAUTH_SECRET, null) != null
        	&& preferences.getString(PREF_KEY_OAUTH_TOKEN, null) != null;
    }
    
    public void login()
	{
		// Check if already logged in
        if (!isTwitterLoggedInAlready())
        {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();
             
            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
 
            try
            {
                requestToken = twitter
                        .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                
                activityOwner.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(requestToken.getAuthenticationURL())));
            }
            catch (TwitterException e)
            {
            	Log.e("TWITTER", Log.getStackTraceString(e));
            	if (listener != null)
            	{
            		listener.onExceptionLogin(e.getMessage());
            	}
            }
        }
        else
        {
        	if (listener != null)
        		listener.onUserAlreadyLoggedIn();
        }
    }
    
    public void logout()
    {
    	if (isTwitterLoggedInAlready())
    	{
    		Editor e = MainActivity.getSharedPreferences().edit();
			
			if (e == null)
			{
				throw new NullPointerException("TwitterHelper: Editor must exist!");
			}
        	
        	e.remove(PREF_KEY_OAUTH_SECRET);
        	e.remove(PREF_KEY_OAUTH_TOKEN);
        	e.commit();
        	
    		user = null;
    		if (listener != null)
    		{
    			listener.onSuccessfulLogout();
    		}
    	}
    }
    
    //We run it in MainActivity to check if we come back twitter authorization service
    public void checkLoginOnCreateActivity()
    {
    	//Twitter loggin 
    	if (!isTwitterLoggedInAlready())
    	{
    		Uri callbackUri = activityOwner.getIntent().getData();
    		//If that condition is true, we know that we came back from twitter Authorise service
    		if (callbackUri != null && callbackUri.toString().startsWith(TWITTER_CALLBACK_URL))
    		{
    			String verifier = callbackUri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

    			new AsyncTask<String, Void, Exception>()
    			{
    				@Override
    				protected Exception doInBackground(String... params)
    				{
    					Exception result = null;
    					try
    					{
    						accessToken = twitter.getOAuthAccessToken(
    								requestToken, params[0]);
    						user = twitter.showUser(accessToken.getUserId());
    						
    						//We put it into preferences
    						Editor e = MainActivity.getSharedPreferences().edit();
    						
    						if (e == null)
    						{
    							throw new NullPointerException("TwitterHelper: Editor must exist!");
    						}

    						e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
    						e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
    						
    						e.commit();
    					}
    					catch(TwitterException e)
    					{
    						Log.e(MainActivity.class.getName(), "TwitterError: " + e.getErrorMessage());
    						result = e;
    					}
    					catch(Exception e)
    					{
    						Log.e(MainActivity.class.getName(), "Error: " + e.getMessage());
    						result = e;
    					}
    					return result;
    				}
    				@Override
    				protected void onPostExecute(Exception result)
    				{
    					if (listener == null)
    						return;
    					
    					if (result != null)
    						listener.onExceptionLogin(result.getMessage());
    					else
    					{
    						listener.onSuccessfulLogin();
    					}
    				}
    			}.execute(verifier);
    		}
    	}
    }
    
    //Getters + Setters
    public static TwitterHelper getInstance()
	{
		if (singleton == null)
			singleton = new TwitterHelper();
		return singleton;
	}
    
    public User getUser()
    {
    	return user;
    }
    
    public void setListener(TwitterHelperListener listener)
	{
		this.listener = listener;
	}
    
    //You have to call it once in onCreate function in MainActivity!!!!
    public void setActivityOwner(MainActivity activityOwner)
  	{
  		this.activityOwner = activityOwner;
  	}
    
    public void tweetMessage(String message, final TweetMessageInterface listener)
    {
    	
    	if (isTwitterLoggedInAlready())
    	{
    		new AsyncTask<String, Void, Void>()
    		{
				@Override
				protected Void doInBackground(String... params)
				{
					try
					{
						StatusUpdate status = new StatusUpdate(params[0]);
						twitter.updateStatus(status);
						listener.onSuccessfulTweet();
					}
					catch (TwitterException e)
					{
						listener.onException(e);
					}
					return null;
				}
    		}.execute(message);
    	}
    }
}