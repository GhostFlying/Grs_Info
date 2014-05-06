package com.ghostflying.grsinformation;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserInfoSettingFragment extends Fragment {
	EditText usernameEdit;
	EditText passwordEdit;
	Button saveButton;
	OnClickListener saveUserInfo;
	UserInfoSettedListener mCallback;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.userinfo_setting_fragment_layout, container, false);
		initView(rootView);	
		return rootView;
	}
	
	private void initView (View rootView) {
		usernameEdit = (EditText) rootView.findViewById(R.id.username);
		passwordEdit = (EditText) rootView.findViewById(R.id.password);
		final SharedPreferences preferences = getActivity().getSharedPreferences("user", Activity.MODE_PRIVATE);
	
		usernameEdit.setText(preferences.getString("username", "username"));
		passwordEdit.setText(preferences.getString("password", ""));
		
		saveButton = (Button) rootView.findViewById(R.id.sure);
		saveUserInfo = new OnClickListener (){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				storeUserInfo (usernameEdit.getText().toString().trim(), passwordEdit.getText().toString().trim(), preferences);
			}
			
		};
		saveButton.setOnClickListener(saveUserInfo);
	}
	
	private void storeUserInfo (String username, String password, SharedPreferences preferences) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
		mCallback.onUserInfoSetted();
		Toast.makeText(getActivity(), "saved successfully.", Toast.LENGTH_SHORT).show();;
	}
	
	public interface UserInfoSettedListener{
		public void onUserInfoSetted ();
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (UserInfoSettedListener)activity;		
	}

}
