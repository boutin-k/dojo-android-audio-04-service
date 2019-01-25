package fr.wildcodeschool.mediaplayer.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import fr.wildcodeschool.mediaplayer.service.MediaService;

import static fr.wildcodeschool.mediaplayer.notification.MediaNotification.*;

public class NotificationReceiver extends BroadcastReceiver {
  /**
   * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
   * @param ctx Context: The Context in which the receiver is running.
   * @param intent Intent: The Intent being received.
   */
  @Override
  public void onReceive(Context ctx, Intent intent)
  {
    if (null != intent && null != intent.getAction()) {
      // Get service binder
      IBinder binder = peekService(ctx, new Intent(ctx, MediaService.class));
      if (null != binder) {
        // Get service instance
        MediaService lService = ((MediaService.MediaBinder)binder).getService();
        switch (intent.getAction()) {
          case ACTION_PLAY:
            lService.playMedia();
            break;
          case ACTION_PAUSE:
            lService.pauseMedia();
            break;
          case ACTION_STOP:
            lService.stopMedia();
            break;
        }
      }
    }
  }
}
