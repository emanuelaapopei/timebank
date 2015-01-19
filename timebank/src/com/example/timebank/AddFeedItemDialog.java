package com.example.timebank;

import com.example.timebank.AddSkillDialog.AddSkillListener;
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

public class AddFeedItemDialog extends DialogFragment{
	//Members
	private EditText skill;
	private EditText feedItemText;
	private View dialogView;
	private GraphUser fbUser;
	private ParseObject currentFeedItem;
	
	//Interfaces
	public interface AddFeedItemListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	
	AddFeedItemListener mListener;
	
	//Methods
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	mListener = (AddFeedItemListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AddFeedItemListener interface");
        }
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        dialogView = inflater.inflate(R.layout.feeditem_dialog, null);

        builder.setView(dialogView);
        
        builder.setMessage(R.string.add_feeditem)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       
                	   saveNewFeedItem(dialog);
                	   mListener.onDialogPositiveClick(AddFeedItemDialog.this); 
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   mListener.onDialogNegativeClick(AddFeedItemDialog.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	private void saveNewFeedItem( DialogInterface dialog)

	{
		fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();
		skill = (EditText) dialogView.findViewById(R.id.skill_from_feeditem);
		feedItemText = (EditText) dialogView.findViewById(R.id.feeditem_text);
		
		ParseObject feedItemParse = new ParseObject("FeedItem");		
		if (skill != null){
			feedItemParse.put("Skill", skill.getText().toString());
		}
		if (feedItemText != null){
			feedItemParse.put("FeedItemText", feedItemText.getText().toString());
		}
		
		if (firstName != null && lastName != null){
			feedItemParse.put("CreatedBy", firstName + " " + lastName);
		}
		
		feedItemParse.saveInBackground();
		currentFeedItem = feedItemParse;
	}

	public ParseObject getSession()
	{
		return currentFeedItem;
	}
}
