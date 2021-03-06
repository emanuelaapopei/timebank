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
import android.widget.Toast;


public class SkillBoardFragment extends Fragment
        implements AddSkillDialog.AddSkillListener, EditSkillDialog.EditSkillListener {
    private static final String TAG = "timeBank";

    private ProfilePictureView profilePictureView;
    private GraphUser fbUser;
    private String userId;

    private ListView listView;
    private List<BaseListElement> listElements;
    private ActionListAdapter listAdapter = null;
    private ImageButton addSkill;
    private int skillNumber;

    public SkillBoardFragment(String UserId) {
        userId = UserId;
        skillNumber = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_skill_board, container, false);

        profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
        profilePictureView.setProfileId(userId);

        addSkill = (ImageButton) view.findViewById(R.id.add_skill);
        addSkill.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                addNewSkill();
            }
        });

        // Find the list view
        listView = (ListView) view.findViewById(R.id.skill_list);

        // Set up the list view items, based on a list of
        // BaseListElement items
        listElements = new ArrayList<BaseListElement>();

        updateSkillList();

        listAdapter = new ActionListAdapter(getActivity(), R.id.skill_list, listElements);
        // Set the list view adapter
        listView.setAdapter(listAdapter);

        init(savedInstanceState);
        return view;
    }

    public void addNewSkill() {
        DialogFragment newFragment = new AddSkillDialog();
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "skill");
    }

    public void editSkill(ParseObject skill, int skillNumber) {
        DialogFragment newFragment = new EditSkillDialog(skill, skillNumber);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "skilla");
    }


    public void updateSkillList() {
        fbUser = ((TimeBankApplication) getActivity().getApplication()).getUser();

        String firstName = fbUser.getFirstName();
        String lastName = fbUser.getLastName();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Skill");

        query.whereEqualTo("CreatedBy", firstName + " " + lastName);
        query.orderByDescending("updatedAt");
        
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> skillList, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved " + skillList.size() + " alerts");

                    ParseObject skillParse = new ParseObject("Skill");
                    for (int i = 0; i < skillList.size(); i++) {
                        skillParse = skillList.get(i);
                        String skill = skillParse.getString("Skill");
                        double skill_level = skillParse.getDouble("Experience");

                        String main_string = skill;
                        String default_string = Utils.getSkillLevel(skill_level);

                        listAdapter.add(new SkillListElement(i, main_string, default_string, skillParse));
                        skillNumber++;
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

    private class SkillListElement extends BaseListElement {
        private ParseObject skillParse;

        public SkillListElement(int requestCode, String text1, String text2, ParseObject parseObj) {
            super(getActivity().getResources().getDrawable(R.drawable.skill),
                    text1,
                    text2,
                    requestCode);

            skillParse = parseObj;
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editSkill(skillParse, getRequestCode());
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
        AddSkillDialog addSkillDialog = (AddSkillDialog) dialog;
        ParseObject skillParse = addSkillDialog.getSession();

        String skill = skillParse.getString("Skill");
        double experience = skillParse.getDouble("Experience");

        listAdapter.add(new SkillListElement(skillNumber, skill, Utils.getSkillLevel(experience), skillParse));
        skillNumber++;

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogEditPositiveClick(DialogFragment dialog) {
        EditSkillDialog editSkillDialog = (EditSkillDialog) dialog;
        ParseObject skillParse = editSkillDialog.getSession();

        //user can only edit the experience        
        double experience = skillParse.getDouble("Experience");

        SkillListElement oldElem = (SkillListElement) listAdapter.getItem(editSkillDialog.getRequestedSkill());
        oldElem.setText2(Utils.getSkillLevel(experience));
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