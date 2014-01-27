package com.example.twitterrssnotifier.layout;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.twitterrssnotifier.R;
import com.example.twitterrssnotifier.miscellaneous.Settings;
import com.example.twitterrssnotifier.miscellaneous.ToastHelper;

/*
 * Represents settings when somebody clicks Settings option from MainActivity.
 */
public class SettingsActivity extends Activity
{
	private SharedPreferences preferences = null;
	private Settings settings = null;
	//UI references
	private EditText howOldMessagesShowEditText = null;
	/* Not useful at present
	private EditText newMessagesTimeEditText = null;
	private CheckBox beepSignalCheckBox = null;
	private CheckBox publishOnTwitterCheckBox = null;*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		/*//Prepare UI elements
		newMessagesTimeEditText =
				(EditText) findViewById(R.id.newMessagesTimeEditText);
		beepSignalCheckBox = 
				(CheckBox) findViewById(R.id.beepSignalCheckBox);
		publishOnTwitterCheckBox =
				(CheckBox) findViewById(R.id.publishOnTwitterCheckBox);*/
		howOldMessagesShowEditText =
				(EditText) findViewById(R.id.howOldMessagesShowEditText);
		
		if (howOldMessagesShowEditText == null)
		{
			throw new NullPointerException("Some UI elements are missed");
		}
		
		//Get shared preferences
		preferences = getSharedPreferences(MainActivity.PREFERENCE_FILE, MODE_PRIVATE);
		//Get our settings
		settings = Settings.getSettings(preferences);
		
		if (settings != null)
		{
			//Integer updateTime = settings.getUpdateTime();
			Integer howOldMessages = settings.getHowOldMessages();
			howOldMessagesShowEditText.setText(howOldMessages.toString());
			//newMessagesTimeEditText.setText(updateTime.toString());
			//beepSignalCheckBox.setChecked(settings.isBeepSignal());
			//publishOnTwitterCheckBox.setChecked(settings.isPublishOnTwitter());
		}
		else
		{
			ToastHelper.showToast(this, getString(R.string.log_in_required));
		}
		
		//Setup listeners
		howOldMessagesShowEditText.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_DONE)
				{
					//We pressed enter
					settings.setHowOldMessages(Integer.parseInt(v.getText().toString()), preferences);
				}
				return false;
			}
		});
		/*newMessagesTimeEditText.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_DONE)
				{
					//We pressed enter
					settings.setUpdateTime(Integer.parseInt(v.getText().toString()), preferences);
				}
				return false;
			}
		});
		beepSignalCheckBox.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				settings.setBeepSignal(beepSignalCheckBox.isChecked(), preferences);
			}
		});
		publishOnTwitterCheckBox.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				settings.setPublishOnTwitter(publishOnTwitterCheckBox.isChecked(), preferences);
			}
		});*/
	}

}
