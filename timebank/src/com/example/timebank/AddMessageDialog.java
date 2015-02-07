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

public class AddMessageDialog extends DialogFragment {

   	private EditText receiver;
   	private EditText content;
	private View dialogView;
	private GraphUser fbUser;
	private ParseObject currentMsg;
	
	private static final String TAG = "timeBank";
	
	public interface AddMessageListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
	
	AddMessageListener mListener;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	mListener = (AddMessageListener) getTargetFragment();
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
        
        dialogView = inflater.inflate(R.layout.msg_dialog, null);

        builder.setView(dialogView);        
        builder.setMessage(R.string.add_msg)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       
                	   saveNewMsg(dialog);
                	   sendNotificationAlert();                	   
                	   mListener.onDialogPositiveClick(AddMessageDialog.this); 
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   mListener.onDialogNegativeClick(AddMessageDialog.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	private void sendNotificationAlert()
	{     
	    fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();
        
        String channel = receiver.getEditableText().toString();
		channel = channel.replaceAll(" ","");	                		
		ParsePush push = new ParsePush();
		push.setChannel(channel);
		push.setMessage(firstName + " " + lastName + " has sent you a message!");
 	   	push.sendInBackground();

	}
	
	private void saveNewMsg(DialogInterface Dialog){

        fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();
                
        receiver = (EditText) dialogView.findViewById(R.id.receiver);
		content = (EditText) dialogView.findViewById(R.id.content);

		ParseObject msgItemParse = new ParseObject("Mesaj");		
		msgItemParse.put("Sender", firstName + " " + lastName);
		if (receiver != null){
			msgItemParse.put("Receiver", receiver.getText().toString());
		}
		if (content != null){			
			msgItemParse.put("Content", content.getText().toString());
		}
		msgItemParse.saveInBackground();
		currentMsg = msgItemParse;
	}
	
	public String getReceiver(){
		return receiver.getText().toString();
	}
	
	public String getContent(){
		return content.getText().toString();
	}
	
	public ParseObject getSession()
	{
		return currentMsg;
	}

}
