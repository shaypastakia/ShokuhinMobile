package shokuhin.shokuhin;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    PlaceholderFragment searchFragment = PlaceholderFragment.newInstance(0, this);
    ViewerFragment viewerFragment = ViewerFragment.newInstance(1, this);

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        if (position == 0)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(0, this))
                    .commit();

        if (position == 1)
        fragmentManager.beginTransaction()
                .replace(R.id.container, viewerFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        static MainActivity main;
        static ListView listView;
        static String[] recipes;
        static HashMap<String, Date> list;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, MainActivity _main) {

            main = _main;
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            try {
                new RetrieveFeedTask(main).execute();
                InputStream inStream = new FileInputStream(main.getFilesDir() + "/list.hmap");
                BufferedInputStream buff = new BufferedInputStream(inStream);
                ObjectInputStream obj = new ObjectInputStream(buff);
                list = (HashMap<String, Date>) obj.readObject();
                obj.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            listView = (ListView)rootView.findViewById(R.id.listView);

            recipes = new String[list.size()];
            int i = 0;
            for (String s : list.keySet()){
                recipes[i] = s;
                i++;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(main,android.R.layout.simple_list_item_1,recipes);
            listView.setAdapter(adapter);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        public void setPage(){

        }
    }

    /**
     * A Class to perform networking activity
     */
    static class RetrieveFeedTask extends AsyncTask<String, Void, Boolean> {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        URL url;
        MainActivity main;

        public RetrieveFeedTask(MainActivity _main){
            main = _main;
        }

        protected Boolean doInBackground(String... strings){
            try {
                URL url = new URL("http://194.83.236.93/~spastakia/Shokuhin/list.hmap");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception("Unable to connect to server");
                }

                input = connection.getInputStream();
                output = new FileOutputStream(main.getFilesDir() + "/list.hmap");
                System.out.print("**************THIS IS THE BEGINNING OF THE REST OF YOUR LIFE :: " + new File(main.getFilesDir() + "/list.hmap").exists());
                byte data[] = new byte[4096];
                long total = 0;
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
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ViewerFragment extends Fragment {
        static MainActivity main;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ViewerFragment newInstance(int sectionNumber, MainActivity _main) {
            main = _main;
            ViewerFragment fragment = new ViewerFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ViewerFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_viewer, container, false);
            WebView webView = (WebView) rootView.findViewById(R.id.webView);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl("http://www.google.co.uk");
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


}
