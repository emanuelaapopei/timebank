package com.example.timebank;

import com.facebook.model.OpenGraphAction;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.net.Uri;
import android.util.Log;
import com.facebook.*;
import com.facebook.model.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;


public class UserBoardFragment extends Fragment implements OnGestureListener{

    private static final String TAG = "timeBank";
    private String userId;
    private ListView listView;
    private List<BaseListElement> listElements;
    private UserBoardActivity activity;
    GestureDetector detector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (UserBoardActivity) getActivity();
        userId = ((TimeBankApplication) getActivity().getApplication()).getUserId();
        detector = new GestureDetector(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_user_board, container, false);
        view.setOnTouchListener(new OnSwipeTouchListener(activity) {
            @Override
            public void onSwipeLeft() {
                activity.finish();
            }
        });
        
        // Find the list view
        listView = (ListView) view.findViewById(R.id.selection_list);

        // Set up the list view items, based on a list of
        listElements = new ArrayList<BaseListElement>();
        listElements.add(new SkillListElement(0));
        listElements.add(new MessagesListElement(1));
        listElements.add(new SessionListElement(2));
        listElements.add(new AlertListElement(3));
        listView.setAdapter(new ActionListAdapter(getActivity(), R.id.selection_list, listElements));        
        return view;
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

    /**
     * Used to inspect the response from posting an action
     */
    private interface PostResponse extends GraphObject {
        String getId();
    }

    private void startAlertActivity(int requestCode) {
        Intent intent = new Intent();
        Uri data = Uri.parse(userId);
        intent.setData(data);
        intent.setClass(getActivity(), AlertActivity.class);
        startActivity(intent);
    }

    private void startSkillBoardActivity(int requestCode) {
        Intent intent = new Intent();
        Uri data = Uri.parse(userId);
        intent.setData(data);
        intent.setClass(getActivity(), SkillBoardActivity.class);
        startActivity(intent);
    }

    private void startSessionActivity(int requestCode) {
        Intent intent = new Intent();
        Uri data = Uri.parse(userId);
        intent.setData(data);
        intent.setClass(getActivity(), SessionActivity.class);
        startActivity(intent);
    }

    private void startMessagesActivity(int requestCode) {
    	Toast.makeText(activity.getApplicationContext(), "INBOX ACTIVITY MISSING", 100).show();
//        Intent intent = new Intent();
//        Uri data = Uri.parse(userId);
//        intent.setData(data);
//        intent.setClass(getActivity(), SessionActivity.class);
//        startActivity(intent);
    }
    
    private class SkillListElement extends BaseListElement {

