package com.example.timebank;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;

import com.example.timebank.SessionFragment;

public class SessionActivity extends FragmentActivity
							 implements AddSessionDialog.AddSessionListener{
	
	private SessionFragment sessionFragment;
	private static final String TAG = "timeBank";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session);
		
		//Log.d(TAG, "hahahahahghghkj");
		
		Uri intentUri = getIntent().getData();
		String userId = intentUri.toString();
		
		FragmentManager manager = getSupportFragmentManager();
		
		if (savedInstanceState == null) {
			
			sessionFragment = new SessionFragment(userId);		
			manager.beginTransaction().replace(R.id.session_fragment, sessionFragment).commit();
		}
	}
	
	 @Override
	    protected void onStart() {
	        super.onStart();
	 }
	 
	 @Override
	    protected void onStop() {
	        super.onStop();
	 }
	 
	 @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	       
	 }
	 
	 @Override
	    public void onDestroy() {
	        super.onDestroy();
	    }
	 
	 @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	 }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.session, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
	@Override
    public void onDialogPositiveClick(DialogFragment dialog) {
      //Log.d(TAG, "hahahahah");
    }

	@Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        
    }

	
}
