package com.ghostflying.grsinformation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.ghostflying.grsinformation.Course;
import com.ghostflying.grsinformation.Course.EachClass;
import com.ghostflying.grsinformation.Course.Frequent;
import com.ghostflying.grsinformation.Course.Semester;
import com.github.kevinsawicki.http.HttpRequest;

public class GetGrsInfoClass {
	private Boolean D = true;
	private String TAG = this.getClass().getName();
	private String username = "";
	private String password = "";
	
	private State state = State.NONE;
	private UserInfoState userInfoState = UserInfoState.NONE;
    private ModeState mode = ModeState.MONEY;
	private Context context = null;
	private ArrayList<Course> coursesData = null;
    private ArrayList<MoneyLog> moneyLogData = null;
	DataChangeListener mCallback;

    final String COURSE_LOGIN_REQUEST_URL = "https://grs.zju.edu.cn/cas/login?service=http%3A%2F%2Fgrs.zju.edu.cn%2Fpy%2Fpage%2Fstudent%2Fgrkcgl.htm";
	final String MONEY_LOG_LOGIN_REQUEST_URL = "https://grs.zju.edu.cn/cas/login?service=http%3A%2F%2Fgrs.zju.edu.cn%2Fgangzhu%2Fstudent%2FlogQuery.htm";
	final String CLASS_LIST_URL = "http://grs.zju.edu.cn/py/page/student/grkcgl.htm";
    final String MONEY_LOG_URL = "http://grs.zju.edu.cn/gangzhu/student/logQuery.htm";
	final String DB_NAME = "courses.db";
	final int DB_VERSION = 1;
	final int PRE_LOGIN_REQUEST = 1;
	final int LOGIN_REQUEST = 2;
	final int TICKET_REQUEST = 3;
	final int PAGE_REQUEST = 4;
	final int PASSWORD_ERR = 5;
	
	private RequestThread requestThread = null; 
	
	private String sessionID = null;
	
	private enum RequestMethod {
		POST, GET
	}
	
	private enum State {
		NONE, LOGED, DONE
	}
	
	private enum UserInfoState {
		SETTED, NONE
	}

    private enum ModeState {
        COURSE, MONEY, BOTH
    }
	
