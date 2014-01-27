package com.example.twitterrssnotifier.miscellaneous;

import android.app.Activity;
import android.widget.Toast;

public class ToastHelper
{
	//Makes our life simpler... show toast from any thread
	public static void showToast(final Activity activity, final String toast)
	{
		activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(activity, toast, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
