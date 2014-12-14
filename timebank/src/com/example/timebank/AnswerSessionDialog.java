package com.example.timebank;

import com.parse.ParseObject;

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

public class AnswerSessionDialog extends DialogFragment{
	
	private View dialogView;
	
	private EditText skill;
	private EditText user;
	private EditText hours;
	private EditText feedback;
	private RatingBar rating;
	
	private ParseObject sessionParse;
	
	private static final String TAG = "timeBank";
	
	public AnswerSessionDialog(ParseObject parseObj)
	{
		sessionParse = parseObj;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
     // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        dialogView = inflater.inflate(R.layout.ans_session_dialog, null);
        
        if (sessionParse == null)
        {
        	Log.d(TAG, "sessionParse is NULL");
        }
        else
        {
        	Log.d(TAG, "sessionParse is NOT NULL");
        	//Log.d(TAG, "skill = " + sessionParse.getString("Skill"));
        }
        
        skill = (EditText) dialogView.findViewById(R.id.skill_ans_session);
        skill.setText(sessionParse.getString("Skill"));
        
		user = (EditText) dialogView.findViewById(R.id.user_ans_session);
		user.setText(sessionParse.getString("Sender"));
		
		hours = (EditText) dialogView.findViewById(R.id.hours_ans_session);
		hours.setText(sessionParse.getInt("Hours") + "h");
			
		feedback = (EditText) dialogView.findViewById(R.id.feedback_ans_session);
				
		rating = (RatingBar) dialogView.findViewById(R.id.ratingBar);
        
     // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.ans_session_dialog, null));
        builder.setView(dialogView);
        
        builder.setMessage(R.string.ans_session)
               .setPositiveButton(R.string.approve_session, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   sessionParse.put("Feedback", feedback.getText().toString());
                	   sessionParse.put("Rating", rating.getRating());
                	   sessionParse.put("Status", "Approved");
                	   sessionParse.saveInBackground();
                	   
                   }
               })
               .setNegativeButton(R.string.reject_session, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   sessionParse.put("Status", "Rejected");
                	   sessionParse.saveInBackground();              	   
                   }
               });
        
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
