package com.example.getpdffromimage;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private EditText edtLink;
    String chap = "-chap-";
    private ArrayList<String> listChap = new ArrayList<>();
    private ArrayList<String> list = new ArrayList<>();
    private ListView edtOutImageLink;
    private Button btnGetPDF;
    private Button btnDownload;
    private ProgressBar progressBar;
    private Bitmap bit;
    private String[] webTruyenDownload = new String[]{
            "https://truyenqq.com/"
    };
    private boolean isRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        initSetting();
        onCLickButton();
    }

    public void initSetting() {
        progressBar = (ProgressBar) findViewById(R.id.prBar);
        edtLink = (EditText) findViewById(R.id.edtLink);
        edtLink.setInputType(InputType.TYPE_NULL);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        edtOutImageLink = (ListView) findViewById(R.id.edtOutImageLink);
        btnGetPDF = (Button) findViewById(R.id.btnGetPDF);
    }

    //https://truyenqq.com/truyen-tranh/renai-kaidan-sayoko-san-7443.html
    //https://truyenqq.com/truyen-tranh/quy-toc-ma-ca-rong-882.html
    public void onCLickButton() {
        btnGetPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFilePDF(edtLink.getText().toString());
            }
        });
        edtLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] dataClipBoard = new String[1];
                final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (!clipboardManager.hasPrimaryClip()) {
                    Toast.makeText(getApplicationContext(), "Clipboard empty! please copy link and click here", Toast.LENGTH_SHORT).show();
                } else {
                    dataClipBoard[0] = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
                    edtLink.setText(dataClipBoard[0]);
                }
            }
        });
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            ArrayList<String> listImage = new ArrayList<>();
                            int i = 0;
                            int j = 0;
                            for (String linkChap : listChap) {
                                PdfDocument pdfDocument = new PdfDocument();
                                try {
                                    listImage = (ArrayList<String>) getURL(new getLinkImage().execute(linkChap).get());
                                    if (listImage.size() > 0) {
                                        for (String page : listImage) {
                                            try {
                                                PageInfo pageInfo = new PageInfo.Builder(1080, 1920, i).create();
                                                PdfDocument.Page item = pdfDocument.startPage(pageInfo);
                                                Canvas canvas = item.getCanvas();
                                                canvas.drawBitmap(new imageDownload().execute(page).get(), 50, 50, null);
                                                i++;
                                                pdfDocument.finishPage(item);
                                            } catch (Exception e) {
                                                Log.d(e.toString(), "run: ");
                                            }
                                        }
                                            Toast.makeText(progressBar.getContext(), "Xong " + (i - 1), Toast.LENGTH_SHORT).show();
                                            try {
                                                String directory_path = Environment.getExternalStorageDirectory().getPath() + "/mypdf/";
                                                File file = new File(directory_path);
                                                if (!file.exists()) {
                                                    file.mkdirs();
                                                }
                                                String targetPdf = directory_path + "Chap" + j + ".pdf";
                                                j++;
                                                File filePath = new File(targetPdf);
                                                try {
                                                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                                                    pdfDocument.writeTo(fileOutputStream);
                                                    fileOutputStream.flush();
                                                    fileOutputStream.close();
                                                    Log.d("Save", "Done ");
                                                    System.gc();
                                                } catch (IOException e) {
                                                    Log.e("main", "error " + e.toString());
                                                }

                                            } catch (Exception e) {
                                                Log.d("Error", "run: "+e.toString());;
                                            }
                                            pdfDocument.close();

                                    } else {
                                        Log.d("Size = 0", "run: ");
                                    }
                                    listImage.clear();
                                } catch (ExecutionException e) {
                                    Log.d(e.toString(), "run: ");
                                } catch (InterruptedException e) {
                                    Log.d(e.toString(), "run: ");
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    }
                }, 30000);
