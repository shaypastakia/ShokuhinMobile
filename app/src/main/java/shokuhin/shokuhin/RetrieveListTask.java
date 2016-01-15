package shokuhin.shokuhin;

import android.os.AsyncTask;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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

    protected ArrayList<String> doInBackground(String... strings){
        try {
            ArrayList<String> temp = new ArrayList<String>();
            ArrayList<String> temp2 = new ArrayList<String>();
            Document doc = Jsoup.connect("http://194.83.236.93/~spastakia/Shokuhin/").get();
            for (Element file : doc.select("a")) {
                temp.add(file.attr("href"));
            }

            for (String s : temp){
                if (s.endsWith(".rec")){
                    String s2 = s.replaceAll("%20", " ").replaceAll(".rec", "");
                    if (search != null){
                        if (s2.toLowerCase().contains(search.toLowerCase()))
                            temp2.add(s2);
                    } else {
                        temp2.add(s2);
                    }

                }

            }
            temp.clear();

            return temp2;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
