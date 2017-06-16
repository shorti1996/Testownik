package com.shorti1996.testownik;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class InTestActivity extends AppCompatActivity {

    private static final String TAG = InTestActivity.class.getName();

    public static String APP_FOLDER_PATH;
    public static String ENVIRONMENT_TEMP_PATH;
    private static final int READ_REQUEST_CODE = 42;

    private ProgressDialog mProgressDialog;

    RxPermissions mRxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_test);

        APP_FOLDER_PATH = getString(R.string.app_folder_path);
        ENVIRONMENT_TEMP_PATH = Environment.getExternalStorageDirectory().getPath() + APP_FOLDER_PATH;

        mRxPermissions = new RxPermissions(this);
        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        chooseQuestionBaseFile();
    }

    private void askForPermission(String permission) {
        mRxPermissions
                .request(permission)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        Toast.makeText(this, "GRANTED", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "DENIED", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void chooseQuestionBaseFile() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("application/zip/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                Log.d(TAG, "Uri: " + uri.toString());
                Toast.makeText(this, "Uri: " + uri.toString(), Toast.LENGTH_SHORT).show();
//                showImage(uri);
                loadQuestions(uri);
            }
        }
    }

    public void loadQuestions(Uri uri) {

        Cursor cursor = this.getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                unzip(uri);
            }
        } finally {
            cursor.close();
        }
    }

    public void unzip(Uri zipFileUri) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_unzipping));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        Uri outPath = Uri.parse(ENVIRONMENT_TEMP_PATH);

        Observable<Uri> zipUri = Observable.just(zipFileUri);
        zipUri.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(err -> Log.e(TAG, err.getMessage()))
                .doOnComplete(() -> mProgressDialog.dismiss())
                .subscribe(uri -> UnzipUtil.unpackZip(this, uri, outPath));
    }

}
