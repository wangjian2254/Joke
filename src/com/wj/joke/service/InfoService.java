package com.wj.joke.service;


import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.wj.joke.Joke;
import com.wj.joke.db.ActiveUserHelper;
import com.wj.joke.db.DBhelper;
import com.wj.joke.R;


import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.app.Notification;

import android.app.NotificationManager;

import android.app.PendingIntent;



import android.os.Binder;

import android.os.Handler;



public class InfoService extends Service {
	
	DefaultHttpClient client;
	HttpResponse httpResponse;
	ActiveUserHelper auh;
	SQLiteDatabase readdb;
	SQLiteDatabase writedb;

	String uname;
	String pwd;
	String updateTime;
	
	Context context;
	
	ContentValues value;
	Intent i;
	int num=0;


	private static final String TAG = "TService";

	private NotificationManager notificationManager;

	

	public void onCreate() {

	Log.d(TAG, "============> TService.onCreate");
	context=this;
	auh = DBhelper.getDBHelper(this);
	
	notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	showNotification();

	super.onCreate();
	
	}
	private Handler handler = new Handler();  
	private Handler imghandler = new Handler();  

	public void onStart(Intent intent, int startId) {

	Log.i(TAG, "============> TService.onStart");

	
	super.onStart(intent, startId);
	new Thread(){
		public void run(){
			
				auh=DBhelper.getDBHelper(context);
				
				//auh.close();
			
            handler.postDelayed(this, 1000*30*3);  
		}
	}.start();
//	new Thread(){
//		public void run(){
//			
////			auh=DBhelper.getDBHelper(context);
////			sync(auh);//瑕佸畾鏃舵墽琛岀殑浠诲姟  
//			//auh.close();
//			
//			imghandler.postDelayed(this, 1000*30*3);  
//		}
//	}.start();
//	
	}
	




	public IBinder onBind(Intent intent) {

	Log.i(TAG, "============> TService.onBind");

	return null;
	}

	public class LocalBinder extends Binder {

	public InfoService getService() {

	return InfoService.this;
	}

	}

	public boolean onUnbind(Intent intent) {

	Log.i(TAG, "============> TService.onUnbind");

	return false;
	}

	public void onRebind(Intent intent) {

	Log.i(TAG, "============> TService.onRebind");
	}

	public void onDestroy() {

	Log.i(TAG, "============> TService.onDestroy");

//	notificationManager.cancel(R.string.service_start);

	

	super.onDestroy();
	}

	private void showNotification() {

		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);              
		Notification n = new Notification(R.drawable.jokemin, "", System.currentTimeMillis());            
		n.flags = Notification.FLAG_AUTO_CANCEL;               
		//Intent i = new Intent(arg0.getContext(), NotificationShow.class);
		//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);          
		//PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(
		        this,
		        R.string.app_name,
		        getI(),
		        PendingIntent.FLAG_UPDATE_CURRENT);
			                 
		n.setLatestEventInfo(
		        this,
		        "",
		        "",
		        contentIntent);
		nm.notify(R.string.app_name, n);
	}

	
	
	public void onDestory(){
		
	}
	
	public void sync(ActiveUserHelper auh) {
		

//			ActiveUserHelper auh = DBhelper.getDBHelper(this);

//		this.infoSync.sync(this, auh);
//			int newcount=infoSync.sync( readdb, wdb);
			
				
				
	}
	public void imgsync(){
//		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
//			auh=DBhelper.getDBHelper(context);
//			if(num%10==4){
//				imgSync.sync(null, auh, 1);
//			}else{
//				if(num%10==7){
//					imgSync.sync(null, auh, 2);
//				}else{
//					imgSync.sync(null, auh, 0);
//				}
//			}
//			num++;
//		}
	}
	
	public void putNotic(int newcount){
		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);              
		Notification n = new Notification(R.drawable.jokemin, " ", System.currentTimeMillis());            
		n.flags = Notification.FLAG_AUTO_CANCEL;               
		//Intent i = new Intent(arg0.getContext(), NotificationShow.class);
		//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);          
		//PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(
		        this,
		        R.string.app_name,
		        getI(),
		        PendingIntent.FLAG_UPDATE_CURRENT);
			                 
		n.setLatestEventInfo(
		        this,
		        " ",
		        " ",
		        contentIntent);
		
		nm.notify(R.string.app_name, n);
	}

	public Intent getI(){
		
		//readdb.close();
		//auh.close();
		i=new Intent(InfoService.this,Joke.class);
		
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return i;
	}

	
	
}
