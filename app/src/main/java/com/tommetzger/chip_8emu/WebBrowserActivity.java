/**
 * Created by Tom on 4/2/17.
 */

package com.tommetzger.chip_8emu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class WebBrowserActivity extends AppCompatActivity
{
    private Toolbar navBar;
    private WebView webView;

    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    private String ROMS_DIRECTORY;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);

        ROMS_DIRECTORY = new File(String.valueOf(this.getDir("ROMs", MODE_PRIVATE))).toString();


        navBar = (Toolbar) findViewById(R.id.navBar);
        navBar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(navBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        this.webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if (canDownloadFrom(url))
                {
                    Log.d("DOWNLOADABLE", "Downloading URL...");
                    downloadAndUnzip(url);
                }
                else
                {
                    Log.d("NOT DOWNLOADABLE", "Loading URL in Browser...");
                    view.loadUrl(url);
                }
                return true;
            }
        });
        webView.loadUrl("http://www.google.com");
    }




    private boolean canDownloadFrom(String url)
    {
        Log.d("", "Checking URL: " + url);
        return (url.endsWith(".zip") || url.endsWith(".c8"));
    }




    private void downloadAndUnzip(String url)
    {
        new DownloadFileFromURL().execute(url);
    }




    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }





    class DownloadFileFromURL extends AsyncTask<String, String, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }




        @Override
        protected String doInBackground(String... params)
        {
            int count;

            try
            {
                Log.d("WEB DOWNLOADER", "Attempting to download file...");
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                String fileName = url.toString().substring(url.toString().lastIndexOf('/') + 1);
                int lengthOfFile = connection.getContentLength();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);

                OutputStream outputStream = new FileOutputStream(ROMS_DIRECTORY + "/" + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = inputStream.read(data)) != -1)
                {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    outputStream.write(data, 0, count);
                }

                outputStream.flush();

                outputStream.close();
                inputStream.close();

                if (fileName.endsWith(".zip"))
                {
                    Log.d("WEB DOWNLOADER", "Unzipping File...");
                    unzipFile(ROMS_DIRECTORY, fileName);
                    Log.d("WEB DOWNLOADER", "Done.");
                    Log.d("WEB DOWNLOADER", "Deleting Orginal Zip...");
                    deleteFile(fileName);
                    Log.d("WEB DOWNLOADER", "Done.");
                }
            }
            catch (Exception e)
            {
                Log.e("ERROR: ", e.getMessage());
            }


            return null;
        }



        private void unzipFile(String path, String zipName)
        {
            InputStream inputStream;
            ZipInputStream zipInputStream;


            try
            {
                String fileName;
                ZipEntry zipEntry;
                int count;

                inputStream = new FileInputStream(path + "/" + zipName);
                zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

                byte[] buffer = new byte[1024];


                while ((zipEntry = zipInputStream.getNextEntry()) != null)
                {
                    fileName = zipEntry.getName();

                    if (zipEntry.isDirectory())
                    {
                        File newDirectory = new File(path + "/" + fileName);
                        newDirectory.mkdirs();
                        continue;
                    }

                    FileOutputStream fileOutputStream = new FileOutputStream(path + "/" + fileName);

                    while ((count = zipInputStream.read(buffer)) != -1)
                    {
                        fileOutputStream.write(buffer, 0, count);
                    }

                    fileOutputStream.close();
                    zipInputStream.closeEntry();
                }

                zipInputStream.close();
            }
            catch (Exception e)
            {
                Log.d("ERROR: ", "Could Not Extraxt Zip");
                e.printStackTrace();
            }
        }




        @Override
        protected void onProgressUpdate(String... progress)
        {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }




        @Override
        protected void onPostExecute(String file_url)
        {
            dismissDialog(progress_bar_type);
        }
    }
}
