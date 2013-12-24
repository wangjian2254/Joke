package com.wj.joke;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.youmi.android.AdManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.feedback.UMFeedbackService;
import com.mobclick.android.MobclickAgent;
import com.wj.joke.db.ActiveUserHelper;
import com.wj.joke.db.DBhelper;
import com.wj.joke.staticvalue.Convert;
import com.wj.joke.R;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Joke extends Activity  {
	
	private Handler mMainHandler;
	
	public static final int INDEX = Menu.FIRST + 1;
	public static final int RELOGIN = Menu.FIRST + 2;
	public static final int REFASH = Menu.FIRST + 3;
	public static final int ALLREFASH = Menu.FIRST + 4;
	
	List<String> jokelist;
	
	public ArrayAdapter c=null;
	
	DefaultHttpClient client;
	HttpResponse httpResponse;
	HttpGet getMethod = null;
	
	SQLiteDatabase readdb = null;
	SQLiteDatabase wdb = null;
	
	ActiveUserHelper auh = null;
	
	String newverson;
	String downloaduri;
	int ADTime=50;
	
	int total=0;
	
	Joke con=null;
	int current=0;
	int page=20;
	int currentid=1;
	String jokeid=null;
	int lookpage=0;
	int tag=0;
	
	
	Map<String, Map<String, String>> contentmap = new HashMap<String, Map<String, String>>();
//	private LinearLayout mLoadLayout;  
//    private ListView mListView; 
    private final LayoutParams mProgressBarLayoutParams = new LinearLayout.LayoutParams(  
            LinearLayout.LayoutParams.WRAP_CONTENT,  
            LinearLayout.LayoutParams.WRAP_CONTENT);  
    private final LayoutParams mTipContentLayoutParams = new LinearLayout.LayoutParams(  
            LinearLayout.LayoutParams.WRAP_CONTENT,  
            LinearLayout.LayoutParams.WRAP_CONTENT);  
    public ProgressDialog myDialog = null;
    public static final String msgs="正在下载数据，请稍后......";
	public static final String titles="正在同步";
	private String clickjokeId;
	private String clickjokeContent;
	
	private int width=0;
	
	TextView content;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);
        width = getWindowManager().getDefaultDisplay().getWidth();
        setContentView(R.layout.sendjoke);
        content=(TextView)findViewById(R.id.content);
        con=this;
        auh=DBhelper.getDBHelper(con);
        jokelist=new ArrayList<String>();
        boolean ishasjoke=false;
        synchronized (Joke.class) {
    		if(readdb==null||!readdb.isOpen()){
    			readdb = auh.getReadableDatabase();
    		}
    		Cursor jokeread;
    	
    		
    		jokeread= readdb
    				.rawQuery(
    						"select id from joke order by id asc ",
    						null);
    		jokeread.moveToFirst();
    		while (!jokeread.isAfterLast()) {
    			ishasjoke=true;
    			break;
    		}
    		jokeread.close();
    		jokeread= readdb
    		.rawQuery(
    				"select count(*) as num from joke ",
    				null);
    		jokeread.moveToFirst();
    		while (!jokeread.isAfterLast()) {
    			total=jokeread.getInt(0);
    			break;
    		}
    		jokeread.close();
    	}
        if(!ishasjoke){
        	InputStream is;
			try {
				is = con.getAssets().open("datas.xml");
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				   StringBuffer buffer = new StringBuffer();
				   String line = "";
				   while ((line = in.readLine()) != null){
				     buffer.append(line);
				   }
				savecontnet(buffer.toString(),auh);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
			}
        	
        	
        }
        
        
        
        
        
        
        mMainHandler = new Handler() {
        	 
            @Override
            public void handleMessage(Message msg) {
                Log.i("tag", "Got an incoming message from the child thread - "
                        + (String) msg.obj);
                // 接收子线程的消息
//                info.setText((String) msg.obj);
                if(msg.arg1==1){
                	Toast.makeText(con, "500条笑话，下载成功。", 5000).show();
                	return;
                }
                
                if(!"0".equals(msg.obj)){
//                	requestThread.stop();
                	loadJoke(1);
                }else{
                	Toast.makeText(con, "网络不给力，未下载到笑话", 4000).show();
                }
            }
 
        };
        synchronized (Joke.class) {
        if(readdb==null||!readdb.isOpen()){
        	readdb = auh.getReadableDatabase();
        }
        
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
        }
        loadJoke(0);
        AdManager.init(this,"7a94fababaddcc6c", "0eefbedb27072257", ADTime, false);
    }
    
    @Override  
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction()==MotionEvent.ACTION_UP){
    		if((int)event.getX()/(width/3)==0){
    			onPrev(null);
    		}
    		if((int)event.getX()/(width/3)==2){
    			onNext(null);
    		}
    	}
		return true;  
    	
    }
    
    private void loadJoke(int doing){
		try {
			int num=0;
			synchronized (Joke.class) {
				if (readdb == null || !readdb.isOpen()) {
					readdb = auh.getReadableDatabase();
				}

				Cursor appverson;
				if (doing==1) {
					appverson = readdb.rawQuery(
							"select id,jid,content,image from joke where id > "
									+ currentid + " order by id asc ", null);
				} else if(doing==2) {
					appverson = readdb.rawQuery(
							"select id,jid,content,image from joke where id < "
									+ currentid + " order by id desc ", null);
				}else{
					if(currentid<1){
						currentid=1;
					}
					appverson = readdb.rawQuery(
							"select id,jid,content,image from joke where id = "
									+ currentid + " order by id desc ", null);
				}
				appverson.moveToFirst();
		    	
				while (!appverson.isAfterLast()) {
					currentid=appverson.getInt(0);
					jokeid=appverson.getString(1);
					content.setText(appverson.getString(2));
					appverson.moveToNext();
					num=1;
					break;
				}
				appverson.close();
				if(num==0){
					if(doing==1){
						Toast.makeText(con, "笑话没有库存了，正在下载……", 5000).show();
						new ChildThread().start();
					}else if (doing==2){
						Toast.makeText(con, "已经到第一条笑话了。", 5000).show();
					}
				}else{
					savePage(String.valueOf(currentid));
				}
			}
		} catch (Exception t) {

		}
    }
    
    public void onPrev(View view) {
    	loadJoke(2);
    }
    public void onNext(View view) {
    	loadJoke(1);
    }
    public void onSmsSend(View view) {
    	Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
    	intent.putExtra("sms_body", content.getText().toString());
    	startActivity(intent);
    }
    public void onWeiboSend(View view) {
    	try{
    		if(Convert.username==null||Convert.userpwd==null){
        		synchronized (Joke.class) {
            		if(readdb==null||!readdb.isOpen()){
            			readdb = auh.getReadableDatabase();
            		}
            		Cursor jokeread;
            	
            		
            		jokeread= readdb
            				.rawQuery(
            						"select username,password from user",
            						null);
            		jokeread.moveToFirst();
            		while (!jokeread.isAfterLast()) {
            			Convert.username=jokeread.getString(0);
            			Convert.userpwd=jokeread.getString(1);
            			break;
            		}
            		jokeread.close();
            		readdb.close();
            	}
        	}
        	if(Convert.username==null&&Convert.userpwd==null){
        		Intent sendJokeIntent=new Intent(this, UserReg.class);
            	this.startActivityForResult(sendJokeIntent,0);
        	}else{
        		Intent sendJokeIntent=new Intent(this, WeiboReg.class);
        		Bundle bundle = new Bundle();  
                bundle.putString("jokeId",jokeid);  
                
                /*将Bundle对象assign给Intent*/ 
                sendJokeIntent.putExtras(bundle);  
            	this.startActivityForResult(sendJokeIntent,0);
        	}
    	}catch(Exception e){
    		e.getMessage();
    	}
    	
    	
    }
    
    
