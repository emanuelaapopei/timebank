package com.example.timebank;
import com.facebook.model.GraphUser;
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

public class EditSkillDialog extends DialogFragment {

	private static final String TAG = "Edit Skill";
	private EditText skill;
    private RatingBar skill_level;
	private View dialogView;
	private ParseObject currentSkill;
	EditSkillListener mListener;
    int skillNr;
	
	public interface EditSkillListener {
        public void onDialogEditPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	EditSkillDialog(ParseObject skillToSave, int requestedCode){
        skillNr = requestedCode;
		currentSkill = skillToSave;
	}	
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	mListener = (EditSkillListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement EditSkillListener interface");
        }
    }
	
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();        
        dialogView = inflater.inflate(R.layout.skill_dialog, null);
        
        if (currentSkill == null)
        {
        	Log.d(TAG, "sessionParse is NULL");
        	return null;
        }
        
        
        //build the view for the current skill
        skill = (EditText) dialogView.findViewById(R.id.skill_from_skill);
        skill.setText(currentSkill.getString("Skill"));
        skill.setEnabled(false);
        skill_level = (RatingBar) dialogView.findViewById(R.id.skill_level);
        skill_level.setRating((float)currentSkill.getDouble("Experience"));        
        builder.setView(dialogView);
        
        builder.setMessage(R.string.update_skill)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       
                	   updateSkill(dialog);
                	   mListener.onDialogEditPositiveClick(EditSkillDialog.this); 
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   mListener.onDialogNegativeClick(EditSkillDialog.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	private void updateSkill(DialogInterface Dialog){
		skill_level = (RatingBar) dialogView.findViewById(R.id.skill_level);	
		if (skill_level != null){
			currentSkill.put("Experience", skill_level.getRating());
		}		
		currentSkill.saveInBackground();
	}
	
	
	public ParseObject getSession()
	{
		return currentSkill;
	}
    public int getRequestedSkill() {
        return skillNr;
    }

}
