package com.example.timebank;


import com.example.timebank.FilterFeedDialog.FilterFeedListener;
import com.facebook.model.GraphUser;
import com.parse.ParseObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AddAlertDialog extends DialogFragment {
	
	private EditText skill;
	private EditText user;
	
	private View dialogView;
	
	private GraphUser fbUser;
	
	private ParseObject currentAlert;
	
	private static final String TAG = "timeBank";
	
	public interface AddAlertListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	
	AddAlertListener mListener;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	mListener = (AddAlertListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AddAlertListener interface");
        }
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
     // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        dialogView = inflater.inflate(R.layout.alert_dialog, null);
        
     // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView);
        
        builder.setMessage(R.string.add_alert)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   saveNewAlert(dialog);
                	   mListener.onDialogPositiveClick(AddAlertDialog.this);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   mListener.onDialogNegativeClick(AddAlertDialog.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	private void saveNewAlert(DialogInterface Dialog)
	{
		skill = (EditText) dialogView.findViewById(R.id.skill_from_alert);
		user = (EditText) dialogView.findViewById(R.id.user_from_alert);
		
		fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
		String firstName = fbUser.getFirstName();
		String lastName = fbUser.getLastName();
		
		ParseObject alert = new ParseObject("Alert");
		if (skill != null)
		{
			alert.put("Skill", skill.getText().toString());
		}
		if (user != null)
		{
			alert.put("AlertFromUser", user.getText().toString());
		}
		if (firstName != null && lastName != null)
		{
			alert.put("CreatedBy", firstName + " " + lastName);
		}
		
		alert.saveInBackground();
		
		currentAlert = alert;
	}
	
	public ParseObject getAlert()
	{
		return currentAlert;
	}

}
