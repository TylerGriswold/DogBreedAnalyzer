package com.example.dogbreedanalyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tag";
    private Button takePhotoButton;
    private Button selectPhotoButton;
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    ImageView imageView = null;
    Bitmap bitmap = null;
    Module module = null;
    Uri selectedImage = null;

    //https://developer.android.com/training/camera/photobasics
    String currentPhotoPath;
    String realPath = "";
    Intent intent = null;
   public static String results;
    int REQUEST_CODE;



    private File createImageFile() throws IOException {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, //prefix
                ".jpg", //suffix
                storageDir //directory
        );
        //Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        System.out.println(currentPhotoPath);
        return image;
    }

    //https://developer.android.com/training/camera/photobasics
    private void dispathTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that there is a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                //Error occurred while creating the file
                System.out.println("Error occurred while creating file.");
            }
            //Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //Add picture to the gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePhotoButton = (Button) findViewById(R.id.takePhotoButton);
        selectPhotoButton = (Button) findViewById(R.id.selectPhotoButton);

        //The below code allows for us to access the camera on the Android phone when clicking the respective button.
        //https://stackoverflow.com/questions/13977245/android-open-camera-from-button (not in use)
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                REQUEST_CODE = 0;
                dispathTakePictureIntent();
                galleryAddPic();
                startActivityForResult(intent, REQUEST_CODE);
                onActivityResult(REQUEST_CODE,RESULT_OK,intent);
            }
        });

        //https://stackoverflow.com/questions/11144783/how-to-access-an-image-from-the-phones-photo-gallery
        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                REQUEST_CODE = 1;
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE);
//                onActivityResult(1,RESULT_OK,intent);
//                onActivityResult(REQUEST_CODE,RESULT_OK,intent);
            }

        });
        onActivityResult(REQUEST_CODE,RESULT_OK,intent);

        Button gps = (Button) findViewById(R.id.bt_gps);
        gps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Activity2.class);
                startActivityForResult(myIntent, 0);
            }

        });

        Button r = (Button) findViewById(R.id.bt_results);
        r.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), results.class);
                startActivityForResult(myIntent, 0);
            }

        });

    }

    public String analysis(String realPath) { //maybe bitmap and module

     try
    {
        // creating bitmap from packaged into app android asset 'image.jpg',
        // app/src/main/assets/image.jpg
//        bitmap = BitmapFactory.decodeStream(getAssets().open(realPath));
        bitmap = BitmapFactory.decodeFile(realPath);
        Log.i(TAG, "analysis: bitmap "+ bitmap);
        // loading serialized torchscript module from packaged into app android asset model.pt,
        // app/src/model/assets/model.pt
        module = Module.load(assetFilePath(this, "ResNetDogs50_final.pt"));
    } catch(IOException e) {
        Log.e("PytorchHelloWorld", "Error reading assets", e);
        finish();
    }

    // preparing input tensor
    Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

    // running the model
    assert module !=null; //Added
    final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

    // getting tensor content as java array of floats
    final float[] scores = outputTensor.getDataAsFloatArray();

    // searching for the index with maximum score
    float maxScore = -Float.MAX_VALUE;
    int maxScoreIdx = -1;
    for(int i = 0; i<scores.length;i++) {
        if (scores[i] > maxScore) {
            maxScore = scores[i];
            maxScoreIdx = i;
        }
    }

    String className = ImageNetClasses.IMAGENET_CLASSES[maxScoreIdx];
    Toast.makeText(this, className, Toast.LENGTH_SHORT).show();

    return className;
//     showing className on UI

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String br;
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            realPath = ImageFilePath.getPath(MainActivity.this, data.getData());
//                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());

            Log.i(TAG, "onActivityResult: file path : " + realPath);
            results = analysis(realPath);
            Log.i(TAG, "onActivityResult: results " + results);

        } else if(requestCode == 0 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            currentPhotoPath = ImageFilePath.getPath(MainActivity.this, data.getData());
//                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());

            Log.i(TAG, "onActivityResult: file path : " + currentPhotoPath);
            results = analysis(currentPhotoPath);

            }else{
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
        }

    }
