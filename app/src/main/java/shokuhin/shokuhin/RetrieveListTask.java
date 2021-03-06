package shokuhin.shokuhin;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import recipe.RecipeMethodsMobile;

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