//    private void loadJoke(boolean doing){
//    	try{
//    	int i=0;
//    	synchronized (Joke.class) {
//    		if(readdb==null||!readdb.isOpen()){
//    			readdb = auh.getReadableDatabase();
//    		}
//    		
//    		Cursor appverson;
//    	if(jokelist.size()!=0){
//    		if(doing){
//    			currentid=Integer.valueOf(jokelist.get(jokelist.size()-1));
//    		}else{
//    			currentid=Integer.valueOf(jokelist.get(0));
//    		}
//    	}
//    	
//    		if(doing){
//    			if(jokelist.size()!=0){
//    				currentid+=1;
//    			}
//    			appverson= readdb
//    			.rawQuery(
//    					"select id,jid,content,image from joke where id >= "+currentid+" order by id asc ",
//    					null);
//    		}else{
//    			appverson= readdb
//    			.rawQuery(
//    					"select id,jid,content,image from joke where id < "+currentid+" order by id desc ",
//    					null);
//    		}
//    		
//    	
//    	appverson.moveToFirst();
//    	
//		while (!appverson.isAfterLast()) {
//			Map<String, String> content;
//			if (!contentmap.containsKey(appverson.getString(0))) {
//				content = new HashMap<String, String>();
//
//			} else {
//				content = contentmap.get(appverson.getString(0));
//			}
//			content.put("id", appverson.getString(0));
//			content.put("jid", appverson.getString(1));
//			content.put("content", appverson.getString(2));
//			content.put("image", appverson.getString(3));
//			contentmap.put( appverson.getString(0),content);
//			if(doing){
//				jokelist.add(appverson.getString(0));
//			}else{
//				jokelist.add(0,appverson.getString(0));
//			}
//			if(!doing&&i==0){
//				
//			}
//			i++;
//			if(i>=page){
//				break;
//			}
//			
//			appverson.moveToNext();
//		}
//		appverson.close();
//    	}
//    	Log.e("current", String.valueOf(current));
//    	if(jokelist.size()==0||jokelist.size()==current-tag){
////    		requestThread.start();
//    		new ChildThread().start();
//    	}else{
//    		
//    		if(doing){
//    			if(current<=0){
//    				current=1;
//    			}
////    			mListView.setSelection(current);
////    			savePage(jokelist.get(jokelist.size()-page>0?jokelist.size()-page:0));
//    		}else{
//    			c.notifyDataSetChanged();
//    			setListAdapter(c);
//    			
//    			mListView.setSelection(i+tag);
//    			savePage(jokelist.get(0));
//    		}
//    		
//    		
//    	}
//    	} catch (Exception t) {
//			Log.e("E", t.getMessage() == null ? "" : t.getMessage());
////			Log.e("request", uri);
//		}
//    	
//    }
    public void savePage(String pid){
    	ContentValues activeuser = new ContentValues();
    	activeuser.put("jid", pid);
    	if(wdb==null||!wdb.isOpen()){
			wdb = auh.getWritableDatabase();
		}
		synchronized (Joke.class) {
			wdb.update("jokeread", activeuser, "id=1",
				null);
		}
		Toast.makeText(con, currentid+" / "+total, 1000).show();
    }
    
    private int request(boolean b) {
    	int newcount=0;
    	String uri=Convert.hosturl+"/InfoUpdate?";
		Log.e("url", uri);
		if(b){
			if(jokelist.size()>0){
				uri+="limit=100&joke="+jokeid;
			}
		}else{
			synchronized (Joke.class) {
	    		if(readdb==null||!readdb.isOpen()){
	    			readdb = auh.getReadableDatabase();
	    		}
	    		
	    		Cursor appverson;
	    		appverson= readdb
    			.rawQuery(
    					"select id,jid from joke  order by id desc ",
    					null);
	    		appverson.moveToFirst();
	    		while (!appverson.isAfterLast()) {
	    			uri+="limit=500&joke="+appverson.getString(1);
	    			break;
	    		}
	    		appverson.close();
			}
		}
		
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
				if (!"1".equals(strResult)) {
					Log.e("request", "3");
					newcount = savecontnet(strResult, auh);
					Log.e("request", "4");
				}
			}
		}  catch (Exception t) {
			Log.e("E", t.getMessage() == null ? "" : t.getMessage());
			Log.e("request", uri);
		} finally {
		}
		return newcount;

	}
    private int savecontnet(String strResult, ActiveUserHelper auh){
    	int newcount=0;
    	try {
		long t1 = System.currentTimeMillis();
		// 返回的xml

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = factory.newDocumentBuilder();
		Document document = db.parse(new java.io.ByteArrayInputStream(strResult
				.getBytes()));

		Element rootElement = document.getDocumentElement();
		long t2 = System.currentTimeMillis();
		synchronized (Joke.class) {
			NodeList nodeList = rootElement.getElementsByTagName("data");
			if(wdb==null||!wdb.isOpen()){
				wdb = auh.getWritableDatabase();
			}
			
			wdb.beginTransaction();
			if(rootElement.hasAttribute("aPhoneVerson")||rootElement.hasAttribute("aPhoneURI")){
				ContentValues appverson = new ContentValues();
				String verson=rootElement.getAttribute("aPhoneVerson");
				String download=rootElement.getAttribute("aPhoneURI");
				String ADTime=rootElement.getAttribute("ADTime");
				appverson.put("newverson", verson);
				appverson.put("uri", download);
				appverson.put("ADTime", ADTime);
				wdb.update("appverson", appverson, "id=1",
						null);
			}
			InsertHelper ih = new InsertHelper(wdb, "joke");
			final int jidnum = ih.getColumnIndex("jid");
			final int contentnum = ih.getColumnIndex("content");
			final int imagenum = ih.getColumnIndex("image");
			final int typenum = ih.getColumnIndex("type");
			final int updateTimenum = ih.getColumnIndex("updateTime");
			String content;
				for (int i = 0; i < nodeList.getLength(); i++) {

					Element data = (Element) nodeList.item(i);
							ih.prepareForInsert();
							ih.bind(jidnum, data.getAttribute("jid"));
							ih.bind(typenum, data.getAttribute("type"));
							ih.bind(updateTimenum, data.getAttribute("updateTime"));
							if(data.hasAttribute("image")){
								ih.bind(imagenum, data.getAttribute("image"));
							}
							content="";
							for(int m=0;m<data.getChildNodes().getLength();m++){
								content+=data.getChildNodes().item(m).getNodeValue();
							}
							ih.bind(contentnum,content);
							ih.execute();
							newcount++;
					}
				}
			wdb.setTransactionSuccessful();
		long t3 = System.currentTimeMillis();
		Log.i("One time", String.valueOf(t2 - t1));
		Log.i("Two time", String.valueOf(t3 - t2));
		} catch (Exception t) {
			Log.e("E", t.getMessage() == null ? "" : t.getMessage());
			Log.e("DB", "InfoSync-438");
		} finally {
			wdb.endTransaction();
		}
		wdb.close();
		total+=newcount;
		return newcount;
	}
    class ChildThread extends Thread {
    	 
//        private static final String CHILD_TAG = "ChildThread";
 
        public void run() {
            this.setName("ChildThread");
 
            //初始化消息循环队列，�?��在Handler创建之前
            Message toMain = mMainHandler.obtainMessage();
            toMain.obj = ""+request(true);

            mMainHandler.sendMessage(toMain);
        }
    }
    class mainDownloadThread extends Thread {
    	
//        private static final String CHILD_TAG = "ChildThread";
    	
    	public void run() {
    		this.setName("mainDownloadThread");
//    		Toast.makeText(con, "程序正在后台 拼命的 下载……", 200).show();
    		//初始化消息循环队列，�?��在Handler创建之前
    		Message toMain = mMainHandler.obtainMessage();
    		toMain.obj = ""+request(false);
    		toMain.arg1=1;
    		
    		mMainHandler.sendMessage(toMain);
    	}
    }
