package com.example.timebank;

import java.util.List;

import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

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
	private int sessionNr;
	private GraphUser fbUser;
	
	private static final String TAG = "timeBank";
	
	public interface AnswerSessionListener {
        public void onApproveClick(DialogFragment dialog);
        public void onRejectClick(DialogFragment dialog);
    }
	
	AnswerSessionListener mListener;
	
	public AnswerSessionDialog(ParseObject parseObj, int requestedCode)
	{
		sessionParse = parseObj;
		sessionNr = requestedCode;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	mListener = (AnswerSessionListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AnswerSessionListener interface");
        }
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
		user.setText(sessionParse.getString("Receiver"));
		
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
                	   
                	   //update the balance for the user
                	   updateBalance(user.getText().toString());
                	   
                	   mListener.onApproveClick(AnswerSessionDialog.this);
                   }
               })
               .setNegativeButton(R.string.reject_session, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   sessionParse.put("Status", "Rejected");
                	   sessionParse.saveInBackground();  
                	   
                	   mListener.onRejectClick(AnswerSessionDialog.this);
                   }
               });
        
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	private void updateBalance(String user)
	{
		//Log.d(TAG, "Update balance for user " + user);
		
		//Increase balance for user who initiated the session
		ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
		query.whereEqualTo("username", user);
		
		query.findInBackground(new FindCallback<ParseObject>() {
    	    public void done(List<ParseObject> userList, ParseException e) {
    	        if (e == null) {
    	        	 Log.d(TAG, "Retrieved " + userList.size() + " users");
     	            
     	            ParseObject userParse = new ParseObject("User");
     	            
     	            for (int i = 0; i < userList.size(); i++)
     	            {
     	            	userParse = userList.get(i);
     	            	int balance = userParse.getInt("balance");
     	            	
     	            	Log.d(TAG, "Old balance =  " + balance);
     	            	
     	            	balance += sessionParse.getInt("Hours");
     	            	
     	            	Log.d(TAG, "New balance =  " + balance);
     	            	
     	            	userParse.put("balance", balance);
     	            	userParse.saveInBackground();
     	            	Log.d(TAG, "Saved the user Parse object");
     	            }
    	        }
    	        else {
    	            Log.d(TAG, "Error: " + e.getMessage());
    	        }
    	    }
		});
		
		//Decrease balance for user approving the session
		fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
		
		String firstName = fbUser.getFirstName();
		String lastName = fbUser.getLastName();
		
		ParseQuery<ParseObject> query2 = ParseQuery.getQuery("User");
		query2.whereEqualTo("username", firstName + " " + lastName);
		
		query2.findInBackground(new FindCallback<ParseObject>() {
    	    public void done(List<ParseObject> userList, ParseException e) {
    	        if (e == null) {
    	        	Log.d(TAG, "Retrieved " + userList.size() + " users");
     	            
     	            ParseObject userParse = new ParseObject("User");
     	            
     	            for (int i = 0; i < userList.size(); i++)
     	            {
     	            	userParse = userList.get(i);
     	            	int balance = userParse.getInt("balance");
     	            	
     	            	Log.d(TAG, "Old balance =  " + balance);
     	            	
     	            	balance -= sessionParse.getInt("Hours");
     	            	
     	            	Log.d(TAG, "New balance =  " + balance);
     	            	
     	            	userParse.put("balance", balance);
     	            	userParse.saveInBackground();
     	            	Log.d(TAG, "Saved the user Parse object");
     	            }
    	        }
    	        else {
    	            Log.d(TAG, "Error: " + e.getMessage());
    	        }
    	    }
		});
	}

	public int getSessionNumber() {
        return sessionNr;
    }
}
