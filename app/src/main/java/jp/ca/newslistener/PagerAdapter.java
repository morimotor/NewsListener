package jp.ca.newslistener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PagerAdapter extends FragmentPagerAdapter {


    ArrayList<String> list = new ArrayList<>();
    ArrayList<NewsData> dataList= new ArrayList<>();

    public PagerAdapter(FragmentManager fm) {

        super(fm);

        list.add("トピックストップ");
        list.add("　海外　");
        list.add("エンターテインメント");
        list.add("コンピュータ");
        list.add("　地域　");
        list.add("　国内　");
        list.add("　経済　");
        list.add("スポーツ");
        list.add("サイエンス");
        list.add("Kick starter");
        list.add("ギズモード");
        list.add("シェアされた記事");
    }
    public void setDataList(ArrayList<NewsData> dataList) {
        this.dataList = dataList;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Fragment getItem(int position) {

        ArrayList<Integer> dataCategory = new ArrayList<>();
        ArrayList<String> dataId = new ArrayList<>();
        ArrayList<String> dataTitle = new ArrayList<>();
        ArrayList<String> dataBody = new ArrayList<>();
        ArrayList<String> dataDate = new ArrayList<>();
        ArrayList<String> dataURL = new ArrayList<>();

        for(int i = 0; i < dataList.size(); i++){
            dataCategory.add(dataList.get(i).getCategory());
            dataId.add(dataList.get(i).getId());
            dataTitle.add(dataList.get(i).getTitle());
            dataBody.add(dataList.get(i).getBody());
            dataDate.add(dataList.get(i).getDate());
            dataURL.add(dataList.get(i).getURL());
        }


        return ListFragment.newInstance(position, dataCategory, dataId, dataTitle, dataBody, dataDate, dataURL);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    public void destroyAllItem(ViewPager pager) {
        for (int i = 0; i < getCount() - 1; i++) {
            try {
                Object obj = this.instantiateItem(pager, i);
                if (obj != null)
                    destroyItem(pager, i, obj);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        if (position <= getCount()) {
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
        }
    }
}