//    public void getListData(){
//    	if(c==null){
//    		c=new JokeAdapter();
//    	}
//    	
//    	setListAdapter(c);
//    }
    
//    class JokeAdapter extends ArrayAdapter {
//    	JokeAdapter() {
//			super(Joke.this, R.layout.jokerow, jokelist);
//		}
//
//		public View getView(int position, View convertView, ViewGroup parent) {
//			View row = convertView;
//			if (row == null) {
//				LayoutInflater inflater = getLayoutInflater();
//				try{
//					row = inflater.inflate(R.layout.jokerow, parent, false);
//				}catch (Exception t) {
//					Log.e("E", t.getMessage() == null ? "" : t.getMessage());
//					Log.e("DB", "InfoSync-438");
//				}
//				
//			}
//			if(position%10==0&&position!=0){
//				savePage(jokelist.get(position));
//			}
//			
//			TextView label = (TextView) row.findViewById(R.id.label);
//			label.setText(contentmap.get(jokelist.get(position)).get("content"));
////			Toast.makeText(con, "[ "+jokelist.get(position)+" ]", 1000).show();
//			return (row);
//
//		}
//	}
//    
//    public void onListItemClick(ListView parent, View v, int position, long id) {
//    	if(position-tag<jokelist.size()&&position-tag>=0){
//    		clickjokeId=jokelist.get(position-tag);
//        	clickjokeContent=contentmap.get(jokelist.get(position-tag)).get("content");
//        	Intent sendJokeIntent=new Intent(con, SendJoke.class);
//            Bundle bundle = new Bundle();  
//            bundle.putString("jokeId",clickjokeId);  
//            bundle.putString("jokeContent",clickjokeContent);
//            sendJokeIntent.putExtras(bundle);  
//        	con.startActivityForResult(sendJokeIntent,0);
//    		savePage(jokelist.get(position-tag));
//    	}
//    	
//    	if(tag==1&&position==0){
//    		loadJoke(false);
//    	}
//    	if(position-tag==jokelist.size()){
//    		loadJoke(true);
//    	}
//    	
//
//	}

