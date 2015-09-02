package jp.ca.newslistener;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    LayoutInflater layoutInflater_;
    Context context;
    ArrayList<NewsData> dataList;   // 全データ

    ArrayList<NewsData> cat;

    static final String TAG = "ListViewAdapter";

    public ListViewAdapter(Context context, int pagerPostion, ArrayList<NewsData> list) {
        this.context = context;
        dataList = list;

        layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cat = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++){
            if(dataList.get(i).getCategory() == pagerPostion)cat.add(dataList.get(i));
        }
        //Log.d("ListViewAdapter", "listsize:" + cat.size() + ", pos:" + pagerPostion);
    }

    public void setNewsList(ArrayList<NewsData> dataList) {
        this.cat = dataList;
    }

    @Override
    public int getCount() {
        return cat.size();
    }

    @Override
    public Object getItem(int position) {
        return cat.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        NewsData item = (NewsData)getItem(position);

        if (null == convertView) {
            convertView = layoutInflater_.inflate(R.layout.listview_item, null);
        }

        // 記事へ移動
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WebViewActivity.class);

                intent.putExtra("URL", dataList.get(position).getURL());
                //intent.putExtra("URL", "http://yahoo.co.jp/");
                intent.putExtra("TITLE", cat.get(position).getTitle());
                context.startActivity(intent);
            }
        });

        // シェア
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(context, ShareListActivity.class);
                intent.putExtra("ARTICLE_ID", cat.get(position).getId());
                context.startActivity(intent);



                return false;
            }
        });



        ((TextView)convertView.findViewById(R.id.title)).setText(item.getTitle());
        ((TextView)convertView.findViewById(R.id.body)).setText(item.getBody());
        ((TextView)convertView.findViewById(R.id.date)).setText(item.getDate());

        return convertView;
    }
}