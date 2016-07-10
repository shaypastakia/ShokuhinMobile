package shokuhin.shokuhin;

import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import recipe.Recipe;
import recipe.RecipeMethodsMobile;

/**
 * Created by shayp on 04/10/2015.
 */
public class RetrieveRecipeTask extends AsyncTask<String, Void, Recipe> {
    MainActivity main;
    String[] recipes;
    int position;

    public RetrieveRecipeTask(MainActivity _main, String[] _recipes, int _position){
        main = _main;
        recipes = _recipes;
        position = _position;
    }

    protected Recipe doInBackground(String... strings) {
        return RecipeMethodsMobile.readRecipe(main, recipes[position]);
    }
}
