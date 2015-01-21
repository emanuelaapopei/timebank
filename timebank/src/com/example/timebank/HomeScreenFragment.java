package com.example.timebank;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.internal.Utility;
import com.facebook.model.*;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import org.json.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment that represents the main home screen for Time Bank.
 */
public class HomeScreenFragment extends Fragment
        implements OnGestureListener
        , AddFeedItemDialog.AddFeedItemListener{

    private static final String TAG = "HomeScreenFragment";
    private static final String PENDING_ANNOUNCE_KEY = "pendingAnnounce";
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");
    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static final String PERMISSION = "publish_actions";
    private ProgressDialog progressDialog;
    private UiLifecycleHelper uiHelper;
    private GraphUser fbUser;
    private boolean pendingAnnounce;
    private MainActivity activity;
    private Uri photoUri;
    private GestureDetector detector;

    private ProfilePictureView profilePictureView;
    private TextView balanceValueView;
    private View currentView;

    private List<BaseListElement> listElements;
    private ActionListAdapter listAdapter = null;
    private ListView listView;

    private ImageButton addFeedButton;
    private ImageButton filterFeedButton;
    private int itemsNumber;

    private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    private FacebookDialog.Callback nativeDialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            boolean resetSelections = true;
            if (FacebookDialog.getNativeDialogDidComplete(data)) {
                if (FacebookDialog.COMPLETION_GESTURE_CANCEL.equals(FacebookDialog.getNativeDialogCompletionGesture(data))) {
                    // Leave selections alone if user canceled.
                    resetSelections = false;
                    showCancelResponse();
                } else {
                    showSuccessResponse(FacebookDialog.getNativeDialogPostId(data));
                }
            }

            if (resetSelections) {
                init(null);
            }
        }

        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(R.string.error_dialog_button_text, null)
                    .setTitle(R.string.error_dialog_title)
                    .setMessage(error.getLocalizedMessage())
                    .show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        detector = new GestureDetector(this);
        uiHelper = new UiLifecycleHelper(getActivity(), sessionCallback);
        uiHelper.onCreate(savedInstanceState);

        itemsNumber = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.homescreen, container, false);

        view.setOnTouchListener(new OnSwipeTouchListener(activity) {
            @Override
            public void onSwipeRight() {
                startUserBoardActivity();
            }
        });

        profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
        profilePictureView.setCropped(true);

        addFeedButton = (ImageButton) view.findViewById(R.id.add_new);
        addFeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                addNewFeedItem();
            }
        });

        filterFeedButton = (ImageButton) view.findViewById(R.id.filter_feed);
        filterFeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Toast.makeText(activity.getApplicationContext(), "Filtering will be here soon", 100).show();
            }
        });

        listView = (ListView) view.findViewById(R.id.feed_list);
        listElements = new ArrayList<BaseListElement>();

        //readFeedList();

        // Set the list view adapter
        listAdapter = new ActionListAdapter(getActivity(), R.id.feed_list, listElements);
        listView.setAdapter(listAdapter);


        balanceValueView = (TextView) view.findViewById(R.id.balance_value);
        init(savedInstanceState);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode >= 0 && requestCode < listElements.size()) {
            listElements.get(requestCode).onActivityResult(data);
        } else {
            uiHelper.onActivityResult(requestCode, resultCode, data, nativeDialogCallback);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(PENDING_ANNOUNCE_KEY, pendingAnnounce);
        uiHelper.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
        activity = null;
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            makeMeRequest(session);

        } else {
            profilePictureView.setProfileId(null);
        }
    }

    private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        profilePictureView.setProfileId(user.getId());
                        ((TimeBankApplication) getActivity().getApplication()).setUser(user);

                        getBalanceFromDB();
                    }
                }
                if (response.getError() != null) {
                    handleError(response.getError());
                }
            }
        });
        request.executeAsync();

    }

    /**
     * Resets the view to the initial defaults.
     */
    private void init(Bundle savedInstanceState) {
//        testButton.setEnabled(false);

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            makeMeRequest(session);
        }
    }


    private void getBalanceFromDB() {
        //check if the current user has balance in the database
        fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();

        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();
        final String username = firstName + " " + lastName;


        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");

        query.whereEqualTo("username", firstName + " " + lastName);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> userList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved " + userList.size() + " users");
                    int balance = 0;

                    ParseObject userParse = new ParseObject("User");
                    if (userList.size() == 0) {
                        //user doesn't have balance - first time he logs in the app
                        //must save a User with default 5h balance
                        userParse.put("fbID", profilePictureView.getProfileId());
                        userParse.put("username", username);
                        userParse.put("balance", 5);

                        userParse.saveInBackground();

                        ((TimeBankApplication) getActivity().getApplication()).setBalance(5);
                        balance = 5;
                    } else {
                        userParse = userList.get(0);
                        balance = userParse.getInt("balance");
                        Log.d(TAG, "balance from db=  " + balance);
                        ((TimeBankApplication) getActivity().getApplication()).setBalance(balance);
                    }

                    //set the balance in GUI--keep this code here
                    Log.d(TAG, "new balance =  " + balance);
                    balanceValueView.setText(String.valueOf(balance));
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });

    }


    private Pair<File, Integer> getImageFileAndMinDimension() {
        File photoFile = null;
        String photoUriString = photoUri.toString();
        if (photoUriString.startsWith("file://")) {
            photoFile = new File(photoUri.getPath());
        } else if (photoUriString.startsWith("content://")) {
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(photoUri, filePath, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePath[0]);
                String filename = cursor.getString(columnIndex);
                cursor.close();

                photoFile = new File(filename);
            }
        }

        if (photoFile != null) {
            InputStream is = null;
            try {
                is = new FileInputStream(photoFile);

                // We only want to get the bounds of the image, rather than load the whole thing.
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);

                return new Pair<File, Integer>(photoFile, Math.min(options.outWidth, options.outHeight));
            } catch (Exception e) {
                return null;
            } finally {
                Utility.closeQuietly(is);
            }
        }
        return null;
    }

    /**
     * Creates a GraphObject with the following format:
     * {
     * url: ${uri},
     * user_generated: true
     * }
     */
    private GraphObject getImageObject(String uri, boolean userGenerated) {
        GraphObject imageObject = GraphObject.Factory.create();
        imageObject.setProperty("url", uri);
        if (userGenerated) {
            imageObject.setProperty("user_generated", "true");
        }
        return imageObject;
    }

    private List<JSONObject> getImageListForAction(String uri, boolean userGenerated) {
        return Arrays.asList(getImageObject(uri, userGenerated).getInnerJSONObject());
    }

    private void requestPublishPermissions(Session session) {
        if (session != null) {
            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSION)
                    // demonstrate how to set an audience for the publish permissions,
                    // if none are set, this defaults to FRIENDS
                    .setDefaultAudience(SessionDefaultAudience.FRIENDS)
                    .setRequestCode(REAUTH_ACTIVITY_CODE);
            session.requestNewPublishPermissions(newPermissionsRequest);
        }
    }

    private void onPostActionResponse(Response response) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (getActivity() == null) {
            // if the user removes the app from the website, then a request will
            // have caused the session to close (since the token is no longer valid),
            // which means the splash fragment will be shown rather than this one,
            // causing activity to be null. If the activity is null, then we cannot
            // show any dialogs, so we return.
            return;
        }

        PostResponse postResponse = response.getGraphObjectAs(PostResponse.class);

        if (postResponse != null && postResponse.getId() != null) {
            showSuccessResponse(postResponse.getId());
            init(null);
        } else {
            handleError(response.getError());
        }
    }

    private void showSuccessResponse(String postId) {
        String dialogBody;
        if (postId != null) {
            dialogBody = String.format(getString(R.string.result_dialog_text_with_id), postId);
        } else {
            dialogBody = getString(R.string.result_dialog_text_default);
        }
        showResultDialog(dialogBody);
    }

    private void showCancelResponse() {
        showResultDialog(getString(R.string.result_dialog_text_canceled));
    }

    private void showResultDialog(String dialogBody) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.result_dialog_button_text, null)
                .setTitle(R.string.result_dialog_title)
                .setMessage(dialogBody)
                .show();
    }

    private void handleError(FacebookRequestError error) {
        DialogInterface.OnClickListener listener = null;
        String dialogBody = null;

        if (error == null) {
            dialogBody = getString(R.string.error_dialog_default_text);
        } else {
            switch (error.getCategory()) {
                case AUTHENTICATION_RETRY:
                    // tell the user what happened by getting the message id, and
                    // retry the operation later
                    String userAction = (error.shouldNotifyUser()) ? "" : getString(error.getUserActionMessageId());
                    dialogBody = getString(R.string.error_authentication_retry, userAction);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
                            startActivity(intent);
                        }
                    };
                    break;

                case AUTHENTICATION_REOPEN_SESSION:
                    // close the session and reopen it.
                    dialogBody = getString(R.string.error_authentication_reopen);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Session session = Session.getActiveSession();
                            if (session != null && !session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        }
                    };
                    break;

                case PERMISSION:
                    // request the publish permission
                    dialogBody = getString(R.string.error_permission);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pendingAnnounce = true;
                            requestPublishPermissions(Session.getActiveSession());
                        }
                    };
                    break;

                case SERVER:
                case THROTTLING:
                    // this is usually temporary, don't clear the fields, and
                    // ask the user to try again
                    dialogBody = getString(R.string.error_server);
                    break;

                case BAD_REQUEST:
                    // this is likely a coding error, ask the user to file a bug
                    dialogBody = getString(R.string.error_bad_request, error.getErrorMessage());
                    break;

                case OTHER:
                case CLIENT:
                default:
                    // an unknown issue occurred, this could be a code error, or
                    // a server side issue, log the issue, and either ask the
                    // user to retry, or file a bug
                    dialogBody = getString(R.string.error_unknown, error.getErrorMessage());
                    break;
            }
        }

        String title = error.getErrorUserTitle();
        String message = error.getErrorUserMessage();
        if (message == null) {
            message = dialogBody;
        }
        if (title == null) {
            title = getResources().getString(R.string.error_dialog_title);
        }

        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.error_dialog_button_text, listener)
                .setTitle(title)
                .setMessage(message)
                .show();
    }


    private void startUserBoardActivity() {
        Intent intent = new Intent();
        String userId = profilePictureView.getProfileId();
        ((TimeBankApplication) getActivity().getApplication()).setUserId(userId);

        try {
            Uri data = Uri.parse(userId);
            intent.setData(data);
            intent.setClass(getActivity(), UserBoardActivity.class);
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity.getApplicationContext(), e.toString(), 100).show();
        }

    }

    /**
     * Used to inspect the response from posting an action
     */
    private interface PostResponse extends GraphObject {
        String getId();
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

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        readFeedList();
    }

    public void addNewFeedItem() {
        DialogFragment newFragment = new AddFeedItemDialog();
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "feeditem");
    }

    public void readFeedList() {
    	
    	listAdapter.clear();
    	itemsNumber = 0;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("FeedItem");

        query.orderByDescending("updatedAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> feedList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved " + feedList.size() + " skills");

                    ParseObject feedItemParse = new ParseObject("FeedItem");
                    for (int i = 0; i < feedList.size(); i++) {
                    	feedItemParse = feedList.get(i);
                        String skill = feedItemParse.getString("Skill");
                        String feedItemText = feedItemParse.getString("FeedItemText");
                        String user = feedItemParse.getString("CreatedBy");

                        String main_string = feedItemText;
                        String default_string = "Skill: " + skill+ " by User: " + user;

                        Log.d(TAG, "Adding new item with values:" + main_string + " " + default_string);
                        //listElements.add(new SessionListElement(i, skill, default_string));
                        listAdapter.add(new FeedListElement(i, main_string, default_string, feedItemParse));
                        itemsNumber++;
                    }

                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });

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

    private class FeedListElement extends BaseListElement {

        private ParseObject feedParse;

        public FeedListElement(int requestCode, String text1, String text2, ParseObject parseObj) {
            super(getActivity().getResources().getDrawable(R.drawable.person_icon),
                    text1,//getActivity().getResources().getString(R.string.session),
                    text2,//getActivity().getResources().getString(R.string.session_default),
                    requestCode);

            feedParse = parseObj;
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {

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

        ParseObject feedParse;

        AddFeedItemDialog addFeedItemDialog = (AddFeedItemDialog) dialog;

        feedParse = addFeedItemDialog.getSession();

        String skill = feedParse.getString("Skill");
        String feedItemText = feedParse.getString("FeedItemText");
        String user = feedParse.getString("CreatedBy");

        String main_string = feedItemText;
        String default_string = "Skill: " + skill+ " by User: " + user;
 

        //Log.d(TAG, "Adding new item with values:" + main_string + " " +default_string);


        listAdapter.add(new FeedListElement(itemsNumber, main_string, default_string, feedParse));
        itemsNumber++;

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // TODO Auto-generated method stub

    }
}