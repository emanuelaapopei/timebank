package com.example.timebank;

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
import android.widget.TextView;

public class AddSessionDialog extends DialogFragment {

    private EditText skill;
    private EditText user;
    private EditText hours;
    private View dialogView;
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

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AddSessionListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddSessionListener");
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

    private void saveNewSession(DialogInterface Dialog) {
        skill = (EditText) dialogView.findViewById(R.id.skill_from_session);
        user = (EditText) dialogView.findViewById(R.id.user_from_session);
        hours = (EditText) dialogView.findViewById(R.id.hours_from_session);

        Log.d(TAG, skill.getText().toString());
        Log.d(TAG, user.getText().toString());
        Log.d(TAG, hours.getText().toString());

        ParseObject session = new ParseObject("Session");
        /*session.put("Skill", "engleza");
		session.put("Sender", "test");
		session.put("Receiver", "test3");
		session.put("Hours", 3);*/

        if (skill != null) {
            session.put("Skill", skill.getText().toString());
        }
        if (user != null) {
            session.put("Sender", user.getText().toString());
            session.put("Receiver", user.getText().toString());
        }
        if (hours != null) {
            session.put("Hours", Integer.parseInt(hours.getText().toString()));
        }

        session.saveInBackground();
		
 	   /*if(valueView == null) Log.e(TAG, "NULL");
        else{
     	   Log.e(TAG,valueView.getText().toString());
     	// Send the positive button event back to the host activity
                
        }*/
    }

}
