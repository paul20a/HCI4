package com.example.bikesafe;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;

public class MainActivity extends ActionBarActivity {

	final int RQS_GooglePlayServices = 0;
	private GoogleMap map;

	private static final int WIDTH_MAX = 50;
	private static final int HUE_MAX = 360;
	private static final int ALPHA_MAX = 255;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// check google play is installed and up to date
		final int code = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
		if (code != ConnectionResult.SUCCESS) {
			// error dialog to prompt install/update
			GooglePlayServicesUtil.getErrorDialog(code, this,
					RQS_GooglePlayServices).show();
		}
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		final ViewTreeObserver vto = this.findViewById(R.id.layout)
				.getViewTreeObserver();

		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			// Suppress version warnings, both versions are taken into
			// account
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {

				Location location = map.getMyLocation();
				LatLng myLatLng = null;
				if (location != null) {
					myLatLng = new LatLng(location.getLatitude(), location
							.getLongitude());
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(
							myLatLng, 17));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
						MainActivity.this.findViewById(R.id.layout)
						.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					else
						MainActivity.this.findViewById(R.id.layout)
						.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});

		InputStream ins = getResources().openRawResource(
				getResources().getIdentifier("raw/points", "raw",
						getPackageName()));

		ArrayList<ParcelableGeoPoint> g = readFile(ins);
		
		map.setMyLocationEnabled(true);

		Intent serviceIntent = new Intent(this, MyService.class);
		serviceIntent.putParcelableArrayListExtra("points", g);
		startService(serviceIntent);
	}

	private void drawCircle(GeoPoint p) {
		map.addCircle(new CircleOptions()
				.center(new LatLng(p.getLatitudeE6()/1e6, p.getLongitudeE6()/1e6))
				.radius(40)
				.strokeWidth(WIDTH_MAX).strokeColor(HUE_MAX)
				.fillColor(Color.HSVToColor(
		                ALPHA_MAX, new float[] {Color.RED, 1, 1})));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	private ArrayList<ParcelableGeoPoint> readFile(InputStream in) {
		ArrayList<ParcelableGeoPoint> points = new ArrayList<ParcelableGeoPoint>();
		try {
			 byte[] b = new byte[in.available()];
		        in.read(b);
		        String temp=new String(b);
				String[] coords = temp.split("\n");
				Log.i("", coords[0]);
			for (int i=0;i<coords.length;i++) {
				String[] latLng = coords[i].split(",");
				Double d1 = (Double.parseDouble(latLng[0]) * 1e6);
				Double d2 = (Double.parseDouble(latLng[1]) * 1e6);
				drawCircle(new GeoPoint(d1.intValue(),
						d2.intValue()));
				points.add(new ParcelableGeoPoint(new GeoPoint(d1.intValue(),
						d2.intValue())));
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return points;
	}

}
