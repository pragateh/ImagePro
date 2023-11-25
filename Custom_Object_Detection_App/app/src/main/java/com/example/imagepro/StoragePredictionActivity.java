package com.example.imagepro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;

public class StoragePredictionActivity extends AppCompatActivity {
    private Button select_image;
    private ImageView image_v;
    private objectDetectorClass objectDetectorClass;
    int SELECT_PICTURE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_prediction);
        //defining storage_prediction button
        select_image = findViewById(R.id.select_image);
        image_v = findViewById(R.id.image_v);
        try{
            objectDetectorClass=new objectDetectorClass(getAssets(),"best-fp16.tflite","yolo_custom_label.txt",640);
            Log.d("MainActivity","Model is successfully loaded");
        }
        catch (IOException e){
            Log.d("MainActivity","Getting some error");
            e.printStackTrace();
        }

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when select_image button is clicked choose image. creating a new function
                image_chooser();
            }
        });
    }

    private void image_chooser() {
        //this function will choose image, create a new intent to navigate to gallery
        Intent i = new Intent();
        //set type of intent as image
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        //now startActivityForResult to select image
        // creating a new int SELECT_PICTURE in use when a picture is selected, it will return uri and 200
        // 200 means successfully selected the picture
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
    //create a function onActivityResult to get uri
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==SELECT_PICTURE){
                //successfully selected picture
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    // check if it is empty or not
                    // if you want you can print uri
                    Log.d("StoragePrediction", "Output_uri: "+selectedImageUri);
                    // now read uri in bitmap format
                    Bitmap bitmap = null;
                    try{
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }

                    //converting Bitmap to Mat image
                    //CV_8UC4 : RGB image
                    //CV_8UC1 : Greyscale Image
                    Mat selected_image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                    Utils.bitmapToMat(bitmap, selected_image);
                    // passing selected_image to recognizePhoto - returns selected_image w box and name drawn on photo
                    selected_image = objectDetectorClass.recognizePhoto(selected_image);
                    //now covert mat image to bitmap
                    Bitmap bitmap1 = null;
                    bitmap1 = Bitmap.createBitmap(selected_image.cols(), selected_image.rows(), Bitmap.Config.ARGB_8888);
                    // putting input and output resp.
                    Utils.matToBitmap(selected_image, bitmap1);
                    //set bitmap to imageview
                    image_v.setImageBitmap(bitmap1);
                    //Before running one change now select device and run
                }
            }
        }
    }
}