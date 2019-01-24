package fr.wildcodeschool.mediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import fr.wildcodeschool.mediaplayer.notification.MediaNotification;
import fr.wildcodeschool.mediaplayer.notification.NotificationReceiver;
import fr.wildcodeschool.mediaplayer.player.WildOnPlayerListener;
import fr.wildcodeschool.mediaplayer.player.WildPlayer;
import fr.wildcodeschool.mediaplayer.service.MediaService;

public class MainActivity extends AppCompatActivity
  implements SeekBar.OnSeekBarChangeListener, ServiceConnection {
  // Bound service
  MediaService mService;
  boolean mBound = false;

  // SeekBar
  private SeekBar mSeekBar = null;
  // SeekBar update delay
  private static final int SEEKBAR_DELAY = 1000;
  // Thread used to update the SeekBar position
  private final Handler mSeekBarHandler = new Handler();
  private Runnable mSeekBarThread;

  // Notification
  private MediaNotification mNotification = null;

  /**
   * Application context accessor
   * https://possiblemobile.com/2013/06/context/
   */
  private static Context appContext;
  public  static Context getAppContext() {
    return appContext;
  }

  /**
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Initialization of the application context
    MainActivity.appContext = getApplicationContext();

    // Bind to MediaService
    Intent intent = new Intent(this, MediaService.class);
    bindService(intent, this, Context.BIND_AUTO_CREATE);

    // Initialization of the SeekBar
    mSeekBar = findViewById(R.id.seekBar);
    mSeekBar.setOnSeekBarChangeListener(this);

    // Thread used to update the SeekBar position according to the audio player
    mSeekBarThread = new Runnable() {
      @Override
      public void run() {
        // Widget should only be manipulated in UI thread
        mSeekBar.post(() -> {
          if (null != getPlayer()) {
            mSeekBar.setProgress(getPlayer().getCurrentPosition());
          }
        });
        // Launch a new request
        mSeekBarHandler.postDelayed(this, SEEKBAR_DELAY);
      }
    };

    // Create the notification
    mNotification =
      new MediaNotification.Builder(getApplicationContext())
        .addActions(NotificationReceiver.class)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.greenday))
        .setContentTitle(getString(R.string.song_title))
        .setContentText(getString(R.string.song_description))
        .buildNotification();
    mNotification.register();
  }

  /**
   *
   */
  @Override
  protected void onResume() {
    super.onResume();
    if (null != getPlayer() && getPlayer().isPlaying()) {
      // Update seekbar position
      mSeekBar.setProgress(getPlayer().getCurrentPosition());
      // Launch a new request
      mSeekBarHandler.postDelayed(mSeekBarThread, SEEKBAR_DELAY);
    }
  }

  /**
   *
   */
  @Override
  protected void onPause() {
    super.onPause();
    mSeekBarHandler.removeCallbacks(mSeekBarThread);
  }

  /**
   *
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindService(this);
    mBound = false;

    if (null != mNotification)
      mNotification.unregister();
  }

  // --------------------------------------------------------------------------
  // Player validity
  // --------------------------------------------------------------------------

  /**
   *
   * @return
   */
  private boolean isPlayerReady() {
    return mBound
      && (null != mService)
      && (null != mService.getPlayer());
  }

  /**
   *
   * @return
   */
  private WildPlayer getPlayer() {
    return isPlayerReady() ? mService.getPlayer() : null;
  }

  // --------------------------------------------------------------------------
  // Service interface
  // --------------------------------------------------------------------------

  /**
   *
   * @param className
   * @param service
   */
  @Override
  public void onServiceConnected(ComponentName className, IBinder service) {
    // We've bound to MediaService, cast the IBinder and get MediaService instance
    MediaService.MediaBinder binder = (MediaService.MediaBinder) service;
    mService = binder.getService();
    mBound = true;

    mService.createMediaPlayer(R.string.song, new WildOnPlayerListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        mSeekBar.setMax(mp.getDuration());
      }

      @Override
      public void onCompletion(MediaPlayer mp) {
        mSeekBarHandler.removeCallbacks(mSeekBarThread);
        mSeekBar.setProgress(0);
      }
    });
  }

  /**
   *
   * @param arg0
   */
  @Override
  public void onServiceDisconnected(ComponentName arg0) {
    mBound = false;
  }


  // --------------------------------------------------------------------------
  // SeekBar interface
  // --------------------------------------------------------------------------

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   * @param progress Current position on the SeekBar
   * @param fromUser Define if it is a user action or a programmatic seekTo
   */
  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (fromUser && null != getPlayer()) {
        getPlayer().seekTo(progress);
      }
  }

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   */
  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // Stop seekBarUpdate here
    mSeekBarHandler.removeCallbacks(mSeekBarThread);
  }

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   */
  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    // Restart seekBarUpdate here
    if (null != getPlayer() && getPlayer().isPlaying()) {
      mSeekBarHandler.postDelayed(mSeekBarThread, SEEKBAR_DELAY);
    }
  }


  // --------------------------------------------------------------------------
  // Buttons onClick
  // --------------------------------------------------------------------------

  /**
   * On play button click
   * Launch the playback of the media
   */
  public void playMedia(View v) {
    if (null != getPlayer() && getPlayer().play()) {
      mSeekBarHandler.postDelayed(mSeekBarThread, SEEKBAR_DELAY);
    }
  }

  /**
   * On pause button click
   * Pause the playback of the media
   */
  public void pauseMedia(View v) {
    if (null != getPlayer() && getPlayer().pause()) {
      mSeekBarHandler.removeCallbacks(mSeekBarThread);
    }
  }

  /**
   * On reset button click
   * Stop the playback of the media
   */
  public void stopMedia(View v) {
    if (null != getPlayer() && getPlayer().stop()) {
      mSeekBarHandler.removeCallbacks(mSeekBarThread);
      mSeekBar.setProgress(0);
    }
  }
}
