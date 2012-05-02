package org.karibu;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class PostNewMain extends TabActivity {
	private TabHost tabHost;
	static double locLat = 0.0, locLng  = 0.0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_tabhost);

		tabHost = getTabHost();
		
		//basic info tab
		Intent basicIntent = new Intent().setClass(this, PostNew.class);
		TabSpec basicSpec = tabHost.newTabSpec("Basic").setIndicator("Basic", getResources().getDrawable(R.drawable.post_write)).setContent(basicIntent);
		//location info tab
		Intent locationIntent = new Intent().setClass(this, PostNewLocation.class);
		TabSpec locationSpec = tabHost.newTabSpec("Location").setIndicator("Location", getResources().getDrawable(R.drawable.marker_red)).setContent(locationIntent);
		
		//add tabs
		tabHost.addTab(basicSpec);
		tabHost.addTab(locationSpec);
		tabHost.setCurrentTab(0);
	}
	
	public void switchTab(int index) {
		tabHost.setCurrentTab(index);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater blowUp = getMenuInflater();
		blowUp.inflate(R.menu.our_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case R.id.main_list:	//back to main list
			Intent intent3 = new Intent(PostNewMain.this, KaribuActivity.class);
			startActivity(intent3);
			break;
		case R.id.post:
			break;
		case R.id.preferences:
			Intent intent2 = new Intent(PostNewMain.this, Prefs.class);
			startActivity(intent2);
			break;
		case R.id.cur_loc:
			Intent intent4 = new Intent(PostNewMain.this, DetailsActivity.class);
			startActivity(intent4);
			break;
		case R.id.aboutUs:
			Intent intent1 = new Intent(PostNewMain.this, AboutUs.class);
			startActivity(intent1);
			break;
		}
		return false;
	}

}
