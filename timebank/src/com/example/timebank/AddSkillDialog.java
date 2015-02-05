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
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

public class AddSkillDialog extends DialogFragment {

   	private EditText skill;
    private RatingBar skill_level;
	private View dialogView;
	private GraphUser fbUser;
	private ParseObject currentSkill, currentFeedItem;
	
	private static final String TAG = "timeBank";
	
	public interface AddSkillListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	
	AddSkillListener mListener;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	mListener = (AddSkillListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AddSkillListener interface");
        }
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        dialogView = inflater.inflate(R.layout.skill_dialog, null);

        builder.setView(dialogView);
        
        builder.setMessage(R.string.add_skill)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       
                	   saveNewSkill(dialog);
                	   
                	   saveNewFeedItem(dialog);
                	   
                	   sendNotificationAlert();
                	   
                	   mListener.onDialogPositiveClick(AddSkillDialog.this); 
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   mListener.onDialogNegativeClick(AddSkillDialog.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	private void sendNotificationAlert()
	{
		//read all alerts from the database that have Skill column set to the current skill
        
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Alert");
		query.whereEqualTo("Skill", skill.getText().toString());
		
		query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> alertList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved " + alertList.size() + " alerts");

                    ParseObject alertParse = new ParseObject("Alert");
                    
                    String firstName = fbUser.getFirstName();
                    String lastName = fbUser.getLastName();
                    
                    for (int i = 0; i < alertList.size(); i++) {
                        alertParse = alertList.get(i);
                                                
                        String user = alertParse.getString("CreatedBy");
                        String forUser = alertParse.getString("AlertFromUser");
                        
                        if (forUser.equals("") || forUser.equals(firstName + " " + lastName))
                        {                        
	                        String channel = user;
	                		channel = channel.replaceAll(" ","");
	                		
	                		ParsePush push = new ParsePush();
	                		push.setChannel(channel);
	                		push.setMessage(firstName + " " + lastName + " has skill "+ skill.getText().toString());
	                 	   	push.sendInBackground();
                        }
                    }
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
	}
	
	private void saveNewSkill(DialogInterface Dialog){

        fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();
		skill = (EditText) dialogView.findViewById(R.id.skill_from_skill);
		skill_level = (RatingBar) dialogView.findViewById(R.id.skill_level);
	
		ParseObject skillParse = new ParseObject("Skill");		
		if (skill != null){
			skillParse.put("Skill", skill.getText().toString());
		}
		if (skill_level != null){
			skillParse.put("Experience", skill_level.getRating());
		}
		
		if (firstName != null && lastName != null){
			skillParse.put("CreatedBy", firstName + " " + lastName);
		}
		
		skillParse.saveInBackground();
		currentSkill = skillParse;
	}
	
	private void saveNewFeedItem(DialogInterface Dialog){

        fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();
        String experienceStr;
        
		skill = (EditText) dialogView.findViewById(R.id.skill_from_skill);
		skill_level = (RatingBar) dialogView.findViewById(R.id.skill_level);
	
		ParseObject feedItemParse = new ParseObject("FeedItem");		
		if (skill != null){
			feedItemParse.put("Skill", skill.getText().toString());
		}
		if (skill_level != null){
			
			experienceStr = Utils.getSkillLevel( skill_level.getRating());
		}
		
		feedItemParse.put("FeedItemText", firstName + " " + lastName + " a adaugat un nou skill.");
		
		if (firstName != null && lastName != null){
			feedItemParse.put("CreatedBy", firstName + " " + lastName);
		}
		
		feedItemParse.saveInBackground();
		currentFeedItem = feedItemParse;
	}
	
	public ParseObject getSession()
	{
		return currentSkill;
	}

}
