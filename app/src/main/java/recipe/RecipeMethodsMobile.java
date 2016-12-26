package recipe;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.List;
import java.util.TreeMap;

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

    public static Recipe requestRecipe (MainActivity main, String title){
        //Request a Recipe, and parse it from JSON
        String responseStr;
        RequestURL req = new RequestURL(MainActivity.url);
        req.addParameter("type", "REQUEST");
        req.addParameter("recipe", title);

        responseStr = RecipeMethodsMobile.request(req);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Recipe r = mapper.readValue(responseStr, Recipe.class);
            return  r;
        } catch (Exception e){
            e.printStackTrace();
            Log.d("RequestRecipe", e.getMessage());
            return null;
        }
    }

    public static TreeMap<String, String> getServerRecipes(MainActivity main, RequestURL _url){
        TreeMap<String, String> temp = new TreeMap<String, String>();
        try {
            TreeMap<String, Timestamp> localRecs = RecipeMethodsMobile.getLastModifiedDates(main);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(localRecs);

            _url.addParameter("type", "SENDTIMES");
            HttpURLConnection connection = getConnection(_url);


            //Send the local times to the server
            connection.setRequestProperty("Content-Type", "text/plain");
//            connection.setRequestProperty("charset", "utf-8");
//            connection.setRequestProperty("Content-Length", "" + Integer.toString(json.getBytes().length));
            BufferedWriter bufWrite = new BufferedWriter(new OutputStreamWriter((connection.getOutputStream()), "UTF-8"));
            bufWrite.write(json);
            bufWrite.flush();
            bufWrite.close();

            //Receive the NEW UPDATE DELETE list from the server
            BufferedReader bufRead = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = bufRead.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            bufRead.close();
            String responseStr = response.toString();
            Log.e("response", "" + responseStr);
            temp = mapper.readValue(responseStr, TreeMap.class);
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
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Connection", "close");
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            String prop = System.getProperty("http.agent");
            connection.setRequestProperty("User-Agent", prop);

            connection.setRequestProperty("Accept", "*/*");
            return connection;
        } catch (Exception e){
            return null;
        }
    }

    public static boolean isServerOnline(RequestURL urlString){
        try {
            urlString.addParameter("type", "ONLINE");
            if (request(urlString) == null)
                return false;
            else
                return true;
        } catch (Exception e){
            return false;
        }
    }


    public static boolean writeRecipe(MainActivity main, Recipe r){
        try {
//            FileOutputStream fos = main.openFileOutput(r.getTitle() + ".rec", Context.MODE_PRIVATE);
            FileOutputStream fos = new FileOutputStream(new File(main.getExternalFilesDir(null), r.getTitle() + ".rec"));
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
//            FileInputStream fis = main.openFileInput(title + ".rec");
            FileInputStream fis = new FileInputStream(new File(main.getExternalFilesDir(null), title + ".rec"));
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

    public static boolean deleteRecipe(MainActivity main, String title){
//        return main.deleteFile(title + ".rec");

        File file = new File(main.getExternalFilesDir(null), title + ".rec");
        if (file.delete())
            return true;
        else {
            file.deleteOnExit();
            return false;
        }
    }

    public static TreeMap<String, Timestamp> getLastModifiedDates(MainActivity main){
        TreeMap<String, Timestamp> temp = new TreeMap<String, Timestamp>();
//        for (String s : main.getFilesDir().list()){
        for (String s : main.getExternalFilesDir(null).list()){
            Recipe r = readRecipe(main, s.replaceAll(".rec", ""));
            temp.put(r.getTitle(), r.getLastModificationDate());
        }

        return temp;
    }

    public static ArrayList<String> getRecipeFileNames(MainActivity main){
//        List<String> files = Arrays.asList(main.getFilesDir().list());
        List<String> files = Arrays.asList(main.getExternalFilesDir(null).list());
        ArrayList<String> recs = new ArrayList<String>();
        for (String s : files){
            s = s.replaceAll(".rec", "");
            recs.add(s);
        }
        return recs;
    }
}
