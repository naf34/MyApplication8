package com.hfad.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.content.ContextCompat.startActivity;

public  class DownloadFileAsync extends Activity {
    String imgUrl = "http://imsi.e-solutio.co.kr/flower.jpg";
    private Thread splashThread;
    ImageView mImgTrans;
    Bitmap mBitmap;
    Context context;
    TextView textlink;
    private static String TAG = "Photo";
    private TimerTask mTask;
    private Timer mTimer;
    ProgressBar progreesBar;
    private File file, dir;
    private String savePath = "ImageTemp";
    private String FileName = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_splash);
        MakePhotoDir();
        Date day = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
        FileName = imgUrl.substring(imgUrl.lastIndexOf('/')+1,imgUrl.length())+String.valueOf(sdf.format(day));
        DownloadPhotoFromURL downloadPhotoFromURL = new DownloadPhotoFromURL();
        if (new File(savePath + File.separator + FileName).exists() == false) {
            downloadPhotoFromURL.execute(imgUrl,FileName);
        } else {
            Toast.makeText(context, "파일이 이미 존재합니다", Toast.LENGTH_SHORT).show();
            File file = new File(dir+"/"+FileName);
            Bitmap photoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            mImgTrans.setImageBitmap(photoBitmap);
        }
//        try{
//            DownloadPhotoFromURL example = new DownloadPhotoFromURL();
//            example.execute(imgUrl,FileName);
//            Log.v("","AsyncTask execute");
//        }catch(StackOverflowError e){
//            e.printStackTrace();
//        }
        mImgTrans = (ImageView)findViewById(R.id.AD_View);
        textlink = (TextView)findViewById(R.id.link);
//        FileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1, imgUrl.length());
////        DownloadPhotoFromURL downloadPhotoFromURL = new DownloadPhotoFromURL();
////        //동일한 파일이 있는지 검사
////        if (new File(dir.getPath() + File.separator + FileName).exists() == false) {
////            downloadPhotoFromURL.execute(imgUrl,FileName);
////        } else {
////            Toast.makeText(context, "파일이 이미 존재합니다", Toast.LENGTH_SHORT).show();
////        }
        final File file = new File(dir+"/"+FileName);
        Bitmap photoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Button btn_finish = (Button)findViewById(R.id.btn_finish);
        mImgTrans.setImageBitmap(photoBitmap);
        //닫기 버튼 이벤트 누르면 메인 액티비티로 전환
        btn_finish.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v){
            startActivity(new Intent(DownloadFileAsync.this,MainActivity.class));
            finish();
            }
        });
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

    public class DownloadPhotoFromURL extends AsyncTask<String, Integer, String> {

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
            pDialog = new ProgressDialog(DownloadFileAsync.this);
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
            mTask = new TimerTask() {
                public void run() {
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            };
            mTimer = new Timer();
            mTimer.schedule(mTask,5000);
//         mTimer.schedule(mTask,3000,5000);
            //3초후에 Task 실행하고 5초마다 반복하라.
             }
        }
    }
