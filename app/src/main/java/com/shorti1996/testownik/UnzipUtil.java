package com.shorti1996.testownik;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by shorti1996 on 16.06.2017.
 */

public class UnzipUtil {

    /***
     * Method that unpacks selected zip file to a selected path
     * @param inputStream if using uri, getContentResolver().openInputStream(uri)
     * @param outputPath where to unpack file
     * @return true if success, false if error
     * @throws IOException
     */
    public static boolean unpackZip(InputStream inputStream, Uri outputPath) throws IOException {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = inputStream;
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(outputPath.getPath() + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(outputPath.getPath() + filename);

                // write using file output stream
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /***
     * Method that unpacks selected zip file to a selected path
     * @param context provide context to use contentResolver
     * @param inputFile uri of specified file (eg. content://com.android.providers.downloads.documents/document/1234)
     * @param outputPath where to unpack file
     * @return true if success, false if error
     * @throws IOException
     */
    public static boolean unpackZip(Context context, Uri inputFile, Uri outputPath) throws IOException {
        FileInputStream fis = (FileInputStream) context.getContentResolver().openInputStream(inputFile);
        return unpackZip(fis, outputPath);
    }

}