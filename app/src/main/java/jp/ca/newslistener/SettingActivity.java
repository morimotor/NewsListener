package jp.ca.newslistener;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity {
    static  final String TAG = "SettingActivity";
    MyCustomAdapter dataAdapter = null;
    WifiManager wifiManager;

    boolean isChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("設定");

        // アクションバーでアプリのアイコンの表示設定
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // アクションバーでアプリタイトルの表示設定
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // アクションバーで戻るボタンの表示設定
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //Generate list View from ArrayList
        displayListView();

        showDialog();

    }

    private void displayListView() {

        //Array list of countries
        ArrayList<Item> itemList = new ArrayList<Item>();


        if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

            // APをスキャン
            wifiManager.startScan();

            // スキャン結果を取得
            List<ScanResult> apList = wifiManager.getScanResults();
            for(int i = 0; i < apList.size(); i++) {
                //Log.d(TAG, "SSID:" + apList.get(i).SSID + " " + apList.get(i).frequency + "MHz " + apList.get(i).level + "dBm");
                boolean breakFlag = false;

                for (int k = 0; k < itemList.size(); k++){
                    if(apList.get(i).SSID.equals(itemList.get(k).getName()))breakFlag = true;
                }

                if(breakFlag)break;
                Item item = new Item(apList.get(i).SSID, false);
                itemList.add(item);
            }
        }

        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.setting_list_item, itemList);
        ListView listView = (ListView) findViewById(R.id.listView);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

    }

    public class Item {

        String name = null;
        boolean selected = false;

        public Item(String name, boolean selected) {
            super();
            this.name = name;
            this.selected = selected;
        }

        public String getName() {
            return name;
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

        private ArrayList<Item> itemList;

        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Item> itemList) {
            super(context, textViewResourceId, itemList);
            this.itemList = new ArrayList<Item>();
            this.itemList.addAll(itemList);
        }


        private class ViewHolder {
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.setting_list_item, null);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
                convertView.setTag(holder);

                holder.name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Log.d(TAG, "onCheckedChanged ");
                        CheckBox cb = (CheckBox) buttonView;
                        ArrayList<String> checkedList = getArray("array");
                        String cbt = cb.getText().toString();
                        isChanged = true;
                        // チェックがついたら
                        if(cb.isChecked()){
                            boolean isExFlag = false;
                            for(int k = 0; k < checkedList.size(); k++){
                                if(cbt.equals(checkedList.get(k)))isExFlag = true;

                            }
                            if(!isExFlag){
                                checkedList.add(cbt);
                                saveArray(checkedList, "array");
                                Log.d(TAG, "onCheckedChanged " + checkedList);

                            }
                            // チェックが外れたら
                        }else{
                            ArrayList<String> ssidCheckedList = new ArrayList<String>();
                            for(int k = 0; k < checkedList.size(); k++){
                                if(cbt.equals(checkedList.get(k)))continue;
                                ssidCheckedList.add(checkedList.get(k));
                                saveArray(ssidCheckedList, "array");
                                Log.d(TAG, "onCheckedChanged " + ssidCheckedList);
                            }

                        }

                        //getSharedPreferences("setting", MODE_PRIVATE).edit().putBoolean("isLogin", true).apply();
                        //Log.d("aaaaaa", "onCheckedChanged " + ssidCheckedList) ;

                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Item item = itemList.get(position);
            holder.name.setText(item.getName());
            holder.name.setChecked(item.isSelected());
            holder.name.setTag(item);

            ArrayList<String> items = getArray("array");
            if (items != null) {
                for(int i = 0; i < items.size(); i++){
                    if(items.get(i).equals(item.getName()))holder.name.setChecked(true);
                }
            }

            return convertView;

        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //アクションバーの戻るを押したときの処理
        if (id == android.R.id.home) {
            Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        // アクションバのhelpbutton
        if(item.getTitle().toString().equals("helpButton")){
            showDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    void showDialog(){
        new MaterialDialog.Builder(this)
                //.title("使い方")
                .content("オフィスの無線LAN APを選択してください")
                        //.contentColor(getResources().getColor(R.color.blue))
                .positiveText("OK")
                .positiveColor(getResources().getColor(R.color.blue))
                .show();
    }

    private void saveArray(ArrayList list, String PrefKey){
        String[] array = (String[]) list.toArray(new String[list.size()]);

        StringBuilder buffer = new StringBuilder();
        String stringItem;
        for(String item : array){
            buffer.append(item).append(",");
        }
        String buf = buffer.toString();
        stringItem = buf.substring(0, buf.length() - 1);

        SharedPreferences prefs1 = getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs1.edit();
        editor.putString(PrefKey, stringItem).apply();
    }

    private ArrayList<String> getArray(String PrefKey){

        SharedPreferences prefs2 = getSharedPreferences("setting", Context.MODE_PRIVATE);
        String stringItem = prefs2.getString(PrefKey, "");
        return new ArrayList<String>(Arrays.asList(stringItem.split(",")));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // メニューの要素を追加して取得
        MenuItem actionItem = menu.add("helpButton");

        // SHOW_AS_ACTION_IF_ROOM:余裕があれば表示
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // アイコンを設定
        actionItem.setIcon(R.drawable.ic_action_help);

        return true;
    }
}

