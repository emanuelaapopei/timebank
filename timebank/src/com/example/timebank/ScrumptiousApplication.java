package com.example.timebank;

import android.app.Application;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;

import java.util.List;

/**
 * Use a custom Application class to pass state data between Activities.
 */
public class ScrumptiousApplication extends Application {

    private List<GraphUser> selectedUsers;
    private GraphPlace selectedPlace;

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
}
