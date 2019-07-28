package com.xencos.spotifycontroller;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "73448c8e74ab4e098e0e3bef49ead759";
    private static final String REDIRECT_URI = "https://com.xencos.spotifycontroller/callback";
    //private static final String REDIRECT_URI = "testschema://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    private static final String TAG = "MainActivity";

    private TextView songTitle;
    private TextView songArtist;
    private ImageView playPause;

    private int mPlayerState = -1;
    private ImageView skipPrev;
    private ImageView skipNext;

    private AudioJackReceiver jackReceiver;

    @Override
    protected void onPause() {
        unregisterReceiver(jackReceiver);
        super.onPause();
    }

    private void initializeElements(){
        songTitle = findViewById(R.id.tv_title);
        songArtist = findViewById(R.id.tv_artist);
        playPause = findViewById(R.id.iv_play_pause);
        skipNext = findViewById(R.id.iv_next);
        skipPrev = findViewById(R.id.iv_prev);

        WheelPicker wheelPicker = findViewById(R.id.playlist_picker);
        List<String> data = new ArrayList<>();
        data.add("Wake Up Happy");
        data.add("Hit the streak!");
        data.add("Gym All the way");
        data.add("Wake Up Happy");
        data.add("Hit the streak!");
        data.add("Gym All the way");
        data.add("Wake Up Happy");
        data.add("Hit the streak!");
        data.add("Gym All the way");
        wheelPicker.setData(data);

        jackReceiver = new AudioJackReceiver();
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(jackReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeElements();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // We will start writing our code here.

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    private void connected() {
        // Then we will write some more code here.

        //mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                        songTitle.setText(track.name);
                        songArtist.setText(track.artist.name);
                        if(playerState.isPaused){
                            mPlayerState = 0;
                            playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                        }else {
                            mPlayerState = 1;
                            playPause.setImageResource(R.drawable.ic_pause_white_48dp);
                        }
                        enableControls();
                    }
                });
    }

    private void enableControls(){
        PlayerApi player = mSpotifyAppRemote.getPlayerApi();

        playPause.setOnClickListener(v -> {
            if(mPlayerState == 0){ //Paused or not playing, should now play
                player.resume();
            }else if(mPlayerState == 1){
                player.pause();
            }else{
                Log.d(TAG, "onClick: ERROR CONNECTING");
            }
        });

        skipNext.setOnClickListener(v -> player.skipNext());
        skipPrev.setOnClickListener(v -> player.skipPrevious());


    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aaand we will finish off here.
    }
}
