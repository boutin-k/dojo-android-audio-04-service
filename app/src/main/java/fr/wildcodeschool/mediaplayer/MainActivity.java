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
   * Called when the activity is starting.
   * @param savedInstanceState Bundle: If the activity is being re-initialized after previously
   * being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
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
   * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(), for your activity
   * to start interacting with the user. This is a good place to begin animations,
   * open exclusive-access devices (such as the camera), etc.
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
   * Called as part of the activity lifecycle when an activity is going into the background,
   * but has not (yet) been killed. The counterpart to onResume().
   */
  @Override
  protected void onPause() {
    super.onPause();
    mSeekBarHandler.removeCallbacks(mSeekBarThread);
  }

  /**
   * Perform any final cleanup before an activity is destroyed. This can happen either because
   * the activity is finishing (someone called finish() on it), or because the system is
   * temporarily destroying this instance of the activity to save space.
   * You can distinguish between these two scenarios with the isFinishing() method.
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Release the service
    unbindService(this);
    mBound = false;
    // Release the notification
    if (null != mNotification)
      mNotification.unregister();
    // Disable the seekbar handler
    mSeekBarHandler.removeCallbacks(mSeekBarThread);
  }

  // --------------------------------------------------------------------------
  // Player validity
  // --------------------------------------------------------------------------

  /**
   * Get the validity of mediaPlayer instance
   * @return boolean: Returns the validity of th WildPlayer
   */
  private boolean isPlayerReady() {
    return mBound
      && (null != mService)
      && (null != mService.getPlayer());
  }

  /**
   * Return the instance of the WildPlayer stored in the service
   * @return WildPlayer: The instance of the WildPlayer
   */
  private WildPlayer getPlayer() {
    return isPlayerReady() ? mService.getPlayer() : null;
  }

  // --------------------------------------------------------------------------
  // Service interface
  // --------------------------------------------------------------------------

  /**
   * Called when a connection to the Service has been established, with the IBinder of the
   * communication channel to the Service.
   * @param className ComponentName: The concrete component name of the service that has been connected.
   * @param service IBinder: The IBinder of the Service's communication channel, which you can now make calls on.
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
   * Called when a connection to the Service has been lost. This typically happens when the
   * process hosting the service has crashed or been killed.
   * @param name ComponentName: The concrete component name of the service whose connection has been lost.
   */
  @Override
  public void onServiceDisconnected(ComponentName name) {
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
