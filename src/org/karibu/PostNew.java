package org.karibu;

import java.io.IOException;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class PostNew extends Activity {
//	private Bundle bundle;
	
	private EditText overview, details;
	private String myOverview, myDetails;
	private DatePicker start, end;
	private String newStart, newEnd;
	private Button postBtn;
	private Spinner spinner;
	private int myCate;
	private double announcementLat, announcementLng;
	
	private int year, month, day;
//	private TextView debug1, debug2;

	static final int SUMMARY_DIALOG = 0;
	static final int OVERVIEW_MISSING_DIALOG = 1;
	static final int DETAILS_MISSING_DIALOG = 2;
	static final int LOCATION_MISSING_DIALOG = 3;
	
	
	private String url = "https://tjjohnso2.wv.cc.cmu.edu:3000/announcements.json";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_form);
//		bundle = this.getIntent().getExtras();
		
		setupView();
		addListenerOnButton();
		
	}
	
	private void setupView() {	//system date	
		overview = (EditText) findViewById(R.id.post_overview);
		details = (EditText) findViewById(R.id.post_details);
		
//		debug1 = (TextView) findViewById(R.id.begindate);
//		debug2 = (TextView) findViewById(R.id.enddate);
		start = (DatePicker) findViewById(R.id.post_begindate);
		end = (DatePicker) findViewById(R.id.post_enddate);
 
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		
		// set current date into datepicker
		start.init(year, month, day, null);
		end.init(year, month, day, null);
		
		//spinner
		spinner = (Spinner) findViewById(R.id.post_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categoryItems, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
	}

	//for spinner
	private class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			myCate = pos;
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// do nothing
			
		}
		
	}
	
	
	private void addListenerOnButton() {
		postBtn = (Button) findViewById(R.id.post_button);
		postBtn.setOnClickListener(new View.OnClickListener() {
 
			public void onClick(View v) {
				newStart = new StringBuilder().append(start.getMonth() + 1).append("-").append(start.getDayOfMonth()).append("-").append(start.getYear()).toString();
//				debug1.setText(newStart);
				newEnd = new StringBuilder().append(end.getMonth() + 1).append("-").append(end.getDayOfMonth()).append("-").append(end.getYear()).toString();
//				debug2.setText(newEnd);
				
				myOverview = overview.getText().toString();
				myDetails = details.getText().toString();
				announcementLat = PostNewMain.locLat;
				announcementLng = PostNewMain.locLng;
				if (myOverview.trim().equals("")) 
					showDialog(OVERVIEW_MISSING_DIALOG);

				else if (announcementLat == 0.0)	//no location input
					showDialog(LOCATION_MISSING_DIALOG);
				
				else if (myDetails.trim().equals("")) 
					showDialog(DETAILS_MISSING_DIALOG);
				
				else 
					showDialog(SUMMARY_DIALOG);
			}
		});
 
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(PostNew.this);
		AlertDialog dialog;
		switch (id) {
		case SUMMARY_DIALOG:
			String[] cateItems = getResources().getStringArray(R.array.categoryItems);
			builder.setTitle("Summary");
			if (announcementLat == 0.0)
				builder.setMessage("Overview:\n" + myOverview + "\n\nDetails:\n" + myDetails + "\n\nCategory: " + cateItems[myCate] + 
						"\n\nStart On: " + newStart  + "\n\nExpired On: " + newEnd + "\n\nLocation: ");
			else 
				builder.setMessage("Overview:\n" + myOverview + "\n\nDetails:\n" + myDetails + "\n\nCategory: " + cateItems[myCate] + 
						"\n\nStart On: " + newStart  + "\n\nExpired On: " + newEnd + 
						"\n\nLocation: " + announcementLat + " " + announcementLng);
			builder.setPositiveButton("Confirm and Post", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //send to server
                	
                	String resp = postNew(url + "?overview=" + myOverview.replace(" ", "%20") + "&details=" + myDetails.replace(" ", "%20") +
                			"&categories=" + myCate + "&start_date=" + newStart + "&end_date=" + newEnd +
                			"&latitude=" + announcementLat + "&longitude=" + announcementLng);
                	if (resp == null) {
                		dialog.dismiss();
                		Toast.makeText(getBaseContext(), "Post Successfully!", Toast.LENGTH_SHORT).show();
                	} else
                		Toast.makeText(getBaseContext(), "Sorry, post failed:\n" + resp + "\nTry Again!", Toast.LENGTH_SHORT).show();
                }
            });
			builder.setNegativeButton("Return to Modify", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	dialog.dismiss();
                }
            });
			dialog = builder.create();
			break;
		case OVERVIEW_MISSING_DIALOG:
			builder.setTitle("Missing Overview");
			builder.setMessage("OVERVIEW of your annoucnement is required");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					overview.requestFocus();
				}
			});
			dialog = builder.create();
			break;
		
		case LOCATION_MISSING_DIALOG:
			builder.setTitle("Missing Location");
			builder.setMessage("You haven't input LOCATION. Please go to the Location tab and simply touch on the map for 1 second to choose the location!");
			builder.setPositiveButton("OK, take me to the location tab", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							//switch to location tab
							PostNewMain parent;
							parent = (PostNewMain) getParent();
							parent.switchTab(1);
						}
					});
					builder.setNegativeButton("I don't want to input location", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog = builder.create();
			break;
			
		case DETAILS_MISSING_DIALOG:
			builder.setTitle("Missing Details");
			builder.setMessage("Why not add a few lines of DETAILS?");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					details.requestFocus();
				}
			});
			builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					showDialog(SUMMARY_DIALOG);
				}
			});
			dialog = builder.create();
			break;
			
		default:
			dialog = null;
		}
		return dialog;
	}

	private String postNew(String url) {
        Log.i("url", url);
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(new HttpPost(url));
			Log.i("response", EntityUtils.toString(response.getEntity()));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		myOverview = savedInstanceState.getString("myOverview");
		myDetails = savedInstanceState.getString("myDetails");
		int startDate[] = savedInstanceState.getIntArray("startDate");
		int endDate[] = savedInstanceState.getIntArray("endDate");
		start.updateDate(startDate[0], startDate[1], startDate[2]);
		end.updateDate(endDate[0], endDate[1], endDate[2]);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("myOverview", myOverview);
		outState.putString("myDetails", myDetails);
		outState.putIntArray("startDate", new int[] {start.getYear(), start.getMonth(), start.getDayOfMonth()});
		outState.putIntArray("endDate", new int[] {end.getYear(), end.getMonth(), end.getDayOfMonth()});
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
			Intent intent3 = new Intent(PostNew.this, KaribuActivity.class);
			startActivity(intent3);
			break;
		case R.id.post:
			break;
		case R.id.preferences:
			Intent intent2 = new Intent(PostNew.this, Prefs.class);
			startActivity(intent2);
			break;
		case R.id.cur_loc:
			Intent intent4 = new Intent(PostNew.this, DetailsActivity.class);
			startActivity(intent4);
			break;
		case R.id.aboutUs:
			Intent intent1 = new Intent(PostNew.this, AboutUs.class);
			startActivity(intent1);
			break;
		}
		return false;
	}

}
