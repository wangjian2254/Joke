package com.wj.joke;



import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.wj.joke.db.DBhelper;
import com.wj.joke.staticvalue.Convert;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class WeiboReg extends Activity {
    SQLiteDatabase readdb = null;
    SQLiteDatabase wdb = null;
    TextView jokeContent;
    TextView weibochoice;
    Button send;
    Button cancel;
    String jokeid;
    String joke;
    RadioButton radio1;
    RadioButton radio2;
    RadioButton radio3;
    CheckBox check1;
    CheckBox check2;
    CheckBox check3;
    DefaultHttpClient client;
    HttpResponse httpResponse;
    private Handler mMainHandler ;
    public Intent sendJokeIntent;
    public Bundle bunde;

    TextView content;
    WeiboReg weiboReg;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weiboReg=this;
        setContentView(R.layout.setweibo);
        radio1=(RadioButton)findViewById(R.id.radio1);
        radio2=(RadioButton)findViewById(R.id.radio2);
        radio3=(RadioButton)findViewById(R.id.radio3);
        check1=(CheckBox)findViewById(R.id.check1);
        check2=(CheckBox)findViewById(R.id.check2);
        check3=(CheckBox)findViewById(R.id.check3);
        weibochoice=(TextView)findViewById(R.id.weibochoice);
        send=(Button)findViewById(R.id.weibo_send);
        cancel=(Button)findViewById(R.id.cancel);
        content=(TextView)findViewById(R.id.content);
        sendJokeIntent=this.getIntent();
//        bunde = sendJokeIntent.getExtras();
//        if(bunde!=null){
//        	jokeid=bunde.getString("jokeId"); 
//        }
        Uri uri = this.getIntent().getData();
        if (uri != null) {
            String msg = null;
            String website = null;
            String[] webname = uri.toString().split("\\?");
            String[] urlparm=webname[1].split("&");

            if(urlparm[0].indexOf("msg")==0){
                msg=urlparm[0].substring(4);
            }
            if(urlparm[1].indexOf("website")==0){
                website=urlparm[1].substring(8);
            }
            if (msg!=null){
                try {
                    Toast.makeText(this, URLDecoder.decode(msg,"UTF-8"), 8000).show();
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
//					e.printStackTrace();
                }
            }
            if(website!=null){
                ContentValues appverson = new ContentValues();
                appverson.put("website", website);
                synchronized (Joke.class) {
                    if(wdb==null||wdb.isOpen()==false){
                        wdb=DBhelper.getDBHelper(this).getWritableDatabase();
                    }
                    wdb.insert("jokeweibo", null, appverson);
                    wdb.close();
                }
            }
            sendJokeIntent=null;
        }
        synchronized (Joke.class) {
            if(readdb==null||!readdb.isOpen()){
                readdb = DBhelper.getDBHelper(this).getReadableDatabase();
            }
            int currentid=0;
            Cursor jokeread=readdb.rawQuery(
                    "select jid from jokeread where id=1 ",
                    null);;
            jokeread.moveToFirst();
            while (!jokeread.isAfterLast()) {
                currentid=jokeread.getInt(0);
                jokeread.moveToNext();
                break;
            }
            jokeread.close();
            //////
            jokeread=readdb.rawQuery(
                    "select jid,content from joke where id="+currentid,
                    null);;
            jokeread.moveToFirst();
            while (!jokeread.isAfterLast()) {
                jokeid=jokeread.getString(0);
                content.setText(jokeread.getString(1));
                jokeread.moveToNext();
                break;
            }
            jokeread.close();
        }

        boolean b=false;
        synchronized (Joke.class) {
            if(readdb==null||!readdb.isOpen()){
                readdb = DBhelper.getDBHelper(this).getReadableDatabase();
            }
            Cursor jokeread;
            jokeread= readdb
                    .rawQuery(
                            "select website from jokeweibo",
                            null);
            jokeread.moveToFirst();
            while (!jokeread.isAfterLast()) {
                if("sina".equals(jokeread.getString(0))){
                    check1.setVisibility(View.VISIBLE);
                    radio1.setText("新浪微博（更换账号）");
                    b=true;
                }
                if("wy".equals(jokeread.getString(0))){
                    check2.setVisibility(View.VISIBLE);
                    radio2.setText("网易微博（更换账号）");
                    b=true;
                }
                if("teng".equals(jokeread.getString(0))){
                    check3.setVisibility(View.VISIBLE);
                    radio3.setText("腾讯微博（更换账号）");
                    b=true;
                }

                jokeread.moveToNext();
            }
            jokeread.close();
            readdb.close();
        }
        if(!b){
            weibochoice.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            send.setVisibility(View.GONE);
        }

        mMainHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // 接收子线程的消息
                if("1".equals(msg.obj)){
                }else{
                    Toast.makeText(weiboReg, msg.obj.toString(), 4000).show();
                }
            }

        };
    }

    public void onSend(View view) {
        new Thread(){
            public void run(){
                //微博分享
                Message toMain = mMainHandler.obtainMessage();
                toMain.obj = ""+request();

                mMainHandler.sendMessage(toMain);
            }
        }.start();
        onBack(null);
    }
    private String request() {

        String uri=Convert.hosturl+"/SendWeibo?username="+Convert.username+"&userpwd="+Convert.userpwd+"&jokeid="+jokeid+"&website=";
        if(check1.getVisibility()==View.VISIBLE&&check1.isChecked()){
            uri+="sina";
        }
        if(check2.getVisibility()==View.VISIBLE&&check2.isChecked()){
            uri+="wy";
        }
        if(check3.getVisibility()==View.VISIBLE&&check3.isChecked()){
            uri+="teng";
        }
        Log.e("url", uri);
        HttpGet getMet = new HttpGet(uri + Convert.testNum);
        client = new DefaultHttpClient();
        client.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
        try {
            httpResponse = client.execute(getMet);
            Log.e("request", "1");
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                // 取出回应字串
                String strResult;

                strResult = EntityUtils.toString(httpResponse.getEntity());
                Log.e("request", "2");
                return strResult;
            }
        }  catch (Exception t) {
            Log.e("E", t.getMessage() == null ? "" : t.getMessage());
            Log.e("request", uri);
            return "网络不给力，请检查网络状况。";
        }
        return "";

    }
    public void onWeiboSend(View view) {
        String website=null;
        if(radio1.isChecked()){
            website="sina";
        }
        if(radio2.isChecked()){
            website="wy";
        }
        if(radio3.isChecked()){
            website="teng";
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Convert.hosturl+"/login?username="+Convert.username+"&website="+website));
        this.startActivity(intent);
    }
    public void onBack(View view) {
        if(sendJokeIntent==null){
            sendJokeIntent=new Intent(WeiboReg.this, Joke.class);
            WeiboReg.this.startActivity(sendJokeIntent);
            WeiboReg.this.finish();
        }else{
            WeiboReg.this.setResult(RESULT_OK, sendJokeIntent);
            WeiboReg.this.finish();
        }

    }
}
