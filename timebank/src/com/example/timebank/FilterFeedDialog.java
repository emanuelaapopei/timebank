package com.example.timebank;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class FilterFeedDialog extends DialogFragment{
	
	private EditText skill;
	private EditText user;
	
	private View dialogView;
	
	public interface FilterFeedListener {
        public void onFilterPositiveClick(DialogFragment dialog);
        public void onFilterNegativeClick(DialogFragment dialog);
    }
	
	FilterFeedListener mListener;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	mListener = (FilterFeedListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement FilterFeedListener interface");
        }
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
     // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        dialogView = inflater.inflate(R.layout.filter_feed_dialog, null);
        
     // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView);
        
        builder.setMessage(R.string.filter_feed)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   mListener.onFilterPositiveClick(FilterFeedDialog.this);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   
                	   mListener.onFilterNegativeClick(FilterFeedDialog.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	public String getSkill()
	{
		skill = (EditText) dialogView.findViewById(R.id.skill_from_filter);
		return skill.getText().toString();
	}
	
	public String getUser()
	{
		user = (EditText) dialogView.findViewById(R.id.user_from_filter);
		return user.getText().toString();
	}

}
