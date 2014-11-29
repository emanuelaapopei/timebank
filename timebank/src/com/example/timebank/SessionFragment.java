package com.example.timebank;

import java.util.ArrayList;
import java.util.List;

import com.facebook.model.OpenGraphAction;
import com.facebook.widget.ProfilePictureView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class SessionFragment extends Fragment {
	
	private ProfilePictureView profilePictureView;
	private String userId;
	
	private ListView listView;
	private List<BaseListElement> listElements;
	
	private Button addSession; 

	public SessionFragment(String UserId) {
		userId = UserId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_session,
				container, false);
		
		profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
		profilePictureView.setProfileId(userId);
		
		addSession = (Button) view.findViewById(R.id.test_button); 
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
		// Add an item for the friend picker
		listElements.add(new SessionListElement(0));
		listElements.add(new SessionListElement(1));
		listElements.add(new SessionListElement(2));
		listElements.add(new SessionListElement(3));
		// Set the list view adapter
		listView.setAdapter(new ActionListAdapter(getActivity(), 
		                    R.id.session_list, listElements));
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
    
    public void addNewSession()
    {
    	DialogFragment newFragment = new AddSessionDialog();
        newFragment.show(getFragmentManager(), "session");
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

	    public SessionListElement(int requestCode) {
	        super(getActivity().getResources().getDrawable(R.drawable.add_session),
	              getActivity().getResources().getString(R.string.session),
	              getActivity().getResources().getString(R.string.session_default),
	              requestCode);
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
}