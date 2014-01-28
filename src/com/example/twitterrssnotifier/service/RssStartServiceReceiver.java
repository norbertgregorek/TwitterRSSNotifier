package com.example.twitterrssnotifier.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * Run service after BOOT_COMPLETED (see AndroidMafinest.xml)
 */
public class RssStartServiceReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent service = new Intent(context, RssService.class);
		context.startService(service);
	}
}