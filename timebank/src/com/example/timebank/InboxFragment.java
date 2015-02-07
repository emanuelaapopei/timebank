package com.example.timebank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class InboxFragment extends Fragment
        implements AddMessageDialog.AddMessageListener {
    private static final String TAG = "timeBank";

    private ProfilePictureView profilePictureView;
    private GraphUser fbUser;
    private String userId;

    private ListView listView;
    private List<BaseListElement> listElements;
    private ActionListAdapter listAdapter = null;
    private ImageButton addMsg;
    private int msgNumber;

    public InboxFragment(String UserId) {
        userId = UserId;
        msgNumber = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox,container, false);

        profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
        profilePictureView.setProfileId(userId);

        addMsg = (ImageButton) view.findViewById(R.id.add_msg);
        addMsg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                addMessage();
            }
        });

        // Find the list view
        listView = (ListView) view.findViewById(R.id.inbox_list);

        // Set up the list view items, based on a list of
        // BaseListElement items
        listElements = new ArrayList<BaseListElement>();

        updateInboxList();

        listAdapter = new ActionListAdapter(getActivity(), R.id.inbox_list, listElements);
        // Set the list view adapter
        listView.setAdapter(listAdapter);

        init(savedInstanceState);
        return view;
    }

    public void addMessage() {
        DialogFragment newFragment = new AddMessageDialog();
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "skill");
    }

    public void viewMessage(ParseObject skill, int skillNumber) {
        DialogFragment newFragment = new EditSkillDialog(skill, skillNumber);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "skilla");
    }


    public void updateInboxList() {
        fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();

        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();
        ParseQuery<ParseObject> query_a = ParseQuery.getQuery("Mesaj");
        query_a.whereEqualTo("Sender", firstName + " " + lastName);
        ParseQuery<ParseObject> query_b = ParseQuery.getQuery("Mesaj");
        query_b.whereEqualTo("Receiver", firstName + " " + lastName);
        
        // TODO cannot have order in subquery parse
        // query_b.orderByDescending("updatedAt");
        
        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query_a);
        queries.add(query_b);        
        
        ParseQuery.or(queries).findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> msgList, ParseException e) {
                if (e == null) {
                	ParseObject msgPars = new ParseObject("Mesaj");
                    for (int i = 0; i < msgList.size(); i++) {
                        msgPars = msgList.get(i);
                        String sender = msgPars.getString("Sender");
                        String receiver = msgPars.getString("Receiver");
                        String msg_content = msgPars.getString("Content");
                        Date created = msgPars.getCreatedAt();
                        
                        boolean is_sender;
                        String text;
                        
                        if(sender.contains(fbUser.getFirstName() + " " + fbUser.getLastName())){
                        	is_sender = true;
                        	text = receiver;
                        } else {
                        	is_sender = false;
                        	text = sender;                        	
                        }

                        listAdapter.add(new MsgListElement(is_sender, i, text, msg_content, created, msgPars));
                        msgNumber++;
                    }

                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
        
//        for (int i = 0; i < queryList.size(); i++) {
//        	MsgListElement item =  (MsgListElement)queryList.get(i);
//        	Date created = item.getParseObj().getCreatedAt();
//        	int index = 0; 
//        	
//        	for (int j = 0; j < queryList.size(); j++) {
//        		MsgListElement itemj =  queryList.get(j);
//            	Date createdj = itemj.getParseObj().getCreatedAt();
//        		if(created.after(createdj))
//        			index++;
//        	}
//        	listAdapter.insert(item, index);                	
//        }

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

    private class MsgListElement extends BaseListElement implements Comparator<MsgListElement>, Comparable<MsgListElement> {
        private ParseObject msgParse;
        private boolean is_sender;
        public Date created_;
        
           
        public MsgListElement(boolean issender, int requestCode, String text1, String text2, Date creadedD, ParseObject parseObj) {
            super(getActivity().getResources().getDrawable(issender? R.drawable.sent : R.drawable.received),text1,text2, requestCode);
            is_sender = issender;
            msgParse = parseObj;
            created_ = creadedD;
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   // viewMessage(msgParse, getRequestCode());
                }
            };
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            // TODO Auto-generated method stub
        }
        
        public ParseObject getParseObj(){
        	return msgParse;
        }

		@Override
		public int compareTo(MsgListElement another) {
			return created_.compareTo(another.created_);
		}

		@Override
		public int compare(MsgListElement lhs, MsgListElement rhs) {
			return lhs.created_.compareTo(rhs.created_);
		}
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
    	AddMessageDialog addMsgDialog = (AddMessageDialog) dialog;
        ParseObject msgParse = addMsgDialog.getSession();
        listAdapter.add(new MsgListElement(true, msgNumber, addMsgDialog.getReceiver(), addMsgDialog.getContent(), msgParse.getCreatedAt(), msgParse));
        msgNumber++;

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // TODO Auto-generated method stub

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


}