//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 按下的如果是BACK，同时没有重�?//			
//			
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		populateMenu(menu);
	}

	public void populateMenu(Menu menu) {
		menu.add(Menu.NONE, INDEX, Menu.NONE, "官网");
		menu.add(Menu.NONE, RELOGIN, Menu.NONE, "意见反馈");
		menu.add(Menu.NONE, REFASH, Menu.NONE, "再下载500个笑话");
//		menu.add(Menu.NONE, ALLREFASH, Menu.NONE, "同步");
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		populateMenu(menu);
		return (super.onCreateOptionsMenu(menu));
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
	}

	public boolean onContextItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onContextItemSelected(item));
	}
	
	public void goWebsite(){
//		Intent intent = new Intent();
//
//        intent.setAction("android.intent.action.VIEW");
//
//        Uri content_uri_browsers = Uri.parse("http://joke.zxxsbook.com");
//
//        intent.setData(content_uri_browsers);
//
//        intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
//
//        con.startActivity(intent);
        Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://joke.zxxsbook.com"));
		this.startActivity(intent);
	}
	
	public void sendSuggest(){
		Intent email = new Intent(android.content.Intent.ACTION_SEND);
		email.setType("plain/text");
		String[] emailReciver = new String[]{"jokeinlive@gmail.com"};
		String emailSubject = "";
		String emailBody = "\n\n\n您对我的应用有何建议，请发送邮件联系我。\n";

		//设置邮件默认地址
		email.putExtra(android.content.Intent.EXTRA_EMAIL, emailReciver);
		//设置邮件默认标题
		email.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
		//设置要默认发送的内容
		email.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
		//调用系统的邮件系统
		startActivity(Intent.createChooser(email, "请选择邮件发送软件"));
	}

	public boolean applyMenuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case INDEX:
			goWebsite();
