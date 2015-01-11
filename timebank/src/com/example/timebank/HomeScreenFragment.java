package com.example.timebank;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.facebook.*;
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
import java.util.Arrays;
import java.util.List;

/**
 * Fragment that represents the main home screen for Time Bank.
 */
public class HomeScreenFragment extends Fragment implements OnGestureListener {

    private static final String TAG = "HomeScreenFragment";
    private static final String PENDING_ANNOUNCE_KEY = "pendingAnnounce";
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");
    private static final int USER_GENERATED_MIN_SIZE = 480;

    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static final String PERMISSION = "publish_actions";

    GestureDetector detector;

    private ProfilePictureView profilePictureView;
    private TextView balanceValueView;
    private View currentView;


    private List<BaseListElement> listElements;
    private Button addFeedButton;
    private ListView listView;


    private boolean pendingAnnounce;
    private MainActivity activity;
    private Uri photoUri;

    private ProgressDialog progressDialog;

    private GraphUser fbUser;
    private UiLifecycleHelper uiHelper;
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

       //addFeedButton = (Button) view.findViewById(R.id.addfeed_button);
        listView = (ListView) view.findViewById(R.id.selection_list);
        balanceValueView = (TextView) view.findViewById(R.id.balance_value);
//        balanceValueView.setText("7");
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
            getBalanceFromDB();
        }
    }


    private void getBalanceFromDB() {
        //Log.d(TAG, "Entered getBalanceFromDB");
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
                    balanceValueView.setText(balance);
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
    }
}