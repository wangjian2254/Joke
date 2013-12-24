package com.wj.joke.db;

import com.wj.joke.staticvalue.Convert;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ActiveUserHelper extends SQLiteOpenHelper {
    private static final String DB_NAME=Convert.DATABASE_NAME;
    public static final String key="id";
    public ActiveUserHelper(Context context) {
        super(context, DB_NAME, null, Convert.DATABASE_VERSON);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO 创建必要表


        db.execSQL("CREATE TABLE jokeread (id INTEGER PRIMARY KEY ,jid INTEGER);");
        db.execSQL("insert into jokeread (id,jid) values (1,0);");
        db.execSQL("CREATE TABLE joke (id INTEGER PRIMARY KEY AUTOINCREMENT,jid TEXT ,type TEXT,content TEXT,image TEXT,updateTime TEXT);");
        update(db);


//		db.execSQL("CREATE TABLE content (id INTEGER PRIMARY KEY AUTOINCREMENT,code TEXT,maincode TEXT,username TEXT,content TEXT,title TEXT,updateSpanTime TEXT,status TEXT,level TEXT,father TEXT,replyType TEXT,type TEXT,lastUpdateTime TEXT);");
//		db.execSQL("create table app (id INTEGER PRIMARY KEY AUTOINCREMENT,code TEXT,appid TEXT,url TEXT);");
//		db.execSQL("CREATE TABLE photo (id INTEGER PRIMARY KEY AUTOINCREMENT,code TEXT,lib TEXT,type TEXT,username TEXT,isDown INTEGER);");
    }
    public void update(SQLiteDatabase db){
        db.execSQL("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT,password TEXT);");

        db.execSQL("CREATE TABLE appverson (id TEXT PRIMARY KEY ,newverson TEXT,uri TEXT,ADTime INTEGER);");
        db.execSQL("insert into appverson (id,newverson,ADTime) values (1,'"+Convert.Verson+"',50);");

        db.execSQL("CREATE TABLE jokeweibo (id INTEGER PRIMARY KEY AUTOINCREMENT,website TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS appverson");
        db.execSQL("DROP TABLE IF EXISTS jokeweibo");
        update(db);
    }

}
