package ca.brainfarm;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import ca.brainfarm.data.ContributionFile;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

/**
 * Created by Eric Thompson on 2017-11-07.
 */

public class ContributionFileDownloader {

    private Context context;
    private ContributionFile contributionFile;

    private Toast startToast;

    public ContributionFileDownloader(Context context, ContributionFile contributionFile) {
        this.context = context;
        this.contributionFile = contributionFile;
    }

    public void startDownload() {
        showStartToast();

        ServiceCall downloadCall = new ServiceCall(
                "DownloadFile",              // DownloadFile service method
                ServiceCall.FORMAT_JSON,     // Request data is in JSON
                ServiceCall.FORMAT_BINARY);  // Expect response as binary data
        downloadCall.addArgument("contributionFileID", contributionFile.contributionFileID);
        downloadCall.executeForStream(new SuccessHandler<InputStream>() {
            @Override
            public void handleSuccess(InputStream result) {
                new DownloadTask().execute(result);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private File saveFile(InputStream inputStream) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, contributionFile.filename);

        try {
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[8 * 1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();

            return file;

        } catch (IOException ex) {
            String message = "Unable to save file!";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void createDownloadNotification(File file) {
        DownloadManager downloadManager =
                (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.addCompletedDownload(
                contributionFile.filename, // Title
                "Download from Brainfarm", // Description
                true,                      // Scannable
                URLConnection.guessContentTypeFromName(file.getName()), // MIME type
                file.getAbsolutePath(),
                file.length(),
                true
        );
    }

    private void showStartToast() {
        String message = "Starting download for " + contributionFile.filename;
        startToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        startToast.show();
    }

    private void showCompleteToast() {
        String message = "Download complete for " + contributionFile.filename;
        startToast.cancel();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private class DownloadTask extends AsyncTask<InputStream, Void, File> {

        @Override
        protected File doInBackground(InputStream... params) {
            return saveFile(params[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            createDownloadNotification(file);
            showCompleteToast();
        }
    }

}
