package com.ghostflying.grsinformation;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ghostflying.grsinformation.Course.Semester;
import com.ghostflying.grsinformation.GetGrsInfoClass.DataChangeListener;
import com.ghostflying.grsinformation.UserInfoSettingFragment.UserInfoSettedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

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
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private int nowSelected = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.root_layout_drawer);

        String[] drawerArray = new String[2];
        drawerArray[0] = "main";
        drawerArray[1] = "login";
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerArray));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());



        mViewPager = (ViewPager) findViewById(R.id.drawer_main);

		queryTodayClasses();
		queryAllCourses(true);

        setUpCoursesView();
	}

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        if (position == nowSelected){
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        }
        switch (position){
            case 0:
                setUpCoursesView();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 1:
                //mOneDayClassesFragment = null;
                //mAllCheckedCoursesFragment = null;
                //SettingPagerAdapter mSettingPagerAdapter = new SettingPagerAdapter(getFragmentManager());
                //mViewPager.setAdapter(mSettingPagerAdapter);
                //getFragmentManager().beginTransaction().replace(R.id.drawer_main, new UserInfoSettingFragment()).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;

        }
        nowSelected = position;
    }

    private void setUpCoursesView() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
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
		todayClasses = mGetGrsInfoClass.getCoursesOfOneDay(calendar.get(Calendar.DAY_OF_WEEK) - 1, Semester.SUMMER);
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

    public class SettingPagerAdapter extends  FragmentPagerAdapter {

        public SettingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment returnFragment = null;
            switch (position) {
                case 0:
                    returnFragment = new UserInfoSettingFragment();
            }
            return  returnFragment;
        }

        @Override
        public int getCount() {
            return 1;
        }
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
			return 2;
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
