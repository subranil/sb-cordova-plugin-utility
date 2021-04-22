package org.sunbird.config;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunbird.storage.StorageUtil;
import org.sunbird.utm.InstallReferrerListener;
import org.sunbird.utm.PlayStoreInstallReferrer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class UtilityPlugin extends CordovaPlugin {

    private static final String SHARED_PREFERENCES_NAME = "org.ekstep.genieservices.preference_file";
    private CallbackContext onActivityResultCallbackContext = null;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getBuildConfigValue")) {
            this.getBuildConfigParam(args, callbackContext);
            return true;
        } else if (action.equals("rm")) {
            this.removeDirectory(args, callbackContext);
            return true;
        } else if (action.equalsIgnoreCase("openPlayStore")) {
            String appId = args.getString(1);
            openGooglePlay(cordova, appId);
            callbackContext.success();

        } else if (action.equalsIgnoreCase("getDeviceAPILevel")) {
            getDeviceAPILevel(callbackContext);

        } else if (action.equalsIgnoreCase("checkAppAvailability")) {
            checkAppAvailability(cordova, args, callbackContext);

        } else if (action.equalsIgnoreCase("getDownloadDirectoryPath")) {
            getDownloadDirectoryPath(callbackContext);

        }else if (action.equalsIgnoreCase("exportApk")) {
            exportApk(args, cordova,callbackContext);

        }else if (action.equalsIgnoreCase("getBuildConfigValues")) {

            getBuildConfigValues(args,callbackContext);
        }else if (action.equalsIgnoreCase("getDeviceSpec")) {
            try {
                callbackContext.success(new DeviceSpecGenerator().getDeviceSpec(cordova.getActivity()));
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        }else if (action.equalsIgnoreCase("createDirectories")) {

            createDirectories(args,callbackContext);
        }else if (action.equalsIgnoreCase("writeFile")) {

            writeFile(args,callbackContext);
        }else if (action.equalsIgnoreCase("getMetaData")) {

            getMetaData(args,callbackContext);
        }else if (action.equalsIgnoreCase("getAvailableInternalMemorySize")) {

            getAvailableInternalMemorySize(callbackContext);
        }else if (action.equalsIgnoreCase("getUtmInfo")) {

            getUtmInfo(cordova, callbackContext);
            return true;
        }else if (action.equalsIgnoreCase("clearUtmInfo")) {

            clearUtmInfo(cordova, callbackContext);
        }else if (action.equalsIgnoreCase("getStorageVolumes")) {

            getStorageVolumes(cordova, callbackContext);
        }else if (action.equalsIgnoreCase("copyDirectory")) {

            copyDirectory(args, callbackContext);
            return true;
        }else if (action.equalsIgnoreCase("renameDirectory")) {

            renameDirectory(args, callbackContext);
        }else if (action.equalsIgnoreCase("canWrite")) {

            canWrite(args, callbackContext);
        }else if (action.equalsIgnoreCase("getFreeUsableSpace")) {

            getUsableSpace(args, callbackContext);
        }else if (action.equalsIgnoreCase("readFromAssets")) {

            readFromAssets(args, callbackContext);
            return true;
        }else if (action.equalsIgnoreCase("copyFile")) {
            copyFile(args, callbackContext);
            return true;
        }else if (action.equalsIgnoreCase("getApkSize")) {
            getApkSize(cordova, callbackContext);
            return true;
        }else if (action.equalsIgnoreCase("verifyCaptcha")) {
            verifyCaptcha(args, callbackContext);
            return true;
        }else if (action.equalsIgnoreCase("getAppAvailabilityStatus")) {
            getAppAvailabilityStatus(cordova, callbackContext, args.getJSONArray(0));
            return true;
        }else if (action.equalsIgnoreCase("startActivityForResult")) {
            if (args.length() != 1) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                return false;
            }
            JSONObject object = args.getJSONObject(0);
            Intent intent = IntentUtil.populateIntent(object, callbackContext);
            int requestCode = object.has("requestCode") ? object.getInt("requestCode") : 1;
            this.onActivityResultCallbackContext = callbackContext;
            startActivity(intent, requestCode, callbackContext);
            return true;
        }else if (action.equalsIgnoreCase("openFileManager")) {
            openFileManager();
            return true;
         }

        return false;
    }

    /**
     * Open the appId details on Google Play .
     *
     * @param appId Application Id on Google Play.
     *              E.g.: com.google.earth
     */
    private static void openGooglePlay(CordovaInterface cordova, String appId) {
        try {
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void getDeviceAPILevel(CallbackContext callbackContext) {
        int apiLevel = Build.VERSION.SDK_INT;
        callbackContext.success(apiLevel);
    }

    private static void checkAppAvailability(CordovaInterface cordova, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            String packageName = args.getString(1);
            cordova.getActivity().getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
            callbackContext.success("true");
        } catch (PackageManager.NameNotFoundException e) {
            callbackContext.success("false");
        }

    }

    public static void getDownloadDirectoryPath(CallbackContext callbackContext) {
        callbackContext.success("file://" + String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)) + "/");
    }

    private static Class<?> getBuildConfigClass(String packageName) {
        return ReflectionUtil.getClass(packageName + ".BuildConfig");
    }

    private static void getBuildConfigParam(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String param = args.getString(1);
        String value;
        try {
            value = BuildConfigUtil.getBuildConfigValue("org.sunbird.app", param).toString();
            callbackContext.success(value);
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }


    }

    public static void getBuildConfigValue(String packageName, String property, CallbackContext callbackContext) {
        Class<?> clazz = getBuildConfigClass(packageName);
        if (clazz == null) {
            callbackContext.error("packageName, can not be null or empty.");
        }

        Object value = ReflectionUtil.getStaticFieldValue(clazz, property);
        if (value != null) {
            callbackContext.success(value.toString());
        } else {
            callbackContext.error("Value Not found");
        }

    }

    public static void getBuildConfigValues(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String packageName = args.getString(0);
        Class<?> clazz = getBuildConfigClass(packageName);
        if(clazz == null) {
            callbackContext.error("packageName, can not be null or empty.");
        }
        HashMap values = ReflectionUtil.getBuildConfigValues(clazz);
        JSONObject jsonObject = new JSONObject(values);
        callbackContext.success(jsonObject.toString());
    }

    private static int getIdOfResource(CordovaInterface cordova, String name, String resourceType) {
        return cordova.getActivity().getResources().getIdentifier(name, resourceType,
                cordova.getActivity().getApplicationInfo().packageName);
    }

    private static void exportApk(JSONArray args, final CordovaInterface cordova, final CallbackContext callbackContext) {
        try {
            String destination = args.getString(1).replace("file://", "");
            ApplicationInfo app = cordova.getActivity().getApplicationInfo();
            String filePath = app.sourceDir;

            // Append file
            File originalApk = new File(filePath);

            File tempFile;
            if (!TextUtils.isEmpty(destination)) {
                tempFile = new File(destination);
            } else {
                tempFile = new File(cordova.getActivity().getExternalCacheDir() + "/ExtractedApk");
            }
            // Make new directory in new location

            // If directory doesn't exists create new
            if (!tempFile.isDirectory())
                if (!tempFile.mkdirs())
                    return;
            // Get application's name and convert to lowercase
            tempFile = new File(tempFile.getPath() + "/"
                    + cordova.getActivity().getString(getIdOfResource(cordova, "_app_name", "string")) + "_"
                    + BuildConfigUtil.getBuildConfigValue("org.sunbird.app", "REAL_VERSION_NAME").toString().replace(".","_") + ".apk");
            // If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return;
                }
            }
            // Copy file to new location
            InputStream in = new FileInputStream(originalApk);
            OutputStream out = new FileOutputStream(tempFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            System.out.println("File copied.");
            callbackContext.success(tempFile.getPath());
        } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
        }
    }

    private static void getApkSize(final CordovaInterface cordova, final CallbackContext callbackContext) {
        try {
            ApplicationInfo app = cordova.getActivity().getApplicationInfo();
            String filePath = app.sourceDir;
            File originalApk = new File(filePath);
            callbackContext.success(String.valueOf(originalApk.length()));
        } catch (Exception ex) {
            callbackContext.error(ex.getMessage());
        }
    }

    private static void createDirectories( JSONArray args, CallbackContext callbackContext)  {
        try {

            String parentDirectory = args.getString(1);
            String[] identifiers = toStringArray(args.getJSONArray(2));
            JSONObject jsonObject = new JSONObject();
            for (int i=0;i<identifiers.length;i++){
                File f = new File(parentDirectory, identifiers[i]);
                if (!f.isDirectory()) {
                    f.mkdirs();
                }
                JSONObject output = new JSONObject();
                output.put("path","file://"+f.getPath()+ "/");
                jsonObject.put(identifiers[i], output);
            }
            callbackContext.success(jsonObject);

        } catch (Exception e) {
            callbackContext.error("false");
        }

    }

    private static void writeFile( JSONArray args, CallbackContext callbackContext)  {
        try {

            JSONArray mapList = args.getJSONArray(1);

            for (int i=0;i<mapList.length();i++){
                JSONObject jsonObject = mapList.getJSONObject(i);
                String destinationPath = jsonObject.getString("path");
                String fileName = jsonObject.getString("fileName");
                String data = jsonObject.getString("data");
                FileUtil.write(destinationPath, fileName, data);
            }
            callbackContext.success();

        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private static void getMetaData( JSONArray args, CallbackContext callbackContext)  {
        try {

            JSONArray inputArray = args.getJSONArray(1);
            JSONObject jsonObject = new JSONObject();
            for (int i=0;i<inputArray.length();i++){
                JSONObject eachItem = inputArray.getJSONObject(i);
                File f = new File(eachItem.getString("path"));

                JSONObject output = new JSONObject();
                output.put("size",FileUtil.getFileSize(f));
                output.put("lastModifiedTime",f.lastModified());
                jsonObject.put(eachItem.getString("identifier"), output);
            }
            callbackContext.success(jsonObject);

        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private static void getAvailableInternalMemorySize(CallbackContext callbackContext)  {
        try {
            callbackContext.success(String.valueOf(new DeviceSpecGenerator().getAvailableInternalMemorySize()));
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private static String[] toStringArray(JSONArray array) throws JSONException {
        int length = array.length();
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            values[i] = array.getString(i);
        }
        return values;
    }

    private static void getUtmInfo(CordovaInterface cordova, CallbackContext callbackContext)  {
        try {
            SharedPreferences splashSharedPreferences = cordova.getActivity().getSharedPreferences(UtilityPlugin.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            boolean isFirstTime = splashSharedPreferences.getBoolean("installed_referrer_api", true);
            if (isFirstTime) {
                PlayStoreInstallReferrer playStoreInstallreferrer = new PlayStoreInstallReferrer();
                playStoreInstallreferrer.start(cordova.getActivity(), new InstallReferrerListener() {
                    @Override
                    public void onHandlerReferrer(Map<String, String> properties) {
                        SharedPreferences sharedPreferences = cordova.getActivity().getSharedPreferences(UtilityPlugin.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("campaign_parameters", String.valueOf((new JSONObject(properties))));

                        callbackContext.success(new JSONObject(properties));
                        splashSharedPreferences.edit().putBoolean("installed_referrer_api", false).apply();
                        editor.commit();
                    }
                });
            } else {
                callbackContext.success("");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private static void clearUtmInfo(CordovaInterface cordova, CallbackContext callbackContext)  {
        try {
            SharedPreferences sharedPreferences = cordova.getActivity().getSharedPreferences(UtilityPlugin.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("campaign_parameters");
            editor.commit();
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private void removeDirectory(JSONArray args, CallbackContext callbackContext){
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    FileUtil.rm(new File(args.getString(0).replace("file://", "")), args.getString(1));
                    callbackContext.success();
                } catch (Exception e) {
                    callbackContext.error("Error while deleting");
                }
            }
        });
    }

    private void copyDirectory(JSONArray args, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    String sourceDirectory = args.getString(1).replace("file://", "");
                    String destinationDirectory = args.getString(2).replace("file://", "");
                    FileUtil.copyFolder(new File(sourceDirectory), new File(destinationDirectory));
                    callbackContext.success();
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });


    }
    private static void getStorageVolumes(CordovaInterface cordova, CallbackContext callbackContext)  {
        try {
            callbackContext.success(StorageUtil.getStorageVolumes(cordova.getContext()));
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private static void renameDirectory(JSONArray args, CallbackContext callbackContext)  {
        try {
            String sourceDirectory = args.getString(1).replace("file://", "");
            String toDirectoryName = args.getString(2);
            FileUtil.renameTo(new File(sourceDirectory), toDirectoryName);
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private static void canWrite(JSONArray args, CallbackContext callbackContext)  {
        try {
            String directory = args.getString(1).replace("file://", "");
            boolean canWrite = new File(directory).canWrite();
            if(canWrite){
                callbackContext.success();
            }else{
                callbackContext.error("Can't write to the folder");
            }

        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private static void getUsableSpace(JSONArray args, CallbackContext callbackContext)  {
        try {
            String directory = args.getString(1).replace("file://", "");
            long freeUsableSpace = FileUtil.getFreeUsableSpace(new File(directory));
            callbackContext.success(String.valueOf(freeUsableSpace));
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }

    }

    private  void readFromAssets(JSONArray args, CallbackContext callbackContext)  {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    String fileName = args.getString(1).replace("file:///android_asset/","");
                    String output = FileUtil.readFileFromAssets(cordova.getActivity().getAssets().open(fileName));
                    if(output != null){
                        callbackContext.success(output);
                    }else{
                        callbackContext.error(0);
                    }

                } catch (Exception e) {
                    e.getMessage();
                    callbackContext.error(e.getMessage());
                }

            }
        });

    }

    private  void copyFile(JSONArray args, CallbackContext callbackContext)  {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    String sourcePath = args.getString(1).replace("file://", "");
                    String destinationPath = args.getString(2).replace("file://", "");
                    String fileName = args.getString(3);
                    File source = new File(sourcePath, fileName);
                    if (source.exists()) {
                        File dest = new File(destinationPath, fileName);
                        dest.getParentFile().mkdirs();

                        FileUtil.cp(source, dest);
                    }
                    callbackContext.success();
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void verifyCaptcha(JSONArray args, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    String apiKey = args.getString(1);
                    verify(apiKey, callbackContext);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void verify(String apiKey, CallbackContext callbackContext) {
        if (apiKey.length() > 0) {
            SafetyNet.getClient(cordova.getActivity())
                    .verifyWithRecaptcha(apiKey)
                    .addOnSuccessListener(cordova.getActivity(),
                            new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                                @Override
                                public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                    String userResponseToken = response.getTokenResult();
                                    if (!userResponseToken.isEmpty()) {
                                        callbackContext.success(userResponseToken);
                                    } else {
                                        callbackContext.error("Repsonse token was empty.");
                                    }
                                }
                            })
                    .addOnFailureListener(cordova.getActivity(),
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (e instanceof ApiException) {
                                        // An error we know about occurred.
                                        ApiException apiException = (ApiException) e;
                                        int statusCode = apiException.getStatusCode();
                                        String message = CommonStatusCodes.getStatusCodeString(statusCode);
                                        callbackContext.error(message);
                                    } else {
                                        // A different, unknown type of error occurred.
                                        callbackContext.error(e.getMessage());
                                    }
                                }
                            });

        } else {
            callbackContext.error("Verify called without providing a Site Key");
        }
    }

    private void getAppAvailabilityStatus(CordovaInterface cordova, CallbackContext callbackContext, JSONArray appList) {
        final PackageManager packageManager = cordova.getContext().getPackageManager();
        List<ApplicationInfo> packagesList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> packageNameList = new ArrayList();
        for(ApplicationInfo p: packagesList) {
            packageNameList.add(p.packageName);
        }
        JSONObject availableAppsMap = new JSONObject();
        try {
            for (int i = 0; i < appList.length(); i++) {
                availableAppsMap.put(appList.getString(i), packageNameList.contains(appList.getString(i)));
            }
        } catch (Exception e) {
            e.getMessage();
        }
        callbackContext.success(availableAppsMap);
    }

    private void startActivity(Intent intent, int requestCode, CallbackContext callbackContext) {
        if (intent.resolveActivityInfo(this.cordova.getActivity().getPackageManager(), 0) != null) {
            cordova.setActivityResultCallback(this);
            this.cordova.getActivity().startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (onActivityResultCallbackContext != null && intent != null) {
            intent.putExtra("requestCode", requestCode);
            intent.putExtra("resultCode", resultCode);
            PluginResult result = new PluginResult(PluginResult.Status.OK, IntentUtil.getIntentJson(intent));
            result.setKeepCallback(true);
            onActivityResultCallbackContext.sendPluginResult(result);
        } else if (onActivityResultCallbackContext != null) {
            Intent canceledIntent = new Intent();
            canceledIntent.putExtra("requestCode", requestCode);
            canceledIntent.putExtra("resultCode", resultCode);
            PluginResult canceledResult = new PluginResult(PluginResult.Status.OK, IntentUtil.getIntentJson(canceledIntent));
            canceledResult.setKeepCallback(true);
            onActivityResultCallbackContext.sendPluginResult(canceledResult);
        }

        this.onActivityResultCallbackContext = null;
    }

    private void openFileManager() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(cordova.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString());
            intent.setDataAndType(uri, "*/*");
            this.cordova.getActivity().startActivity(intent);
        }
}