package org.sunbird.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * Created by swayangjit on 9/6/19.
 */
public class StorageUtil {

    // Methods
    private final static String METHOD_GET_VOLUME_LIST = "getVolumeList";
    private final static String METHOD_GET_VOLUME_STATE = "getState";
    private final static String METHOD_IS_REMOVABLE = "isRemovable";
    private final static String METHOD_GET_PATH = "getPath";

    // Classes
    private final static String CLASS_GET_VOLUME_LIST = "android.os.storage.StorageVolume";

    private static final long GB = 1073741824;
    private static final long MB = 1048576;
    private static final int KB = 1024;

    public static JSONArray getStorageVolumes(Context context) {
        final StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            final Method getVolumeList = storageManager.getClass().getMethod(METHOD_GET_VOLUME_LIST);
            final Class<?> storageValumeClazz = Class.forName(CLASS_GET_VOLUME_LIST);
            final Method getPath = storageValumeClazz.getMethod(METHOD_GET_PATH);
            Method isRemovable = storageValumeClazz.getMethod(METHOD_IS_REMOVABLE);
            Method mGetState = null;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                try {
                    mGetState = storageValumeClazz.getMethod(METHOD_GET_VOLUME_STATE);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            final Object invokeVolumeList = getVolumeList.invoke(storageManager);
            final int length = Array.getLength(invokeVolumeList);
            JSONArray storageList = new JSONArray();
            for (int i = 0; i < length; i++) {
                final Object storageVolume = Array.get(invokeVolumeList, i);
                final String path = (String) getPath.invoke(storageVolume);
                final boolean removable = (Boolean) isRemovable.invoke(storageVolume);
                String state = null;
                if (mGetState != null) {
                    state = (String) mGetState.invoke(storageVolume);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        state = Environment.getStorageState(new File(path));
                    } else {
                        if (removable) {
                            state = EnvironmentCompat.getStorageState(new File(path));
                        } else {
                            state = Environment.MEDIA_MOUNTED;
                        }
                    }
                }
                long totalSize = 0;
                long availaleSize = 0;
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    totalSize = getTotalSize(path);
                    availaleSize = getAvailableSize(path);
                }
                JSONObject storageVolumeObj = new JSONObject();
                storageVolumeObj.put("availableSize", availaleSize);
                storageVolumeObj.put("totalSize", fmtSpace(getRealisticTotalSize(totalSize)));

                storageVolumeObj.put("state", state);
                storageVolumeObj.put("path", "file://"+path+"/");
                storageVolumeObj.put("isRemovable", removable);
                String appStorageArea = getAppStorageArea(context, path);
                if(appStorageArea!= null){
                    storageVolumeObj.put("contentStoragePath", appStorageArea);
                }else{
                    storageVolumeObj.put("contentStoragePath", "file://" + path + "/");
                }
                if(totalSize > 0){
                    storageList.put(storageVolumeObj);
                }
            }
            Log.i("StorageVolumeList", storageList.toString());
            return storageList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getAppStorageArea(Context context, String rootDirectoryPath) {
        File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
        for (File d : dirs) {
            if (d != null) {
                String path = d.getPath();
                if (path.contains(rootDirectoryPath)) {
                    return "file://" + path + "/";
                }
            }
        }
        return null;
    }

    private static long getTotalSize(String path) {
        final StatFs statFs = new StatFs(path);
        long blockSize;
        long blockCountLong;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            blockCountLong = statFs.getBlockCountLong();
        } else {
            blockSize = statFs.getBlockSize();
            blockCountLong = statFs.getBlockCount();
        }
        return blockSize * blockCountLong;
    }

    private static long getRealisticTotalSize(long totalSize) {
        if (totalSize > 2147483648L && totalSize < 4294967296L) {
            return 4294967296L;
        } else if (totalSize > 2147483648L && totalSize < 8589934592L) {
            return 8589934592L;
        } else if (totalSize > 9663676416L && totalSize < 17179869184L) {
            return 17179869184L;
        } else if (totalSize > 21474836480L && totalSize < 34359738368L) {
            return 34359738368L;
        } else if (totalSize > 53687091200L && totalSize < 68719476736L) {
            return 68719476736L;
        } else if (totalSize > 118111600640L && totalSize < 137438953472L) {
            return 137438953472L;
        } else {
            return totalSize;
        }

    }

    private static long getAvailableSize(String path) {
        final StatFs statFs = new StatFs(path);
        long blockSize;
        long availableBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            availableBlocks = statFs.getAvailableBlocksLong();
        } else {
            blockSize = statFs.getBlockSize();
            availableBlocks = statFs.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    private static String fmtSpace(long space) {
        if (space <= 0) {
            return "0";
        }
        double gbValue = (double) space / GB;
        if (gbValue >= 1) {
            return String.format("%.0f GB", gbValue);
        } else {
            double mbValue = (double) space / MB;
            if (mbValue >= 1) {
                return String.format("%.2f MB", mbValue);
            } else {
                final double kbValue = space / KB;
                return String.format("%.2f KB", kbValue);
            }
        }
    }
}
