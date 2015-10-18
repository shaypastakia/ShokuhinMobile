package shokuhin.shokuhin;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import recipe.Recipe;

/**
 * Created by shayp on 04/10/2015.
 */
public class RecipeListFragment extends Fragment {

    MainActivity main;
    ListView listView;
    String[] recipes;
    ConcurrentHashMap<String, Date> list;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public RecipeListFragment() {
    }

    public RecipeListFragment initialise(int sectionNumber, MainActivity _main){
        main = _main;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        setArguments(args);

        try {
//            new RetrieveListTask(main).execute();
            InputStream inStream = new FileInputStream(new RetrieveListTask(main).execute().get());
            BufferedInputStream buff = new BufferedInputStream(inStream);
            ObjectInputStream obj = new ObjectInputStream(buff);
            HashMap<String, Date> ls = (HashMap<String, Date>) obj.readObject();
            list = new ConcurrentHashMap<>(ls);
            obj.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView)rootView.findViewById(R.id.listView);

        try {
            recipes = new String[list.size()];
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(main, "Unable to load List", Toast.LENGTH_SHORT).show();
            return null;
        }
        int i = 0;
        for (String s : list.keySet()){
            recipes[i] = s;
            i++;
        }
        Arrays.sort(recipes);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(main,android.R.layout.simple_list_item_1,recipes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Recipe recipe;
                try {
//                        Toast.makeText(main, "1", Toast.LENGTH_LONG).show();
                    RetrieveRecipeTask task = new RetrieveRecipeTask(main, recipes, position);
                    task.execute();
                    main.recipe = (Recipe)task.get();
//                        Toast.makeText(main, "2", Toast.LENGTH_LONG).show();
//                        Toast.makeText(main, "3", Toast.LENGTH_LONG).show();
                        main.getFragmentManager().beginTransaction()
                                .replace(R.id.container, main.viewerFragment)
                                .commit();
//                        Toast.makeText(main, "4", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(main, "FAILED", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
