package com.example.twitterrssnotifier.layout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.example.twitterrssnotifier.R;
import com.example.twitterrssnotifier.database.MessageFilter;
import com.example.twitterrssnotifier.miscellaneous.Miscellaneous;

/*
 * Represents dialogbox when somebody click filter in RssMessageActivity.
 */
public class MessageFilterDialog extends DialogFragment
{
	/*
	 * Listener that invokes appropriate function when some button was clicked.
	 */
	public interface MessageFilterDialogListener
	{
		public void onConfirmButtonClick(MessageFilter filter);
		public void onCancelButtonClick();
	}
	private MessageFilterDialogListener listener = null;
	
	private DatePicker fromDatePicker = null;
	private DatePicker toDatePicker = null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		//Some initial operations
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.fragment_message_filter, null);
		//Get UI
		fromDatePicker = (DatePicker) v.findViewById(R.id.fromDatePicker);
		toDatePicker = (DatePicker) v.findViewById(R.id.toDatePicker);
		
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		
		//Adds icon and title
		b.setIcon(R.drawable.ic_launcher)
		.setTitle(R.string.filter);
		
		b.setView(v);
		
		b.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
		{
			@Override                                             
			public void onClick(DialogInterface dialog, int which)
			{
				if (listener != null)
				{
					long beginTime = Miscellaneous.getDateInMilliseconds(fromDatePicker, 0);
					long endTime = Miscellaneous.getDateInMilliseconds(toDatePicker, 24);
					listener.onConfirmButtonClick(new MessageFilter(beginTime, endTime));
				}
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
	public void setListener(MessageFilterDialogListener listener)
	{
		this.listener = listener;
	}
}
