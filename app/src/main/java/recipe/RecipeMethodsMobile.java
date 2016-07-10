package recipe;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import shokuhin.shokuhin.MainActivity;

/**
 * Created by shayp on 10/07/2016.
 */
public class RecipeMethodsMobile {

    /**
     *  Make a HTTP Request. Provide a RequestURL.
     *  @param _url The RequestURL, containing the URL to connect to, and the request parameters.
     * @return The response from the server, as a String.
     */
    public static String request(RequestURL _url){
        String responseStr = null;
        try {
            HttpURLConnection connection = getConnection(_url);
            //Use InputStream to send POST
            BufferedReader bufRead = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = bufRead.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            bufRead.close();
            connection.disconnect();
            responseStr = response.toString();
        } catch (Exception e){
            e.printStackTrace();
            Log.e("RecipeMethodsMobile|req", e.getMessage());
            return responseStr;
        }
        return responseStr;
    }

    public static HashMap<Recipe, String> getServerRecipes(MainActivity main, RequestURL _url){
        HashMap<Recipe, String> temp = new HashMap<Recipe, String>();
        try {
            HashMap<String, Timestamp> localRecs = RecipeMethodsMobile.getLastModifiedDates(main);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(localRecs);

            _url.addParameter("type", "SENDTIMES");
            HttpURLConnection connection = getConnection(_url);


//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("charset", "utf-8");
//            connection.setRequestProperty("Content-Length", "" + Integer.toString(json.getBytes().length));
//            BufferedWriter bufWrite = new BufferedWriter(new OutputStreamWriter((connection.getOutputStream()), "UTF-8"));
//            bufWrite.write(json);
//            bufWrite.flush();
//            bufWrite.close();

            DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
            outStream.writeUTF(json);
            outStream.flush();
            outStream.close();

            BufferedReader bufRead = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = bufRead.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            bufRead.close();
            String responseStr = response.toString();

            connection.disconnect();
            return temp;
        } catch (Exception e) {
            return null;
        }
    }

    private static HttpURLConnection getConnection(RequestURL urlString){
        try {
            String dataUrl = urlString.toString();
            URL url;
            HttpURLConnection connection = null;
            url = new URL(dataUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            connection.setRequestProperty("Accept", "*/*");
            return connection;
        } catch (Exception e){
            return null;
        }
    }


    public static boolean writeRecipe(MainActivity main, Recipe r){
        try {
            FileOutputStream fos = main.openFileOutput(r.getTitle() + ".rec", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(r);
            os.close();
            fos.close();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static Recipe readRecipe(MainActivity main, String title){
        try {
            FileInputStream fis = main.openFileInput(title);
            ObjectInputStream is = new ObjectInputStream(fis);
            Recipe rec = (Recipe) is.readObject();
            is.close();
            fis.close();
            return rec;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, Timestamp> getLastModifiedDates(MainActivity main){
        HashMap<String, Timestamp> temp = new HashMap<String, Timestamp>();
        for (String s : main.getFilesDir().list()){
            Recipe r = readRecipe(main, s);
            temp.put(r.getTitle(), r.getLastModificationDate());
        }

        return temp;
    }

    public static ArrayList<String> getRecipeFileNames(MainActivity main){
        List<String> files = Arrays.asList(main.getFilesDir().list());
        ArrayList<String> recs = new ArrayList<String>();
        for (String s : files){
            s = s.replaceAll(".rec", "");
            recs.add(s);
        }
        return recs;
    }
}
