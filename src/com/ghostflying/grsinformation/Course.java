package com.ghostflying.grsinformation;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Course {
	
	String name = null;
	String teacher = null;
	String courseNum;
	ArrayList<EachClass> classes = new ArrayList<EachClass>();
	
/*	String location = null;
	String freStr = null;
	String dayStr = null;
	String semesterStr = null;
	String classStr;

	Semester semester = null;
	DayOfTheWeek day = null;
	Frequent fre = null;
	int startClass = -1;
	int endClass = -1;*/
	
	
	public class EachClass {
		String location = null;
		String freStr = null;
		String dayStr = null;
		String semesterStr = null;
		String classStr;

		Semester semester = null;
		DayOfTheWeek day = null;
		Frequent fre = null;
		int startClass = -1;
		int endClass = -1;
		
		public void classStrToInt () {
			Pattern classStrPattern = Pattern.compile("\\d");
			Matcher classStrMatcher = classStrPattern.matcher(classStr);
			classStrMatcher.find();
			startClass = Integer.valueOf(classStrMatcher.group());
			classStrMatcher.find();
			endClass = Integer.valueOf(classStrMatcher.group());
			
		}
		
		public void semesterToEnum () {
			switch (semesterStr) {
			case "春":
				semester = Semester.SPRING;
				break;
			case "夏":
				semester = Semester.SUMMER;
				break;
			case "秋":
				semester = Semester.AUTUMN;
				break;
			case "冬":
				semester = Semester.WINTER;
				break;
			}
		}
		
		public void freToEnum () {
			switch (freStr) {
			case "每周":
				fre = Frequent.EVERY_WEEK;
				break;
			case "单周":
				fre = Frequent.ODD_WEEK;
				break;
			case "双周":
				fre = Frequent.EVEN_WEEK;
				break;
			}
		}
		
		public void dayToEnum () {
			switch (dayStr) {
			case "星期日":
				day = DayOfTheWeek.SUN;
				break;
			case "星期一":
				day = DayOfTheWeek.MON;
				break;
			case "星期二":
				day = DayOfTheWeek.TUE;
				break;
			case "星期三":
				day = DayOfTheWeek.WED;
				break;
			case "星期四":
				day = DayOfTheWeek.THU;
				break;
			case "星期五":
				day = DayOfTheWeek.FRI;
				break;
			case "星期六":
				day = DayOfTheWeek.SAT;
				break;
			}
		}
	}
	
	public boolean addOneClass (String semesterStr, String freStr, String dayStr, String location, String classStr){
		EachClass mEachClass = new EachClass();
		mEachClass.semesterStr = semesterStr;
		mEachClass.freStr = freStr;
		mEachClass.dayStr = dayStr;
		mEachClass.location = location;
		mEachClass.classStr = classStr;
		
		classes.add(mEachClass);
		return true;
	}
	
	enum Semester {
		SPRING, SUMMER, AUTUMN, WINTER, SPRING_SUMMER, AUTUMN_WINTER, NONE
	}
	
	enum DayOfTheWeek {
		SUN, MON, TUE, WED, THU, FRI, SAT
	}
	
	enum Frequent{
		EVERY_WEEK, ODD_WEEK, EVEN_WEEK
	}
	
	public boolean convertData () {
		for (EachClass e: classes) {
			e.semesterToEnum();
			e.freToEnum();
			e.dayToEnum();
			e.classStrToInt();
		}
		
		return true;
	}
	
}
