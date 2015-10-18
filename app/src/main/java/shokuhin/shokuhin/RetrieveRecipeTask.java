package shokuhin.shokuhin;

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

import recipe.Recipe;

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
        InputStream input;
        OutputStream output;
        HttpURLConnection connection;
        Recipe recipe = null;

        try {

            URL url = new URL("http://194.83.236.93/~spastakia/Shokuhin/" + recipes[position].replaceAll(" ", "%20") + ".rec");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Unable to connect to server");
            }

            input = connection.getInputStream();
            output = new FileOutputStream(main.getFilesDir() + recipes[position] + ".rec");
            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.close();
            input.close();

            InputStream inStream = new FileInputStream(main.getFilesDir() + recipes[position] + ".rec");
            BufferedInputStream buff = new BufferedInputStream(inStream);
            ObjectInputStream obj = new ObjectInputStream(buff);
            recipe = (Recipe) obj.readObject();
            obj.close();
            return recipe;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
