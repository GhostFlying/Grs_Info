package com.ghostflying.grsinformation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ClassesListDbHelper extends SQLiteOpenHelper {

	public ClassesListDbHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String createCoursesSql = "CREATE TABLE courses (id VARCHAR PRIMARY KEY, name VARCHAR," +
					"teacher VARCHAR)";
		String createClassesSql = "CREATE TABLE classes (id VARCHAR PRIMARY KEY, location VARCHAR," +
					"dayofweek INTEGER, semester INTEGER, start INTEGER, end INTEGER, fre INTEGER)";
		
		db.execSQL(createClassesSql);
		db.execSQL(createCoursesSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