//    void scrapeme(){
//        final ImageView img = findViewById(R.id.imgView);
//        final TextView breed = findViewById(R.id.breed);
//        final TextView temp = findViewById(R.id.temp);
//        final TextView pop = findViewById(R.id.pop);
//        final TextView height = findViewById(R.id.height);
//        final TextView weight = findViewById(R.id.weight);
//        final TextView life = findViewById(R.id.life);
//        final TextView group = findViewById(R.id.group);
//        final TextView description = findViewById(R.id.desc);
//        final TextView fun_fact = findViewById(R.id.ff);
//        new Thread(new Runnable() {
//            final dogScrape webpageScrape = new dogScrape();
//
//            @Override
//            public void run() {
//                String db = results.replace("_", "-");
//                String webPath = "https://www.akc.org/dog-breeds/".concat(db);
//                Log.i(TAG, "run: webpage: " + webPath);
//                try {
//                    Document doc = Jsoup.connect(webPath).get();
//                    Elements pool = doc.select("body");
//                    Elements img = pool.select("section#breed-care").select("div").select("img");
//                    Elements spans = pool.select("div").select("ul").select("span");
//
//                    List<String> details = spans.eachText();
//                    ArrayList<String> dogDetails = new ArrayList<String>();
//                    Elements facts = pool.select("div").select("section#fact-slider").select("span");
//                    ArrayList<String> dogFacts = new ArrayList<String>();
//                    Random rand = new Random();
//
//
//
//                    Elements para = pool.select("section");
//
//                    //puts fun facts into String ArrayList
//                    for(int i = 0; i < facts.size()-1; i++) {
//                        if(!facts.select("span").get(i).text().isEmpty()) {
//                            dogFacts.add(facts.select("span").get(i).text());
//                        }
//                    }
//
//                    dogDetails.add(img.attr("data-src"));
//
//                    //gets dog details
//                    for(int i = 0; i < details.size()-1; i++)
//                        if(details.get(i).equals("Temperament:")) {
//                            dogDetails.add(details.get(i+1));
//                        }
//                        else if(details.get(i).equals("AKC Breed Popularity:")) {
//                            dogDetails.add(details.get(i+1));
//                        }
//                        else if(details.get(i).equals("Height:")) {
//                            dogDetails.add(details.get(i+1));
//                        }
//                        else if(details.get(i).equals("Weight:")) {
//                            dogDetails.add(details.get(i+1));
//                        }
//                        else if(details.get(i).equals("Life Expectancy:")) {
//                            dogDetails.add(details.get(i+1));
//                        }
//                        else if(details.get(i).equals("Group:")) {
//                            dogDetails.add(details.get(i+1));
//                        }
//
//                    //Puts description and random fact into report
//                    dogDetails.add(para.get(0).select("div").last().text());
//                    dogDetails.add(dogFacts.get(rand.nextInt(dogFacts.size()-1)));
//
//                    webpageScrape.setBreed(db.toUpperCase());
//                    webpageScrape.setImgUrl(dogDetails.get(0));
//                    webpageScrape.setTemp(dogDetails.get(1));
//                    webpageScrape.setPop(dogDetails.get(2));
//                    webpageScrape.setHeight(dogDetails.get(3));
//                    webpageScrape.setWeight(dogDetails.get(4));
//                    webpageScrape.setLife(dogDetails.get(5));
//                    webpageScrape.setGroup(dogDetails.get(6));
//                    webpageScrape.setDescription(dogDetails.get(7));
//                    webpageScrape.setFun_fact(dogDetails.get(8));
//                }
//
//                catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        breed.setText(webpageScrape.getBreed());
//                        Picasso.get()
//                                .load(webpageScrape.getImgUrl())
//                                .into(img);
//
//                        temp.setText(webpageScrape.getTemp());
//                        pop.setText(webpageScrape.getPop());
//                        height.setText(webpageScrape.getHeight());
//                        weight.setText(webpageScrape.getWeight());
//                        life.setText(webpageScrape.getLife());
//                        group.setText(webpageScrape.getGroup());
//                        description.setText(webpageScrape.getDescription());
//                        fun_fact.setText(webpageScrape.getFun_fact());
//
//                    }
//                });
//
//
//            }
//        }).start();
//    }

//    Dog Analyzer
    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }




}
