package com.example.twitterrssnotifier.miscellaneous;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/*
 * Class that represents setiings for a single user.
 * ATTENTION! Only HOW_OLD_MESSAGES is used at present because I'm not sure 
 * if the rest of them is compulsory...
 */
public class Settings
{
	//Some constans
	private static String UPDATE_TIME = "UPDATE_TIME";
	private static String BEEP_SIGNAL = "BEEP_SIGNAL";
	private static String PUBLISH_ON_TWITTER = "PUBLISH_ON_TWITTER";
	private static String HOW_OLD_MESSAGES = "HOW_OLD_MESSAGES";
	
	private int updateTime;
	private boolean beepSignal;
	private boolean publishOnTwitter;
	private int howOldMessages;
	
	public int getUpdateTime()
	{
		return updateTime;
	}
	public void setUpdateTime(int updateTime, SharedPreferences preferences)
	{
		if (!TwitterHelper.getInstance().isTwitterLoggedInAlready())
		{
			throw new NullPointerException("You have to be login to get settings");
		}
		String login = TwitterHelper.getInstance().getUser().getScreenName();
		this.updateTime = updateTime;
		
		Editor e = preferences.edit();
		e.putInt(login + UPDATE_TIME, this.updateTime);
		e.commit();
	}
	
	public boolean isBeepSignal()
	{
		return beepSignal;
	}
	public void setBeepSignal(boolean beepSignal, SharedPreferences preferences)
	{
		if (!TwitterHelper.getInstance().isTwitterLoggedInAlready())
		{
			throw new NullPointerException("You have to be login to get settings");
		}
		
		String login = TwitterHelper.getInstance().getUser().getScreenName();
		this.beepSignal = beepSignal;
		
		Editor e = preferences.edit();
		e.putBoolean(login + BEEP_SIGNAL, this.beepSignal);
		e.commit();
	}
	public boolean isPublishOnTwitter()
	{
		return publishOnTwitter;
	}
	public void setPublishOnTwitter(boolean publishOnTwitter, SharedPreferences preferences)
	{
		if (!TwitterHelper.getInstance().isTwitterLoggedInAlready())
		{
			throw new NullPointerException("You have to be login to get settings");
		}
		String login = TwitterHelper.getInstance().getUser().getScreenName();
		this.publishOnTwitter = publishOnTwitter;
		
		Editor e = preferences.edit();
		e.putBoolean(login + PUBLISH_ON_TWITTER, this.publishOnTwitter);
		e.commit();
	}
	
	public int getHowOldMessages()
	{
		return howOldMessages;
	}
	public void setHowOldMessages(int howOldMessages, SharedPreferences preferences)
	{
		if (!TwitterHelper.getInstance().isTwitterLoggedInAlready())
		{
			throw new NullPointerException("You have to be login to get settings");
		}
		String login = TwitterHelper.getInstance().getUser().getScreenName();
		this.howOldMessages = howOldMessages;
		
		Editor e = preferences.edit();
		e.putInt(login + HOW_OLD_MESSAGES, this.howOldMessages);
		e.commit();
	}
	
	public Settings(int updateTime, boolean beepSignal,
			boolean publishOnTwitter, int howOldMessages)
	{
		//Default settings
		this.updateTime = updateTime;
		this.beepSignal = beepSignal;
		this.publishOnTwitter = publishOnTwitter;
		this.howOldMessages = howOldMessages;
	}
	public Settings()
	{
		this(10, true, true, -1);
	}
	
	//Convenient method of getting settings. 
	public static Settings getSettings(SharedPreferences pref)
	{
		if (!TwitterHelper.getInstance().isTwitterLoggedInAlready())
		{
			throw new NullPointerException("You have to be login to get settings");
		}
		String login = TwitterHelper.getInstance().getUser().getScreenName();
		
		int updateTime = pref.getInt(login + UPDATE_TIME, -1);
		
		//Creates new settings when they're not defined
		if (updateTime == -1)
		{
			Settings settings = new Settings();
			Editor e = pref.edit();
			
			e.putInt(login + UPDATE_TIME, settings.getUpdateTime());
			e.putBoolean(login + BEEP_SIGNAL, settings.isBeepSignal());
			e.putBoolean(login + PUBLISH_ON_TWITTER, settings.isPublishOnTwitter());
			
			e.commit();
			
			return settings;
		}
		boolean beepSignal = pref.getBoolean(login + BEEP_SIGNAL, true);
		boolean publishOnTwitter = pref.getBoolean(login + PUBLISH_ON_TWITTER, true);
		int howOldMessages = pref.getInt(login + HOW_OLD_MESSAGES, 1);
		
		return new Settings(updateTime, beepSignal, publishOnTwitter, howOldMessages);
	}
}
