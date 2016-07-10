package recipe;

import android.util.Pair;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by shayp on 10/07/2016.
 */
public class RequestURL {
    String url;
    ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();

    /**
     * Construct a new URL, from the IP address provided (as a String)
     * @param _url either in the format "192.168.1.147", or "shokuh.in"
     */
    public RequestURL(String _url){
        this.url = _url;
    }

    /**
     * Add a Parameter and its value to the URL
     * @param _key The key, e.g. "type"
     * @param _value The value, e.g. "REQUEST"
     */
    public void addParameter(String _key, String _value){
        parameters.add(new Pair<String, String>(_key, _value));
    }

    public String toString(){
        String temp = "http://";
        temp += url;
        temp += ":8080/ShokuhinServer/shokuhin";
        if (!parameters.isEmpty())
            temp += "?";
        for (Pair<String, String> pair : parameters){
            temp += pair.first;
            temp += "=";
            try {
                temp += URLEncoder.encode(pair.second, "utf-8");
            } catch (Exception e){
                temp += (pair.second).replaceAll(" ", "%20");
            }
            if (parameters.indexOf(pair) < parameters.size()-1){
                temp += "&";
            }
        }
        return temp;
    }
}
