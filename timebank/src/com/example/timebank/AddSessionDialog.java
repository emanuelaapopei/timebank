package com.example.timebank;

import com.facebook.model.GraphUser;
import com.parse.ParseObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

public class AddSessionDialog extends DialogFragment {
	
	private EditText skill;
	private EditText user;
	private EditText hours;
		
	private View dialogView;
	
	private GraphUser fbUser;
	
	private ParseObject currentSession;
	
	private static final String TAG = "timeBank";
	
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AddSessionListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
 // Use this instance of the interface to deliver action events
    AddSessionListener mListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	mListener = (AddSessionListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AddSessionListener interface");
        }
    }
		
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
     // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        dialogView = inflater.inflate(R.layout.session_dialog, null);
        
     // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //builder.setView(inflater.inflate(R.layout.session_dialog, null));
        builder.setView(dialogView);
        
        builder.setMessage(R.string.add_session)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   saveNewSession(dialog);
                	   //Log.d(TAG, "before calling onDialogPositiveClick");
                	   mListener.onDialogPositiveClick(AddSessionDialog.this);                  
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	// Send the negative button event back to the host activity
                       mListener.onDialogNegativeClick(AddSessionDialog.this);
                   }
               });
        
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	private void saveNewSession(DialogInterface Dialog)
	{
		skill = (EditText) dialogView.findViewById(R.id.skill_from_session);
		user = (EditText) dialogView.findViewById(R.id.user_from_session);
		hours = (EditText) dialogView.findViewById(R.id.hours_from_session);
		
		Log.d(TAG,skill.getText().toString());
		Log.d(TAG,user.getText().toString());
		Log.d(TAG,hours.getText().toString());
		
		fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
		
		String firstName = fbUser.getFirstName();
		String lastName = fbUser.getLastName();
		
		//Log.d(TAG, firstName + " " + lastName);
		
		ParseObject session = new ParseObject("Session");
		
		if (skill != null)
		{
			session.put("Skill", skill.getText().toString());
		}
		if (user != null)
		{
			session.put("Sender", firstName + " " + lastName);
			session.put("Receiver", user.getText().toString());
		}
		if (hours != null)
		{
			session.put("Hours", Integer.parseInt(hours.getText().toString()) );
		}
		
		session.put("Status", "New");
		
		session.saveInBackground();
		
		currentSession = session;
		
 	   /*if(valueView == null) Log.e(TAG, "NULL");
        else{
     	   Log.e(TAG,valueView.getText().toString());
     	// Send the positive button event back to the host activity
                
        }*/	
	}

	public String getSkill()
	{
		return skill.getText().toString();
	}
	
	public String getUser()
	{
		return user.getText().toString();
	}
	
	public String getHours()
	{
		return hours.getText().toString();
	}
	
	public ParseObject getSession()
	{
		return currentSession;
	}
}
