package com.example.timebank;

import com.facebook.model.OpenGraphAction;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.*;
import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.ProfilePictureView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;


public class UserBoardFragment extends Fragment {

    private static final String TAG = "timeBank";
    private String userId;
    private Uri photoUri;
    private GraphUser fbUser;
    private ImageView photoThumbnail;

    private ListView listView;
    private List<BaseListElement> listElements;
    private ActionListAdapter listAdapter = null;

    private ProfilePictureView profilePictureView;
    private UserBoardActivity activity;

    public UserBoardFragment(String UserId) {
        userId = UserId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (UserBoardActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_user_board, container, false);

        // Find the list view
        listView = (ListView) view.findViewById(R.id.selection_list);

        // Set up the list view items, based on a list of
        listElements = new ArrayList<BaseListElement>();
        listElements.add(new SkillListElement(0));
        listElements.add(new LocationListElement(1));
        listElements.add(new PeopleListElement(2));
        listElements.add(new PhotoListElement(3));
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

        String userId = profilePictureView.getProfileId();
        Uri data = Uri.parse(userId);
        intent.setData(data);
        intent.setClass(getActivity(), AlertActivity.class);
        startActivity(intent);
    }

    private void startSkillBoardActivity(int requestCode) {
        Intent intent = new Intent();

        String userId = profilePictureView.getProfileId();
        Uri data = Uri.parse(userId);
        intent.setData(data);
        intent.setClass(getActivity(), SkillBoardActivity.class);
        startActivity(intent);
    }

    private void startSessionActivity(int requestCode) {
        Intent intent = new Intent();

        String userId = profilePictureView.getProfileId();
        Uri data = Uri.parse(userId);
        intent.setData(data);
        intent.setClass(getActivity(), SessionActivity.class);
        startActivity(intent);
    }

    private class SkillListElement extends BaseListElement {

        private static final String FOOD_KEY = "food";
        private static final String FOOD_URL_KEY = "food_url";

        private final String[] skillChoices;
        private final String[] foodUrls;
        private String foodChoiceUrl = null;
        private String skillChoice = null;

