package com.example.dogbreedanalyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;


public class results extends AppCompatActivity {
    MainActivity main = new MainActivity();
    private static final String TAG = "tag";
    String dog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        ImageButton next = (ImageButton) findViewById(R.id.bt_main);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }
        });

        Button gps = (Button) findViewById(R.id.bt_gps);
        gps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Activity2.class);
                startActivityForResult(myIntent, 0);
            }

        });


        final ImageView img = findViewById(R.id.imgView);
        final TextView breed = findViewById(R.id.breed);
        final TextView temp = findViewById(R.id.temp);
        final TextView pop = findViewById(R.id.pop);
        final TextView height = findViewById(R.id.height);
        final TextView weight = findViewById(R.id.weight);
        final TextView life = findViewById(R.id.life);
        final TextView group = findViewById(R.id.group);
        final TextView description = findViewById(R.id.desc);
        final TextView fun_fact = findViewById(R.id.ff);

        new Thread(new Runnable() {
            final dogScrape webpageScrape = new dogScrape();


            @Override
            public void run() {
                Log.i(TAG, "run: string " + main.results);
                String db = main.results.replace("_","-");
                String webPath = "https://www.akc.org/dog-breeds/".concat(db);
                try {
                    Document doc = Jsoup.connect(webPath).get();
                    Elements pool = doc.select("body");
                    Elements img = pool.select("section#breed-care").select("div").select("img");
                    Elements spans = pool.select("div").select("ul").select("span");

                    List<String> details = spans.eachText();
                    ArrayList<String> dogDetails = new ArrayList<String>();
                    Elements facts = pool.select("div").select("section#fact-slider").select("span");
                    ArrayList<String> dogFacts = new ArrayList<String>();
                    Random rand = new Random();



                    Elements para = pool.select("section");

                    //puts fun facts into String ArrayList
                    for(int i = 0; i < facts.size()-1; i++) {
                        if(!facts.select("span").get(i).text().isEmpty()) {
                            dogFacts.add(facts.select("span").get(i).text());
                        }
                    }

                    dogDetails.add(img.attr("data-src"));

                    //gets dog details
                    for(int i = 0; i < details.size()-1; i++)
                        if(details.get(i).equals("Temperament:")) {
                            dogDetails.add(details.get(i+1));
                        }
                        else if(details.get(i).equals("AKC Breed Popularity:")) {
                            dogDetails.add(details.get(i+1));
                        }
                        else if(details.get(i).equals("Height:")) {
                            dogDetails.add(details.get(i+1));
                        }
                        else if(details.get(i).equals("Weight:")) {
                            dogDetails.add(details.get(i+1));
                        }
                        else if(details.get(i).equals("Life Expectancy:")) {
                            dogDetails.add(details.get(i+1));
                        }
                        else if(details.get(i).equals("Group:")) {
                            dogDetails.add(details.get(i+1));
                        }

                    //Puts description and random fact into report
                    dogDetails.add(para.get(0).select("div").last().text());
                    dogDetails.add(dogFacts.get(rand.nextInt(dogFacts.size()-1)));

                    webpageScrape.setBreed(db.replace("-", " ").toUpperCase());
                    webpageScrape.setImgUrl(dogDetails.get(0));
                    webpageScrape.setTemp(dogDetails.get(1));
                    webpageScrape.setPop(dogDetails.get(2));
                    webpageScrape.setHeight(dogDetails.get(3));
                    webpageScrape.setWeight(dogDetails.get(4));
                    webpageScrape.setLife(dogDetails.get(5));
                    webpageScrape.setGroup(dogDetails.get(6));
                    webpageScrape.setDescription(dogDetails.get(7));
                    webpageScrape.setFun_fact(dogDetails.get(8));
                }

                catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        breed.setText(webpageScrape.getBreed());
                        Picasso.get()
                                .load(webpageScrape.getImgUrl())
                                .into(img);

                        temp.setText(webpageScrape.getTemp());
                        pop.setText(webpageScrape.getPop());
                        height.setText(webpageScrape.getHeight());
                        weight.setText(webpageScrape.getWeight());
                        life.setText(webpageScrape.getLife());
                        group.setText(webpageScrape.getGroup());
                        description.setText(webpageScrape.getDescription());
                        fun_fact.setText(webpageScrape.getFun_fact());

                    }
                });


            }
        }).start();





    }
}