        public SkillListElement(int requestCode) {
            super(getActivity().getResources().getDrawable(R.drawable.add_skill),
                    null, null, requestCode);
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
                        startSkillBoardActivity(getRequestCode());
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "No active session. Check connection!", 100).show();
                    }
                }
            };
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            // TODO Auto-generated method stub

        }
    }

    private class SessionListElement extends BaseListElement {

        private static final String FRIENDS_KEY = "friends";

        private List<GraphUser> selectedUsers;

        public SessionListElement(int requestCode) {
            super(getActivity().getResources().getDrawable(R.drawable.add_session),
                    null, null, requestCode);
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
                        startSessionActivity(getRequestCode());
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "No active session. Check connection!", 100).show();
                    }
                }
            };
        }

        @Override
        protected void onActivityResult(Intent data) {
            selectedUsers = ((TimeBankApplication) getActivity().getApplication()).getSelectedUsers();
            setUsersText();
            notifyDataChanged();
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            if (selectedUsers != null) {
                action.setTags(selectedUsers);
            }
        }

        @Override
        protected void onSaveInstanceState(Bundle bundle) {
            if (selectedUsers != null) {
                bundle.putByteArray(FRIENDS_KEY, getByteArray(selectedUsers));
            }
        }

        @Override
        protected boolean restoreState(Bundle savedState) {
            byte[] bytes = savedState.getByteArray(FRIENDS_KEY);
            if (bytes != null) {
                selectedUsers = restoreByteArray(bytes);
                setUsersText();
                return true;
            }
            return false;
        }

        private void setUsersText() {
            String text = null;
            if (selectedUsers != null) {
                if (selectedUsers.size() == 1) {
                    text = String.format(getResources().getString(R.string.single_user_selected),
                            selectedUsers.get(0).getName());
                } else if (selectedUsers.size() == 2) {
                    text = String.format(getResources().getString(R.string.two_users_selected),
                            selectedUsers.get(0).getName(), selectedUsers.get(1).getName());
                } else if (selectedUsers.size() > 2) {
                    text = String.format(getResources().getString(R.string.multiple_users_selected),
                            selectedUsers.get(0).getName(), (selectedUsers.size() - 1));
                }
            }
            if (text == null) {
                text = getResources().getString(R.string.action_people_default);
            }
            setText2(text);
        }

        private byte[] getByteArray(List<GraphUser> users) {
            // convert the list of GraphUsers to a list of String where each element is
            // the JSON representation of the GraphUser so it can be stored in a Bundle
            List<String> usersAsString = new ArrayList<String>(users.size());

            for (GraphUser user : users) {
                usersAsString.add(user.getInnerJSONObject().toString());
            }
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                new ObjectOutputStream(outputStream).writeObject(usersAsString);
                return outputStream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Unable to serialize users.", e);
            }
            return null;
        }

        private List<GraphUser> restoreByteArray(byte[] bytes) {
            try {
                @SuppressWarnings("unchecked")
                List<String> usersAsString =
                        (List<String>) (new ObjectInputStream(new ByteArrayInputStream(bytes))).readObject();
                if (usersAsString != null) {
                    List<GraphUser> users = new ArrayList<GraphUser>(usersAsString.size());
                    for (String user : usersAsString) {
                        GraphUser graphUser = GraphObject.Factory
                                .create(new JSONObject(user), GraphUser.class);
                        users.add(graphUser);
                    }
                    return users;
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Unable to deserialize users.", e);
            } catch (IOException e) {
                Log.e(TAG, "Unable to deserialize users.", e);
            } catch (JSONException e) {
                Log.e(TAG, "Unable to deserialize users.", e);
            }
            return null;
        }
    }

    private class MessagesListElement extends BaseListElement {

        private GraphPlace selectedPlace = null;

        public MessagesListElement (int requestCode) {
            super(getActivity().getResources().getDrawable(R.drawable.add_messages),
                    null, null, requestCode);
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Session.getActiveSession() != null &&
                            Session.getActiveSession().isOpened()) {
                        startMessagesActivity(getRequestCode());
                    } else {
                        //activity.showSettingsFragment();
                    }
                }
            };
        }

        @Override
        protected void onActivityResult(Intent data) {
            selectedPlace = ((TimeBankApplication) getActivity().getApplication()).getSelectedPlace();
            notifyDataChanged();
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            if (selectedPlace != null) {
                action.setPlace(selectedPlace);
            }
        }       
    }

    private class AlertListElement extends BaseListElement {
        public AlertListElement(int requestCode) {
            super(getActivity().getResources().getDrawable(R.drawable.add_alert),
                    null, null, requestCode);
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
                        startAlertActivity(getRequestCode());
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "No active session. Check connection!", 100).show();
                    }
                }
            };
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
        }
    }

    private class ActionListAdapter extends ArrayAdapter<BaseListElement> {
        private List<BaseListElement> listElements;

        public ActionListAdapter(Context context, int resourceId, List<BaseListElement> listElements) {
            super(context, resourceId, listElements);
            this.listElements = listElements;
            for (int i = 0; i < listElements.size(); i++) {
                listElements.get(i).setAdapter(this);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater =
                        (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.listitemenu, null);
            }

            BaseListElement listElement = listElements.get(position);
            if (listElement != null) {
                view.setOnClickListener(listElement.getOnClickListener());
                ImageView icon = (ImageView) view.findViewById(R.id.icon);
                if (icon != null) {
                    icon.setImageDrawable(listElement.getIcon());
                }
            }
            return view;
        }

    }

    public UserBoardFragment(String UserId) {
        userId = UserId;
    }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

}