//                } else {
//
//                    Toast.makeText(getApplicationContext(), "List image empty begin get link image", Toast.LENGTH_SHORT).show();
//                    progressBar.setVisibility(View.VISIBLE);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            for (String item : listChap) {
//                                getLinkImage get = new getLinkImage();
//                                get.execute(item);
//                            }
//                            progressBar.setVisibility(View.GONE);
//                            btnDownload.setText("Download image");
//                        }
//                    }, 2000);
//                }
            }

        });
    }

    public void getPDF(String page, PdfDocument pdfDocument, int i) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            PageInfo pageInfo = new PageInfo.Builder(1080, 1920, i).create();
            PdfDocument.Page item = pdfDocument.startPage(pageInfo);
            Canvas canvas = item.getCanvas();
            try {
                canvas.drawBitmap(new imageDownload().execute(page).get(), 50, 50, null);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Something wrong" + e.toString(), Toast.LENGTH_SHORT).show();
            }
            i++;
            pdfDocument.finishPage(item);
            Toast.makeText(progressBar.getContext(), "Xong " + (i - 1), Toast.LENGTH_SHORT).show();
            try {
                String directory_path = Environment.getExternalStorageDirectory().getPath() + "/mypdf/";
                File file = new File(directory_path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String targetPdf = directory_path + "Chap.pdf";
                File filePath = new File(targetPdf);
                try {
                    pdfDocument.writeTo(new FileOutputStream(filePath));
                    Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e("main", "error " + e.toString());
                    Toast.makeText(getApplicationContext(), "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Something wrong" + e.toString(), Toast.LENGTH_SHORT).show();
            }
            pdfDocument.close();
        } else {
            Toast.makeText(getApplicationContext(), "Version android don'n correct please update to Android 5.0 KITKAT", Toast.LENGTH_SHORT).show();
        }
    }

    private class imageDownload extends AsyncTask<String, String, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(strings[0]).openConnection();
                httpURLConnection.setConnectTimeout(10000);
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Bitmap bit = BitmapFactory.decodeStream(httpURLConnection.getInputStream());
                    httpURLConnection.disconnect();
                    System.gc();
                    return bit;
                } else {
                }

            } catch (Exception e) {
                Log.d("NULL", "doInBackground: " + e.toString());
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap s) {
            super.onPostExecute(s);
            bit = s;

        }
    }


    //https://www.qr-code-generator.com https://truyenqq.com/truyen-tranh/dinh-menh-giua-vi-than-va-tieu-co-nuong-7431.html
    private void downloadFilePDF(String link) {
        if (!link.isEmpty()) {
            new getLinkChap().execute(link);
        } else {
            Toast.makeText(getApplicationContext(), "Link is empty", Toast.LENGTH_SHORT).show();

        }
    }

    public void getPermission() {
        String[] permission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ArrayList<String> listPermission = new ArrayList<>();
        for (String permiss : permission) {
            if (ContextCompat.checkSelfPermission(this, permiss) != PackageManager.PERMISSION_GRANTED) {
                listPermission.add(permiss);
            }
            if (!listPermission.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermission.toArray(new String[listPermission.size()]), 1);
            }
        }
    }

    public class getLinkImage extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String inputLink;
                    StringBuffer bufferLink = new StringBuffer();
                    while ((inputLink = reader.readLine()) != null) {
                        bufferLink.append(inputLink);
                    }
                    reader.close();
                    return bufferLink.toString();
                } else {
                    Log.d("", "Error connect: " + httpURLConnection.getResponseCode() + "/" + httpURLConnection.getResponseMessage());
                }

            } catch (IOException e) {
                Log.d("", "Error" + e.toString());
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String sNew = s.replace(" ", "");
            if (edtLink.getText().toString().startsWith(webTruyenDownload[0])) {
                list.addAll((ArrayList<String>) getURL(s));
            }
        }

    }

    public class getLinkChap extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String inputLink;
                    StringBuffer bufferLink = new StringBuffer();
                    while ((inputLink = reader.readLine()) != null) {
                        bufferLink.append(inputLink);
                    }
                    reader.close();
                    return bufferLink.toString();
                } else {
                    Log.d("", "Error connect: " + httpURLConnection.getResponseCode() + "/" + httpURLConnection.getResponseMessage());
                }

            } catch (IOException e) {
                Log.d("", "Error" + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String sNew = s.replace(" ", "");
            if (edtLink.getText().toString().startsWith(webTruyenDownload[0])) {
                listChap.addAll((ArrayList<String>) getURLChap(s));
                ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, listChap);
                edtOutImageLink.setAdapter(stringArrayAdapter);
                Log.d("Finish get link chap", "Count" + listChap.size());
            }
        }
    }

    public List<String> getURLChap(String s) {
        List<String> datalist = new ArrayList<>();
        String[] data = s.split("\"");
        for (int i = 10; i < data.length; i++) {
            if (data[i].equals("col-md-10 col-sm-10 col-xs-8 ")) {
                datalist.add(data[i + 4]);
            }
        }
        return datalist;
    }

    public List<String> getURL(String s) {
        List<String> datalist = new ArrayList<>();
        String[] data = s.split("\"");
        for (int i = 10; i < data.length; i++) {
            if (data[i].equals("lazy")) {
                datalist.add(data[i + 2]);
            }
        }
        return datalist;
    }

}
