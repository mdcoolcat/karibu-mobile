package org.karibu;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class PostNewLocation extends MapActivity  {
	//layout resources
	private MapView mapView;
	private TextView debugText1, debugText2;
	private Button tabOkButton;
	
	private List<Overlay> overlays;	//0: touchy, 1: compass 2: user location
	private MyLocationOverlay compass;
	private MapController controller; // animate to location
	// user location
	private LocationManager lm;
	private String provider;
	private Location userLocation;
	private int lat = 0, lng = 0;
	private double announcementLat = 0.0, announcementLng = 0.0;
	private int x, y; // position on screen
	private Drawable redPin, blueDot;	//blueDot is for user location
	
	private long start, stop; // time variables
	
	/* This is the test URL Karibu uses when connected to the CMU network. */
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

//        networkSetup();
		
		setContentView(R.layout.post_location);
		
		//prepare views
		mapView = (MapView) findViewById(R.id.tabMap);
		mapView.setBuiltInZoomControls(true);
		debugText1 = (TextView) findViewById(R.id.debugInfo1);
		debugText2 = (TextView) findViewById(R.id.debugInfo2);
		addListenerOnButton();
		
		// map overlay
		overlays = mapView.getOverlays();
		Touchy t = new Touchy(); // touch screen overlay
		overlays.add(t);
		compass = new MyLocationOverlay(PostNewLocation.this, mapView);
		overlays.add(compass);
		controller = mapView.getController();
		
		
		redPin = this.getResources().getDrawable(R.drawable.marker_red);
		blueDot = this.getResources().getDrawable(R.drawable.blue_dot);
		
		
		//manager listen to user location
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria(); //for selecting a location provider
		provider = lm.getBestProvider(crit, false); // default
		userLocation = lm.getLastKnownLocation(provider); // update location for listener
		debugText1.setText(provider);
		//user location updated!														
		if (userLocation != null) {
			debugText2.setText("get location!!");
			lat = (int) (userLocation.getLatitude() * 1E6);
			lng = (int) (userLocation.getLongitude() * 1E6) ;
			
			CustomPinPoint myPinPoint = new CustomPinPoint(blueDot,
					PostNewLocation.this);
			GeoPoint myLoc = new GeoPoint(lat, lng);
			OverlayItem overlayItem = new OverlayItem(myLoc, "", "");
			controller.setCenter(myLoc);
			controller.animateTo(myLoc);
			controller.setZoom(17);
			
			myPinPoint.addPinPoint(overlayItem);
			overlays.add(myPinPoint);
		
		} 
		
		else {
			debugText2.setText("provider disabled");
			controller.setZoom(17);
		}				
		
	}

	private void addListenerOnButton() {
		tabOkButton = (Button) findViewById(R.id.post_location_button);
		tabOkButton.setOnClickListener(new View.OnClickListener() {
 
			public void onClick(View v) {
				PostNewMain parent;
				parent = (PostNewMain) getParent();
				parent.switchTab(0);
//				Intent intent = new Intent(PostNewLocation.this, PostNew.class);
//				Bundle bundle = new Bundle();
//				bundle.putDouble("aLat", announcementLat);
//				bundle.putDouble("aLng", announcementLng);
//				intent.putExtras(bundle);
//				startActivity(intent);
			}
		});
 
	}

	
	@Override
	protected void onPause() {
		compass.disableCompass();
		super.onPause();
	}

	@Override
	protected void onResume() {
		compass.enableCompass();
		super.onResume();
	}

	protected boolean isRouteDisplayed() {
		return false;
	}


	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	private class Touchy extends Overlay {
		GeoPoint touchedPt;

		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				start = event.getEventTime();
				x = (int) event.getX();
				y = (int) event.getY();
				touchedPt = mapView.getProjection().fromPixels(x, y);
			}
			if (event.getAction() == MotionEvent.ACTION_UP)
				stop = event.getEventTime();
			if (stop - start > 1000) {
				AlertDialog alert = new AlertDialog.Builder(
						PostNewLocation.this).create();
				alert.setTitle("Place the PinPoint");
				//button 1
				alert.setButton("Place it!",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								CustomPinPoint locationPin = new CustomPinPoint(
										redPin, PostNewLocation.this);
								OverlayItem overlayItem = new OverlayItem(
										touchedPt, "what's up", ".....");
								locationPin.addPinPoint(overlayItem);
								overlays.add(locationPin);
								
								announcementLat = touchedPt.getLatitudeE6() / 1E6;
								announcementLng = touchedPt.getLongitudeE6() / 1E6;
								PostNewMain.locLat = announcementLat;
								PostNewMain.locLng = announcementLng;
								Toast.makeText(getBaseContext(), "Lat: "+ announcementLat +"\nLong: "+announcementLng, Toast.LENGTH_SHORT).show();
								
								//get the address
								Geocoder geoCoder = new Geocoder(
										getBaseContext(), Locale.getDefault());
								try {
									List<Address> addrList = geoCoder.getFromLocation(announcementLat, announcementLng, 1);
									if (addrList.size() > 0) {
										String addr = "";
										for (int i = 0; i < addrList.get(0)
												.getMaxAddressLineIndex(); i++)
											addr += addrList.get(0)
													.getAddressLine(i) + "\n";
										Toast.makeText(getBaseContext(), addr,
												Toast.LENGTH_LONG).show();
									} else {
										Toast.makeText(
												getBaseContext(),
												"location not available: "
														+ touchedPt
																.getLatitudeE6()
														+ " "
														+ touchedPt
																.getLongitudeE6(),
												Toast.LENGTH_SHORT).show();
									}
								} catch (IOException e) {
									Toast.makeText(getBaseContext(),
											e.getMessage(), Toast.LENGTH_LONG)
											.show();
								}
							
								
							}
						});
				alert.setButton2("Cancel", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
					
				});
				alert.show();
				return true;
			}
			return false;
		}
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
			Intent intent3 = new Intent(PostNewLocation.this, KaribuActivity.class);
			startActivity(intent3);
			break;
		case R.id.post:
			break;
		case R.id.preferences:
			Intent intent2 = new Intent(PostNewLocation.this, Prefs.class);
			startActivity(intent2);
			break;
		case R.id.cur_loc:
			Intent intent4 = new Intent(PostNewLocation.this, DetailsActivity.class);
			startActivity(intent4);
			break;
		case R.id.aboutUs:
			Intent intent1 = new Intent(PostNewLocation.this, AboutUs.class);
			startActivity(intent1);
			break;
		}
		return false;
	}

}