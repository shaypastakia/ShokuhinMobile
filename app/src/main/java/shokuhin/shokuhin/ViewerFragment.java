package shokuhin.shokuhin;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
    ScrollView scrollView;
    Recipe rec;
    ArrayList<String> ingredients = new ArrayList<String>();

    boolean autoSpeak = false;
    int index = -1;

    String highlightColour = "#5CC857";
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public ViewerFragment() {
        setRetainInstance(true);
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
        index = -1;
        View rootView = inflater.inflate(R.layout.fragment_viewer, container, false);
        int width = main.size.x;
        int height = main.size.y;
        textView = (TextView)rootView.findViewById(R.id.textView);
        infoButton = (Button)rootView.findViewById(R.id.infoButton);
        ingredientsButton = (Button)rootView.findViewById(R.id.ingredientsButton);
        methodButton = (Button)rootView.findViewById(R.id.methodButton);
        speakButton = (Button) rootView.findViewById(R.id.speakButton);
        prevButton = (Button) rootView.findViewById(R.id.prevButton);
        nextButton = (Button) rootView.findViewById(R.id.nextButton);
        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);

        if (width * height >= 4000000){
            textView.setTextSize(40);
            infoButton.setTextSize(35);
            ingredientsButton.setTextSize(35);
            methodButton.setTextSize(29);
            speakButton.setTextSize(35);
            prevButton.setTextSize(35);
            nextButton.setTextSize(35);
        }
        rec = main.recipe;
        try {
            rec.getTitle();
            Intent sendIntent = new Intent("com.google.android.keep");
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, rec.getTitle());

            String temp = "";
            for (String  s : rec.getIngredients()){
                if (rec.getIngredients().indexOf(s) != rec.getIngredients().size()-1)
                   temp = temp.concat(s + "\n");
                else
                   temp = temp.concat(s);
            }
            sendIntent.putExtra(Intent.EXTRA_TEXT, temp);
            sendIntent.setType("text/plain");
            main.setShareIntent(sendIntent);
        } catch (Exception e){
            Toast.makeText(main, "Unable to load Recipe", Toast.LENGTH_LONG).show();
            return null;
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextStep();
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevStep();
            }
        });

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String step = android.text.Html.fromHtml(rec.getMethodSteps().get(index)).toString();
                    if (step == null){
                        return;
                    }

                    if (step.length() > TextToSpeech.getMaxSpeechInputLength()){
                        main.speech.speak("Sorry, but the method step is too long for me to read.",
                                TextToSpeech.QUEUE_FLUSH, null, null);
                    }

                    if (main.firstSpeak){
                        Toast.makeText(main, "Press and Hold 'Speak' to enable AutoSpeak", Toast.LENGTH_LONG).show();
                        main.firstSpeak = false;
                    }
                    main.speech.speak(step, TextToSpeech.QUEUE_FLUSH, null, null);
                } catch (Exception e){
                    Toast.makeText(main, "Unable to produce Speech", Toast.LENGTH_SHORT).show();
                }
            }
        });

        speakButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (autoSpeak){
                    Toast.makeText(main, "Disabled AutoSpeak", Toast.LENGTH_SHORT).show();
                    autoSpeak = false;
                } else {
                    Toast.makeText(main, "Enabled AutoSpeak", Toast.LENGTH_SHORT).show();
                    autoSpeak = true;
                }
                return false;
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
                getIngredients(true);
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
        scrollView.scrollTo(0, 0);
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

    public void getIngredients(boolean display){
        ingredients.clear();
        scrollView.scrollTo(0, 0);
        prevButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        speakButton.setVisibility(View.INVISIBLE);
        String text = "";

        for (String s : rec.getIngredients()){
            text = text.concat(s + "<br><br>");
            String[] words = s.split(" ");
            for(String word : words) {
                if (word.length() > 0 && Character.isUpperCase(word.charAt(0))) {
                    ingredients.add(word);
                }
            }
        }
        if (display)
        textView.setText(Html.fromHtml(text));
    }

    public void getMethod(){
            getIngredients(false);
        scrollView.scrollTo(0, 0);
        prevButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        speakButton.setVisibility(View.VISIBLE);
        ArrayList<String> steps = rec.getMethodSteps();
        for (String s : steps){
            steps.set(steps.indexOf(s), steps.get(steps.indexOf(s)).replaceAll("\n", "<br>"));
        }
        for (String s : ingredients){
            for (String s2 : rec.getMethodSteps()){
                if (s2.contains(s) || s2.contains(s.toLowerCase())){
                    String temp = "";
                    temp = s2.replaceAll(s, "<font color=" + highlightColour + "><b>" + s + "</b></font>");
                    temp = temp.replaceAll(s.toLowerCase(), "<font color=" + highlightColour + "><b>" + s + "</b></font>");
                    steps.set(steps.indexOf(s2), temp);
                }
            }
        }

        if (index == -1) {

            textView.setText(Html.fromHtml(steps.get(index + 1)));
            index++;
        } else {
            textView.setText(Html.fromHtml(steps.get(index).replaceAll("\n", "<br>")));
        }
    }

    public void prevStep(){
        try {
            scrollView.scrollTo(0, 0);
            ArrayList<String> steps = rec.getMethodSteps();
            textView.setText(Html.fromHtml(steps.get(index - 1)));
            index--;
            if (autoSpeak)
                speakButton.performClick();
        } catch (Exception e){

        }
    }

    public void nextStep(){
        try {
            scrollView.scrollTo(0, 0);
            ArrayList<String> steps = rec.getMethodSteps();
            textView.setText(Html.fromHtml(steps.get(index + 1)));
            index++;
            if (autoSpeak)
                speakButton.performClick();
        } catch (Exception e){

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

}
