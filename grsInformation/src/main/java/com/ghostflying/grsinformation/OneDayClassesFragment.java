package com.ghostflying.grsinformation;

import java.util.ArrayList;
import java.util.HashMap;

import com.ghostflying.grsinformation.Course.Frequent;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardListView;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OneDayClassesFragment extends Fragment {
	
	private static final String LIST_DATA = "list_data";
	
	
	private CardArrayAdapter cardAdapter;
	private ArrayList<Card> cards ;
	private ArrayList<HashMap <String, Object>> classesData;
	public static OneDayClassesFragment newInstance(ArrayList<HashMap <String, Object>> data) {
		OneDayClassesFragment fragment = new OneDayClassesFragment();
		
/*		Bundle args = new Bundle();		
		args.putSerializable(LIST_DATA, data);
		fragment.setArguments(args);*/
		return fragment;		
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
/*		if (classesData == null) {
			classesData = (ArrayList<HashMap<String, Object>>) getArguments().getSerializable(LIST_DATA);
		}*/
		classesData = MainActivity.todayClasses;
		return inflater.inflate(R.layout.one_day_listview_main, container, false);
	}

	
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initCard();
    }
	
	public void dataUpdated (ArrayList<HashMap <String, Object>> newData) {
		if (cards != null) {
			classesData = newData;
			transferData(newData, cards);
			cardAdapter.notifyDataSetChanged();
		}
	}
	
	private void transferData (ArrayList<HashMap <String, Object>> origin, ArrayList<Card> newData) {
		newData.clear();
		for (HashMap<String, Object> eachClass: origin) {
			String fre = Course.freToStr((Frequent)eachClass.get("fre"));
			String title = ((String)eachClass.get("name")) ;
			String content = "\n" + String.valueOf((int)eachClass.get("start")) + "-" +
					String.valueOf((int)eachClass.get("end")) + "\n" +
					(String) eachClass.get("location") + "\n" +
					(String) eachClass.get("teacher") + "\n" +
					fre;
			
			ClassCard mCard = new ClassCard(getActivity(), title, content);
			newData.add(mCard);
		}

        if (origin.size() == 0) {
            String title = "当日无课程";
            String content = "";
            ClassCard mCard = new ClassCard(getActivity(), title, content);
            newData.add(mCard);
        }
	}
	
	private void initCard() {
		CardListView cardList = (CardListView) getActivity().findViewById(R.id.cardlist);
		cards = new ArrayList<Card>();		
		transferData (classesData, cards);
		
		cardAdapter = new CardArrayAdapter(getActivity(),cards);
		AnimationAdapter animCardArrayAdapter = new SwingLeftInAnimationAdapter(cardAdapter);
		animCardArrayAdapter.setAbsListView(cardList);
		cardList.setAdapter(cardAdapter);
		cardList.setExternalAdapter(animCardArrayAdapter, cardAdapter);
	}
	
	public class ClassCard extends Card{

        protected String mTitleHeader;
        protected String mTitleMain;

        public ClassCard(Context context,String titleHeader,String titleMain) {
            super(context, R.layout.classes_card_view);
            this.mTitleHeader=titleHeader;
            this.mTitleMain=titleMain;
            init();
        }

        private void init(){

            //Create a CardHeader
        	CustomHeader header = new CustomHeader(getActivity());

            //Set the header title
            header.setTitle(mTitleHeader);
            
            
            this.addCardHeader(header);
            
            this.setTitle(mTitleMain);
        }

    }
	
	public class CustomHeader extends CardHeader {

	    public CustomHeader(Context context) {
	        super(context, R.layout.custom_header_layout);
	    }

	}

}
