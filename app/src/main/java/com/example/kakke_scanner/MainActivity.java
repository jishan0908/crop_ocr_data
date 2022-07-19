package com.example.kakke_scanner;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.BuildConfig;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    //private static final String TAG = "MY EXCEPTION";
    Button button_capture,button_copy;
    TextView textview_data;
    Bitmap bitmap;
    //code for camera
    private static final int REQUEST_CAMERA_CODE=100;
    private static final String TAG ="MAIN_TAG";
    private Uri imageUri=null;

    private static final int STORAGE_REQUEST_CODE=101;

    //arrays of permission required to pick image from Camera,Gallery
    private String[] cameraPermissions;
    private String [] storagePermissions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // info about the 2 buttons capture and copy

        button_capture=findViewById(R.id.button_capture);
        button_copy=findViewById(R.id.button_copy);

        // textview info initially the welcome message
        textview_data=findViewById(R.id.text_data);



        //Use the camera to capture ocr,for that case need to access the camera permission
        //manifest changed at this point

        //ask the runtime permission to access the camera
        //Handle the runtime permission
        /*if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA
            },REQUEST_CAMERA_CODE);
        }*/
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        //implement the button listener

        button_capture.setOnClickListener(view -> {
            //launches the crop image activity
            //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);

            System.out.println("!!!!!!!!!!!!!!BUTTON CLICKEDDDDD!!!!!!!!!!");

            //showInputImageDialog();
            startCropActivity();



            //capture the image result override the activity result method outside onCreate method

        });

        button_copy.setOnClickListener(view -> {
            //will require "copy" method to copy in the clipboard
            String scanned_text=textview_data.getText().toString();
            copyToClipBoard(scanned_text);
        });



    }

   /* private void showInputImageDialog() {
        PopupMenu popupMenu =new PopupMenu(this,button_capture);

        popupMenu.getMenu().add(Menu.NONE,1,1,"CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2,2,"GALLERY");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id=menuItem.getItemId();
                if(id==1){
                    if(checkCameraPermissions()){
                        pickImageCamera();
                    }
                    else{
                        requestCameraPermissions();

                    }
                }
                else if(id==2){
                    if(checkStoragePermission()){
                        pickImageGallery();
                    }
                    else{
                        requestStoragePermission();
                    }

                }
                return false;
            }
        });



    }*/


   @Override
   public void onActivityResult(int requestCode, int resultCode,  Intent data) {
       try {
           super.onActivityResult(requestCode, resultCode, data);

           System.out.println("((((((((((())))))))) FROM onActivityResult ------> JUST AFTER SUPER  ");
           System.out.println("printing args-- > "+requestCode+"   "+resultCode+"   "+data);



           if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
               Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_SHORT).show();

               CropImage.ActivityResult result = CropImage.getActivityResult(data);
               if (resultCode == RESULT_OK) {

                   Uri resultUri = result.getUri();
                   bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                   getTextFromImage(bitmap);
               }
           }


       }catch(Exception e){
           Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

           System.out.println("********printing FULL exception ********-- > "+e);

       }
   }

    ///The OCR method
    private void getTextFromImage(Bitmap bitmap){
       try {
           ArrayList<String> myList = new ArrayList<String>();
           //call the text recognizer from the vision API
           TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
           if (!recognizer.isOperational()) {
               Toast.makeText(MainActivity.this, "Error Occurred!!!!!!", Toast.LENGTH_SHORT).show();
           } else {
               ///Start extracting the text from the image
               Frame frame = new Frame.Builder().setBitmap(bitmap).build();
               SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
               StringBuilder stringBuilder = new StringBuilder();
               System.out.println("textBlockSparseArray size--------:::   " + textBlockSparseArray.size());
               System.out.println("\n");


               for (int i = 0; i < textBlockSparseArray.size(); i++) {
                   TextBlock textBlock = textBlockSparseArray.valueAt(i);
                   System.out.println("textBlock.getValue ------>>>  " + textBlock.getValue());

                   stringBuilder.append(textBlock.getValue());
                   stringBuilder.append("\n");
               }

               //call our textview
               textview_data.setText(stringBuilder.toString());
               button_capture.setText("Retake");
               button_copy.setVisibility(View.VISIBLE);

               System.out.println("FINAL FINAL StringBuilder--->>>  " + stringBuilder);
               System.out.println("FINAL FINAL StringBuilder ENDDDDINGGG--->>>  ");
               String[] lines = stringBuilder.toString().split("\\n");
               Float total = 0.0f;

               //String s = "eXamPLestring>1.67>>ReSTOfString>>0.99>>ahgf>>.9>>>123>>>2323.12";
               Pattern p = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");

               for (String s : lines) {
                   if (s.contains("ZR") || s.endsWith("R")) {
                       // System.out.println("Content = " + s);
                       // System.out.println("Length = " + s.length());
                       Matcher m = p.matcher(s);
                       while (m.find()) {
                           System.out.println(">> " + m.group());
                           total=total+Float.parseFloat(m.group());
                       }
                       //total=total+Double.parseDouble(s);

                   }

               }
               //System.out.println("TOTAL IS :::::::  "+total);
            Toast.makeText(this, "KAKKE TOTAL : "+total.toString(), Toast.LENGTH_LONG).show();

            }
       }catch(Exception e){
           Toast.makeText(this,"ERROR OCCURRED WHILE ADDING",Toast.LENGTH_SHORT).show();
       }



    }
    
    private void copyToClipBoard(String text){
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip= ClipData.newPlainText("Copied data",text);
        clipBoard.setPrimaryClip(clip);

        Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    private void startCropActivity(){

       System.out.println("@@@@@@@@@@@@@@@@@@@startCropActivityCalled@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@c");
       CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

        System.out.println("@@@@@@@@@@@@@@@@@@@startCropActivityLEAVINGLEAVINGLEAVING@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@c");


    }

    private void pickImageGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

    }

//    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if(result.getResultCode()==Activity.RESULT_OK){
//                        imageUri=result.getData().getData();
//                        ////System.out.println()
//                    }
//                    else{
//                     Toast.makeText(MainActivity.this,"PPPPPP",Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//
//
//            }
//
//
//    );

    private void pickImageCamera(){
        ContentValues values =new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Sample Title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Sample Description");
        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
    }

//    private ActivityResultLauncher<Intent> cameraActivityResultLauncher=registerForActivityResult(
//        new ActivityResultContracts.StartActivityForResult(),
//        new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//              if(result.getResultCode()== Activity.RESULT_OK){
//                  //Image take from camera'
//                  ////System.out.println()
//                  //already have the image in imageUri using function pickImageCamera
//
//              }
//              else{
//                 Toast.makeText(MainActivity.this,"CAncelledddsdasda",Toast.LENGTH_SHORT).show();
//              }
//            }
//        }
//
//
//    );

    private boolean checkStoragePermission(){
        boolean result=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermissions(){
        boolean cameraResult = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean storageResult =ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  cameraResult && storageResult;
    }

    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this,cameraPermissions,REQUEST_CAMERA_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_CODE: {
                if(grantResults.length>0){
                    boolean cameraAccepted =grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted =grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickImageCamera();
                    }
                    else{
                        Toast.makeText(this,"Camera and Storage permissions are required",Toast.LENGTH_SHORT).show();

                    }
                }
                else{
                    Toast.makeText(this,"CAncelled",Toast.LENGTH_SHORT).show();
                }
            }
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                       pickImageGallery();
                    }else{
                        Toast.makeText(this,"Storage permission is reequired",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }
}