package com.ghostflying.grsinformation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoursesListDbHelper extends SQLiteOpenHelper {
	
	public final static String COURSES_TABLE_NAME = "courses";
	public final static String CLASSES_TABLE_NAME = "classes";
    public final static String MONEY_LOG_TABLE_NAME = "log";

	public CoursesListDbHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String createCoursesSql = "CREATE TABLE " + COURSES_TABLE_NAME + " (id VARCHAR PRIMARY KEY," +
					"name VARCHAR, teacher VARCHAR)";
		String createClassesSql = "CREATE TABLE " + CLASSES_TABLE_NAME + " (id VARCHAR ," +
					"location VARCHAR, dayofweek INTEGER, semester INTEGER, start INTEGER, end INTEGER, "
					+ "fre INTEGER, c_id VARCAHR PRIMARY KEY)";
		String createLogSql = "CREATE TABLE " + MONEY_LOG_TABLE_NAME + " (teacherName VARCHAR ," +
                    "time VARCHAR, teacherCount VARCHAR, teacherCount2 VARCHAR, teacherState VARCHAR, " +
                    "uniCount VARCHAR, uniCount2 VARCHAR, uniState VARCHAR, change VARCHAR, " +
                    "summary VARCHAR, ps VARCHAR)";
		db.execSQL(createClassesSql);
		db.execSQL(createCoursesSql);
        db.execSQL(createLogSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
