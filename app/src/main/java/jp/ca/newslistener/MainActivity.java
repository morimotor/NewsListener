package jp.ca.newslistener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.OnCheckedChangeListener;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    protected final String TAG = "MainActivity";
    private Activity activity = this;

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private PagerAdapter adapter;

    private AccountHeader headerResult = null;
    private Drawer result = null;

    ArrayList<NewsData> dataList = new ArrayList<>();

    private CallbackManager callbackManager;
    private MaterialDialog md;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private String fbAuthToken;

    // facebook user
    private String fbUserID;
    private String fbProfileName;



    public class User {
        String id;
        String name;

    }


    public class Articles {
        public List< List<Article> > articles;

    }

    public class Articles_R {
        public List<Article> articles;

    }

    class Article{
        public String article_id;
        public String body;
        public String title;
        public String up_datetime;
        public String url;

        public Article(String article_id, String body, String title, String up_datetaime, String url){
            this.article_id = article_id;
            this.body = body;
            this.title = title;
            this.up_datetime = up_datetaime;
            this.url = url;
        }
    }


    TextToSpeech tts = null;
    MaterialDialog ttsmd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // アプリ初期化のとき
        //getSharedPreferences("facebook", MODE_PRIVATE).edit().putBoolean("isLogin", false).apply();
        //getSharedPreferences("facebook", MODE_PRIVATE).edit().putBoolean("firstLogin", true).apply();
        // facebookアプリとこのアプリのfacebookuをログアウトする。



        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                fbAuthToken = currentAccessToken.getToken();
                fbUserID = currentAccessToken.getUserId();


                Log.d(TAG, "User id: " + fbUserID);
                Log.d(TAG, "Access token is: " + fbAuthToken);


            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                fbProfileName = currentProfile.getName();

                Log.d(TAG, "User name: " + fbProfileName );
            }
        };

        profileTracker.startTracking();
        accessTokenTracker.startTracking();

        // 読み上げ
        tts = new TextToSpeech(activity, this);
        tts.setSpeechRate(1.2f);
        tts.setPitch(1.0f);




        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.destroyAllItem(pager);
        adapter.setDataList(dataList);
        adapter.notifyDataSetChanged();
        pager.setAdapter(adapter);

        // アカウントの発行
        //makeAccount();

        getArticles();

        if(getSharedPreferences("account", MODE_PRIVATE).getBoolean("isAccount", false))
        {

        }


        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        pager.setCurrentItem(0);
        pager.setOffscreenPageLimit(10);

        tabs.setIndicatorColor(getResources().getColor(R.color.blue));
        tabs.setBackgroundColor(getResources().getColor(R.color.white));
        tabs.setUnderlineColor(getResources().getColor(R.color.gray));
        tabs.setDividerColor(getResources().getColor(R.color.gray));
        tabs.setTextSize(40);
        tabs.setDividerWidth(1);
        tabs.setIndicatorHeight(8);
        tabs.setUnderlineHeight(3);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                result.setSelection(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        Intent intent = new Intent(this, MyService.class);
        startService(intent);


        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withHeaderBackground(R.color.gray)
                .addProfiles(
                        new ProfileDrawerItem().withName("hogehoge さん").withEmail("aaa@gmail.com").withIcon(getResources().getDrawable(R.drawable.ic_action_refresh))
                )
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder(this)
                //this layout have to contain child layouts
                .withRootView(R.id.drawer_container)
                .withToolbar(toolbar)
                //.withAccountHeader(headerResult)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("トピックストップ"),
                        new PrimaryDrawerItem().withName("海外"),
                        new PrimaryDrawerItem().withName("エンターテインメント"),
                        new PrimaryDrawerItem().withName("コンピュータ"),
                        new PrimaryDrawerItem().withName("地域"),
                        new PrimaryDrawerItem().withName("国内"),
                        new PrimaryDrawerItem().withName("経済"),
                        new PrimaryDrawerItem().withName("スポーツ"),
                        new PrimaryDrawerItem().withName("サイエンス"),
                        new PrimaryDrawerItem().withName("Kick starter"),
                        new PrimaryDrawerItem().withName("ギズモード"),
                        new PrimaryDrawerItem().withName("シェアされた記事"),
                        new DividerDrawerItem(),
                        new SwitchDrawerItem().withName("入退室ボイス").withIcon(FontAwesome.Icon.faw_cog).withChecked(getSharedPreferences("setting", Context.MODE_PRIVATE).getBoolean("isVoice", false)).withOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(IDrawerItem iDrawerItem, CompoundButton compoundButton, boolean b) {
                                getSharedPreferences("setting", Context.MODE_PRIVATE).edit().putBoolean("isVoice", b).commit();
                                //Log.d(TAG, "onCheckedChanged " + b);
                            }
                        }),

                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("設定").withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName("アプリ終了").withIcon(FontAwesome.Icon.faw_cube)
                )
                //.withSavedInstance(savedInstanceState)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                         //Log.d(TAG, "onItemClick " + position);
                        if (drawerItem != null && position < 12) {
                            pager.setCurrentItem(position);
                        }
                        if (drawerItem != null && position == 15) {
                            result.setSelection(pager.getCurrentItem());
                            Intent intent = new Intent(activity, SettingActivity.class);
                            startActivity(intent);

                        }
                        if (drawerItem != null && position == 16) {
                            Intent intent = new Intent(activity, MyService.class);
                            stopService(intent);
                            finish();
                        }

                        return false;
                    }
                })
                .build();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void makeLoginDialog(){
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.facebook_login, null);

        md = new MaterialDialog.Builder(this)
                .title("facebookへログイン")
                .customView(view, true)
                .show();
        md.setCanceledOnTouchOutside(false);
        md.setCancelable(false);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick ");
                md.dismiss();
            }
        });


        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess ");
                getSharedPreferences("facebook", MODE_PRIVATE).edit().putBoolean("isLogin", true).apply();

                GraphRequest request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject, GraphResponse response) {

                                Log.d(TAG, "onCompleted jsonObject: " + jsonObject);
                                Log.d(TAG, "onCompleted response: " + response);

                                Gson gson = new Gson();
                                User user = gson.fromJson(jsonObject.toString(),
                                        User.class);
                                Log.d(TAG, "id:" + user.id);
                                Log.d(TAG, "name:" + user.name);

                                getSharedPreferences("facebook", MODE_PRIVATE).edit().putString("user_id", user.id).apply();
                                getSharedPreferences("facebook", MODE_PRIVATE).edit().putString("user_name", user.name).apply();

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel ");
                getSharedPreferences("facebook", MODE_PRIVATE).edit().putBoolean("isLogin", false).apply();
            }

            @Override
            public void onError(FacebookException e) {
                Log.d(TAG, "onError ");
                getSharedPreferences("facebook", MODE_PRIVATE).edit().putBoolean("isLogin", false).apply();
            }
        });


    }

    public void makeAccount() {

        boolean isFirstLogin = getSharedPreferences("facebook", MODE_PRIVATE).getBoolean("firstLogin", true);
        if(!isFirstLogin)return;

        RequestParams params = new RequestParams();
        AsyncHttpClient client = new AsyncHttpClient();

        String user_name = getSharedPreferences("facebook", MODE_PRIVATE).getString("user_name", "");
        String user_id = getSharedPreferences("facebook", MODE_PRIVATE).getString("user_id", "");
        String url = null;
        try {
            url = "http://news-lis.herokuapp.com/user?name=" + URLEncoder.encode(user_name, "UTF-8") + "&icon=aaa" + "&user_id=" + URLEncoder.encode(user_id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "makeAccount " + url);

        // /user?name=~&icon=~ アカウントを登録するuser_idが発行されて帰ってくる

        RequestHandle get = client.get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = null;
                try {
                    res = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "makeAccount onSuccess " + res);
                getSharedPreferences("facebook", MODE_PRIVATE).edit().putBoolean("firstLogin", false).apply();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "makeAccount onFailure");
            }


        });

    }

    public void getArticles() {

        dataList.clear();

        //・/articles/<category_id>?start_time=~&end_time=~記事取得
        RequestParams params1 = new RequestParams();
        AsyncHttpClient client1 = new AsyncHttpClient();

        //String url = "http://news-lis.herokuapp.com/articles/";
        String url = "http://private-fd63b7-techcamp.apiary-mock.com/articles";

        Log.d(TAG, "getArticles " + url);

        // /user?name=~&icon=~ アカウントを登録するuser_idが発行されて帰ってくる

        RequestHandle get = client1.get(url, params1, new AsyncHttpResponseHandler() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = null;
                try {
                    res = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "getArticles onSuccess " + res);


                Gson gson = new Gson();
                Articles articles = gson.fromJson(res, Articles.class);
                Log.d(TAG, "getArticles onSuccess " + articles);
                //Log.d(TAG, "onSuccess " + articles.articles.get(0).get(0).body);

                for (int i = 0; i < 11; i++) { // カテゴリ
                    for (int n = 0; n < articles.articles.get(i).size(); n++) { // 記事
                        Article art = articles.articles.get(i).get(n);
                        NewsData data = new NewsData(i, art.article_id, art.title, art.body, art.up_datetime, art.url);

                        Log.d(TAG, "onSuccess art:" + art.title);
                        dataList.add(data);
                    }
                }
                updateFragment();
                updateFragment();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "getArticles onFailure");
                Toast.makeText(activity, "通信に失敗しました", Toast.LENGTH_SHORT);
            }
        });


        // reccomend
        RequestParams params2 = new RequestParams();
        AsyncHttpClient client2 = new AsyncHttpClient();

        url = "http://news-lis.herokuapp.com/get_recommends/?user_id=morimoto";
        get = client2.get(url, params2, new AsyncHttpResponseHandler() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = null;
                try {
                    res = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "getArticles onSuccess " + res);

                //dataList.clear();

                Gson gson = new Gson();
                Articles_R articles = gson.fromJson(res, Articles_R.class);
                Log.d(TAG, "getArticles onSuccess2 " + articles);
                //Log.d(TAG, "onSuccess " + articles.articles.get(0).get(0).body);

                for (int n = 0; n < articles.articles.size(); n++) { // 記事
                    Article art = articles.articles.get(n);
                    NewsData data = new NewsData(11, art.article_id, art.title, art.body, art.up_datetime, art.url);

                    Log.d(TAG, "onSuccess art2:" + art.title);
                    dataList.add(data);
                }

                updateFragment();
                updateFragment();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "getArticles onFailure");
                Toast.makeText(activity, "通信に失敗しました", Toast.LENGTH_SHORT);
            }
        });

    }


    // TODO: http://qiita.com/rmdroid/items/0a6161c5f74a982c58c2
    public void updateFragment(){
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new PagerAdapter(getSupportFragmentManager());

        adapter.destroyAllItem(pager);
        adapter.setDataList(dataList);
        adapter.notifyDataSetChanged();
        pager.setAdapter(adapter);

        tabs.setViewPager(pager);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        MenuItem actionItem1 = menu.add("playButton");                  // メニューの要素を追加して取得
        actionItem1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);   // SHOW_AS_ACTION_IF_ROOM:余裕があれば表示
        actionItem1.setIcon(R.drawable.ic_action_play);                 // アイコンを設定

        MenuItem actionItem2 = menu.add("stopButton");
        actionItem2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem2.setIcon(R.drawable.ic_action_stop);

        MenuItem actionItem3 = menu.add("refreshButton");
        actionItem3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        actionItem3.setIcon(R.drawable.ic_action_refresh);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(item.getTitle().toString().equals("playButton")){

            String readText = null;

            int category = pager.getCurrentItem();
            for(int i = 0; i < dataList.size(); i++){
                if(dataList.get(i).category == category){

                    tts.speak(dataList.get(i).title + "。。", TextToSpeech.QUEUE_ADD, null);
                    tts.speak(dataList.get(i).body + "。。", TextToSpeech.QUEUE_ADD, null);
                }
            }

            /*
            ttsmd = new MaterialDialog.Builder(this)
                    .title("準備中")
                    .content("Please wait...")
                    .progress(true, 0)
                    .show();
                    */
        }

        if(item.getTitle().toString().equals("stopButton")){
            tts.stop();
        }

        // アクションバのrefreshbutton
        if(item.getTitle().toString().equals("refreshButton")){
            getArticles();
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            Locale locale = Locale.JAPANESE;
            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale);

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "tts progress on Done " + utteranceId);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.d(TAG, "tts progress on Error " + utteranceId);
                    }

                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "tts progress on Start " + utteranceId);
                        ttsmd.dismiss();
                    }

                });
            } else {
                Log.d(TAG, "Error SetLocale");
                Log.d(TAG, "onInit error setlocal");
            }
        } else {
            Log.d(TAG, "onInit Error");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        profileTracker.startTracking();
        accessTokenTracker.startTracking();

        Log.d(TAG, "onResume " + AccessToken.getCurrentAccessToken());

        //boolean isLogin = getSharedPreferences("facebook", MODE_PRIVATE).getBoolean("isLogin", false);
        //if(!isLogin)
            //makeLoginDialog();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ");

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);

        profileTracker.stopTracking();
        accessTokenTracker.stopTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tts.stop();
        tts.shutdown();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:

                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

}
