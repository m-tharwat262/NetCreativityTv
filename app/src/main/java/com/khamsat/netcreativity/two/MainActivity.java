package com.khamsat.netcreativity.two;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.github.florent37.shapeofview.shapes.ArcView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{

    /*-- CUSTOMIZE --*/
    /*-- you can customize these options for your convenience --*/
    private static String webview_url   = "https://netcreativity.tv";    // web address or local file location you want to open in webview
    private static String file_type     = "*/*";    // file types to be allowed for upload
    private boolean multiple_files      = true;         // allowing multiple file upload

    /*-- MAIN VARIABLES --*/
    WebView webView;



    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Context mContext;

    private ArcView mArcView;

    private WebView mWebView;

    private TextView mStateTextView;
    private TextView mButtonTextView;



    private static final String TAG = MainActivity.class.getSimpleName();

    private String cam_file_data = null;        // for storing camera file information
    private ValueCallback<Uri> file_data;       // data/header received after file selection
    private ValueCallback<Uri[]> file_path;     // received file(s) temp. location

    private final static int file_req_code = 1;


    private boolean isClicked = false;
    private MyChrome myChrome;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;

            /*-- if file request cancelled; exited camera. we need to send null value to make future attempts workable --*/
            if (resultCode == Activity.RESULT_CANCELED) {
                file_path.onReceiveValue(null);
                return;
            }

            /*-- continue if response is positive --*/
            if(resultCode== Activity.RESULT_OK){
                if(null == file_path){
                    return;
                }
                ClipData clipData;
                String stringData;

                try {
                    clipData = intent.getClipData();
                    stringData = intent.getDataString();
                }catch (Exception e){
                    clipData = null;
                    stringData = null;
                }
                if (clipData == null && stringData == null && cam_file_data != null) {
                    results = new Uri[]{Uri.parse(cam_file_data)};
                }else{
                    if (clipData != null) { // checking if multiple files selected or not
                        final int numSelectedFiles = clipData.getItemCount();
                        results = new Uri[numSelectedFiles];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            results[i] = clipData.getItemAt(i).getUri();
                        }
                    } else {
                        try {
                            Bitmap cam_photo = (Bitmap) intent.getExtras().get("data");
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            cam_photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            stringData = MediaStore.Images.Media.insertImage(this.getContentResolver(), cam_photo, null, null);
                        }catch (Exception ignored){}
                            /* checking extra data
                            Bundle bundle = intent.getExtras();
                            if (bundle != null) {
                                for (String key : bundle.keySet()) {
                                    Log.w("ExtraData", key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                                }
                            }*/
                        results = new Uri[]{Uri.parse(stringData)};
                    }
                }
            }

            file_path.onReceiveValue(results);
            file_path = null;
        }else{
            if(requestCode == file_req_code){
                if(null == file_data) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                file_data.onReceiveValue(result);
                file_data = null;
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.activity_main_web_view);
        assert webView != null;



        mContext = MainActivity.this;

        mArcView = findViewById(R.id.activity_main_arc_view);

        mStateTextView = findViewById(R.id.activity_main_state);
        mButtonTextView= findViewById(R.id.activity_main_button);


        triggerWebView();


//
//
//
//
//
//
//
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setAllowFileAccess(true);
//
//
//        webSettings.setDomStorageEnabled(true);
//        webSettings.setSaveFormData(true);
//        webSettings.setSupportZoom(true);
//        webSettings.setMediaPlaybackRequiresUserGesture(false);
//
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        webSettings.setPluginState(WebSettings.PluginState.ON);
//        webSettings.setMediaPlaybackRequiresUserGesture(false);
//
//        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);
//
//
//        if(Build.VERSION.SDK_INT >= 21){
//            webSettings.setMixedContentMode(0);
//            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        }else {
//            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
//        webView.setWebViewClient(new Callback());
//        webView.loadUrl(webview_url);
//        webView.setWebChromeClient(new WebChromeClient() {
//
//
//
//
//
//
//
//
//
//
//
//
//            /*--
//            openFileChooser is not a public Android API and has never been part of the SDK.
//            handling input[type="file"] requests for android API 16+; I've removed support below API 21 as it was failing to work along with latest APIs.
//            --*/
//        /*    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
//                file_data = uploadMsg;
//                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                i.addCategory(Intent.CATEGORY_OPENABLE);
//                i.setType(file_type);
//                if (multiple_files) {
//                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                }
//                startActivityForResult(Intent.createChooser(i, "File Chooser"), file_req_code);
//            }
//        */
//            /*-- handling input[type="file"] requests for android API 21+ --*/
//            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//
//                if(file_permission() && Build.VERSION.SDK_INT >= 21) {
//                    file_path = filePathCallback;
//                    Intent takePictureIntent = null;
//                    Intent takeVideoIntent = null;
//
//                    boolean includeVideo = false;
//                    boolean includePhoto = false;
//
//                    /*-- checking the accept parameter to determine which intent(s) to include --*/
//                    paramCheck:
//                    for (String acceptTypes : fileChooserParams.getAcceptTypes()) {
//                        String[] splitTypes = acceptTypes.split(", ?+"); // although it's an array, it still seems to be the whole value; split it out into chunks so that we can detect multiple values
//                        for (String acceptType : splitTypes) {
//                            switch (acceptType) {
//                                case "*/*":
//                                    includePhoto = true;
//                                    includeVideo = true;
//                                    break paramCheck;
//                                case "image/*":
//                                    includePhoto = true;
//                                    break;
//                                case "video/*":
//                                    includeVideo = true;
//                                    break;
//                            }
//                        }
//                    }
//
//                    if (fileChooserParams.getAcceptTypes().length == 0) {   //no `accept` parameter was specified, allow both photo and video
//                        includePhoto = true;
//                        includeVideo = true;
//                    }
//
//                    if (includePhoto) {
//                        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
//                            File photoFile = null;
//                            try {
//                                photoFile = create_image();
//                                takePictureIntent.putExtra("PhotoPath", cam_file_data);
//                            } catch (IOException ex) {
//                                Log.e(TAG, "Image file creation failed", ex);
//                            }
//                            if (photoFile != null) {
//                                cam_file_data = "file:" + photoFile.getAbsolutePath();
//                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                            } else {
//                                cam_file_data = null;
//                                takePictureIntent = null;
//                            }
//                        }
//                    }
//
//                    if (includeVideo) {
//                        takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                        if (takeVideoIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
//                            File videoFile = null;
//                            try {
//                                videoFile = create_video();
//                            } catch (IOException ex) {
//                                Log.e(TAG, "Video file creation failed", ex);
//                            }
//                            if (videoFile != null) {
//                                cam_file_data = "file:" + videoFile.getAbsolutePath();
//                                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
//                            } else {
//                                cam_file_data = null;
//                                takeVideoIntent = null;
//                            }
//                        }
//                    }
//
//                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
//                    contentSelectionIntent.setType(file_type);
//                    if (multiple_files) {
//                        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                    }
//
//                    Intent[] intentArray;
//                    if (takePictureIntent != null && takeVideoIntent != null) {
//                        intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
//                    } else if (takePictureIntent != null) {
//                        intentArray = new Intent[]{takePictureIntent};
//                    } else if (takeVideoIntent != null) {
//                        intentArray = new Intent[]{takeVideoIntent};
//                    } else {
//                        intentArray = new Intent[0];
//                    }
//
//                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
//                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
//                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File chooser");
//                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
//                    startActivityForResult(chooserIntent, file_req_code);
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//
//
////            @Nullable
////            @Override
////            public Bitmap getDefaultVideoPoster() {
////                return  super.getDefaultVideoPoster();
////            }
////
////
////            @Override
////            public void onShowCustomView(View view, CustomViewCallback callback) {
////                super.onShowCustomView(view, callback);
////
////                if(view instanceof FrameLayout) {
////
////                    FrameLayout frame = (FrameLayout) view;
////
////                    if(frame.getFocusedChild() instanceof VideoView) {
////
////                        VideoView video = (VideoView)
////                        frame.getFocusedChild();
////                        frame.removeView(video);
////                        video.start();
////
////                    }
////
////                }
////
////            }
//
//
//        });
    }




    private void triggerWebView() {

        if (!isNetworkAvailable()) {

            mStateTextView.setVisibility(View.VISIBLE);
            mButtonTextView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);


            mStateTextView.setText(R.string.state_no_internet);
            mButtonTextView.setText(R.string.button_refresh);
            mButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    triggerWebView();

                }
            });

        } else {


            webView.setVisibility(View.VISIBLE);
            mStateTextView.setVisibility(View.GONE);
            mButtonTextView.setVisibility(View.GONE);


            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAllowFileAccess(true);


            webSettings.setDomStorageEnabled(true);
            webSettings.setSaveFormData(true);
            webSettings.setSupportZoom(true);
            webSettings.setMediaPlaybackRequiresUserGesture(false);

            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            webSettings.setPluginState(WebSettings.PluginState.ON);
            webSettings.setMediaPlaybackRequiresUserGesture(false);

            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);


            if (Build.VERSION.SDK_INT >= 21) {
                webSettings.setMixedContentMode(0);
                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            webView.setWebViewClient(new Callback());
            webView.loadUrl(webview_url);
            myChrome = new MyChrome();
            webView.setWebChromeClient(myChrome);


        }
    }

        private class MyChrome extends WebChromeClient {
            private View mCustomView;
            private WebChromeClient.CustomViewCallback mCustomViewCallback;
            protected FrameLayout mFullscreenContainer;
            private int mOriginalOrientation;
            private int mOriginalSystemUiVisibility;

            MyChrome() {}



            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                if(file_permission() && Build.VERSION.SDK_INT >= 21) {
                    file_path = filePathCallback;
                    Intent takePictureIntent = null;
                    Intent takeVideoIntent = null;

                    boolean includeVideo = false;
                    boolean includePhoto = false;

                    /*-- checking the accept parameter to determine which intent(s) to include --*/
                    paramCheck:
                    for (String acceptTypes : fileChooserParams.getAcceptTypes()) {
                        String[] splitTypes = acceptTypes.split(", ?+"); // although it's an array, it still seems to be the whole value; split it out into chunks so that we can detect multiple values
                        for (String acceptType : splitTypes) {
                            switch (acceptType) {
                                case "*/*":
                                    includePhoto = true;
                                    includeVideo = true;
                                    break paramCheck;
                                case "image/*":
                                    includePhoto = true;
                                    break;
                                case "video/*":
                                    includeVideo = true;
                                    break;
                            }
                        }
                    }

                    if (fileChooserParams.getAcceptTypes().length == 0) {   //no `accept` parameter was specified, allow both photo and video
                        includePhoto = true;
                        includeVideo = true;
                    }

                    if (includePhoto) {
                        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                            File photoFile = null;
                            try {
                                photoFile = create_image();
                                takePictureIntent.putExtra("PhotoPath", cam_file_data);
                            } catch (IOException ex) {
                                Log.e(TAG, "Image file creation failed", ex);
                            }
                            if (photoFile != null) {
                                cam_file_data = "file:" + photoFile.getAbsolutePath();
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            } else {
                                cam_file_data = null;
                                takePictureIntent = null;
                            }
                        }
                    }

                    if (includeVideo) {
                        takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        if (takeVideoIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                            File videoFile = null;
                            try {
                                videoFile = create_video();
                            } catch (IOException ex) {
                                Log.e(TAG, "Video file creation failed", ex);
                            }
                            if (videoFile != null) {
                                cam_file_data = "file:" + videoFile.getAbsolutePath();
                                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
                            } else {
                                cam_file_data = null;
                                takeVideoIntent = null;
                            }
                        }
                    }

                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType(file_type);
                    if (multiple_files) {
                        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }

                    Intent[] intentArray;
                    if (takePictureIntent != null && takeVideoIntent != null) {
                        intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
                    } else if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else if (takeVideoIntent != null) {
                        intentArray = new Intent[]{takeVideoIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooserIntent, file_req_code);
                    return true;
                } else {
                    return false;
                }
            }


            public Bitmap getDefaultVideoPoster() {
                if (mCustomView == null) {
                    return null;
                }

                return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
            }




            public void onHideCustomView() {
                ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                setRequestedOrientation(this.mOriginalOrientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            }

            public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
                if (this.mCustomView != null) {
                    onHideCustomView();
                    return;
                }

                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                this.mOriginalOrientation = getRequestedOrientation();
                this.mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                isClicked = true;
            }




        }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    /*-- callback reporting if error occurs --*/
    public class Callback extends WebViewClient{
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }
    }

    /*-- checking and asking for required file permissions --*/
    public boolean file_permission(){
        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            return false;
        }else{
            return true;
        }
    }

    /*-- creating new image file here --*/
    private File create_image() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    /*-- creating new video file here --*/
    private File create_video() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name    = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name     = "file_"+file_name+"_";
        File sd_directory   = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".3gp", sd_directory);
    }

    /*-- back/down key handling --*/
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if(isClicked) {
                    myChrome.onHideCustomView();
                } else {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}