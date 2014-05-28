package com.ghostflying.grsinformation;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ghostflying.grsinformation.Course.Semester;
import com.ghostflying.grsinformation.GetGrsInfoClass.DataChangeListener;
import com.ghostflying.grsinformation.UserInfoSettingFragment.UserInfoSettedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends Activity 
		implements UserInfoSettedListener, DataChangeListener{

	//OneDayClassesFragment mOneDayClassesFragment;
	public static ArrayList<HashMap <String, Object>> todayClasses; 
	public static ArrayList<Course> coursesData;
    public static ArrayList<MoneyLog> moneyLogData;
	GetGrsInfoClass mGetGrsInfoClass;
	//AllCheckedCoursesFragment mAllCheckedCoursesFragment;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mDrawerTitle;

    private int nowSelected = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.root_layout_drawer);

        mDrawerTitles = getResources().getStringArray(R.array.titles);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.open_drawer,  /* "open drawer" description */
                R.string.close_drawer  /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mDrawerTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(getString(R.string.app_name));
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

/*        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });*/

		queryTodayClasses();
		queryAllCourses(true);
        queryMoneyLog();
        if (!isUserSetted()) {
            getFragmentManager().beginTransaction().replace(R.id.content_frame, new UserInfoSettingFragment()).commit();
        }
        else {
            getFragmentManager().beginTransaction().replace(R.id.content_frame, OneDayClassesFragment.newInstance(todayClasses)).commit();
        }


	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
        getActionBar().setTitle(mDrawerTitles[position]);
        mDrawerTitle = mDrawerTitles[position];
        switch (position){
            case 0:
                //mOneDayClassesFragment = OneDayClassesFragment.newInstance(todayClasses);
                getFragmentManager().beginTransaction().replace(R.id.content_frame, OneDayClassesFragment.newInstance(todayClasses)).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 1:
                //mAllCheckedCoursesFragment = AllCheckedCoursesFragment.newInstance(coursesData);
                getFragmentManager().beginTransaction().replace(R.id.content_frame, AllCheckedCoursesFragment.newInstance(coursesData)).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 2:
                getFragmentManager().beginTransaction().replace(R.id.content_frame, MoneyLogFragment.newInstance()).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 3:
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new UserInfoSettingFragment()).commit();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
        }
        nowSelected = position;
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
        queryMoneyLog();
        updateFragfmentData ();
	}
	
	@Override
	public void onUserInfoSetted() {
		// TODO Auto-generated method stub
		checkQueryClass ();		
		if (mGetGrsInfoClass.checkUserInfo()){
			//mGetGrsInfoClass.getClassesList();
            mGetGrsInfoClass.getAllInfo();
		}
		
	}

	
	private void queryTodayClasses() {
		Calendar calendar = Calendar.getInstance();
		checkQueryClass ();		
		todayClasses = mGetGrsInfoClass.getCoursesOfOneDay(calendar.get(Calendar.DAY_OF_WEEK) - 1, Semester.SUMMER);
	}
	
	private void queryAllCourses(boolean checked) {
		checkQueryClass ();	
		coursesData = mGetGrsInfoClass.getAllCoursesList(checked);

	}

    private void queryMoneyLog() {
        checkQueryClass();
        moneyLogData = mGetGrsInfoClass.getMoneyLogFromDb();
    }

    private void updateFragfmentData () {
        if (nowSelected == 0) {
            getFragmentManager().beginTransaction().replace(R.id.content_frame, OneDayClassesFragment.newInstance(todayClasses)).commit();
        }
        else if (nowSelected == 1){
            getFragmentManager().beginTransaction().replace(R.id.content_frame, AllCheckedCoursesFragment.newInstance(coursesData)).commit();
        }
        else if (nowSelected == 2) {
            getFragmentManager().beginTransaction().replace(R.id.content_frame, MoneyLogFragment.newInstance()).commit();
        }
        Toast.makeText(this,"信息更新完成", Toast.LENGTH_SHORT).show();
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
