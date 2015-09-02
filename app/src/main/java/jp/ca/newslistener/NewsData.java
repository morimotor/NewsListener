package jp.ca.newslistener;

public class NewsData {
    int category;
    String id;
    String title;
    String body;
    String date;
    String url;

    NewsData(int category, String id, String title, String body, String date, String url){
        this.category = category;
        this.id = id;
        this.title = title;
        this.body = body;
        this.date = date;
        this.url = url;
    }
    public int getCategory(){ return this.category; }

    public String getTitle() {
        return this.title;
    }

    public String getId(){
        return id;
    }

    public String getBody() {
        return body;
    }

    public  String getDate(){
        return date;
    }

    public String getURL(){
        return url;
    }

}