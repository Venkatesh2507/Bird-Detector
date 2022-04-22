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

public class MainActivity extends AppCompatActivity {
    Button loadImage;
    ImageView birdImage;
    TextView result;
    TextView link;
    Uri imageUri;
    Button camera;
    private static final int  pic_id=2;
    private static final int PICK_IMAGE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadImage = findViewById(R.id.loadImage);
        birdImage = findViewById(R.id.imageView);
        result = findViewById(R.id.resultTextView);
        link = findViewById(R.id.linkTextView);
        camera = findViewById(R.id.cameraButton);
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
              Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
              startActivityForResult(camera_intent,pic_id);
         }
     });

    }



    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1: if (resultCode==RESULT_OK){
                imageUri = data.getData();
                Bitmap imageBitmap=null;
                try {
                    imageBitmap = URItoBitmap(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //birdImage.setImageURI(imageUri);
                birdImage.setImageBitmap(imageBitmap);
                outputGenerator(imageBitmap);
            }
                 break;
            case 2: if (resultCode==RESULT_OK){
                Bundle bundle = data.getExtras();
                Bitmap bitmapImage = (Bitmap) bundle.get("data");
                birdImage.setImageBitmap(bitmapImage);
                outputGenerator(bitmapImage);
            }
        }
    }
    private void outputGenerator(Bitmap imageBitmap) {
        try {
            LiteModelAiyVisionClassifierBirdsV13 model = LiteModelAiyVisionClassifierBirdsV13.newInstance(MainActivity.this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imageBitmap);

            // Runs model inference and gets result.
            LiteModelAiyVisionClassifierBirdsV13.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

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
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
    }

}