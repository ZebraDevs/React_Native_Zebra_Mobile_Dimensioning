package com.zebramobiledimensioning;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zebramobiledimensioning.DimensioningConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZebraMobileDimensioningModule extends ReactContextBaseJavaModule {
  public static final String NAME = "ZebraMobileDimensioning";
  public static final String DIMENSIONING_EVENT = "DimensioningEvent";
  public static final String ACTION = "ACTION";
  private static final String TAG = ZebraMobileDimensioningModule.class.getSimpleName();
  private ReactApplicationContext reactContext;

  private static final int REQUEST_CODE = 100;
  private static final String DATE_TIME_LOGGING_FORMAT = "yyyyMMdd_HHmmss";

  public ZebraMobileDimensioningModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    reactContext.addActivityEventListener(mActivityEventListener);
  }

  @Override
  public String getName() {
    System.out.println("MODULE_NAME <--- " + NAME);
    return NAME;
  }

  @ReactMethod
  public void EnableDimension(ReadableMap params) {
    Log.d(TAG, "EnableDimension <-- " + params.toString());

    Bundle extras = new Bundle();
    extras.putString(DimensioningConstants.TOKEN, params.getString(DimensioningConstants.TOKEN));
    extras.putString(DimensioningConstants.MODULE, params.getString(DimensioningConstants.MODULE));
    sendIntentApi(DimensioningConstants.INTENT_ACTION_ENABLE_DIMENSION, extras);
  }

  @ReactMethod
  public void GetDimension(ReadableMap params) {
    Log.d(TAG, "GetDimension <-- " + params.toString());

    Bundle extras = new Bundle();
    extras.putString(DimensioningConstants.TOKEN, params.getString(DimensioningConstants.TOKEN));
    extras.putString(DimensioningConstants.OBJECT_ID, params.getString(DimensioningConstants.OBJECT_ID));
    extras.putString(DimensioningConstants.PARCEL_ID, params.getString(DimensioningConstants.OBJECT_ID));
    sendIntentApi(DimensioningConstants.INTENT_ACTION_GET_DIMENSION, extras);
  }

  @ReactMethod
  public void DisableDimension(ReadableMap params) {
    Log.d(TAG, "DisableDimension <-- " + params.toString());

    Bundle extras = new Bundle();
    extras.putString(DimensioningConstants.TOKEN, params.getString(DimensioningConstants.TOKEN));
    sendIntentApi(DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION, extras);
  }

  @ReactMethod
  public void GetDimensionParameters(ReadableMap params) {
    Log.d(TAG, "GetDimensionParameters <-- " + params.toString());

    Bundle extras = new Bundle();
    extras.putString(DimensioningConstants.TOKEN, params.getString(DimensioningConstants.TOKEN));
    sendIntentApi(DimensioningConstants.INTENT_ACTION_GET_DIMENSION_PARAMETER, extras);
  }

  @ReactMethod
  public void SetDimensionParameters(ReadableMap params) {
    Log.d(TAG, "SetDimensionParameters <-- " + params.toString());

    Bundle extras = new Bundle();
    extras.putString(DimensioningConstants.TOKEN, params.getString(DimensioningConstants.TOKEN));
    extras.putString(DimensioningConstants.DIMENSIONING_UNIT, params.getString(DimensioningConstants.DIMENSIONING_UNIT));
    extras.putBoolean(DimensioningConstants.REPORT_IMAGE, params.getBoolean(DimensioningConstants.REPORT_IMAGE));
    extras.putInt(DimensioningConstants.TIMEOUT, params.getInt(DimensioningConstants.TIMEOUT));
    sendIntentApi(DimensioningConstants.INTENT_ACTION_SET_DIMENSION_PARAMETER, extras);
  }

  private void sendEvent(ReactContext reactContext, String eventName, WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  public final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      try {
        if (requestCode == REQUEST_CODE) {
          if (intent != null) {
            String actionName = intent.getAction();
            emitReactNativeEvent(actionName, intent);
          }
        }
      } catch (Exception e) {
        Log.d(TAG, "onActivityResult", e);
      }
    }
  };

  private boolean isDimensioningServiceAvailable() {
    PackageManager pm = reactContext.getPackageManager();
    @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
    for (ApplicationInfo applicationInfo : packages) {
      try {
        if (applicationInfo.packageName.equals(DimensioningConstants.ZEBRA_DIMENSIONING_PACKAGE)) {
          return true;
        }
      } catch (Exception e) {
        Log.d(TAG, "isDimensioningServiceAvailable", e);
      }
    }
    return false;
  }

  /**
   * sendIntentApi Calling of each action is done in sendIntentApi.
   *
   * @param action Intent action to be performed
   * @param extras To be sent along with intent
   */
  public void sendIntentApi(String action, Bundle extras) {
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "sendIntentApi " + action);
        if (isDimensioningServiceAvailable()) {
          Activity activity = getCurrentActivity(); // Get the current activity from ReactContext
          if (activity == null) {
            Log.e(TAG, "Activity is null. Cannot send intent.");
            WritableMap params = Arguments.createMap();
            params.putString(ACTION, action);
            params.putInt(DimensioningConstants.RESULT_CODE, DimensioningConstants.ERROR);
            params.putString(DimensioningConstants.RESULT_MESSAGE, getReactApplicationContext().getString(R.string.not_supported_message));
            sendEvent(getReactApplicationContext(), DIMENSIONING_EVENT, params);
            return;
          }

          Intent intent = new Intent();
          intent.setAction(action);
          intent.setPackage(DimensioningConstants.ZEBRA_DIMENSIONING_PACKAGE);
          intent.putExtra(DimensioningConstants.APPLICATION_PACKAGE, activity.getPackageName());

          if (extras != null) {
            intent.putExtras(extras);
          }

          PendingIntent lobPendingIntent = activity.createPendingResult(REQUEST_CODE, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
          intent.putExtra(DimensioningConstants.CALLBACK_RESPONSE, lobPendingIntent);

          if (DimensioningConstants.INTENT_ACTION_ENABLE_DIMENSION.equals(intent.getAction())) {
            activity.startForegroundService(intent);
          } else {
            if (DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION.equals(intent.getAction())) {
              intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            }
            activity.sendBroadcast(intent); // Use activity to send the broadcast
          }
        } else {
          Log.e(TAG, "Dimensioning service not available");
          WritableMap params = Arguments.createMap();
          params.putString(ACTION, action);
          params.putInt(DimensioningConstants.RESULT_CODE, DimensioningConstants.ERROR);
          params.putString(DimensioningConstants.RESULT_MESSAGE, getReactApplicationContext().getString(R.string.not_installed_message));
          sendEvent(getReactApplicationContext(), DIMENSIONING_EVENT, params);
        }
      }
    });
  }

  private WritableMap processIntentToWritableMap(Intent intent) {
    WritableMap map = Arguments.createMap();

    if (intent == null) {
      return map;
    }

    Bundle extras = intent.getExtras();
    if (extras == null) {
      return map;
    }

    for (String key : extras.keySet()) {
      Object value = extras.get(key);
      Log.d(TAG, "IntentExtras: Key: " + key + ", Value: " + value + ", Type: " + (value != null ? value.getClass().getName() : "null"));

      if (value instanceof Integer) {
        map.putInt(key, (Integer) value);
      } else if (value instanceof String) {
        map.putString(key, (String) value);
      } else if (value instanceof String[]) {
        WritableArray writableArray = new WritableNativeArray();
        for (String s : (String[]) value) {
            writableArray.pushString(s);
        }
        map.putArray(key, writableArray);
      } else if (value instanceof Boolean) {
        map.putBoolean(key, (Boolean) value);
      } else if (value instanceof Double) {
        map.putDouble(key, (Double) value);
      } else if (value instanceof BigDecimal) {
        map.putString(key, ((BigDecimal) value).toString());
      } else if (value instanceof Instant) {
        map.putString(key, ((Instant) value).toString());
      } else if (value instanceof Serializable) {
        map.putString(key, value.toString());
      }
    }

    // Handle Bitmap conversion to Base64
    if (extras.containsKey(DimensioningConstants.IMAGE)) {
      Bitmap bitmapImage = (Bitmap) extras.getParcelable(DimensioningConstants.IMAGE);
      if (bitmapImage != null) {
        Instant timestamp = (Instant) extras.getSerializable(DimensioningConstants.TIMESTAMP);
        timestamp = (timestamp == null) ? Instant.now() : timestamp;
        String formattedTimestamp = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(DATE_TIME_LOGGING_FORMAT));
        File imageFile = saveBitmapToCache(bitmapImage, formattedTimestamp);
        if (imageFile != null) {
          String imagePath = imageFile.getAbsolutePath();
          map.putString(DimensioningConstants.IMAGE, imagePath);  // Pass the file path to React Native
        }
      }
    }

    return map;
  }

  private File saveBitmapToCache(Bitmap bitmap, String timestamp) {
    File cacheDir = getReactApplicationContext().getCacheDir();
    File imageFile = new File(cacheDir, "image_" + timestamp + ".png");
    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
      fos.flush();
    } catch (IOException e) {
      Log.e(TAG, "SaveBitmapToCache", e);
      return null;
    }
    return imageFile;
  }

  public void emitReactNativeEvent(String actionName, Intent intent) {
    WritableMap params = processIntentToWritableMap(intent);
    params.putString(ACTION, actionName);
    sendEvent(getReactApplicationContext(), DIMENSIONING_EVENT, params);
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("DIMENSIONING_EVENT", DIMENSIONING_EVENT);
    constants.put("ACTION", ACTION);

    constants.put("INTENT_ACTION_ENABLE_DIMENSION", DimensioningConstants.INTENT_ACTION_ENABLE_DIMENSION);
    constants.put("INTENT_ACTION_DISABLE_DIMENSION", DimensioningConstants.INTENT_ACTION_DISABLE_DIMENSION);
    constants.put("INTENT_ACTION_GET_DIMENSION", DimensioningConstants.INTENT_ACTION_GET_DIMENSION);
    constants.put("INTENT_ACTION_GET_DIMENSION_PARAMETER", DimensioningConstants.INTENT_ACTION_GET_DIMENSION_PARAMETER);
    constants.put("INTENT_ACTION_SET_DIMENSION_PARAMETER", DimensioningConstants.INTENT_ACTION_SET_DIMENSION_PARAMETER);

    constants.put("TOKEN", DimensioningConstants.TOKEN);
    constants.put("MODULE", DimensioningConstants.MODULE);
    constants.put("RESULT_CODE", DimensioningConstants.RESULT_CODE);
    constants.put("RESULT_MESSAGE", DimensioningConstants.RESULT_MESSAGE);
    constants.put("READY_LENGTH", DimensioningConstants.READY_LENGTH);
    constants.put("READY_WIDTH", DimensioningConstants.READY_WIDTH);
    constants.put("READY_HEIGHT", DimensioningConstants.READY_HEIGHT);
    constants.put("DIMENSIONING_UNIT", DimensioningConstants.DIMENSIONING_UNIT);
    constants.put("FRAMEWORK_VERSION", DimensioningConstants.FRAMEWORK_VERSION);
    constants.put("SERVICE_VERSION", DimensioningConstants.SERVICE_VERSION);
    constants.put("BUNDLE_VERSION", DimensioningConstants.BUNDLE_VERSION);
    constants.put("REGULATORY_APPROVAL", DimensioningConstants.REGULATORY_APPROVAL);
    constants.put("SUPPORTED_UNITS", DimensioningConstants.SUPPORTED_UNITS);
    constants.put("REPORT_IMAGE", DimensioningConstants.REPORT_IMAGE);
    constants.put("TIMEOUT", DimensioningConstants.TIMEOUT);
    constants.put("LENGTH", DimensioningConstants.LENGTH);
    constants.put("WIDTH", DimensioningConstants.WIDTH);
    constants.put("HEIGHT", DimensioningConstants.HEIGHT);
    constants.put("LENGTH_STATUS", DimensioningConstants.LENGTH_STATUS);
    constants.put("WIDTH_STATUS", DimensioningConstants.WIDTH_STATUS);
    constants.put("HEIGHT_STATUS", DimensioningConstants.HEIGHT_STATUS);
    constants.put("TIMESTAMP", DimensioningConstants.TIMESTAMP);
    constants.put("IMAGE", DimensioningConstants.IMAGE);
    constants.put("PARCEL_ID", DimensioningConstants.PARCEL_ID);
    constants.put("OBJECT_ID", DimensioningConstants.OBJECT_ID);
    constants.put("OBJECT_TYPE", DimensioningConstants.OBJECT_TYPE);
    constants.put("MESSAGE", DimensioningConstants.MESSAGE);

    constants.put("PARCEL_MODULE", DimensioningConstants.PARCEL_MODULE);
    constants.put("SUCCESS", DimensioningConstants.SUCCESS);
    constants.put("FAILURE", DimensioningConstants.FAILURE);
    constants.put("ERROR", DimensioningConstants.ERROR);
    constants.put("CANCELED", DimensioningConstants.CANCELED);
    constants.put("NO_DIM", DimensioningConstants.NO_DIM);
    constants.put("BELOW_RANGE", DimensioningConstants.BELOW_RANGE);
    constants.put("IN_RANGE", DimensioningConstants.IN_RANGE);
    constants.put("ABOVE_RANGE", DimensioningConstants.ABOVE_RANGE);
    constants.put("INCH", DimensioningConstants.INCH);
    constants.put("CM", DimensioningConstants.CM);

    return constants;
  }
}
