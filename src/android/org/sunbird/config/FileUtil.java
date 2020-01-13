package org.sunbird.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
/**
 * Created by swayangjit on 17/3/19.
 */
public class FileUtil {
    public static boolean rm(File fileOrDirectory, String skippDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (!skippDirectory.equals(child.getName())) {
                        rm(child, skippDirectory);
                    }
                }
            }
        }

        return fileOrDirectory.delete();
    }

    public static void write(String path, String fileName, String data) throws IOException {
        File manifestFile = new File(path, fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(manifestFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        outputStreamWriter.write(data);
        outputStreamWriter.flush();
        outputStreamWriter.close();
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    public static long getFileSize(final File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (!file.isDirectory()) {
            return file.length();
        }

        final List<File> dirs = new LinkedList<>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists()) {
                continue;
            }
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                continue;
            }
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory()) {
                    dirs.add(child);
                }
            }
        }
        return result;
    }

    public static void copyFolder(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String files[] = source.list();

            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);

                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            cp(source, destination);
        }
    }

    public static void cp(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    public static void renameTo(File sourceDirectory, String toDirectoryName) throws IOException {
        File oldFile = new File(sourceDirectory, toDirectoryName);
        File newFile = new File(sourceDirectory, toDirectoryName + "_temp");

        oldFile.renameTo(newFile);
    }

    public static long getFreeUsableSpace(File externalFilesDir) {
        if (externalFilesDir != null) {
            return externalFilesDir.getUsableSpace();
        }
        return 0;
    }

    public static String readFileFromAssets(InputStream fileInputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = fileInputStream;
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.getMessage();
            return null;
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }

                if (fIn != null) {
                    fIn.close();
                }

                if (input != null) {
                    input.close();
                }

            } catch (Exception e2) {
                e2.getMessage();
            }
        }

    }
}
