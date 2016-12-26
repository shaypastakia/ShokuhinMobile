package shokuhin.shokuhin;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import recipe.Recipe;

/**
 * Created by shayp on 04/10/2015.
 */
public class RecipeListFragment extends Fragment {

    MainActivity main;
    ListView listView;
    String[] recipes;
    ArrayList<String> list;
    String search;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public RecipeListFragment() {
        setRetainInstance(true);
    }

    public RecipeListFragment initialise(int sectionNumber, MainActivity _main, String _search){
        main = _main;
        if (_search != null)
            search = _search;

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        setArguments(args);

        try {

            if (search != null && !search.equals(""))
                list = new RetrieveListTask(main, search).execute().get();
            else
                list = new RetrieveListTask(main).execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }

    public RecipeListFragment initialise(int sectionNumber, MainActivity _main, ArrayList<String> _recipes){
        main = _main;

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        setArguments(args);

        list = _recipes;

        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView)rootView.findViewById(R.id.listView);

//      As per: http://antonioleiva.com/swiperefreshlayout/
        final SwipeRefreshLayout refresh;
        refresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        main.sync();
                        refresh.setRefreshing(false);
                    }
                });

            }
        });

        int width = main.size.x;
        try {
            recipes = new String[list.size()];
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(main, "Unable to load List", Toast.LENGTH_SHORT).show();
            return null;
        }
        int i = 0;
        for (String s : list){
            recipes[i] = s;
            i++;
        }
        Arrays.sort(recipes);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(main,android.R.layout.simple_list_item_1,recipes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                try {
                    RetrieveRecipeTask task = new RetrieveRecipeTask(main, recipes, position);
                    task.execute();
                    main.recipe = task.get();
                    main.setViewerFragment();
                    main.mNavigationDrawerFragment.selectItem(1);
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
