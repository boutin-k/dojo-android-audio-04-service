package fr.wildcodeschool.mediaplayer.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import fr.wildcodeschool.mediaplayer.R;

@SuppressWarnings("unused")
public class MediaNotification {
  public static final String ACTION_PLAY  = "Play";
  public static final String ACTION_PAUSE = "Pause";
  public static final String ACTION_STOP  = "Stop";

  private static final String CHANNEL_ID      = "fr.wildcodeschool.mediaplayer";
  private static final String CHANNEL_NAME    = "Audio controller";

  private static final int NOTIFICATION_ID    = 100;
  private static final int REQUEST_CODE_PLAY  = 101;
  private static final int REQUEST_CODE_PAUSE = 102;
  private static final int REQUEST_CODE_STOP  = 103;

  private Context mContext;
  private NotificationManager mManager;
  private NotificationCompat.Builder mNotifBuilder;

  /**
   * Constructor
   * @param pCtx Context: The application context
   */
  private MediaNotification(@NonNull Context pCtx) {
    mContext = pCtx;
    // Populate notification
    mNotifBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
      // Show controls on lock screen even when user hides sensitive content.
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setSmallIcon(R.drawable.ic_stat_music_note)
      .setAutoCancel(true)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);

    // Since API 26, we can create a channel for the notifications.
    // The channel is displayed in the application settings.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
      && null == getManager().getNotificationChannel(CHANNEL_ID)) {
      // Create channel If it does not exists anymore.
      createChannel();
    }
  }

  /**
   * Only since API 26, the notifications could have a channel.
   * This method create a specific channel for the audio controller.
   */
  @RequiresApi(Build.VERSION_CODES.O)
  private void createChannel() {
    // create android channel
    NotificationChannel lChannel = new NotificationChannel(
      CHANNEL_ID,
      CHANNEL_NAME,
      NotificationManager.IMPORTANCE_DEFAULT);
    // Sets whether notifications posted to this channel appear on the lockscreen or not
    lChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    // Create the CHANNEL
    getManager().createNotificationChannel(lChannel);
  }

  /**
   * Register the notification from the NotificationManager
   */
  public void register() {
    // Register the notification
    getManager().notify(NOTIFICATION_ID, mNotifBuilder.build());
  }

  /**
   * Unregister the notification from the NotificationManager
   */
  public void unregister() {
    // Unregister the notification
    getManager().cancel(NOTIFICATION_ID);
  }

  /**
   * Get the class instance that manage the notifications.
   * @return NotificationManager: Class to notify the user of events that happen
   */
  private NotificationManager getManager() {
    if (mManager == null) {
      mManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    return mManager;
  }


  // --------------------------------------------------------------------------
  // NESTED BUILDER PATTERN
  // --------------------------------------------------------------------------
  public static class Builder {
    private static final int FLAG_NONE = 0;
    private MediaNotification mNotification;
    private Context mContext;

    /**
     * Constructor
     * @param pContext Context: The application context
     */
    public Builder(@NonNull Context pContext) {
      // Save the context
      mContext = pContext;
      // Get an instance of MediaNotification class
      mNotification = new MediaNotification(pContext);
    }

    /**
     * Add a large icon to the notification
     * @param lIcon Bitmap: Icon of the notification
     * @return The instance of the Builder
     */
    public Builder setLargeIcon(Bitmap lIcon) {
      mNotification.mNotifBuilder.setLargeIcon(lIcon);
      return this;
    }

    /**
     * Add a title to the notification
     * @param title String: Title of the notification
     * @return The instance of the Builder
     */
    public Builder setContentTitle(String title) {
      mNotification.mNotifBuilder.setContentTitle(title);
      return this;
    }

    /**
     * Add a teaser to the notification
     * @param text String: Teaser of the notification
     * @return The instance of the Builder
     */
    public Builder setContentText(String text) {
      mNotification.mNotifBuilder.setContentText(text);
      return this;
    }

    /**
     * Add buttons to the notification
     * @param pReceiver BroadcastReceiver: The intent action listener.
     * @return The instance of the Builder
     */
    public Builder addActions(Class<? extends BroadcastReceiver> pReceiver) {

      // Create PLAY intent
      Intent playIntent = new Intent(ACTION_PLAY);
      playIntent.setClass(mContext, pReceiver);
      PendingIntent playPendingIntent =
        PendingIntent.getBroadcast(mContext, REQUEST_CODE_PLAY, playIntent, FLAG_NONE);

      // Create PAUSE intent
      Intent pauseIntent = new Intent(ACTION_PAUSE);
      pauseIntent.setClass(mContext, pReceiver);
      PendingIntent pausePendingIntent =
        PendingIntent.getBroadcast(mContext, REQUEST_CODE_PAUSE, pauseIntent, FLAG_NONE);

      // Create STOP intent
      Intent stopIntent = new Intent(ACTION_STOP);
      stopIntent.setClass(mContext, pReceiver);
      PendingIntent stopPendingIntent =
        PendingIntent.getBroadcast(mContext, REQUEST_CODE_STOP, stopIntent, FLAG_NONE);

      mNotification.mNotifBuilder
        // Add media control buttons that invoke intents in your media service
        .addAction(R.drawable.ic_stat_play,  ACTION_PLAY,  playPendingIntent)  // #0: play button
        .addAction(R.drawable.ic_stat_pause, ACTION_PAUSE, pausePendingIntent) // #1: pause button
        .addAction(R.drawable.ic_stat_stop,  ACTION_STOP,  stopPendingIntent); // #2: stop button

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Apply the media style template
        mNotification.mNotifBuilder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
          // Add 3 actions index in the list to display them in notification.
          // #0: play button
          // #1: pause button
          // #2: stop button
          .setShowActionsInCompactView(0, 1, 2));
      }
      return this;
    }

    /**
     * Finalize the build by returning the notification instance
     * @return The instance of the Notification
     */
    public MediaNotification buildNotification() {
      // Return the instance of the class
      return mNotification;
    }
  }
}
