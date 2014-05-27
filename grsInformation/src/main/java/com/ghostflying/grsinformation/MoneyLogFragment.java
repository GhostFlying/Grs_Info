package com.ghostflying.grsinformation;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardListView;


public class MoneyLogFragment extends Fragment {


    //private OnFragmentInteractionListener mListener;
    private ArrayList<MoneyLog> moneyLogData = null;
    private ArrayList<Card> cards = null;
    private CardArrayAdapter cardAdapter = null;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment MoneyLogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MoneyLogFragment newInstance() {
        MoneyLogFragment fragment = new MoneyLogFragment();
        return fragment;
    }
    public MoneyLogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        moneyLogData = MainActivity.moneyLogData;
        return inflater.inflate(R.layout.money_log_fragment_main, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initCard();
    }

    private void initCard() {
        CardListView cardList = (CardListView) getActivity().findViewById(R.id.money_log_cardlist);
        cards = new ArrayList<>();
        transferData (moneyLogData, cards);

        cardAdapter = new CardArrayAdapter(getActivity(),cards);
        AnimationAdapter animCardArrayAdapter = new SwingLeftInAnimationAdapter(cardAdapter);
        animCardArrayAdapter.setAbsListView(cardList);

        cardList.setAdapter(cardAdapter);
        cardList.setExternalAdapter(animCardArrayAdapter, cardAdapter);
    }

    private void transferData(ArrayList<MoneyLog> origin, ArrayList<Card> newData) {
        for (MoneyLog log:origin) {
            String title = "";
            String content = "";

            title = log.time;
            content = "导师部分：" + log.teacherCount + "    状态：" + log.teacherState +
                    "\n" + "学校部分：" + log.uniCount + "    状态：" + log.uniState +
                    "\n" + "补发部分：" + log.teacherCount2 + "/" + log.uniCount2;
            LogCard card = new LogCard(getActivity(), title, content);
            newData.add(card);
        }
        if (origin.size() == 0) {
            LogCard card = new LogCard(getActivity(), "当前无数据，请刷新显示", "");
            newData.add(card);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    public class LogCard extends Card{

        protected String mTitleHeader;
        protected String mTitleMain;

        public LogCard(Context context, String titleHeader, String titleMain) {
            super(context, R.layout.money_log_card_view);
            this.mTitleHeader=titleHeader;
            this.mTitleMain=titleMain;
            init();
        }

        private void init(){

            //Create a CardHeader
            CustomHeader header = new CustomHeader(getActivity());

            //Set the header title
            header.setTitle(mTitleHeader);


            addCardHeader(header);

            setTitle(mTitleMain);
        }

    }

    public class CustomHeader extends CardHeader {

        public CustomHeader(Context context) {
            super(context, R.layout.custom_header_layout);
        }

    }


}
