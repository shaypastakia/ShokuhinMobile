package shokuhin.shokuhin;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import recipe.Recipe;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    MainActivity main = this;
    RecipeListFragment searchFragment;// = new RecipeListFragment().initialise(0, this, (String)null);
    ViewerFragment viewerFragment = new ViewerFragment().initialise(1, this);
    public Recipe recipe;
    Point size = new Point();
    public TextToSpeech speech;
    String searchTerm;
    public boolean firstSpeak = true;
    public ShareActionProvider share;
    public File dir;
    public static String url = "192.168.1.147";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchFragment = new RecipeListFragment().initialise(0, this, (String) null);

        dir = getFilesDir();
        if (speech == null) {
            speech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        speech.setLanguage(Locale.UK);
                    }
                }
            });
        }
        getWindowManager().getDefaultDisplay().getSize(size);
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
    protected void onDestroy() {
        super.onDestroy();
        File dir = getCacheDir();
        try {
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
                deleteDir(getFilesDir());
            }
        } catch (Exception e) {
        }
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        if (position == 0 && searchFragment == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new RecipeListFragment().initialise(0, this, (String) null))
                    .commit();
            mTitle = "Recipe List";
        }

        if (position == 0 && searchFragment != null){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, searchFragment)
                    .commit();
            searchFragment = null;
            mTitle = "Recipe List";
        }

        if (position == 1) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, viewerFragment)
                    .commit();
            mTitle = "Recipe Viewer";
        }
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


        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.main, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        share = (ShareActionProvider) item.getActionProvider();

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.main, menu);
//            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void setShareIntent(Intent shareIntent) {
        if (share != null) {
            share.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getTitle() != null && item.getTitle().equals("Title Search")) {

            //Text Entry Dialog based on Aaron, http://stackoverflow.com/questions/10903754/input-text-dialog-android
            AlertDialog.Builder entry = new AlertDialog.Builder(this);
            entry.setTitle("Enter a search term");
            final EditText textBox = new EditText(this);
            textBox.setInputType(InputType.TYPE_CLASS_TEXT);
            entry.setView(textBox);
            entry.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    searchTerm = textBox.getText().toString();
                    if (searchTerm == null) {
                        return;
                    }

                    if (searchTerm == "") {
                        searchTerm = null;
                        searchFragment = null;
                        onNavigationDrawerItemSelected(0);
                        return;
                    }

                    searchFragment = new RecipeListFragment().initialise(0, main, searchTerm);
                    searchTerm = null;
                    onNavigationDrawerItemSelected(0);
                }
            });

            entry.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    return;
                }
            });

            entry.show();
        } else if (item.getTitle() != null && item.getTitle().equals("Tags Search")) {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);

            if (!connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
                Toast.makeText(main, "Feature unavailable using 3G.", Toast.LENGTH_SHORT).show();
                return false;
            }

            //Text Entry Dialog based on Aaron, http://stackoverflow.com/questions/10903754/input-text-dialog-android
            AlertDialog.Builder entry = new AlertDialog.Builder(this);
            entry.setTitle("Enter tags, separated by a comma.");
            final EditText textBox = new EditText(this);
            textBox.setInputType(InputType.TYPE_CLASS_TEXT);
            entry.setView(textBox);
            entry.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (textBox.getText().toString() == null || textBox.getText().toString().equals("")) {
                        searchFragment = null;
                        onNavigationDrawerItemSelected(0);
                        return;
                    }

                    try {
                        String[] terms = textBox.getText().toString().split(",");
                        for (int i = 0; i < terms.length; i++)
                            terms[i] = terms[i].trim().toLowerCase();

                        ArrayList<String> titles = new ArrayList<String>();
                        for (Recipe r : new RetrieveAllRecipesTask(main).execute().get()) {
                            ArrayList<String> tags = r.getTags();
                            for (int i = 0; i < tags.size(); i++)
                                tags.set(i, tags.get(i).toLowerCase());

                            if (tags.containsAll(Arrays.asList(terms))) {
                                titles.add(r.getTitle());
                            }
                        }

                        searchFragment = new RecipeListFragment().initialise(0, main, titles);
                        onNavigationDrawerItemSelected(0);


                    } catch (Exception e) {
                        Toast.makeText(main, "Unable to perform a Tags search: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            entry.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    return;
                }
            });

            entry.show();
        } else if (item.getTitle() != null && item.getTitle().equals("Sync")) {
            sync();
        }

        return super.onOptionsItemSelected(item);
    }

    public void setViewerFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, viewerFragment)
                .commitAllowingStateLoss();
    }


    @Override
    public void onBackPressed() {
        searchTerm = null;
        searchFragment = null;
        onNavigationDrawerItemSelected(0);
    }

    public void sync(){
        try {
            if (new SyncTask(this).execute().get()){
                Toast.makeText(MainActivity.this, "Sync Complete!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Sync Error :'(", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
