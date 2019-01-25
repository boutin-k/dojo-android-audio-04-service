package fr.wildcodeschool.mediaplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.*;

import fr.wildcodeschool.mediaplayer.player.WildOnPlayerListener;
import fr.wildcodeschool.mediaplayer.player.WildPlayer;

public class MediaService extends Service {
  // Binder given to clients
  private final IBinder mBinder = new MediaBinder();

  // Audio player
  private WildPlayer mPlayer = null;

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class MediaBinder extends Binder {
    public MediaService getService() {
      // Return this instance of MediaService so clients can call public methods
      return MediaService.this;
    }
  }

  /**
   * Return the communication channel to the service. May return null if clients
   * can not bind to the service. The returned IBinder is usually for a complex
   * interface that has been described using aidl.
   * @param intent Intent: The Intent that was used to bind to this service, as given to Context.bindService.
   * @return Return an IBinder through which clients can call on to the service. This value may be null.
   */
  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  /**
   * Called when all clients have disconnected from a particular interface published by the service.
   * The default implementation does nothing and returns false.
   * @param intent Intent: The Intent that was used to bind to this service.
   * @return Return true if you would like to have the service's onRebind(Intent) method later
   * called when new clients bind to it.
   */
  @Override
  public boolean onUnbind(Intent intent) {
    mPlayer.release();
    // Don't allow rebind
    return false;
  }

  /**
   * Instanciate a new MediaPlayer according to the StringId parameter
   * @param pId int: The id of the string stored in resource
   * @param pListener WildOnPlayerListener
   */
  public void createMediaPlayer(@StringRes int pId, @NonNull WildOnPlayerListener pListener) {
    // Initialization of the wild audio player
    mPlayer = new WildPlayer(getApplicationContext());
    mPlayer.init(pId, pListener);
  }

  /**
   * get the instance of the service mediaPlayer
   * @return Return the instance of the mediaPlayer
   */
  @Nullable
  public WildPlayer getPlayer() {
    return mPlayer;
  }

  /**
   * Called by the system to notify a Service that it is no longer used and is being removed.
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mPlayer != null) mPlayer.release();
  }

  /**
   * On play button click
   * Launch the playback of the media
   */
  public void playMedia() {
    if (null != mPlayer) mPlayer.play();
  }

  /**
   * On pause button click
   * Pause the playback of the media
   */
  public void pauseMedia() {
    if (null != mPlayer) mPlayer.pause();
  }

  /**
   * On reset button click
   * Stop the playback of the media
   */
  public void stopMedia() {
    if (null != mPlayer) mPlayer.stop();
  }
}
