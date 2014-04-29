package com.ghostflying.grsinformation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kevinsawicki.http.HttpRequest;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		//new getResponse().execute(null, null, null);
		GetGrsInfoClass mGetGrsInfoClass = new GetGrsInfoClass();
		mGetGrsInfoClass.preLogin();
		

	}
	
/*	private class getResponse extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			HttpRequest request = HttpRequest.post("https://grs.zju.edu.cn/cas/login?service=http%3A%2F%2Fgrs.zju.edu.cn%2Fpy%2Fpage%2Fstudent%2Fgrkcb.htm");
			String res = request.body();
			String cookie = request.header("Set-Cookie");
			Pattern rc = Pattern.compile(".+(?=; Path)");
			Matcher mc = rc.matcher(cookie);
			String cookieFinal = null;
			if (mc.find()) {
				cookieFinal = mc.group();
				Log.d("main", cookieFinal);
			}
			
			Pattern r = Pattern.compile("(?<=value=\").+(?=\" />)");
			Matcher m = r.matcher(res);
			while (m.find()) {
				Log.d("main", m.group());
			}
			m.find();
			String lt = m.group();
			m.find();
			String num = m.group();
			Log.d("main", "lt = " + lt);
			Log.d("main", "number = " + num);
			
			
			Map<String, String> formdata = new HashMap<String, String>();
			formdata.put("username", "21324024");
			formdata.put("password", "123316");
			formdata.put("submit", "");
			formdata.put("lt", lt);
			formdata.put("execution", num);
			formdata.put("_eventId", "submit");
			HttpRequest request2 = HttpRequest.post("https://grs.zju.edu.cn/cas/login?service=http%3A%2F%2Fgrs.zju.edu.cn%2Fpy%2Fpage%2Fstudent%2Fgrkcb.htm");
			request2.followRedirects(false);
			request2.header("Cookie", cookieFinal);
			request2.header("Connection", "keep-alive");
			request2.header("Host", "grs.zju.edu.cn");
			request2.form(formdata);
			String body2 = request2.body();
			int code = request2.code();
			
			String reDirectUrl = request2.header("Location");
			cookie = request2.header("Set-Cookie");
			mc = rc.matcher(cookie);
			if (mc.find()) {
				cookieFinal = cookieFinal + ";" + mc.group();
			}
			
			cookieFinal = cookieFinal + "CASPRIVACY = \"\"";
			
			
			HttpRequest request3 = HttpRequest.get(reDirectUrl);
			request3.followRedirects(false);
			request3.header("Connection", "keep-alive");
			request3.header("Host", "grs.zju.edu.cn");
			request3.header("Cookie", cookieFinal);
			
			String body = request3.body();
			code = request3.code();
			cookie = request3.header("Set-Cookie");
			mc = rc.matcher(cookie);
			if (mc.find()) {
				cookieFinal = mc.group();
			}
			
			HttpRequest request4 = HttpRequest.get("http://grs.zju.edu.cn/py/page/student/grkcb.htm");
			request4.header("Cookie", cookieFinal);
			String result = request4.body();
			
			
			
			
			return null;
		}
		
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			TextView textView = (TextView) rootView
					.findViewById(R.id.section_label);
			textView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

}
