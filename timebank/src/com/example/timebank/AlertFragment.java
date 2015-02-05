package com.example.timebank;

import java.util.ArrayList;
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


public class AlertFragment extends Fragment 
							implements AddAlertDialog.AddAlertListener{
	private static final String TAG = "timeBank";
	
	private ProfilePictureView profilePictureView;
    private GraphUser fbUser;
	private String userId;

    private List<BaseListElement> listElements;
    private ActionListAdapter listAdapter = null;
    private ListView listView;
	private ImageButton addAlert;
	private int alertNumber;

	public AlertFragment(String UserId) {
		userId = UserId;
		alertNumber = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_alert,
				container, false);
		
		profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
		profilePictureView.setProfileId(userId);
		
		addAlert = (ImageButton) view.findViewById(R.id.add_alert);
		addAlert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	addNewAlert();
            }
        }); 
		
		// Find the list view
		listView = (ListView) view.findViewById(R.id.alert_list);

		// Set up the list view items, based on a list of
		// BaseListElement items
		listElements = new ArrayList<BaseListElement>();
		
		//read the elements from the database
		updateAlertList();
		
		listAdapter = new ActionListAdapter(getActivity(), R.id.alert_list, listElements);
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
    
    public void addNewAlert()
    {
    	DialogFragment newFragment = new AddAlertDialog();
    	newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "alert");
    }
    
    public void updateAlertList()
    {
    	fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();
    	
    	String firstName = fbUser.getFirstName();
		String lastName = fbUser.getLastName();
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Alert");
		
		query.whereEqualTo("CreatedBy", firstName + " " + lastName);
		query.orderByDescending("updatedAt");
		
		query.findInBackground(new FindCallback<ParseObject>() {
    	    public void done(List<ParseObject> alertList, ParseException e) {
    	        if (e == null) {
    	            Log.d(TAG, "Retrieved " + alertList.size() + " alerts");
    	            
    	            ParseObject alert = new ParseObject("Alert");
    	            for (int i = 0; i < alertList.size(); i++ )
    	            {
    	            	alert = alertList.get(i);
    	            	String skill = alert.getString("Skill");
    	            	String alertFromUser = alert.getString("AlertFromUser");
    	            	
    	            	String main_string = "Alert for skill: " + skill;
    	            	String default_string = "";
    	            	if (!alertFromUser.equals(""))
    	            	{
    	            		default_string = "from user " + alertFromUser;
    	            	}
    	            	else 
    	            	{
    	            		default_string = "no user specified";
    	            	}
    	            	
    	            	listAdapter.add(new AlertListElement(i, main_string, default_string, alert));
    	            	alertNumber++;
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
    
    public void updateView()
    {
    	if (listAdapter != null)
    	{
    		listAdapter.notifyDataSetChanged();
    	}
    	else
    	{
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
	
	private class AlertListElement extends BaseListElement {
		private ParseObject alertParse;
		
	    public AlertListElement(int requestCode, String text1, String text2, ParseObject parseObj) {
	        super(getActivity().getResources().getDrawable(R.drawable.alert),
	              text1,
	              text2,
	              requestCode);
	        
	        alertParse = parseObj;
	    }

	    @Override
	    protected View.OnClickListener getOnClickListener() {
	        return new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	                // Do nothing for now
	            }
	        };
	    }

		@Override
		protected void populateOGAction(OpenGraphAction action) {
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		AddAlertDialog addAlertDialog = (AddAlertDialog) dialog;
        ParseObject alertParse = addAlertDialog.getAlert();
		
        String skill = alertParse.getString("Skill");
    	String alertFromUser = alertParse.getString("AlertFromUser");
    	
    	String main_string = "Alert for skill: " + skill;
    	String default_string = "";
    	if (!alertFromUser.equals(""))
    	{
    		default_string = "from user " + alertFromUser;
    	}
    	else 
    	{
    		default_string = "no user specified";
    	}
    	
    	//Log.d(TAG, "Adding new item with values:" + main_string + " " +default_string);
    	//listElements.add(new SessionListElement(i, skill, default_string));
    	//listAdapter.add(new AlertListElement(alertNumber, main_string, default_string, alertParse));
    	listAdapter.insert(new AlertListElement(alertNumber, main_string, default_string, alertParse), 0);
    	alertNumber++;
    	
    	listAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}
}