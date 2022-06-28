package com.example.birddetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.birddetector.ml.LiteModelAiyVisionClassifierBirdsV13;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button loadImage;      /*intializing the widgets*/
    ImageView birdImage;
    TextView result;
    TextView link;
    Uri imageUri;
    Button camera;
    ImageView volume;
    TextToSpeech textToSpeech;
    private static final int  pic_id=2;
    private static final int PICK_IMAGE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  /*assigning the id's to the widgets*/
        loadImage = findViewById(R.id.loadImage);    
        birdImage = findViewById(R.id.imageView);        
        result = findViewById(R.id.resultTextView);
        link = findViewById(R.id.linkTextView);
        camera = findViewById(R.id.cameraButton);
        volume = findViewById(R.id.sound);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() { /*text to speech class used so as to spell the scientific
                                                                                                        name of the bird*/
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR)
                    textToSpeech.setLanguage(Locale.UK);
            }
        });
        volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(result.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
            }
        });
        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
       link.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://google.com/search?q="+result.getText().toString()));
               startActivity(intent);
           }
       });
     camera.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
              Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); /* intent started so as to open the camera and click a pic  of bird*/
              startActivityForResult(camera_intent,pic_id);
         }
     });

    }



    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI); /*intent started so as to open the gallery and pick the image of 
        startActivityForResult(gallery, PICK_IMAGE);                                                   of bird */
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1: if (resultCode==RESULT_OK){
                imageUri = data.getData();
                Bitmap imageBitmap=null;
                try {
                    imageBitmap = URItoBitmap(imageUri);  /* this case defines when the image is clicked through camera and image uri is converted to bitmap*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //birdImage.setImageURI(imageUri);
                birdImage.setImageBitmap(imageBitmap);
                link.setText("Click here for more information");
                outputGenerator(imageBitmap);
            }
                 break;
            case 2: if (resultCode==RESULT_OK){
                Bundle bundle = data.getExtras();           /* this case defines when the image is taken through gallery */
                Bitmap bitmapImage = (Bitmap) bundle.get("data");
                birdImage.setImageBitmap(bitmapImage);
                outputGenerator(bitmapImage);
            }
        }
    }
    private void outputGenerator(Bitmap imageBitmap) {
        try {
            LiteModelAiyVisionClassifierBirdsV13 model = LiteModelAiyVisionClassifierBirdsV13.newInstance(MainActivity.this);    //Machine learning model used

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imageBitmap);

            // Runs model inference and gets result.
            LiteModelAiyVisionClassifierBirdsV13.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();
            
            // calculates the maximum probable case from the listed probablities
            int index=0;
            float max=probability.get(0).getScore();
            for (int i=0;i< probability.size();i++) {
                if (max < probability.get(i).getScore()) {
                    max = probability.get(i).getScore();
                    index = i;
                }
            }

            Category output = probability.get(index);
            result.setText(output.getLabel());

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private Bitmap URItoBitmap(Uri imageUri) throws IOException {          
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);  //image uri converted to bitmap
    }

}
