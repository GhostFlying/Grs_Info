package com.ghostflying.grsinformation;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.HashMap;

import com.ghostflying.grsinformation.Course.EachClass;
import com.ghostflying.grsinformation.OneDayClassesFragment.ClassCard;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class AllCheckedCoursesFragment extends Fragment {
	private static final String COURSES_DATA = "courses_data";
	
	private ArrayList<Course> coursesData;
	private CardArrayAdapter cardAdapter;
	private ArrayList<Card> cards ;
	
	public static AllCheckedCoursesFragment newInstance(ArrayList<Course> data){
		AllCheckedCoursesFragment fragment = new AllCheckedCoursesFragment();
/*		Bundle args = new Bundle();
		args.putSerializable(COURSES_DATA, data);
		fragment.setArguments(args);*/
		return fragment;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		coursesData = MainActivity.coursesData;
		return inflater.inflate(R.layout.all_courses_listview, container, false);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initCard();
    }
	
	public void dataUpdated (ArrayList<Course> newData) {
		if (cards != null) {
			transferData(newData, cards);
			cardAdapter.notifyDataSetChanged();
		}
	}
	private void initCard() {
		CardListView cardList = (CardListView) getActivity().findViewById(R.id.courses_cardlist);
		cards = new ArrayList<Card>();		
		transferData (coursesData, cards);
		
		cardAdapter = new CardArrayAdapter(getActivity(),cards);
		AnimationAdapter animCardArrayAdapter = new SwingLeftInAnimationAdapter(cardAdapter);
		animCardArrayAdapter.setAbsListView(cardList);
		
		cardList.setAdapter(cardAdapter);
		cardList.setExternalAdapter(animCardArrayAdapter, cardAdapter);
	}
	
	private void transferData (ArrayList<Course> origin, ArrayList<Card> newData) {
		for (Course course : origin) {
			String title = course.name;
			String newLine = "\n ";
			String content = course.teacher + newLine + course.courseNum + newLine + newLine;
			for (EachClass each: course.classes){
				String fre = Course.freToStr(each.fre);
				String location = each.location;
				String day = each.dayToStr();
				String time = Integer.toString(each.startClass) + "-" + Integer.toString(each.endClass);
				String semester = each.semesterToStr();
				
				content = newLine + content + semester + "  " + day + "  "+ time + newLine + 
						location + "  " + fre + newLine + newLine;
			}
			
			CourseCard mCard = new CourseCard(getActivity(), title, content);
			newData.add(mCard);		
			
		}
	}
	
	public class CourseCard extends Card{

        protected String mTitleHeader;
        protected String mTitleMain;

        public CourseCard(Context context,String titleHeader,String titleMain) {
            super(context, R.layout.courses_card_view);
            this.mTitleHeader=titleHeader;
            this.mTitleMain=titleMain;
            init();
        }

        private void init(){

            //Create a CardHeader
            CardHeader header = new CardHeader(getActivity());

            //Set the header title
            header.setTitle(mTitleHeader);

            
            addCardHeader(header);
            
            setTitle(mTitleMain);
        }

    }
}