        public SkillListElement(int requestCode) {
            super(getActivity().getResources().getDrawable(R.drawable.add_skill),
                   null,null,requestCode);
            skillChoices = getActivity().getResources().getStringArray(R.array.skill_types);
            foodUrls = getActivity().getResources().getStringArray(R.array.food_og_urls);
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
        protected void onSaveInstanceState(Bundle bundle) {
            if (skillChoice != null && foodChoiceUrl != null) {
                bundle.putString(FOOD_KEY, skillChoice);
                bundle.putString(FOOD_URL_KEY, foodChoiceUrl);
            }
        }

        @Override
        protected boolean restoreState(Bundle savedState) {
            String food = savedState.getString(FOOD_KEY);
            String foodUrl = savedState.getString(FOOD_URL_KEY);
            if (food != null && foodUrl != null) {
                skillChoice = food;
                foodChoiceUrl = foodUrl;
                //setFoodText();
                return true;
            }
            return false;
        }

        private void showMealOptions() {
            String title = getActivity().getResources().getString(R.string.select_skill);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title).
                    setCancelable(true).
                    setItems(skillChoices, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            foodChoiceUrl = foodUrls[i];
                            if (foodChoiceUrl.length() == 0) {
                                getCustomFood();
                            } else {
                                skillChoice = skillChoices[i];
                                //setFoodText();
                                notifyDataChanged();
                            }
                        }
                    });
            builder.show();
        }

        private void getCustomFood() {
            String title = getActivity().getResources().getString(R.string.enter_meal);
            final EditText input = new EditText(getActivity());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title)
                    .setCancelable(true)
                    .setView(input)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            skillChoice = input.getText().toString();
                            //setFoodText();
                            notifyDataChanged();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            AlertDialog dialog = builder.create();
            // always popup the keyboard when the alert dialog shows
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            // TODO Auto-generated method stub

        }
    }

    private class PeopleListElement extends BaseListElement {

        private static final String FRIENDS_KEY = "friends";

        private List<GraphUser> selectedUsers;

        public PeopleListElement(int requestCode) {
            super(getActivity().getResources().getDrawable(R.drawable.add_session),
                   null,null,requestCode);
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

    private class LocationListElement extends BaseListElement {

        private static final String PLACE_KEY = "place";

        private GraphPlace selectedPlace = null;

        public LocationListElement(int requestCode) {
            super(getActivity().getResources().getDrawable(R.drawable.add_messages),
            		null,null,requestCode);
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Session.getActiveSession() != null &&
                            Session.getActiveSession().isOpened()) {
                        startSkillBoardActivity(getRequestCode());
                    } else {
                        //activity.showSettingsFragment();
                    }
                }
            };
        }

        @Override
        protected void onActivityResult(Intent data) {
            selectedPlace = ((TimeBankApplication) getActivity().getApplication()).getSelectedPlace();
            setPlaceText();
            notifyDataChanged();
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            if (selectedPlace != null) {
                action.setPlace(selectedPlace);
            }
        }

        @Override
        protected void onSaveInstanceState(Bundle bundle) {
            if (selectedPlace != null) {
                bundle.putString(PLACE_KEY, selectedPlace.getInnerJSONObject().toString());
            }
        }

        @Override
        protected boolean restoreState(Bundle savedState) {
            String place = savedState.getString(PLACE_KEY);
            if (place != null) {
                try {
                    selectedPlace = GraphObject.Factory.create(new JSONObject(place), GraphPlace.class);
                    setPlaceText();
                    return true;
                } catch (JSONException e) {
                    Log.e(TAG, "Unable to deserialize place.", e);
                }
            }
            return false;
        }

        private void setPlaceText() {
            String text = null;
            if (selectedPlace != null) {
                text = selectedPlace.getName();
            }
            if (text == null) {
                text = getResources().getString(R.string.action_messages);
            }
            setText2(text);
        }

    }

    private class PhotoListElement extends BaseListElement {
        private static final int CAMERA = 0;
        private static final int GALLERY = 1;
        private static final String PHOTO_URI_KEY = "photo_uri";
        private static final String TEMP_URI_KEY = "temp_uri";
        private static final String FILE_PREFIX = "scrumptious_img_";
        private static final String FILE_SUFFIX = ".jpg";

        private Uri tempUri = null;

        public PhotoListElement(int requestCode) {
            super(getActivity().getResources().getDrawable(R.drawable.add_alert),
                    null, null, requestCode);
            photoUri = null;
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
        protected void onActivityResult(Intent data) {
            if (tempUri != null) {
                photoUri = tempUri;
            } else if (data != null) {
                photoUri = data.getData();
            }
            setPhotoThumbnail();
            setPhotoText();
        }

        private void setPhotoText() {
            if (photoUri == null) {
                setText2(getResources().getString(R.string.action_photo_default));
            } else {
                setText2(getResources().getString(R.string.action_photo_ready));
            }
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
        }

        @Override
        protected void onSaveInstanceState(Bundle bundle) {
            if (photoUri != null) {
                bundle.putParcelable(PHOTO_URI_KEY, photoUri);
            }
            if (tempUri != null) {
                bundle.putParcelable(TEMP_URI_KEY, tempUri);
            }
        }

        @Override
        protected boolean restoreState(Bundle savedState) {
            photoUri = savedState.getParcelable(PHOTO_URI_KEY);
            tempUri = savedState.getParcelable(TEMP_URI_KEY);
            setPhotoText();
            return true;
        }


        private void setPhotoThumbnail() {
            photoThumbnail.setImageURI(photoUri);
        }

        private Uri getTempUri() {
            String imgFileName = FILE_PREFIX + System.currentTimeMillis() + FILE_SUFFIX;
            File image = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imgFileName);
            return Uri.fromFile(image);
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


}