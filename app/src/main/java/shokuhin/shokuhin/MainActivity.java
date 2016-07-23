package shokuhin.shokuhin;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringTokenizer;

import recipe.Recipe;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final int CONTAIN = 0;
    public static final int ENTITLED = 1;
    public static final int TAGGED = 2;
    public static final int COUNT = 3;
    public static final int UNCOOKED = 4;

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

        if (item.getItemId() == R.id.title_search) {

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
        } else if (item.getItemId() == R.id.advanced_search) {
//            ConnectivityManager connMgr = (ConnectivityManager)
//                    getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            if (!connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
//                Toast.makeText(main, "Feature unavailable using 3G.", Toast.LENGTH_SHORT).show();
//                return false;
//            }

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
        } else if (item.getItemId() == R.id.sync) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    sync();
                }
            });

        } else if (item.getItemId() == R.id.voice) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
//                  As per: http://www.androidhive.info/2014/07/android-speech-to-text-tutorial/
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What would you like to search for?");
                    try {
                        startActivityForResult(intent, 100);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(getApplicationContext(), "Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If the request code isn't for Voice Commands, exit.
        if (requestCode != 100)
            return;

        //Store the phrase spoken, as well as a list of recipes on the device.
        String speech = "";
        ArrayList<Recipe> recipes = null;

        ArrayList<String> results = new ArrayList<String>();
        //Store the function, as specified in 'speech'.
        int function;

        //If the results were properly captured, continue.
        //Assign the speech and a list of all Recipes to variables.
        if (resultCode == RESULT_OK && null != data) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            speech = result.get(0).toLowerCase();
            try {
                recipes = new RetrieveAllRecipesTask(main).execute().get();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Unable to retrieve recipes. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(MainActivity.this, "Unable to process Voice Command", Toast.LENGTH_SHORT).show();
            return;
        }

        //At this point, Speech and Recipes are assigned.
        //Establish the function, and assign it to 'function'.
        String query = "";

        if (speech.contains("contain")) {
            function = CONTAIN;
        } else if (speech.contains("entitled")) {
            function = ENTITLED;
        } else if (speech.contains("tagged")) {
            function = TAGGED;
        } else if (speech.contains("haven't")) {
            function = UNCOOKED;
            try {
                for (Recipe r : new RetrieveAllRecipesTask(main).execute().get()) {
                    if (r.getRating() == 0) {
                        results.add(r.getTitle());
                    }
                }
                speak("Here are recipes you haven't cooked yet");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Unable to find recipes", Toast.LENGTH_SHORT).show();
            }
        }else if (speech.contains("many")){ //Special Case
            function = COUNT;
            speak("There are " + recipes.size() + " recipes on this device");
            return;
        } else {
            speak("Unable to recognise " + speech);
            return;
        }

        //For the CONTAIN, ENTITLED, and TAGGED functions, elicit a Query.
        query = getQuery(function, speech);

        //At this point, COUNT and UNCOOKED have been completed.
        //CONTAIN, ENTITLED, and TAGGED require further processing.
        //It is now "function" and "Query" being processed.

        //First, split the Query by AND.
        ArrayList<String> terms = new ArrayList<String>();
        if (!query.contains("and"))
            terms.add(query);
        else {
            String[] tokens = query.split("and");
            terms.addAll(Arrays.asList(tokens));
            int i = 0;
            for (String s : terms){
                i = terms.indexOf(s);
                terms.set(i, s.trim());
            }
        }

        //'terms' now provides a list of search terms.
        //Process the recipes with the terms, and elicit the results.
        String phrase = "Here are recipes containing ";
        if (terms.size() == 1){
            phrase = phrase.concat(terms.get(0));
        } else {
            for (String s : terms){
                phrase = phrase.concat(s);
                if (terms.indexOf(s) < terms.size()-1)
                    phrase = phrase.concat(", ");
            }
        }
        switch (function){
            case CONTAIN:
                results = contain(recipes, terms);
                phrase = phrase.concat(" in their ingredients");
                break;

            case ENTITLED:
                results = entitled(recipes, terms);
                phrase = phrase.concat(" in their title");
                break;

            case  TAGGED:
                results = tagged(recipes, terms);
                phrase = phrase.concat(" in their tags");
                break;
        }

        //Results are passed into the "results" ArrayList, and these are then processed.
        searchFragment = new RecipeListFragment().initialise(0, main, results);
        onNavigationDrawerItemSelected(0);
        speak(phrase);
    }

    private ArrayList<String> tagged(ArrayList<Recipe> recipes, ArrayList<String> terms){
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<Recipe> temp = new ArrayList<Recipe>();
        temp.addAll(recipes);

        for (String s : terms){
            for (Recipe r : recipes){
                if (!searchHelper(r.getTags(), s))
                    temp.remove(r);
            }
        }

        for (Recipe r : temp)
            results.add(r.getTitle());

        return results;
    }

    private ArrayList<String> contain(ArrayList<Recipe> recipes, ArrayList<String> terms){
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<Recipe> temp = new ArrayList<Recipe>();
        temp.addAll(recipes);

        for (String s : terms){
            for (Recipe r : recipes){
                if (!searchHelper(r.getIngredients(), s))
                    temp.remove(r);
            }
        }

        for (Recipe r : temp)
            results.add(r.getTitle());

        return results;
    }

    //Aid the CONTAIN and TAGGED searches
    private boolean searchHelper(ArrayList<String> list, String term){
        for (String s : list){
            if (s.toLowerCase().contains(term))
                return true;
        }

        return false;
    }

    private ArrayList<String> entitled (ArrayList<Recipe> recipes, ArrayList<String> terms){
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<Recipe> temp = new ArrayList<Recipe>();
        temp.addAll(recipes);

        for (String s : terms){
            for (Recipe r : recipes){
                if (!r.getTitle().toLowerCase().contains(s))
                    temp.remove(r);
            }
        }

        for (Recipe r : temp){
            results.add(r.getTitle());
        }
        return results;
    }

    //Remove the function part of the speech, returning only the query
    private String getQuery(int function, String speech){
        Log.d("Speech", speech);
        int index = 0;
        ArrayList<String> words = new ArrayList<String>();
        ArrayList<String> wordsCopy = new ArrayList<String>();

        StringTokenizer token = new StringTokenizer(speech);
        while (token.hasMoreTokens()){
            words.add(token.nextToken());
        }

        wordsCopy.addAll(words);

        if (function == CONTAIN){
            for (String s : words){
                if (!s.contains("contain"))
                    wordsCopy.remove(s);
                else {
                    wordsCopy.remove(0);
                    break;
                }
            }
        } else if (function == ENTITLED){
            for (String s : words){
                if (!s.contains("entitled"))
                    wordsCopy.remove(s);
                else {
                    wordsCopy.remove(0);
                    break;
                }
            }
        } else if (function == TAGGED){
            for (String s : words){
                if (!s.contains("tagged"))
                    wordsCopy.remove(s);
                else {
                    wordsCopy.remove(0);
                    break;
                }
            }
            if (wordsCopy.get(0).equals("with"))
                wordsCopy.remove(0);
        }

        //Reconstruct a sentence from the ArrayList. Don't add a space if it's the last word.
        String temp = "";
        for (String s : wordsCopy){
            temp += s;
            if (wordsCopy.indexOf(s) < wordsCopy.size()-1)
                temp += " ";
        }

        //Return the sentence.
        return temp;
    }

    //Convenience method for using the system voice
    private void speak(String s){
        speech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
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
