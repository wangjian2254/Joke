package com.wj.joke;



import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.wj.joke.db.ActiveUserHelper;
import com.wj.joke.db.DBhelper;
import com.wj.joke.staticvalue.Convert;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserReg extends Activity {
    char[] numberChars={'1','2','3','4','5','6','7','8','9','0','a','b','c','d','e','f','g','h','i','g','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','G','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    TextView jokeContent;
    String jokeid;
    String joke;
    EditText username;
    EditText userpwd;
    EditText usernick;

    DefaultHttpClient client;
    HttpResponse httpResponse;

    SQLiteDatabase wdb = null;

    ActiveUserHelper auh = null;

    public Intent sendJokeIntent;
    public Bundle bunde;

    private Handler mMainHandler ;

    UserReg userReg;

//	private Thread requestThread;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userreg);
        auh=DBhelper.getDBHelper(this);
        userReg=this;
        username=(EditText)findViewById(R.id.name);
        usernick=(EditText)findViewById(R.id.nick);

//        username.setKeyListener(new NumberKeyListener() {
//			
//			@Override
//			public int getInputType() {
//				// TODO Auto-generated method stub
//				return 0;
//			}
//			
//			@Override
//			protected char[] getAcceptedChars() {
//				// TODO Auto-generated method stub
//				return numberChars;
//			}
//		});
        userpwd=(EditText)findViewById(R.id.pwd);
//        userpwd.setKeyListener(new NumberKeyListener() {
//					
//					@Override
//					public int getInputType() {
//						// TODO Auto-generated method stub
//						return 0;
//					}
//					
//					@Override
//					protected char[] getAcceptedChars() {
//						// TODO Auto-generated method stub
//						return numberChars;
//					}
//				});
        sendJokeIntent=this.getIntent();
//        bunde = sendJokeIntent.getExtras();
//        jokeid=bunde.getString("jokeId"); 
//        joke=bunde.getString("jokeContent"); 
//        jokeContent.setText(joke);
        mMainHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // 接收子线程的消息
                if("1".equals(msg.obj)){
//                	requestThread.stop();
//                	loadJoke(true);
                    try{
                        Intent sendJokeIntent=new Intent(userReg, WeiboReg.class);
                        userReg.startActivityForResult(sendJokeIntent,0);
                    }catch(Exception e){
                        e.getMessage();
                    }

                }else{
                    Toast.makeText(UserReg.this, msg.obj.toString(), 4000).show();
                }
            }

        };
    }
    public boolean checkUserName(String userName) {
        String regex = "([a-z]|[A-Z]|[0-9])+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(userName);
        return m.matches();
    }
    private String request() {
        if("".equals(username.getText().toString())||"".equals(userpwd.getText().toString())){
            return "用户名 和 密码 不能为空。";
        }
        if(!checkUserName(username.getText().toString())||!checkUserName(userpwd.getText().toString())){
            return "用户名 和 密码 只能以数字和字母组成。";
        }
        String uri=null;
        try {
            uri =Convert.hosturl+ "/Reg?username="+username.getText().toString()+"&userpwd="+userpwd.getText().toString()+"&usernick="+URLEncoder.encode(usernick.getText().toString(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                if ("1".equals(strResult)) {
                    ContentValues appverson = new ContentValues();
                    appverson.put("username", username.getText().toString());
                    appverson.put("password", userpwd.getText().toString());
                    Convert.username=username.getText().toString();
                    Convert.userpwd=userpwd.getText().toString();
                    synchronized (Joke.class) {
                        if(wdb==null||wdb.isOpen()==false){
                            wdb=auh.getWritableDatabase();
                        }
                        wdb.insert("user", null,appverson);
                    }
                    return "1";

                }
                return strResult;
            }
        }  catch (Exception t) {
            Log.e("E", t.getMessage() == null ? "" : t.getMessage());
            Log.e("request", uri);
            return "网络不给力，请检查网络状况。";
        }
        return "";

    }
    public void onSend(View view) {
        new Thread(){
            public void run(){
                //注册
                Message toMain = mMainHandler.obtainMessage();
                toMain.obj = ""+request();

                mMainHandler.sendMessage(toMain);
            }
        }.start();
    }

    public void onWeiboSend(View view) {

    }
    public void onBack(View view) {
        if(sendJokeIntent==null){
            sendJokeIntent=new Intent(UserReg.this, Joke.class);
        }
//    	SendJoke.this.startActivity(sendJokeIntent); 
        UserReg.this.setResult(RESULT_OK, sendJokeIntent);

        UserReg.this.finish();
    }
}
