package shokuhin.shokuhin;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import recipe.Recipe;
import recipe.RecipeMethodsMobile;
import recipe.RequestURL;

/**
 * Created by shayp on 07/07/2016.
 */
public class SyncTask extends AsyncTask<String, Void, Boolean> {
        MainActivity main;

        public SyncTask(MainActivity main) {this.main = main;}

        protected Boolean doInBackground(String... params) {
            //Based on example at http://www.appifiedtech.net/2015/06/13/android-http-request-example/


            RequestURL urlTime = new RequestURL(MainActivity.url);
            urlTime.addParameter("type", "TIMES");
            RecipeMethodsMobile.getServerRecipes(main, new RequestURL(MainActivity.url));
            System.exit(0);
            HashMap<String, Timestamp> localTimes = RecipeMethodsMobile.getLastModifiedDates(main);


            RequestURL url = new RequestURL(MainActivity.url);
            url.addParameter("type", "MINE");
            String resp = RecipeMethodsMobile.request(url);
            try {
                ObjectMapper mapper = new ObjectMapper();
                ArrayList<String> recs = mapper.readValue(resp, ArrayList.class);
                for (String s : recs){
                    RequestURL req = new RequestURL(MainActivity.url);
                    req.addParameter("type", "REQUEST");
                    req.addParameter("recipe", s);
                    Recipe r = mapper.readValue(RecipeMethodsMobile.request(req), Recipe.class);
                    RecipeMethodsMobile.writeRecipe(main, r);
                }
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }


//            String dataUrl = "http://192.168.1.147:8080/ShokuhinServer/shokuhin";
//            URL url;
//            HttpURLConnection connection = null;
//            String responseStr = "";
//            Toast.makeText(main, "1 :: " + "test", Toast.LENGTH_SHORT).show();
//            try {
//                url = new URL(dataUrl);
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setUseCaches(false);
//                connection.setDoInput(true);
//                connection.setDoOutput(true);
//
//                //Use OutputStream to send GET
//                InputStream is = connection.getInputStream();
//                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//                String line;
//                StringBuffer response = new StringBuffer();
//                while ((line = rd.readLine()) != null) {
//                    response.append(line);
//                    response.append('\r');
//                }
//                rd.close();
//                responseStr = response.toString();
//            } catch (Exception e){
//                e.printStackTrace();
//            } finally {
//                connection.disconnect();
//            }
//            return responseStr;
        }
}
