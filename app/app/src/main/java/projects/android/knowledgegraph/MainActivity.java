package projects.android.knowledgegraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {
    private final String[] RELATIONS = new String[]{"IsA", "HasA", "UsedFor", "CapableOf", "SimilarTo", "MadeOf"};
    private final String[] RELATIONS_NATURAL = new String[]{"is a ", "has ", "is used for ", "is capable of ", "is similar to ", "is made of "};
    private HashMap<String, String> relationToLanguage;

    private TextView[] relationViews;
    private EditText target;
    private TextView prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUp();
    }

    private void setUp(){
        target = findViewById(R.id.target);
        prompt = findViewById(R.id.prompt);
        relationViews = new TextView[] {findViewById(R.id.rel1), findViewById(R.id.rel2), findViewById(R.id.rel3),
                findViewById(R.id.rel4), findViewById(R.id.rel5), findViewById(R.id.rel6)};


        relationToLanguage = new HashMap<>();
        for (int i = 0; i < RELATIONS.length; i++){
            relationToLanguage.put(RELATIONS[i], RELATIONS_NATURAL[i]);
        }
    }

    public void buildKnowledgeGraph(View view) {
        prompt.setText("Loading...");
        for (TextView relationView : relationViews){
            relationView.setText("");
        }

        final String targetWord = target.getText().toString();
        final Context context = this.getApplicationContext();

        new AsyncTask<Void, Void, HashMap<String, ArrayList<String>>>(){
            @Override
            protected HashMap<String, ArrayList<String>> doInBackground(Void... voids){
                CountDownLatch latch = new CountDownLatch(RELATIONS.length);
                HashMap<String, ArrayList<String>> knowledgeGraph = new HashMap<>();

                for (String relation : RELATIONS){
                    ConceptNetQuery worker = new ConceptNetQuery(targetWord.toLowerCase(), relation, knowledgeGraph, context, latch);
                    worker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                try {
                    latch.await();
                    return knowledgeGraph;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(HashMap<String, ArrayList<String>> knowledgeGraph){
                prompt.setText("A " + targetWord + "...");
                int view = 0;
                for (int i = 0; i < knowledgeGraph.keySet().size(); i++){
                    String relation = RELATIONS[i];
                    ArrayList<String> endPoints = knowledgeGraph.get(relation);
                    String results = endPoints.size() > 0 ? endPoints.toString() : "No results for this relation";
                    relationViews[i].setText(relationToLanguage.get(relation) + "\n" + results);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);;
    }

}