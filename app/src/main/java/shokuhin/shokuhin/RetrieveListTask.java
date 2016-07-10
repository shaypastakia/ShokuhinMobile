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
import recipe.RecipeMethodsMobile;
import recipe.RequestURL;

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
        try {
            ArrayList<String> recipes = new ArrayList<String>();

            for (String s : RecipeMethodsMobile.getRecipeFileNames(main)){
                if (search != null){
                    if (s.toLowerCase().contains(search.toLowerCase()))
                        recipes.add(s);
                } else {
                    recipes.add(s);
                }
            }

            return recipes;
        } catch (Exception e){
            e.printStackTrace();
            Log.e("Retrieve List Task (1)", "" + e.getMessage());
            return null;
        }
    }
}