//			String[] codearr = { uname };
//			showMainData(codearr);
			return true;
		case RELOGIN:
			UMFeedbackService.openUmengFeedbackSDK(this);
//			new Handler().postDelayed(new Runnable() {
//				public void run() {
//					Intent mainIntent = new Intent(Welcome.this, TMain.class);
//					Welcome.this.startActivity(mainIntent);
//					Welcome.this.finish();
//					overridePendingTransition(android.R.anim.slide_in_left,
//							android.R.anim.slide_out_right);
//				}
//			}, 1500);
			return true;
		case REFASH:
			new mainDownloadThread().start();
			Toast.makeText(con, "程序正在后台 拼命的 下载……", 200).show();
//			refash();
//			Toast.makeText(this, "刷新了当前页面�?", 4000).show();
			return true;
		case ALLREFASH:
//			refash();
//			Toast.makeText(this, "正在同步。（功能尚未�?���?, 4000).show();
			return true;
		}
		return false;
	}
//	@Override
//	public void onScroll(AbsListView view, int firstVisibleItem,
//			int visibleItemCount, int totalItemCount) {
//		// TODO Auto-generated method stub
//		current = firstVisibleItem + visibleItemCount - 1;  
//		lookpage=firstVisibleItem;
////        if (jokelist.size() > mCount) {  
////            mListView.removeFooterView(mLoadLayout);  
////        }  
//	}
//	@Override
//	public void onScrollStateChanged(AbsListView view, int scrollState) {
//		// TODO Auto-generated method stub
//		Log.e("list", "scrollchanged");
//		if (current == jokelist.size()+tag  
//                && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {  
//			loadJoke(true);
//        }
//		if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
//			Toast.makeText(con, "[ "+jokelist.get(current-tag)+" / "+total+" ]", 400).show();
//		}
//		if(mListView.getFirstVisiblePosition()==0&& scrollState == OnScrollListener.SCROLL_STATE_IDLE&&tag==1){
//			loadJoke(false);
//		}
//	}
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}

//	@Override
//	public void onScrollStateChanged(AbsListView view, int scrollState) {
//		// TODO Auto-generated method stub
//		
//	}

}