package com.ghostflying.grsinformation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import com.ghostflying.grsinformation.Course.Semester;
import com.ghostflying.grsinformation.GetGrsInfoClass.DataChangeListener;
import com.ghostflying.grsinformation.UserInfoSettingFragment.UserInfoSettedListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity 
		implements UserInfoSettedListener, DataChangeListener{

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
	
	
	OneDayClassesFragment mOneDayClassesFragment;
	public static ArrayList<HashMap <String, Object>> todayClasses; 
	public static ArrayList<Course> coursesData;
	GetGrsInfoClass mGetGrsInfoClass;
	AllCheckedCoursesFragment mAllCheckedCoursesFragment;
	
	

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
		
		queryTodayClasses();
		queryAllCourses(true);
		if (!isUserSetted()) {
			mViewPager.setCurrentItem(2, true);
		}		
	}
	
	private boolean isUserSetted () {
		checkQueryClass ();
		if (coursesData.isEmpty()) {
			return false;
		}
		return mGetGrsInfoClass.checkUserInfo();
	}
	
	private void checkQueryClass () {
		if (mGetGrsInfoClass == null) {
			mGetGrsInfoClass = new GetGrsInfoClass(this);
		}
	}
	

	@Override
	public void onDbChanged() {
		// TODO Auto-generated method stub		
		queryTodayClasses();
		queryAllCourses(true);
	}
	
	@Override
	public void onUserInfoSetted() {
		// TODO Auto-generated method stub
		checkQueryClass ();
		if (mGetGrsInfoClass.checkUserInfo()){
			mGetGrsInfoClass.getClassesList();
		}
		
	}

	
	private void queryTodayClasses() {
		Calendar calendar = Calendar.getInstance();
		checkQueryClass ();		
		todayClasses = mGetGrsInfoClass.getCoursesOfOneDay(calendar.get(7) - 1, Semester.SUMMER);
		//((OneDayClassesFragment)mSectionsPagerAdapter.getItem(0)).dataUpdated(todayClasses);
		if (mOneDayClassesFragment != null) {
			mOneDayClassesFragment.dataUpdated(todayClasses);
		}
		
	}
	
	private void queryAllCourses(boolean checked) {
		checkQueryClass ();	
		coursesData = mGetGrsInfoClass.getAllCoursesList(checked);
		//((AllCheckedCoursesFragment)mSectionsPagerAdapter.getItem(1)).dataUpdated(coursesData);
		
		
		if (mAllCheckedCoursesFragment != null && !mAllCheckedCoursesFragment.isDetached()) {
			mAllCheckedCoursesFragment.dataUpdated(coursesData);
		}
	}

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
			switch (position) {
			case 0:
				mOneDayClassesFragment = OneDayClassesFragment.newInstance(todayClasses);
				return mOneDayClassesFragment;
			case 1:
				mAllCheckedCoursesFragment = AllCheckedCoursesFragment.newInstance(coursesData);
				return mAllCheckedCoursesFragment;
			case 2:
				return new UserInfoSettingFragment();
			}
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
