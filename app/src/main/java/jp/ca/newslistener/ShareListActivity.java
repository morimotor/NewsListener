package jp.ca.newslistener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.rey.material.widget.CheckBox;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ShareListActivity extends AppCompatActivity {

    static final String TAG = "ShareListActivity";
    MyCustomAdapter dataAdapter = null;
    boolean isChanged = false;
    Activity activity = this;

    ArrayList<Item> itemList = new ArrayList<Item>();

    public class Users {
        public List<User> users;

    }

    public class User {
        String name;
        String user_id;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_list);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        final String article_id = i.getStringExtra("ARTICLE_ID");


        getSupportActionBar().setTitle("シェア");

        // アクションバーでアプリのアイコンの表示設定
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // アクションバーでアプリタイトルの表示設定
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // アクションバーで戻るボタンの表示設定
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < itemList.size(); i++){

                    //Log.d(TAG, "onClick " + itemList.get(i).isSelected());
                    if(!itemList.get(i).isSelected())continue;

                    RequestParams params = new RequestParams();
                    AsyncHttpClient client = new AsyncHttpClient();

                    String url = "http://news-lis.herokuapp.com/recommend?user_id=" + itemList.get(i).getId() + "&articles=" + article_id;
                    //Log.d(TAG, "onClick " + url);
                    RequestHandle get = client.get(url, params, new AsyncHttpResponseHandler() {
                        @TargetApi(Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = null;
                            try {
                                res = new String(responseBody, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            Log.d(TAG, "onSuccess " + res);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.d(TAG, "getArticles onFailure");

                        }
                    });
                }

                Toast.makeText(activity, "SHAREしました。", Toast.LENGTH_SHORT).show();
                //Toast.makeText(activity, "通信に失敗しました", Toast.LENGTH_SHORT);

                finish();
            }
        });

        getFriends();

    }
    private void getFriends() {

        RequestParams params = new RequestParams();
        AsyncHttpClient client = new AsyncHttpClient();

        String url = "http://news-lis.herokuapp.com/get_user/";

        RequestHandle get = client.get(url, params, new AsyncHttpResponseHandler() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = null;
                try {
                    res = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "onSuccess " + res);

                Gson gson = new Gson();
                Users users = gson.fromJson(res, Users.class);

                itemList.clear();
                for(int i = 0; i < users.users.size(); i++){
                    itemList.add(new Item(users.users.get(i).name, users.users.get(i).user_id, false));
                }

                Log.d(TAG, "onSuccess " + users.users.get(1).name);

                displayListView();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "getArticles onFailure");
                Toast.makeText(activity, "通信に失敗しました", Toast.LENGTH_SHORT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //アクションバーの戻るを押したときの処理
        if (id == android.R.id.home) {
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }




    private void displayListView() {

        Log.d(TAG, "displayListView  "  + itemList);

        dataAdapter = new MyCustomAdapter(this, R.layout.activity_share_list, itemList);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);



    }

    public class Item {

        String name = null;
        boolean selected = false;
        String id = null;

        public Item(String name, String id, boolean selected) {
            super();
            this.name = name;
            this.id = id;
            this.selected = selected;
        }

        public String getName() {
            return name;
        }
        public String getId(){
            return id;
        }
        public void setName(String name) {
            this.name = name;
        }

        public boolean isSelected() {
            return selected;
        }
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

    }

    private class MyCustomAdapter extends ArrayAdapter<Item> {


        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Item> itemList) {
            super(context, textViewResourceId, itemList);
        }


        private class ViewHolder {
            CheckBox name;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.setting_list_item, null);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
                convertView.setTag(holder);

            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ViewHolder finalHolder = holder;
            holder.name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    itemList.get(position).setSelected(finalHolder.name.isChecked());
                    Log.d(TAG, "onCheckedChanged " + itemList.get(position).getId());
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalHolder.name.setChecked(finalHolder.name.isChecked());
                    itemList.get(position).setSelected(finalHolder.name.isSelected());

                }
            });

            Item item = itemList.get(position);
            holder.name.setText(item.getName());
            holder.name.setChecked(item.isSelected());
            holder.name.setTag(item);

            return convertView;

        }
    }



}
