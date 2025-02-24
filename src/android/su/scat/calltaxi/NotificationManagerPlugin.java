package su.scat.calltaxi;
import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Application;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import java.util.*;
import java.util.stream.*;

public class NotificationManagerPlugin extends CordovaPlugin {

    @TargetApi(26)
    private void openAppNotificationSettings() {
        // only call on Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Activity activity = this.cordova.getActivity();

            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
            activity.startActivity(intent);
        }
    }

    /**
     * After you create a notification channel, you cannot change the notification channel's visual
     * and auditory behaviors programmatically—only the user can change the channel behaviors from
     * the system settings. To provide your users easy access to these notification settings, you
     * should add an item in your app's settings UI that opens these system settings.
     *
     * See: https://developer.android.com/training/notify-user/channels#UpdateChannel
     **/

    @TargetApi(26)
    private void openNotificationChannelSettings(String channelId) {
        // only call on Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Activity activity = this.cordova.getActivity();

            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
            activity.startActivity(intent);
        }
    }

    /**
     * Users can modify the settings for notification channels, including behaviors such as
     * vibration and alert sound. You might like to know the settings a user has applied to your
     * notification channels.
     *
     * See: https://developer.android.com/training/notify-user/channels#ReadChannel
     **/

    @TargetApi(26)
    private JSONObject getNotificationChannel(String channelId) throws JSONException {
        JSONObject channelJSON = new JSONObject();
        // only call on Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Activity activity = this.cordova.getActivity();
            final NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationChannel channel = manager.getNotificationChannel(channelId);

            channelJSON.put("id", channel.getId());
            channelJSON.put("group", channel.getGroup());
            channelJSON.put("description", channel.getDescription());
            channelJSON.put("importance", channel.getImportance());
            channelJSON.put("lightColor", channel.getLightColor());
            channelJSON.put("sound", channel.getSound());
            channelJSON.put("lockscreenVisibility", channel.getLockscreenVisibility());
        }

        return channelJSON;
    }

    @TargetApi(26)
    private JSONObject setNotificationChannel(String channelId,CharSequence channelName,String channelDescription,String channelSound,String str_channelVibrate){
        JSONObject channelJSON=new JSONObject();
        // only call on Android O and above
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            final Activity activity=this.cordova.getActivity();
            final NotificationManager manager=(NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            //final NotificationChannel channel = manager.getNotificationChannel(channelId);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel=new NotificationChannel(channelId,channelName,importance);
            
            channel.setDescription(channelDescription);
            
            Uri soundUri=Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+activity.getApplicationContext().getPackageName()+"/raw/"+channelSound);
            //Uri soundUri=Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+activity.getApplicationContext().getPackageName()+"/"+channelSound);
            //AudioAttributes audioAttributes=new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            //channel.setSound(soundUri,audioAttributes);
            channel.setSound(soundUri,Notification.AUDIO_ATTRIBUTES_DEFAULT);

            long[] longPrimitiveArray = Arrays.stream(str_channelVibrate.split(",")).map(String::trim).mapToLong(Long::valueOf).toArray();
            channel.enableVibration(true);
            channel.setVibrationPattern(longPrimitiveArray);

            // Register the channel with the system; you can't change the importance or other notification behaviors after this
            NotificationManager notificationManager=(NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(channel);
        }

        return channelJSON;
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {

            if ("setNotificationChannel".equals(action)) {
                callbackContext.success(setNotificationChannel(args.getString(0),args.getString(1),args.getString(2),args.getString(3),args.getString(4)));
                return true;
            }

            if ("getNotificationChannel".equals(action)) {
                callbackContext.success(getNotificationChannel(args.getString(0)));
                return true;
            }

            if ("openNotificationChannelSettings".equals(action)) {
                openNotificationChannelSettings(args.getString(0));
                callbackContext.success();
                return true;
            }

            if ("openAppNotificationSettings".equals(action)) {
                openAppNotificationSettings();
                callbackContext.success();
                return true;
            }

        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        }

        return false;
    }
}
