package com.example.timebank;

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

public class AddSkillDialog extends DialogFragment {
	
	private EditText skill;
	private EditText experience;

	private View dialogView;
	
	private GraphUser fbUser;
	
	private static final String TAG = "timeBank";
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
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	private void saveNewSkill(DialogInterface Dialog)
	{
		skill = (EditText) dialogView.findViewById(R.id.skill_from_skill);
		experience = (EditText) dialogView.findViewById(R.id.experience_from_skill);
		
		fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
		
		String firstName = fbUser.getFirstName();
		String lastName = fbUser.getLastName();
	
		ParseObject skillParse = new ParseObject("Skill");	
		
		if (skill != null)
		{
			skillParse.put("Skill", skill.getText().toString());
		}
		if (experience != null)
		{
			skillParse.put("Experience", experience.getText().toString());
		}
		
		if (firstName != null && lastName != null)
		{
			skillParse.put("CreatedBy", firstName + " " + lastName);
		}
		
		skillParse.saveInBackground();
	}

}
