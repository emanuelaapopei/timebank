package com.example.timebank;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class InboxActivity extends FragmentActivity {

	private InboxFragment inboxFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inbox);
		
		Uri intentUri = getIntent().getData();
		String userId = intentUri.toString();
		
		FragmentManager manager = getSupportFragmentManager();
		
		if (savedInstanceState == null) {
			
			inboxFragment = new InboxFragment(userId);		
			manager.beginTransaction().replace(R.id.inbox_fragment, inboxFragment).commit();
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
		getMenuInflater().inflate(R.menu.skill_board, menu);
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

	
}
