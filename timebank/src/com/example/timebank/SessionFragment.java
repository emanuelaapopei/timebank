package com.example.timebank;

import java.util.ArrayList;
import java.util.List;

import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class SessionFragment extends Fragment
        implements AddSessionDialog.AddSessionListener, 
        		   AnswerSessionDialog.AnswerSessionListener{
    private static final String TAG = "timeBank";

    private ProfilePictureView profilePictureView;
    private GraphUser fbUser;
    private String userId;
    private int sessionNumber;

    private ListView listView;
    private List<BaseListElement> listElements;
    private ActionListAdapter listAdapter = null;
    private ImageButton addSession;

    public SessionFragment(String UserId) {
        userId = UserId;
        sessionNumber = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session,
                container, false);

        profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
        profilePictureView.setProfileId(userId);

        addSession = (ImageButton) view.findViewById(R.id.add_session);
        addSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                addNewSession();
            }
        });

        // Find the list view
        listView = (ListView) view.findViewById(R.id.session_list);

        // Set up the list view items, based on a list of
        // BaseListElement items
        listElements = new ArrayList<BaseListElement>();

        //read the elements from the database
        readSessionList();

        // Set the list view adapter
        listAdapter = new ActionListAdapter(getActivity(), R.id.session_list, listElements);
        listView.setAdapter(listAdapter);

        init(savedInstanceState);
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void addNewSession() {
        DialogFragment newFragment = new AddSessionDialog();
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "session");

    }

    public void answerSession(ParseObject SessionParse, int SessionNumber, boolean ans){
    	Log.d(TAG, "Approve session "+ SessionNumber);
        DialogFragment newFragment = new AnswerSessionDialog(SessionParse, SessionNumber, ans);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "answerSession");
    }

    public void readSessionList() {
        fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();

        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Session");

        query.whereEqualTo("Sender", firstName + " " + lastName);
        query.orderByDescending("updatedAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> sessionList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved " + sessionList.size() + " scores");

                    ParseObject session = new ParseObject("Session");
                    for (int i = 0; i < sessionList.size(); i++) {
                        session = sessionList.get(i);
                        String skill = session.getString("Skill");
                        String receiver = session.getString("Receiver");
                        int hours = session.getInt("Hours");
                        String status = session.getString("Status");

                        String main_string = skill + " - " + status;
                        String default_string = "to " + receiver + " for " + hours + " hours";

                        listAdapter.add(new SessionListElement(i, main_string, default_string, session));
                        sessionNumber++;
                    }

                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
        
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Session");
        query2.whereEqualTo("Receiver", firstName + " " + lastName);
        query2.orderByDescending("updatedAt");
        
        query2.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> sessionList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved " + sessionList.size() + " sessions for me");

                    ParseObject session = new ParseObject("Session");
                    for (int i = 0; i < sessionList.size(); i++) {
                        session = sessionList.get(i);
                        String skill = session.getString("Skill");
                        String sender = session.getString("Sender");
                        int hours = session.getInt("Hours");
                        String status = session.getString("Status");

                        String main_string = skill + " - " + status;
                        String default_string = "from " + sender + " for " + hours + " hours";

                        listAdapter.add(new SessionListElement(sessionNumber, main_string, default_string, session));
                        sessionNumber++;
                    }

                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });

        if (!getActivity().isFinishing()) {
            // Update view
            updateView();
        }

    }

    public void updateView() {
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "Adapter is null");
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        Log.d(TAG, "inside onDialogPositiveClick");

        ParseObject session;

        AddSessionDialog addSessionDialog = (AddSessionDialog) dialog;

        session = addSessionDialog.getSession();

        String skill = session.getString("Skill");
        String receiver = session.getString("Receiver");
        int hours = session.getInt("Hours");
        String status = session.getString("Status");

        String main_string = skill + " - " + status;
        String default_string = "to " + receiver + " for " + hours + " hours";

        Log.d(TAG, "Adding new item with values:" + main_string + " " + receiver + " " + hours);
        //listElements.add(new SessionListElement(i, skill, default_string));
        listAdapter.add(new SessionListElement(sessionNumber, main_string, default_string, session));
        //listAdapter.insert(new SessionListElement(sessionNumber, main_string, default_string, session), 0);
        sessionNumber++;

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // TODO Auto-generated method stub

    }

    /**
     * Resets the view to the initial defaults.
     */
    private void init(Bundle savedInstanceState) {

    }

    private class ActionListAdapter extends ArrayAdapter<BaseListElement> {
        private List<BaseListElement> listElements;

        public ActionListAdapter(Context context, int resourceId,
                                 List<BaseListElement> listElements) {
            super(context, resourceId, listElements);
            this.listElements = listElements;
            // Set up as an observer for list item changes to
            // refresh the view.
            for (int i = 0; i < listElements.size(); i++) {
                listElements.get(i).setAdapter(this);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater =
                        (LayoutInflater) getActivity()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.listitem, null);
            }

            BaseListElement listElement = listElements.get(position);
            if (listElement != null) {
                view.setOnClickListener(listElement.getOnClickListener());
                ImageView icon = (ImageView) view.findViewById(R.id.icon);
                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);
                if (icon != null) {
                    icon.setImageDrawable(listElement.getIcon());
                }
                if (text1 != null) {
                    text1.setText(listElement.getText1());
                }
                if (text2 != null) {
                    text2.setText(listElement.getText2());
                }
            }
            return view;
        }

    }

    private class SessionListElement extends BaseListElement {

        private ParseObject sesionParse;

        public SessionListElement(int requestCode, String text1, String text2, ParseObject parseObj) {
            super(getActivity().getResources().getDrawable(R.drawable.session),
                    text1,//getActivity().getResources().getString(R.string.session),
                    text2,//getActivity().getResources().getString(R.string.session_default),
                    requestCode);

            sesionParse = parseObj;
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String status = sesionParse.getString("Status");
                    fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();

                    String firstName = fbUser.getFirstName();
                    String lastName = fbUser.getLastName();
                    
                    if (status.equals("New")) {
                    	
                    	String Sender = sesionParse.getString("Sender");
                    	if (Sender.equals(firstName + " " + lastName))
                    	{
                    		AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
                            alert.setMessage("You can't answer to a session created by you.");
                            alert.show();
                    	}
                    	else
                    	{
                    		answerSession(sesionParse, getRequestCode(), true);
                    	}
                    } else {
                    	String Sender = sesionParse.getString("Sender");
                    	if (Sender.equals(firstName + " " + lastName))
                    	{
                    		answerSession(sesionParse, getRequestCode(), false);
                    	}
                    	else 
                    	{
	                        AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
	                        alert.setMessage("You have already approved/rejected this session");
	                        alert.show();
                    	}
                    }
                }
            };
        }
        
        protected void changeStatus(String Status)
        {
        	String main_text = this.getText1();
        	String skill = main_text.substring(0, main_text.indexOf("-"));
        	main_text = "";
        	main_text = skill + "- " + Status;
        	this.setText1(main_text);
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            // TODO Auto-generated method stub

        }
    }

	@Override
	public void onApproveClick(DialogFragment dialog) {
		AnswerSessionDialog ansSessionDialog = (AnswerSessionDialog) dialog;
		int sessionNum = ansSessionDialog.getSessionNumber();
		
		SessionListElement session = (SessionListElement) listAdapter.getItem(sessionNum);
		session.changeStatus("Approved");
		
		Log.d(TAG, "Update session "+ sessionNum);
		
		listAdapter.notifyDataSetChanged();		
	}

	@Override
	public void onRejectClick(DialogFragment dialog) {
		AnswerSessionDialog ansSessionDialog = (AnswerSessionDialog) dialog;
		int sessionNum = ansSessionDialog.getSessionNumber();
		
		SessionListElement session = (SessionListElement) listAdapter.getItem(sessionNum);
		session.changeStatus("Rejected");
		
		listAdapter.notifyDataSetChanged();
	}


}