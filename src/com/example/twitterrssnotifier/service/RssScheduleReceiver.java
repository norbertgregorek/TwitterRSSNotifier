package com.example.twitterrssnotifier.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * This class runs our RssService.
 * Service implementation comes from: http://www.vogella.com/tutorials/AndroidServices/article.html
 */
public class RssScheduleReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//TODO service doesn't work correctly right now
		RssService.initializeService(context);
	}
}