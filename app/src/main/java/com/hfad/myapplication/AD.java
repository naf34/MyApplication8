package com.hfad.myapplication;

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
import android.support.annotation.Nullable;
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

public class AD extends Activity {
    String imgUrl = "http://imsi.e-solutio.co.kr/flower.jpg";
    private Thread splashThread;
    ImageView mImgTrans;
    Bitmap mBitmap;
    Context context;
    private static String TAG = "Photo";

    ProgressBar progreesBar;
    private File file, dir;
    private String savePath = "ImageTemp";
    private String FileName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        context = this.getBaseContext();
        super.onCreate(savedInstanceState);
        //상태바 없애는 코드 http://commin.tistory.com/63
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        context = this.getBaseContext();
        MakePhotoDir();
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

        FileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1, imgUrl.length());
        DownloadFileAsync DownloadFileAsync = new DownloadFileAsync();
        //동일한 파일이 있는지 검사
        if (new File(dir.getPath() + File.separator + FileName).exists() == false) {
            DownloadFileAsync.execute(imgUrl, FileName);
        } else {
            Toast.makeText(context, "파일이 이미 존재합니다", Toast.LENGTH_SHORT).show();
        }


    }

    private void MakePhotoDir() {
        dir = new File(Environment.getExternalStorageDirectory(), savePath);
        if (!dir.exists())
            dir.mkdirs();//makedir
    }

    public String getRealPathFromURI(Uri contentUri) {
        //갤러리 이미지 파일의 실제 경로 구하기
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private  class DownloadFileAsync extends AsyncTask<String, Integer, String> {
        int count;
        int lengthOfFile = 0;
        InputStream input = null;
        OutputStream output = null;
        private String tempFileName;//파일 명
        private final String SAVE_FOLDER = "/save_folder";//저장할 폴더
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AD.this);
//                pDialog.setMessage("Loading");
//                pDialog.show();
        }

        protected String doInBackground(String... args) {
            tempFileName = args[1];
            file = new File(dir, args[1]);//다운로드할 파일명
            try {
                URL url = new URL(args[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                lengthOfFile = connection.getContentLength();//파일 크기를 가져옴
//                mBitmap = BitmapFactory.decodeStream(connection.getInputStream());
                input = new BufferedInputStream(url.openStream());
                output = new FileOutputStream(file);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return String.valueOf(-1);
                    }
                    total = total + count;
                    if (lengthOfFile > 0) {//파일 총 크기가 0보다 크면
                        publishProgress((int) (total * 100 / lengthOfFile));
                    }
                    output.write(data, 0, count);
                }
                output.flush();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ioex) {

                    }
                }
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException ioex) {
                    }
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        //백그라운드 작업의 진행상태를 표시하기 위해서 호출하는 메소드
        //progressBar.setProgress(progress[0]);
        //textView.setText("다운로드:"+progress[0]+"%");
        protected void onPostExecute(String result) {
            //pdLoading.dismiss();
            if (result == null) {
                Toast.makeText(getApplicationContext(), "다운로드 완료되었습니다.", Toast.LENGTH_LONG).show();
                File file = new File(dir + "/" + tempFileName);
                //이미지 스캔해서 갤러리 업데이트
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Bitmap photoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                mImgTrans.setImageBitmap(photoBitmap);
            } else {
                Toast.makeText(getApplicationContext(), "다운로드 에러", Toast.LENGTH_LONG).show();
            }
        }
    }
}


