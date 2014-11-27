package com.example.bikesafe;

import java.util.ArrayList;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;

import com.google.android.maps.GeoPoint;

public class MyService extends Service implements LocationListener {
	WakeLock wakeLock;
	ArrayList<ParcelableGeoPoint> points;
	LocationManager locationManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				4000, 0, this);

		Intent resultIntent = new Intent(this, MainActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder mBuilder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Bike Safe")
				.setContentIntent(resultPendingIntent)
				.setContentText("Running");

		this.startForeground(startId, mBuilder.build());
		monitorLocation();
		points = intent.getExtras().getParcelableArrayList("points");

		return START_STICKY;

	}

	private void monitorLocation() {

	}

	private void keepAwake() {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"MyWakelockTag");
		wakeLock.acquire();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		keepAwake();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private double distanceBetween(double lat, double lng, GeoPoint b) {
		final double earthRad = 3958.75 * 1760.00000000;
		final double latDistance = Math.toRadians(b.getLatitudeE6() - lat);
		final double lngDistance = Math.toRadians(b.getLongitudeE6() - lng);
		final double sinLat = Math.sin(latDistance / 2);
		final double sinLng = Math.sin(lngDistance / 2);
		final double calc = Math.pow(sinLat, 2) + Math.pow(sinLng, 2)
				* Math.cos(Math.toRadians(lat))
				* Math.cos(Math.toRadians(b.getLatitudeE6()));
		return earthRad * 2 * Math.atan2(Math.sqrt(calc), Math.sqrt(1 - calc));

	}

	@Override
	public void onLocationChanged(Location location) {
		for(int i=0;i>points.size();i++){
		double d=distanceBetween(location.getLatitude(),location.getLongitude(),points.get(i).getGeoPoint());
		if(d<43){
		 Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		 long[] pattern = {0, 100, 1000, 300, 200, 100};
		 v.vibrate(pattern,-1);}
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}