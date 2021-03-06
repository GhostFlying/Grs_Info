package com.ghostflying.grsinformation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Course implements Serializable{
	
	String name = null;
	String teacher = null;
	String courseNum;
	ArrayList<EachClass> classes = new ArrayList<EachClass>();
	
	
	public class EachClass implements Serializable{
		String location = null;
		String freStr = null;
		String dayStr = null;
		String semesterStr = null;
		String classStr;

		Semester semester = null;
		int day = -1;
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
		
		public String dayToStr () {
			String[] daysOfWeek = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
			return daysOfWeek[day];
		}
		
		public String semesterToStr () {
			String[] semesters = {"春学期", "夏学期", "秋学期", "冬学期", "春夏学期", "秋冬学期", "其它"};
			return semesters[semester.ordinal()];
		}
		
		public void dayToInteger () {
			switch (dayStr) {
			case "星期日":
				day = 0;
				break;
			case "星期一":
				day = 1;
				break;
			case "星期二":
				day = 2;
				break;
			case "星期三":
				day = 3;
				break;
			case "星期四":
				day = 4;
				break;
			case "星期五":
				day = 5;
				break;
			case "星期六":
				day = 6;
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
	
	
	enum Frequent{
		EVERY_WEEK, ODD_WEEK, EVEN_WEEK
	}
	
	public boolean convertData () {
		for (EachClass e: classes) {
			e.semesterToEnum();
			e.freToEnum();
			e.dayToInteger();
			e.classStrToInt();
		}
		
		return true;
	}
	
	public static String freToStr (Frequent fre) {
		String freS = "";
		switch (fre){
		case EVERY_WEEK:
			freS = "每周";
			break;
		case ODD_WEEK:
			freS = "单周";
			break;
		case EVEN_WEEK:
			freS = "双周";
			break;
		}
		return freS;
	}
	
}
