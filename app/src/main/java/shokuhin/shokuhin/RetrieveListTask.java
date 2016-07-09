package shokuhin.shokuhin;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import recipe.Recipe;

/**
 * Created by shayp on 04/10/2015.
 */
public class RetrieveListTask extends AsyncTask<String, Void, ArrayList<String>> {
    InputStream input = null;
    OutputStream output = null;
    HttpURLConnection connection = null;
    MainActivity main;
    String search;

    public RetrieveListTask(MainActivity _main){
        main = _main;
    }

    public RetrieveListTask(MainActivity _main, String _search){
        main = _main;
        search = _search;
    }

    protected  ArrayList<String> doInBackground(String... strings){
            String responseStr = "";
        try {
            String dataUrl = "http://192.168.1.147:8080/ShokuhinServer/shokuhin?type=REQUEST&recipe=Blueberry%20Muffins";
            URL url;
            HttpURLConnection connection = null;
            url = new URL(dataUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            connection.setRequestProperty("Accept","*/*");

            //Use OutputStream to send GET
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            responseStr = response.toString();
            ObjectMapper mapper = new ObjectMapper();
            Recipe r = mapper.readValue(responseStr, Recipe.class);
//            ArrayList<String> recipes = mapper.readValue(responseStr, ArrayList.class);
            ArrayList<String> recipes = new ArrayList<String>();
            recipes.add(r.getTitle());
            recipes.add("" + r.getCookTime());
            recipes.add("" + r.getCourse());
            recipes.add("" + r.getRating());
            return recipes;
        } catch (Exception e){
            e.printStackTrace();
            Log.e("Retrieve List Task (1)", "" + e.getMessage());
            return null;
        }
    }

//    protected ArrayList<String> doInBackground(String... strings){
//        try {
//            ArrayList<String> temp = new ArrayList<String>();
//            ArrayList<String> temp2 = new ArrayList<String>();
//            Document doc = Jsoup.connect("http://194.83.236.93/~spastakia/Shokuhin/").get();
//            for (Element file : doc.select("a")) {
//                temp.add(file.attr("href"));
//            }
//
//            for (String s : temp){
//                if (s.endsWith(".rec")){
//                    String s2 = s.replaceAll("%20", " ").replaceAll(".rec", "");
//                    if (search != null){
//                        if (s2.toLowerCase().contains(search.toLowerCase()))
//                            temp2.add(s2);
//                    } else {
//                        temp2.add(s2);
//                    }
//
//                }
//
//            }
//            temp.clear();
//
//            return temp2;
//        } catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }

}
