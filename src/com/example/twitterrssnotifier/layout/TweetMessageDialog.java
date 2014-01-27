package com.example.twitterrssnotifier.layout;

import twitter4j.TwitterException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.twitterrssnotifier.R;
import com.example.twitterrssnotifier.miscellaneous.TwitterHelper;

/*
 * Represents dialogbox that appears when user want to Tweet a message.
 * It emerges when you long click a message (for about 2 seconds)
 * and press Tweet it! option.
 */
public class TweetMessageDialog extends DialogFragment
{
	/*
	 * Listener that invokes appropriate function when some button was clicked.
	 */
	public interface TweetMessageInterface
	{
		public void onSuccessfulTweet();
		public void onException(TwitterException e);
	}
	private TweetMessageInterface listener = null;
	
	static final String MESSAGE = "MESSAGE";
	
	private EditText tweetMessageEditText = null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		//Some initial operations
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.fragment_tweet_message, null);
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		
		//Adds icon and title
		b.setIcon(R.drawable.ic_launcher)
		.setTitle(R.string.tweet_message);
		
		b.setView(v);
		
		tweetMessageEditText = (EditText) v.findViewById(R.id.tweetMessageEditText);
		
		Bundle args = getArguments();
		if (args != null)
		{
			String message = args.getString(MESSAGE);
			tweetMessageEditText.setText(message);
		}
		
		b.setPositiveButton(R.string.tweet_message, new DialogInterface.OnClickListener()
		{
			@Override                                             
			public void onClick(DialogInterface dialog, int which)
			{
				String message = tweetMessageEditText.getText().toString();
				TwitterHelper.getInstance().tweetMessage(message, listener);
			}
		});
		b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			}
		});
		return b.create();
	}
	public void setListener(TweetMessageInterface listener)
	{
		this.listener = listener;
	}
}