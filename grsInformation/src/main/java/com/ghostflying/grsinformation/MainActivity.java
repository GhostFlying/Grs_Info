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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
