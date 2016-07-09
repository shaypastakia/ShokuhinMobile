package shokuhin.shokuhin;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by shayp on 07/07/2016.
 */
public class SyncTask extends AsyncTask<String, Void, String> {
        MainActivity main;

        public SyncTask(MainActivity main) {this.main = main;}

        protected String doInBackground(String... params) {
            //Based on example at http://www.appifiedtech.net/2015/06/13/android-http-request-example/
            String dataUrl = "http://192.168.1.147:8080/ShokuhinServer/shokuhin";
            URL url;
            HttpURLConnection connection = null;
            String responseStr = "";
            Toast.makeText(main, "1 :: " + "test", Toast.LENGTH_SHORT).show();
            try {
                url = new URL(dataUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

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
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return responseStr;
        }
}
