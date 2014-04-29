package com.ghostflying.grsinformation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

public class GetGrsInfoClass {
	private Boolean D = true;
	private String TAG = this.getClass().getName();
	private String username = DebugInfo.username;
	private String password = DebugInfo.password;
	
	public State state = State.NONE;
	
	
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
	
	public enum State {
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
				getClassesList((Map<String, String>) msg.obj);
				break;
			case PAGE_REQUEST:
				checkIsLoged((Map<String, String>) msg.obj);
				break;
			}
		}
	};
	
	public GetGrsInfoClass () {
		
	}

	
	public boolean preLogin() {
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
	
	private boolean getClassesList (Map<String,String> logedPara){
		if (requestThread == null) {
			sessionID = logedPara.get("cookie");
			requestThread = new RequestThread (CLASS_LIST_URL);
			requestThread.start();
			return true;
		}
		return false;
	}
	
	private boolean checkIsLoged (Map<String,String> logedPara) {
		
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
				break;
			case LOGIN_REQUEST:
				returnValue.put("reDirectUrl", reDirectUrl);
				break;
			case TICKET_REQUEST:
				returnValue.put("cookie", returnCookie);
				break;
			case PAGE_REQUEST:
				returnValue.put("content", body);
				break;
			}
			returnMessage = Message.obtain(RequestCallback, returnType, returnValue);
			returnMessage.sendToTarget();	
			requestThread = null;
			if (D) {
				Log.d(TAG, "requestThread exit.");
			}	
		}
	}
}
