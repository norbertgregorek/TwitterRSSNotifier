package com.example.twitterrssnotifier.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//This class runs our RssService after REPEAT_TIME of system rebooting
public class RssScheduleReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//TODO service doesn't work correctly right now
		RssService.initializeService(context);
	}
}