	private Handler RequestCallback = new Handler() {
		public void handleMessage (Message msg) {
			switch (msg.what) {
			case PRE_LOGIN_REQUEST:
				doLogin((Map<String, String>) msg.obj);
				break;
			case LOGIN_REQUEST:
				sendTicket((Map<String, String>) msg.obj);
				break;
			case TICKET_REQUEST:
				getLogedSession((Map<String, String>) msg.obj);
				break;
			case PAGE_REQUEST:
                if (mode == ModeState.COURSE) {
                    coursesData = (ArrayList<Course>) msg.obj;
                }
                else {
                    moneyLogData = (ArrayList<MoneyLog>) msg.obj;
                }
                storeCoursesList ();
				break;
			case PASSWORD_ERR:
				Toast.makeText(context, "用户名/密码错误，情检查", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	public GetGrsInfoClass (Context context) {
		this.context = context;
		mCallback = (DataChangeListener) context;
	}
	
	public boolean checkUserInfo () {
		SharedPreferences pre = context.getSharedPreferences("user", Activity.MODE_PRIVATE);
		username = pre.getString("username", "");
		password = pre.getString("password", "");
		if (password.length() > 0) {
			userInfoState = UserInfoState.SETTED;
			return true;
		}
		return false;
	}
	
	public interface DataChangeListener {
		public void onDbChanged ();
	}
	
	public boolean getClassesList() {
        mode = ModeState.COURSE;
		if (state == State.LOGED) {
			if (requestThread == null) {
				requestThread = new RequestThread (CLASS_LIST_URL);
				requestThread.start();
			}
			else {
				Log.e(TAG, "Thread is running.");
			}
		}
		else {
			preLogin();
		}
		
		return false;
	}

    public boolean getMoneyLog() {
        if (mode == ModeState.COURSE) {
            mode = ModeState.MONEY;
        }
        if (state == State.LOGED) {
            if (requestThread == null) {
                HashMap<String, String> formData = new HashMap<>();
                formData.put("year-begin", "2010");
                formData.put("month-begin", "1");
                formData.put("year-end", "2014");
                formData.put("month-end", "12");
                formData.put("submit-query", "查　　询");
                requestThread = new RequestThread (PAGE_REQUEST, MONEY_LOG_URL, RequestMethod.POST, sessionID, formData);
                requestThread.start();
            }
            else {
                Log.e(TAG, "Thread is running.");
            }
        }
        else {
            preLogin();
        }
        return false;
    }

    public void getAllInfo() {
        mode = ModeState.BOTH;
        getMoneyLog();
    }
	
	
	private boolean storeCoursesList () {
		CoursesListDbHelper dbHelper = new CoursesListDbHelper(context, DB_NAME, null, 1);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CoursesListDbHelper.COURSES_TABLE_NAME, null, null);
        db.delete(CoursesListDbHelper.CLASSES_TABLE_NAME, null, null);
        db.delete(CoursesListDbHelper.MONEY_LOG_TABLE_NAME, null, null);
		ContentValues cv = new ContentValues();
        if (mode == ModeState.COURSE) {
            for (Course c : coursesData){
                cv.clear();
                cv.put("id", c.courseNum);
                cv.put("teacher", c.teacher);
                cv.put("name", c.name);
                db.replace(CoursesListDbHelper.COURSES_TABLE_NAME, null, cv);
                for (Course.EachClass each : c.classes) {
                    cv.clear();
                    cv.put("c_id", c.courseNum + c.classes.indexOf(each));
                    cv.put("id", c.courseNum);
                    cv.put("semester", each.semester.ordinal());
                    cv.put("location", each.location);
                    cv.put("fre", each.fre.ordinal());
                    cv.put("start", each.startClass);
                    cv.put("end", each.endClass);
                    cv.put("dayofweek", each.day);
                    db.replace(CoursesListDbHelper.CLASSES_TABLE_NAME, null, cv);
                }
            }
        }
        else {
            for (MoneyLog log : moneyLogData) {
                cv.clear();
                cv.put("teacherName", log.teacherName);
                cv.put("teacherCount", log.teacherCount);
                cv.put("teacherCount2", log.teacherCOunt2);
                cv.put("teacherState", log.teacherState);
                cv.put("uniCount", log.uniCount);
                cv.put("uniCount2", log.uniCount2);
                cv.put("uniState", log.uniState);
                cv.put("time", log.time);
                cv.put("change", log.change);
                cv.put("summary", log.summary);
                cv.put("ps", log.ps);
                db.insert(CoursesListDbHelper.MONEY_LOG_TABLE_NAME, null, cv);
            }
        }
		db.close();
		state = State.DONE;
        if (mode == ModeState.BOTH) {
            mode = ModeState.COURSE;
            getClassesList();
        }
        else {
            mode = ModeState.MONEY;
            mCallback.onDbChanged();
        }
		return false;
	}

	
	public ArrayList<HashMap <String, Object>> getCoursesOfOneDay (int day, Semester semester) {
		ArrayList<HashMap <String, Object>> classesOneDay = new ArrayList<HashMap <String, Object>>();	
		Cursor coursesCursor = null;
		Cursor classesCursor = null;
		
		CoursesListDbHelper dbHelper = new CoursesListDbHelper(context, DB_NAME, null, 1);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] sectionStr = new String[2];
		sectionStr[0] = Integer.toString(day);
		sectionStr[1] = Integer.toString(semester.ordinal());
		classesCursor = db.query(CoursesListDbHelper.CLASSES_TABLE_NAME, 
				null, "dayofweek = ? AND semester = ?", sectionStr, null, null, "start");
		
		while (classesCursor.moveToNext()) {
			String[] classSectionStr = new String[1];
			classSectionStr[0] = classesCursor.getString(0);
			coursesCursor = db.query(CoursesListDbHelper.COURSES_TABLE_NAME, 
					null, "id = ?", classSectionStr, null, null, null);
			HashMap <String, Object> oneClass = new HashMap <String, Object>();
			
			if (coursesCursor.moveToFirst()) {
				oneClass.put("name", coursesCursor.getString(1));
				oneClass.put("teacher", coursesCursor.getString(2));
			}
			coursesCursor.close();
			
			oneClass.put("location", classesCursor.getString(1));
			oneClass.put("start", classesCursor.getInt(4));
			oneClass.put("end", classesCursor.getInt(5));
			oneClass.put("fre", Frequent.values()[classesCursor.getInt(6)]);
			
			classesOneDay.add(oneClass);
		}
		
		classesCursor.close();
		db.close();
		return classesOneDay;
	}
	
	public ArrayList<Course> getAllCoursesList (boolean checked) {
		if (state == State.DONE && !checked) {
			return coursesData;
		}
		else {
			return getAllCoursesFromDb(checked);
		}
	}
	
	private ArrayList<Course> getAllCoursesFromDb (boolean checked){
		ArrayList <Course> coursesFromDb = new ArrayList<Course>();
		Cursor coursesCursor = null;
		Cursor classesCursor = null;
		
		CoursesListDbHelper dbHelper = new CoursesListDbHelper(context, DB_NAME, null, 1);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		coursesCursor = db.query(CoursesListDbHelper.COURSES_TABLE_NAME, 
				null, null, null, null, null, null);
		while (coursesCursor.moveToNext()) {
			Course mCourse = new Course();
			mCourse.courseNum = coursesCursor.getString(0);
			mCourse.name = coursesCursor.getString(1);
			mCourse.teacher = coursesCursor.getString(2);
			String[] sectionStr = new String[1];
			sectionStr[0] = mCourse.courseNum;
			classesCursor = db.query(CoursesListDbHelper.CLASSES_TABLE_NAME, 
					null, "id = ?", sectionStr, null, null, null);
			while (classesCursor.moveToNext()) {
				EachClass each = mCourse.new EachClass();
				each.location = classesCursor.getString(1);
				each.day = classesCursor.getInt(2);
				each.semester = Semester.values()[classesCursor.getInt(3)];
				each.startClass = classesCursor.getInt(4);
				each.endClass = classesCursor.getInt(5);
				each.fre = Frequent.values()[classesCursor.getInt(6)];	
				mCourse.classes.add(each);
			}
			classesCursor.close();
			if (!(classesCursor.getCount() < 1 && checked)) {
				coursesFromDb.add(mCourse);
			}
		}
		coursesCursor.close();
		db.close();
		return coursesFromDb;
		
	}
	
	private boolean preLogin() {
        String url;
        if (mode == ModeState.COURSE) {
            url = COURSE_LOGIN_REQUEST_URL;
        }
        else {
            url = MONEY_LOG_LOGIN_REQUEST_URL;
        }
		if (requestThread == null) {
			requestThread = new RequestThread (PRE_LOGIN_REQUEST, url, RequestMethod.POST);
			requestThread.start();
		}		
		return false;
	}
	
	private boolean doLogin(Map<String,String> hiddenPara) {
        String url;
        if (mode == ModeState.COURSE) {
            url = COURSE_LOGIN_REQUEST_URL;
        }
        else {
            url = MONEY_LOG_LOGIN_REQUEST_URL;
        }
		if (requestThread == null) {
			Map<String, String> formData = new HashMap<String, String>();
			formData.put("username", username);
			formData.put("password", password);
			formData.put("submit", "");
			formData.put("lt", hiddenPara.get("lt"));
			formData.put("execution", hiddenPara.get("execution"));
			formData.put("_eventId", "submit");
			requestThread = new RequestThread (LOGIN_REQUEST, url, RequestMethod.POST, hiddenPara.get("cookie"), formData);
			requestThread.start();
			return true;
		}		
		return false;		
	}
	
	private boolean sendTicket(Map<String,String> ticketPara) {
		if (requestThread == null) {
			requestThread = new RequestThread (TICKET_REQUEST, ticketPara.get("reDirectUrl"), RequestMethod.GET );
			requestThread.start();
			return true;
		}
		return false;
	}
	

	private boolean getLogedSession (Map<String,String> logedPara){
		sessionID = logedPara.get("cookie");
		state = State.LOGED;
		if (D) {
			Log.d(TAG, "Loged successfully.");
		}
        if (mode == ModeState.COURSE) {
            getClassesList();
        }
        else {
            getMoneyLog();
        }

		return false;
	}
	
	
	
	private String parseCookie (String setCookie) {
		String cookie = null;
		Pattern cookiePattern = Pattern.compile(".+(?=; Path)");
		Matcher cookieMatcher = cookiePattern.matcher(setCookie);
		if (cookieMatcher.find()) {
			cookie = cookieMatcher.group();
			if (D) {
				Log.d(TAG, "get Cookie: " + cookie);
			}			
		}
		return cookie;
	}
	
	private String[] parseHidden (String body) {
		String[] returnArray = new String[2];
		Pattern hiddenPattern = Pattern.compile("(?<=value=\").+(?=\" />)");
		Matcher hiddenMatcher = hiddenPattern.matcher(body);
		for (int i = 0; i<2; i++) {
			hiddenMatcher.find();
			returnArray[i] = hiddenMatcher.group();
		}		
		return returnArray;		
	}
	
	private class RequestThread extends Thread {
		String url = null;
		RequestMethod method;
		Map<String, String> formData = null;
		String cookie = null;
		HttpRequest request = null;
		Map<String, String> returnValue;
		String body = null;
		String reDirectUrl = null;
		String returnCookie = null;
		Message returnMessage = null;
		int returnCode;
		int returnType;
		
		public RequestThread (int requestType, String requestUrl, RequestMethod requestMethod, String requestCookie, Map<String, String> requestForm) {
			url = requestUrl;
			method = requestMethod;
			formData = requestForm;
			cookie = requestCookie;	
			returnType = requestType;
		}
		
		public RequestThread (int requestType, String requestUrl, RequestMethod requestMethod, String requestCookie) {
			url = requestUrl;
			method = requestMethod;
			cookie = requestCookie;	
			returnType = requestType;
		}
		
		public RequestThread (int requestType, String requestUrl, RequestMethod requestMethod){
			url = requestUrl;
			method = requestMethod;
			returnType = requestType;
		}
		
		public RequestThread (String requestUrl) {
			url = requestUrl;			
			cookie = sessionID;
			method = RequestMethod.GET;
			returnType = PAGE_REQUEST;
		}
		
		public synchronized void run() {
			if (D) {
				Log.d(TAG, "requestThread run.");
			}			
			switch (method) {
			case POST:
				request = HttpRequest.post(url);
				break;
			case GET:
				request = HttpRequest.get(url);
				break;
			}
			request.followRedirects(false);
			if (cookie != null) {
				request.header("Cookie", cookie);
			}
			if (formData != null) {
				request.form(formData);
			}
			returnCode = request.code();
			if (D) {
				Log.d(TAG, "Return Code: " + String.valueOf(returnCode));
			}

            body = request.body();

			switch (returnCode) {
			case 200:
				break;
			case 302:
				reDirectUrl = request.header("Location");
				break;
			case 500:
				Log.e(TAG, "Error 500.\n" + request.body());
				break;
			default:
				Log.e(TAG, "Unknown error.\n" + String.valueOf(returnCode) + "\n" + request.body());
			}
			
			if (returnType == PRE_LOGIN_REQUEST && returnCode != 200){
				state = GetGrsInfoClass.State.NONE;
				Log.e(TAG, "Error when preLogin.");
				return;
			}
			
			if (returnType == LOGIN_REQUEST && returnCode != 302) {
				state = GetGrsInfoClass.State.NONE;
				Log.e(TAG, "Error when login request.");
				if (returnCode == 200) {
					returnMessage = Message.obtain(RequestCallback, PASSWORD_ERR, null);
					returnMessage.sendToTarget();	
				}
				return;
			}
			
			
			if (returnType == TICKET_REQUEST && returnCode != 302) {
				state = GetGrsInfoClass.State.NONE;
				Log.e(TAG, "Error when post ticket.");
				return;
			}
			
			if (returnType == PAGE_REQUEST && returnCode == 302){
				Log.e(TAG, "Error: Not Login.");
				state = GetGrsInfoClass.State.NONE;
				return;
			} else if (returnType == PAGE_REQUEST && returnCode != 200) {
				Log.e(TAG, "Error when get page.");
				return;
			}
			
			if (returnType == PRE_LOGIN_REQUEST || returnType ==  TICKET_REQUEST) {
				returnCookie = request.header("Set-Cookie");	
				returnCookie = parseCookie(returnCookie);
			}			

			
			returnValue = new HashMap<String, String>();
			switch (returnType) {
			case PRE_LOGIN_REQUEST:
				String[] ltAndExcu = parseHidden(body);
				returnValue.put("lt", ltAndExcu[0]);
				returnValue.put("execution", ltAndExcu[1]);
				returnValue.put("cookie", returnCookie);
				returnMessage = Message.obtain(RequestCallback, returnType, returnValue);
				break;
			case LOGIN_REQUEST:
				returnValue.put("reDirectUrl", reDirectUrl);
				returnMessage = Message.obtain(RequestCallback, returnType, returnValue);
				break;
			case TICKET_REQUEST:
				returnValue.put("cookie", returnCookie);
				returnMessage = Message.obtain(RequestCallback, returnType, returnValue);
				break;
			case PAGE_REQUEST:
                if (mode == ModeState.COURSE) {
                    returnMessage = Message.obtain(RequestCallback, returnType, parseCoursesList(body));
                }
                else {
                    returnMessage = Message.obtain(RequestCallback, returnType, parseMoneyList(body));
                }
				break;
			}
			
					
			requestThread = null;
			returnMessage.sendToTarget();	
			
			if (D) {
				Log.d(TAG, "requestThread exit.");
			}	
		}

        private ArrayList<MoneyLog> parseMoneyList (String body) {
            ArrayList<MoneyLog> returnList = new ArrayList<MoneyLog>();

            String[] logBody = body.split("</tr>");

            Pattern teacherNamePattern = Pattern.compile("(?<=name=\"2\">).+?(?=</td>)");
            Pattern datePattern = Pattern.compile("(?<=name=\"3\">).+?(?=</td>)");
            Pattern teacherCountPattern = Pattern.compile("(?<=name=\"4\">).+?(?=</td>)");
            Pattern teacherCount2Pattern = Pattern.compile("(?<=name=\"5\">).+?(?=</td>)");
            Pattern teacherStatePattern = Pattern.compile("(?<=name=\"6\">).+?(?=</td>)");
            Pattern uniCountPattern = Pattern.compile("(?<=name=\"7\">).+?(?=</td>)");
            Pattern uniCount2Pattern = Pattern.compile("(?<=name=\"8\">).+?(?=</td>)");
            Pattern uniStatePattern = Pattern.compile("(?<=name=\"9\">).+?(?=</td>)");
            Pattern changePattern = Pattern.compile("(?<=name=\"10\">).*?(?=</td>)");
            Pattern summaryPattern = Pattern.compile("(?<=name=\"11\">).*?(?=</td>)");
            Pattern psPattern = Pattern.compile("(?<=name=\"12\">).*?(?=</td>)");
            for (int i = 1; i< logBody.length - 1; i++) {
                MoneyLog mLog = new MoneyLog();
                Matcher m = null;
                m = teacherNamePattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.teacherName = m.group();
                }
                m = datePattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.time = m.group();
                }
                m = teacherCountPattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.teacherCount = m.group();
                }
                m = teacherCount2Pattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.teacherCOunt2 = m.group();
                }
                m = teacherStatePattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.teacherState = m.group();
                }
                m = uniCountPattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.uniCount = m.group();
                }
                m = uniCount2Pattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.uniCount2 = m.group();
                }
                m = uniStatePattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.uniState = m.group();
                }
                m = changePattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.change = m.group();
                }
                m = summaryPattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.summary = m.group();
                }
                m = psPattern.matcher(logBody[i]);
                if (m.find()) {
                    mLog.ps = m.group();
                }
                returnList.add(mLog);
            }
            return  returnList;
        }
		
		private ArrayList<Course> parseCoursesList (String body) {
			ArrayList<Course> courses = new ArrayList<Course>();
			String tableBody = body.substring(body.indexOf("<tbody>"));			
			String[] courseBodys = tableBody.split("<tr class");
			
						
			Pattern courseNumberPattern = Pattern.compile("(?<=</span></td>)\\s+<td>\\d+(?=</td>)", Pattern.DOTALL);
			Pattern courseNamePattern = Pattern.compile("(?<=\"white\">).+(?=</a></td>)");
			Pattern courseLocationAndTimePattern = Pattern.compile("(?<=<td class=\"vat\">).+(?=</td>)");
			Pattern courseSeperaPattern = Pattern.compile(".+(?=<)");
			for (int i = 1; i < courseBodys.length; i++) {
				Course mCourse = new Course ();
				
				Matcher courseNumberMatcher = courseNumberPattern.matcher(courseBodys[i]);
				if (courseNumberMatcher.find()) {
					mCourse.courseNum = courseNumberMatcher.group().substring(11);
				} 
				else {
					Log.e(TAG, "CourseNum Match error at No." + Integer.toString(i));
				}				
				
				Matcher courseNameMatcher = courseNamePattern.matcher(courseBodys[i]);
				if (courseNameMatcher.find()) {
					mCourse.name = courseNameMatcher.group();
				} 
				else {
					Log.e(TAG, "CourseName Match error at No." + Integer.toString(i));
				}
				
				Matcher courseLocationAndTimeMatcher = courseLocationAndTimePattern.matcher(courseBodys[i]);
				if (courseLocationAndTimeMatcher.find()) {
					String locationAndTimeTable = courseLocationAndTimeMatcher.group();
					String[]  locationAndTimes = locationAndTimeTable.split("</span>");
					String semesterStr = null;
					String freStr = null;
					String dayStr = null;
					String classStr = null;
					String location = null;
					for (int j = 1; j < locationAndTimes.length; j++) {
						Matcher courseSeperaMatcher = courseSeperaPattern.matcher(locationAndTimes[j]);
						if (courseSeperaMatcher.find()) {
							String splitStr = courseSeperaMatcher.group().replace("<br>", "");
							if (D) {
								//Log.d("", splitStr);
							}
							switch (j % 4) {
							case 1:
								semesterStr = splitStr.substring(0, splitStr.indexOf('('));
								freStr = splitStr.substring(splitStr.indexOf('(') + 1, splitStr.indexOf(')'));								
								break;
							case 2:
								dayStr = splitStr;
								break;
							case 3:
								classStr = splitStr;
								break;
							case 0:
								location = splitStr;
								mCourse.addOneClass(semesterStr, freStr, dayStr, location, classStr);
								break;
								
							}
						}
					}
				} 
				else {
					Log.d(TAG, "CourseLocationAndTime Match is null at No." + Integer.toString(i));
				}
				
				String[] splitedByTd = courseBodys[i].split("</td>");
				mCourse.teacher = splitedByTd[7].substring(11);
				mCourse.convertData();
				courses.add(mCourse);
			}	
			

			
			return courses;
		}
		

		

	}
}
