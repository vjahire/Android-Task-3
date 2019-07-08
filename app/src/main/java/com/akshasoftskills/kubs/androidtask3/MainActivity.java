package com.akshasoftskills.kubs.androidtask3;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView progressTv;
    ImageView imageImg, actionImg;
    ProgressBar downloadProBar;
    DownloadTask downloadTask;
    AlertDialog.Builder alertBuilder;

    String root = Environment.getExternalStorageDirectory().toString();
    String filepath = root + "/zdownloadedfile.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionImg = findViewById(R.id.start_btn);
        progressTv = findViewById(R.id.progress_tv);
        downloadProBar = findViewById(R.id.download_pro_bar);

        imageImg = findViewById(R.id.image_img);


        actionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (downloadTask==null)
//                    downloadTask = new DownloadTask();
                if (downloadTask == null) {
                    downloadTask = new DownloadTask();
                    downloadTask.execute();
                    actionImg.setImageDrawable(getDrawable(R.drawable.ic_pause_black_24dp));
                } else {
                    if (downloadTask.getStatus() == AsyncTask.Status.FINISHED)
                        showDeleteAlert();
                    else {
                        downloadTask.cancel(true);
                        downloadTask = null;
                        actionImg.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_24dp));
                    }
                }
            }
        });


    }

    private void showDeleteAlert() {
        alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setTitle("Download Alert")
                .setMessage("File is already downloaded, do you want to download it again?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(filepath);
                        if (file.delete()) {
                            downloadTask = new DownloadTask();
                            downloadTask.execute();
                            actionImg.setImageDrawable(getDrawable(R.drawable.ic_pause_black_24dp));
                        } else
                            Toast.makeText(MainActivity.this, "Unable to delete existing file", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    class DownloadTask extends AsyncTask<String, Integer, String> {

        private static final String TAG = "DownloadTask";

        @Override
        protected void onPreExecute() {
            imageImg.setImageBitmap(null);
        }

        @Override
        protected String doInBackground(String... strings) {

            String root = Environment.getExternalStorageDirectory().toString();
            String filepath = root + "/zdownloadedfile.jpg";

            URL url;
            URLConnection connection;

            try {
//                url = new URL("https://wallpapershome.com/images/wallpapers/orchid-7680x4320-5k-4k-wallpaper-8k-hd-flowers-drops-pink-5355.jpg");
                url = new URL("https://c4.wallpaperflare.com/wallpaper/834/902/413/ultra-hd-8k-7680x4320-nature-photography-wallpaper-13dbe3fd4db92bb5dff6929728ebd838.jpg");
//                url = new URL("https://snappygoat.com/f/07932f965fdc542a1b9e102b754271e3554762df/daisy-flower-blossom-bloom-1317232.jpg");
                connection = url.openConnection();
                File fileThatExists = new File(filepath);
                long lengthOfExistingFile = 0;
                OutputStream fOutStream;


                if (fileThatExists.exists()) {
                    lengthOfExistingFile = fileThatExists.length();
                    fOutStream = new FileOutputStream(filepath, true);
                    connection.setRequestProperty("Range", "bytes=" + lengthOfExistingFile + "-");
                } else fOutStream = new FileOutputStream(filepath);

                Map<String, List<String>> reqProperties = connection.getHeaderFields();
                Log.i(TAG, "doInBackground: " + reqProperties.toString());

                connection.connect();

                long lengthOfFile = connection.getContentLength(); //10599451

                if (lengthOfFile == -1) {
                    publishProgress(100);
                    return filepath;
                } else lengthOfFile += +lengthOfExistingFile;

                InputStream input = new BufferedInputStream(connection.getInputStream());
                byte[] data = new byte[1024];

                long total = lengthOfExistingFile;
                int count;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    fOutStream.write(data, 0, count);

                    long percentage = (total * 100) / lengthOfFile;
                    publishProgress((int) percentage);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "doInBackground: ", e);
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: ", e);
                e.printStackTrace();
                //todo check is intenseOf file not found then delete old file
            }
            return filepath;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            downloadProBar.setProgress(values[0]);
            progressTv.setText(String.format(getString(R.string.name_percentage), values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
//            downloadProBar.setProgress(100);
//            progressTv.setText("100%");
            imageImg.setImageBitmap(decodeSampledBitmapFromResource(s, 1800, 1200));
            actionImg.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_24dp));
        }
    }


    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
