package com.example.timebank;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Use a custom Application class to pass state data between Activities.
 */
public class TimeBankApplication extends Application {

    private List<GraphUser> selectedUsers;
    private GraphPlace selectedPlace;
    private GraphUser fbUser;
    private int balance;
    private String userId;
    
    public List<GraphUser> getSelectedUsers() {
        return selectedUsers;
    }
    public void setSelectedUsers(List<GraphUser> users) {
        selectedUsers = users;
    }

    public GraphPlace getSelectedPlace() {
        return selectedPlace;
    }
    public void setSelectedPlace(GraphPlace place) {
        this.selectedPlace = place;
    }
    
    public GraphUser getUser()
	{
		return fbUser;		
	}
	public void setUser(GraphUser User)
	{
		this.fbUser = User;
	}

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

	public int getBalance()
	{
		return balance;
	}
	public void setBalance(int newBalance)
	{
		balance = newBalance;
	}
	
	@Override
	  public void onCreate() {
	    super.onCreate();

		// Initialize the Parse SDK.
	    Parse.initialize(this, "aGRzy0mD7CnzhIrseg4wWFpS2LjX2wyIXX0yh5Yu", "PMNgqCNC17R5XHYxK5wo2ENOeUsimtox4JcD40d5");
	 	
	 	ParsePush.subscribeInBackground("", new SaveCallback() {
	 		  @Override
	 		  public void done(ParseException e) {
	 		    if (e == null) {
	 		    	Toast toast = Toast.makeText(getApplicationContext(), R.string.alert_dialog_success, Toast.LENGTH_SHORT);
					toast.show();
	 		    } else {
	 		    	e.printStackTrace();

					Toast toast = Toast.makeText(getApplicationContext(), R.string.alert_dialog_failed, Toast.LENGTH_SHORT);
					toast.show();
	 		    }
	 		  }
	 		});
	  }
}
