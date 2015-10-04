package shokuhin.shokuhin;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shayp on 04/10/2015.
 */
public class RetrieveListTask extends AsyncTask<String, Void, Boolean> {
    InputStream input = null;
    OutputStream output = null;
    HttpURLConnection connection = null;
    MainActivity main;

    public RetrieveListTask(MainActivity _main){
        main = _main;
    }

    protected Boolean doInBackground(String... strings){
        try {
            URL url = new URL("http://194.83.236.93/~spastakia/Shokuhin/list.hmap");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Toast.makeText(main, "Failed to connect", Toast.LENGTH_LONG).show();
                throw new Exception("Unable to connect to server");
            }

            input = connection.getInputStream();
            output = new FileOutputStream(main.getFilesDir() + "/list.hmap");
            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.close();
            input.close();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
