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
  private final boolean mAllowRebind = false;

  // Audio player
  private WildPlayer mPlayer = null;

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class MediaBinder extends Binder {
    public MediaService getService() {
      // Return this instance of LocalService so clients can call public methods
      return MediaService.this;
    }
  }

  /**
   *
   * @param intent
   * @return
   */
  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  /**
   *
   * @param intent
   * @return
   */
  @Override
  public boolean onUnbind(Intent intent) {
    mPlayer.release();
    // All clients have unbound with unbindService()
    return mAllowRebind;
  }

  /**
   *
   * @param pId
   * @param pListener
   */
  public void createMediaPlayer(@StringRes int pId, @NonNull WildOnPlayerListener pListener) {
    // Initialization of the wild audio player
    mPlayer = new WildPlayer(getApplicationContext());
    mPlayer.init(pId, pListener);
  }

  /**
   *
   * @return
   */
  @Nullable
  public WildPlayer getPlayer() {
    return mPlayer;
  }

  /**
   *
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
