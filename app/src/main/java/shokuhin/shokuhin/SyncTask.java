package shokuhin.shokuhin;

import android.os.AsyncTask;

import java.util.TreeMap;

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
            TreeMap<String, String> results = RecipeMethodsMobile.getServerRecipes(main, new RequestURL(MainActivity.url));
            for (String s : results.keySet()){
                if (results.get(s).equals("NEW"))
                    newRecipe(s);
                else if (results.get(s).equals("UPDATE"))
                    updateRecipe(s);
                else if (results.get(s).equals("DELETE"))
                    RecipeMethodsMobile.deleteRecipe(main, s);
                else
                    return false;
            }
            return true;
        }

    private void newRecipe(String title){
        Recipe rec = RecipeMethodsMobile.requestRecipe(main, title);
        RecipeMethodsMobile.writeRecipe(main, rec);
    }

    private void updateRecipe(String title){
        RecipeMethodsMobile.deleteRecipe(main, title);
        Recipe rec = RecipeMethodsMobile.requestRecipe(main, title);
        RecipeMethodsMobile.writeRecipe(main, rec);
    }
}
