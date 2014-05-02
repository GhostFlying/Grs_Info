package com.ghostflying.grsinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ghostflying.grsinformation.Course;
import com.github.kevinsawicki.http.HttpRequest;

public class GetGrsInfoClass {
	private Boolean D = true;
	private String TAG = this.getClass().getName();
	private String username = DebugInfo.username;
	private String password = DebugInfo.password;
	
	private State state = State.NONE;
	
	
	final String LOGIN_REQUEST_URL = "https://grs.zju.edu.cn/cas/login?service=http%3A%2F%2Fgrs.zju.edu.cn%2Fpy%2Fpage%2Fstudent%2Fgrkcb.htm";
	final String CLASS_LIST_URL = "http://grs.zju.edu.cn/py/page/student/grkcgl.htm";
	final int PRE_LOGIN_REQUEST = 1;
	final int LOGIN_REQUEST = 2;
	final int TICKET_REQUEST = 3;
	final int PAGE_REQUEST = 4;
	
	private RequestThread requestThread = null; 
	
	private String sessionID = null;
	
	private enum RequestMethod {
		POST, GET
	}
	
	private enum State {
		NONE, LOGED
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
				break;
			}
		}
	};
	
	public GetGrsInfoClass () {
		
	}
	
	public boolean getClassesList() {
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
	
	private boolean preLogin() {
		if (requestThread == null) {
			requestThread = new RequestThread (PRE_LOGIN_REQUEST, LOGIN_REQUEST_URL, RequestMethod.POST);
			requestThread.start();
		}		
		return false;
	}
	
	private boolean doLogin(Map<String,String> hiddenPara) {
		if (requestThread == null) {
			Map<String, String> formData = new HashMap<String, String>();
			formData.put("username", username);
			formData.put("password", password);
			formData.put("submit", "");
			formData.put("lt", hiddenPara.get("lt"));
			formData.put("execution", hiddenPara.get("execution"));
			formData.put("_eventId", "submit");
			requestThread = new RequestThread (LOGIN_REQUEST, LOGIN_REQUEST_URL, RequestMethod.POST, hiddenPara.get("cookie"), formData);
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
		getClassesList();
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
			body = request.body();
			
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
			case PAGE_REQUEST:;
				returnMessage = Message.obtain(RequestCallback, returnType, parseCoursesList(body));
				break;
			}
			
					
			requestThread = null;
			returnMessage.sendToTarget();	
			
			if (D) {
				Log.d(TAG, "requestThread exit.");
			}	
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
