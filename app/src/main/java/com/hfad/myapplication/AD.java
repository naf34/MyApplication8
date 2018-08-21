package com.hfad.myapplication;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.icu.util.Output;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AD extends Activity {
    String imgUrl = "http://imsi.e-solutio.co.kr/flower.jpg";
    private Thread splashThread;
    ImageView mImgTrans;
    Bitmap mBitmap;
    Context context;
    private static String TAG = "Photo";
    private  TimerTask mTask;
    private Timer mTimer;
    ProgressBar progreesBar;
    private File file, dir;
    private String savePath = "ImageTemp";
    private String FileName = null;
    //퍼미션 부여 여부를 판단하기 위한 변수
    boolean fileReadPermission;
    boolean fileWritePermission;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        context = this.getBaseContext();
        super.onCreate(savedInstanceState);
        //상태바 없애는 코드 http://commin.tistory.com/63
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        //퍼미션을 체크한다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fileReadPermission = true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fileWritePermission = true;
        }

        //퍼미션 부여 안될 경우 퍼비션 요청
        if (!fileReadPermission || !fileWritePermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }

        //AsyncTask를 실행한다. url을 String으로 넘겨 준다.
//        new OpenHttpConnection().execute(imageUrl);
        //앱버전 코드
        PackageInfo packageInfo = null;         //패키지에 대한 전반적인 정보
        try {
            packageInfo = getPackageManager().getPackageInfo(
                    this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //PackageInfo 초기화
        //버전이름,버전코드 띄우기
        int versionCode = packageInfo.versionCode;
        String versionName = packageInfo.versionName;
        TextView versionCodeTextView = (TextView) findViewById(R.id.versionCode);
        versionCodeTextView.setText("VersionCode:" + Integer.toString(versionCode));
        TextView versionNameTextView = (TextView) findViewById(R.id.versionName);
        versionNameTextView.setText("VersionName:" + versionName);
        TextView messageView = (TextView) findViewById(R.id.MessageView);
        mImgTrans = (ImageView) findViewById(R.id.Logo);
        messageView.setText("잠시만 기다려 주세요");
//        Intent intent = new Intent(this,MainActivity.class);
////        startActivity(intent);
////        finish();

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(30);
        progressBar.setSecondaryProgress(70);
        progressBar.setVisibility(progressBar.VISIBLE);
        progressBar.setIndeterminate(false);
        BitmapFactory.Options bmOptions;
        TimerTask mTask = new TimerTask() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), DownloadFileAsync.class);
                startActivity(intent);
                finish();
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTask, 5000);
//         mTimer.schedule(mTask,3000,5000);
        //3초후에 Task 실행하고 5초마다 반복하라.
    }
    protected void onDestroy(){
        Log.i("test","onDestroy()");
        mTimer.cancel();
        super.onDestroy();
    }
    //퍼미션 부여 요청 결과 확인
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                fileReadPermission = true;
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                fileWritePermission = true;
        }
    }


}


