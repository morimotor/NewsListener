package jp.ca.newslistener;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class ListFragment extends android.support.v4.app.Fragment {

    static final String ARG_POSITION = "position";
    static final String ARG_CATEGORY = "category";
    static final String ARG_ID = "id";
    static final String ARG_TITLE = "title";
    static final String ARG_BODY = "body";
    static final String ARG_DATE = "date";
    static final String ARG_URL = "url";

    int position;

    ListView mListView;
    ListViewAdapter mAdapter;
    ArrayList<NewsData> dataList = new ArrayList<>();
    ArrayList<Integer> dataCategory;
    ArrayList<String> dataId;
    ArrayList<String> dataTitle;
    ArrayList<String > dataBody;
    ArrayList<String> dataDate;
    ArrayList<String> dataURL;

    public static ListFragment newInstance(int position, ArrayList<Integer> dataCategory, ArrayList<String> dataId, ArrayList<String> dataTitle, ArrayList<String> dataBody, ArrayList<String> dataDate, ArrayList<String> dataURL) {
        ListFragment f = new ListFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        b.putIntegerArrayList(ARG_CATEGORY, dataCategory);
        b.putStringArrayList(ARG_ID, dataId);
        b.putStringArrayList(ARG_TITLE, dataTitle);
        b.putStringArrayList(ARG_BODY, dataBody);
        b.putStringArrayList(ARG_DATE, dataDate);
        b.putStringArrayList(ARG_URL, dataURL);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt(ARG_POSITION);

        dataCategory = getArguments().getIntegerArrayList(ARG_CATEGORY);
        dataId = getArguments().getStringArrayList(ARG_ID);
        dataTitle = getArguments().getStringArrayList(ARG_TITLE);
        dataBody = getArguments().getStringArrayList(ARG_BODY);
        dataDate = getArguments().getStringArrayList(ARG_DATE);
        dataURL = getArguments().getStringArrayList(ARG_URL);

        for(int i = 0; i < dataCategory.size(); i++){
            NewsData _tmp = new NewsData(dataCategory.get(i), dataId.get(i), dataTitle.get(i), dataBody.get(i), dataDate.get(i), dataURL.get(i));
            dataList.add(_tmp);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mListView = (ListView) view.findViewById(R.id.listView);

        // 行間の透過 Drawableを設定
        mListView.setDivider(new ColorDrawable(Color.argb(0, 255, 255, 255)));
        mListView.setDividerHeight(20); //行間の高さを設定



        mAdapter = new ListViewAdapter(getActivity(), position, dataList);

        // mAdapter.setNewsList(dataList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            }
        });
        mAdapter.notifyDataSetChanged();
        //mListView.invalidate();

        return view;
    }

}
