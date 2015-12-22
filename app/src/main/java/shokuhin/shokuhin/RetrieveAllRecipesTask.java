package shokuhin.shokuhin;

import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;

import recipe.Recipe;

/**
 * Created by shayp on 22/12/2015.
 */
public class RetrieveAllRecipesTask extends AsyncTask<String, Void, ArrayList<Recipe>> {
    MainActivity main;
    ArrayList<String> recipeNames;
    ArrayList<Recipe> recipes = new ArrayList<Recipe>();

    public RetrieveAllRecipesTask(MainActivity _main){
        main = _main;
        try {
            recipeNames = new RetrieveListTask(main).execute().get();

        String[] list = new String[recipeNames.size()];
        int i = 0;
        for (String s : recipeNames){
            list[i] = s;
            i++;
        }

        i = 0;
        for (String s : list) {
            recipes.add(new RetrieveRecipeTask(main, list, i).execute().get());
            i++;
        }

        } catch (Exception e){
            Toast.makeText(main, "Unable to retrieve Recipe List for Tag Search: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    protected ArrayList<Recipe> doInBackground(String... strings) {

        return recipes;
    }

}
