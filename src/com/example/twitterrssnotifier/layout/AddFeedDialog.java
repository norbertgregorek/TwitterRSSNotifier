package com.example.twitterrssnotifier.layout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.twitterrssnotifier.R;

/*
 * Represents dialogbox when somebody click add new feed button on action bar.
 */
public class AddFeedDialog extends DialogFragment
{
	/*
	 * Listener that invokes appropriate function when some button was clicked.
	 */
	public interface AddFeedDialogListener
	{
		public void onConfirmButtonClick(String url);
		public void onCancelButtonClick();
	}
	//Reference to our listener
	private AddFeedDialogListener listener = null;
	
	//UI references
	private EditText urlEditText = null;
	
	public void setListener(AddFeedDialogListener listener)
	{
		this.listener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		//Some initial operations
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.fragment_add_new_rss, null);
		urlEditText = (EditText) v.findViewById(R.id.urlEditText);
		
		//Prepare dialog here
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		
		//Adds icon and title
		b.setIcon(R.drawable.ic_launcher)
		.setTitle(R.string.add_new_rss_feed);
		
		b.setView(v);
		
		b.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
		{
			@Override                                             
			public void onClick(DialogInterface dialog, int which)
			{
				if (listener != null)
					listener.onConfirmButtonClick(urlEditText.getText().toString());
			}
		});
		b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (listener != null)
					listener.onCancelButtonClick();
			}
		});
		return b.create();
	}
}