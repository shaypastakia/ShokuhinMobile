package shokuhin.shokuhin;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.text.method.CharacterPickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import recipe.Recipe;

/**
 * Created by shayp on 04/10/2015.
 */
public class ViewerFragment extends Fragment {
    MainActivity main;
    Button infoButton;
    Button ingredientsButton;
    Button methodButton;
    Button speakButton;
    Button prevButton;
    Button nextButton;
    TextView textView;
    Recipe rec;
    TextToSpeech speech;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public ViewerFragment() {
    }

    public ViewerFragment initialise(int sectionNumber, MainActivity _main){
        main = _main;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        setArguments(args);
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_viewer, container, false);
        textView = (TextView)rootView.findViewById(R.id.textView);
        infoButton = (Button)rootView.findViewById(R.id.infoButton);
        ingredientsButton = (Button)rootView.findViewById(R.id.ingredientsButton);
        methodButton = (Button)rootView.findViewById(R.id.methodButton);
        speakButton = (Button) rootView.findViewById(R.id.speakButton);
        prevButton = (Button) rootView.findViewById(R.id.prevButton);
        nextButton = (Button) rootView.findViewById(R.id.nextButton);
        rec = main.recipe;
        try {
            rec.getTitle();
        } catch (Exception e){
            Toast.makeText(main, "Unable to load Recipe", Toast.LENGTH_LONG).show();
            return null;
        }

        speech = new TextToSpeech(main, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    speech.setLanguage(Locale.UK);
                }
            }
        });

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = "cheese is a type of meat";
                speech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInfo();
            }
        });

        ingredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIngredients();
            }
        });

        methodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMethod();
            }
        });

        getInfo();
        return rootView;
    }

    public void getInfo(){
        prevButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        speakButton.setVisibility(View.INVISIBLE);
        String course = "";
        String tags = "";
        switch (rec.getCourse()){
            case 0:
                course = "Breakfast";
                break;
            case 1:
                course = "Lunch";
                break;
            case 2:
                course = "Dinner";
                break;
            case 3:
                course = "Dessert";
                break;
            case 4:
                course = "Snack";
                break;
            case 5:
                course = "General";
                break;
        }

        int size = rec.getTags().size();
        for (String s : rec.getTags()){
            tags = tags.concat(s);
            if (rec.getTags().indexOf(s) < size-1)
                tags = tags.concat(", ");
            else
                tags = tags.concat(".");
        }

        textView.setText(rec.getTitle() + "\n\nPreparation Time: " + rec.getPrepTime() + " mins" +
                "\n\nCooking Time: " + rec.getCookTime() + " mins" + "\n\nRating: " +
                rec.getRating() + "/5" + "\n\nServes: " + rec.getServings() +
                "\n\nCourse: " + course + "\n\nTags: " + tags);
    }

    public void getIngredients(){
        prevButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        speakButton.setVisibility(View.INVISIBLE);
        String text = "";

        for (String s : rec.getIngredients()){
            text = text.concat(s + "\n\n");
        }
        textView.setText(text);
    }

    public void getMethod(){
        prevButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        speakButton.setVisibility(View.VISIBLE);
        textView.setText("");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

}
