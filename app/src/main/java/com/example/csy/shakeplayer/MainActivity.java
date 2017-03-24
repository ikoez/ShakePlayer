package com.example.csy.shakeplayer;

import com.musicg.api.ClapApi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import android.os.SystemClock;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.widget.Toast;


public class MainActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, SensorEventListener, GestureDetector.OnGestureListener, View.OnTouchListener {

    private static final String TAG = "MainActivity";

    private GestureDetector gestureDetector;
    private boolean firstScroll = false;
    private RelativeLayout root_layout;
    private int playerWidth, playerHeight;
    private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量,3.调节亮度
    private static final int GESTURE_MODIFY_PROGRESS = 1;
    private static final int GESTURE_MODIFY_VOLUME = 2;
    private static final int GESTURE_MODIFY_BRIGHT = 3;
    private static final float STEP_PROGRESS = 2f;// 设定进度滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快

    private MediaPlayer mMediaPlayer=null;
    private AudioManager mAudioManager=null;
    public DataEntity bookmarkentity;
    private DataList bookmarklist;

    private SensorManager sensorManager;
    private Sensor sensor1, sensor2;
    private long lastSense, lastCapture = -1;
    private long lastSenseT, lastCaptureT = -1;
    private long lastSenseC, lastCaptureC = -1;
    private float x, y, z;
    private float lastx, lasty, lastz;

    private Button buttonShare=null;
    private Button mPlayButton=null;
    private Button mPauseButton=null;
    private Button mStopButton=null;
    private Button addbookmark=null;
    private SeekBar mSoundSeekBar=null;
    private SeekBar mSoundProcessBar=null;
    private Switch sw,swpocket,swclap;

    private Timer mTimer=new Timer();
    private int maxStreamVolume;
    private int currentStreamVolume;
    private float moveStep = 0;

    private String musicname;

    private TextView starttime;
    private TextView finishtime;
    private TextView textbar;
    private TextMoveLayout textMoveLayout;
    private ViewGroup.LayoutParams layoutParams;
    private int screenWidth;
    public static MainActivity instance;

    public static final int DETECT_NONE = 0;
    public static final int DETECT_CLAP = 1;
    public static int selectedDetection = DETECT_NONE;

    private DetectorThread detectorThread;
    private RecorderThread recorderThread;
    private Thread detectedTextThread;
    public static int clapsValue = 0;

    private float[] m_rotationMatrix = new float[16];
    private float[] mGravity;
    private float[] mGeomagnetic;
    float m_lastPitch = 0.f;
    float m_lastYaw = 0.f;
    float m_lastRoll = 0.f;
    private float[] m_orientation = new float[4];
    private float previousroll, previouspitch = 0;

    Filter [] m_filters = { new Filter(), new Filter(), new Filter() };
    private class Filter {
        static final int AVERAGE_BUFFER = 10;
        float []m_arr = new float[AVERAGE_BUFFER];
        int m_idx = 0;

        public float append(float val) {
            m_arr[m_idx] = val;
            m_idx++;
            if (m_idx == AVERAGE_BUFFER)
                m_idx = 0;
            return avg();
        }
        public float avg() {
            float sum = 0;
            for (float x: m_arr)
                sum += x;
            return sum / AVERAGE_BUFFER;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        if (sensor1 != null) {

            sensorManager.registerListener(this, sensor1, SensorManager.SENSOR_DELAY_UI);
        }
        if (sensor2 != null) {
            sensorManager.registerListener(this, sensor2, SensorManager.SENSOR_DELAY_UI);
        }



        bookmarklist = new DataList();
        bookmarkentity = new DataEntity();
        bookmarkentity.setBookmarktime(0);
        bookmarklist.additem(bookmarkentity);
        mMediaPlayer= MediaPlayer.create(this, R.raw.spongebob);
        Uri uri=null;
        Intent intent=getIntent();
        if(intent.getExtras()!=null) {
            Bundle bundle = intent.getExtras();
            musicname = (String) bundle.getSerializable("uri");
            uri=Uri.parse(musicname);
        }
        if (uri!=null) mMediaPlayer=MediaPlayer.create(this,uri);
        TextView musicName= (TextView)findViewById(R.id.musicname);
        Pattern p = Pattern.compile("[^/]+\\..+");
        Matcher m=p.matcher(musicname);
        if(m.find()==true)
        {
            musicName.setText(m.group().toString());
        }
        mAudioManager = (AudioManager)this.getSystemService(AUDIO_SERVICE);

        DetectHelper();

        sw = (Switch)findViewById(R.id.switchvolume);
        sw.setChecked(false);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                          @Override
                                          public void onCheckedChanged(CompoundButton buttonView,
                                                                       boolean isChecked) {

                                              if (isChecked) {

                                              } else {

                                              }

                                          }
        });
        swpocket = (Switch)findViewById(R.id.switch2);
        swpocket.setChecked(false);
        swpocket.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {

                } else {

                }

            }
        });

        swclap = (Switch)findViewById(R.id.switchclap);
        swclap.setChecked(false);
        swclap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {

                } else {

                }

            }
        });

        startVoiceDetection();
        //buttonShare=(Button)findViewById(R.id.buttonshare);
        mPlayButton=(Button)findViewById(R.id.Play);
        mPauseButton=(Button)findViewById(R.id.Pause);
        mStopButton=(Button)findViewById(R.id.Stop);
        //addbookmark=(Button)findViewById(R.id.bookmark);
        mSoundSeekBar=(SeekBar)findViewById(R.id.SoundSeekBar);
        mSoundProcessBar=(SeekBar)findViewById(R.id.soundprocessseekBar);
        mPlayButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
//        addbookmark.setOnClickListener(this);
        //buttonShare.setOnClickListener(this);

        starttime=(TextView)findViewById(R.id.starttime);
        starttime.setText("00:00:00");
        finishtime=(TextView)findViewById(R.id.finishtime);
        int hour=mMediaPlayer.getDuration()/(1000*60*60);
        int minute=(mMediaPlayer.getDuration()%(1000*60*60))/(1000*60);
        int second=((mMediaPlayer.getDuration()%(1000*60*60))%(1000*60))/1000;
        String temp=" ";
        if(hour<10&&minute<10&&second<10)
            temp="0"+Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour<10&&minute<10&&second>=10)
            temp="0"+Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour<10&&minute>10&&second<10)
            temp="0"+Integer.toString(hour)+":"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour<10&&minute>10&&second>10)
            temp="0"+Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour>10&&minute<10&&second<10)
            temp=Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour>10&&minute<10&&second>10)
            temp=Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour>10&&minute>10&&second<10)
            temp=Integer.toString(hour)+":"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour>10&&minute>10&&second>10)
            temp=Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
        finishtime.setText(temp);

        textbar = new TextView(this);
        textbar.setBackgroundColor(Color.rgb(252, 246, 246));
        textbar.setTextColor(Color.rgb(236, 0, 4));
        textbar.setTextSize(16);
        moveStep = (float) (((float) screenWidth / (float) mMediaPlayer.getDuration()/1000) * 0.8);
        screenWidth=getWindowManager().getDefaultDisplay().getWidth();
        layoutParams = new ViewGroup.LayoutParams(screenWidth, 50);
        textMoveLayout = (TextMoveLayout) findViewById(R.id.textLayout);
        textMoveLayout.addView(textbar, layoutParams);
        textbar.layout(0, 20, screenWidth, 80);
        textbar.setText("00:00:00");

        maxStreamVolume=mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentStreamVolume=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mSoundSeekBar.setMax(maxStreamVolume);
        mSoundSeekBar.setProgress(currentStreamVolume);
        mSoundSeekBar.setOnSeekBarChangeListener(this);
        mSoundProcessBar.setProgress(0);
        mSoundProcessBar.setMax(mMediaPlayer.getDuration());
        mSoundProcessBar.setOnSeekBarChangeListener(this);
        final Handler handleProgress = new Handler() {
            public void handleMessage(Message msg) {

                double position = mMediaPlayer.getCurrentPosition();
                double duration = mMediaPlayer.getDuration();

                if (duration > 0) {
                    double pos = mSoundProcessBar.getMax() * position / duration;
                    mSoundProcessBar.setProgress((int) pos);
                }
            };
        };
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if(mMediaPlayer==null)
                    return;
                if (mMediaPlayer.isPlaying() && mSoundProcessBar.isPressed() == false) {
                    handleProgress.sendEmptyMessage(0);
                }
            }
        };

        mTimer.schedule(mTimerTask, 0, 1000);
        instance=this;
    }

    private void stopVoiceDetection() {
        if (recorderThread != null) {
            recorderThread.stopRecording();
            recorderThread = null;
        }
        if (detectorThread != null) {
            detectorThread.stopDetection();
            detectorThread = null;
        }
        selectedDetection = DETECT_NONE;
    }


    private void startVoiceDetection() {
        selectedDetection = DETECT_CLAP;
        recorderThread = new RecorderThread();
        recorderThread.start();
        detectorThread = new DetectorThread(recorderThread);
        detectorThread.start();
        goListeningView();
    }

    private void goListeningView() {
        if (detectedTextThread == null) {
            detectedTextThread = new Thread() {
                public void run() {
                    try {
                        while (recorderThread != null && detectorThread != null) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (detectorThread != null) {
                                        Log.e("Clap", "Detected");
                                        long now = System.currentTimeMillis();

                                        if ((now-lastCaptureC )<2000) return;
                                        if ((swclap.isChecked())&&(clapsValue>0)) {
                                            if (!mMediaPlayer.isPlaying()){
                                                mMediaPlayer.start();
                                                Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_LONG).show();
                                            }else{
                                                mMediaPlayer.pause();
                                                Toast.makeText(MainActivity.this, "Paused", Toast.LENGTH_LONG).show();
                                            }
                                            lastCaptureC = now;
                                        }
                                    }

                                }
                            });
                            sleep(100);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        detectedTextThread = null;
                    }
                }
            };
            detectedTextThread.start();
        }
    }

    public void changetimestamp(int progress){
        textbar.layout((int) (progress * moveStep), 20, screenWidth, 80);
        int hour=mMediaPlayer.getCurrentPosition()/(1000*60*60);
        int minute=(mMediaPlayer.getCurrentPosition()%(1000*60*60))/(1000*60);
        int second=((mMediaPlayer.getCurrentPosition()%(1000*60*60))%(1000*60))/1000;
        String temp=" ";
        if(hour<10&&minute<10&&second<10)
            temp="0"+Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour<10&&minute<10&&second>=10)
            temp="0"+Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour<10&&minute>10&&second<10)
            temp="0"+Integer.toString(hour)+":"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour<10&&minute>10&&second>10)
            temp="0"+Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour>10&&minute<10&&second<10)
            temp=Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour>10&&minute<10&&second>10)
            temp=Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour>10&&minute>10&&second<10)
            temp=Integer.toString(hour)+":"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour>10&&minute>10&&second>10)
            temp=Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
        textbar.setText(temp);

    }

    public void DetectHelper(){
        gestureDetector = new GestureDetector(this, this);
        root_layout = (RelativeLayout) findViewById(R.id.viewFlipper);
        root_layout.setLongClickable(true);
        gestureDetector.setIsLongpressEnabled(true);
        root_layout.setOnTouchListener(this);
        ViewTreeObserver viewObserver = root_layout.getViewTreeObserver();
        viewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                root_layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerWidth = root_layout.getWidth();
                playerHeight = root_layout.getHeight();
            }
        });
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        firstScroll = true;// 设定是触摸屏幕后第一次scroll的标志
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float mOldX = e1.getX(), mOldY = e1.getY();
        int y = (int) e2.getRawY();
        if (firstScroll) {// 以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱
            // 横向的距离变化大则调整进度，纵向的变化大则调整音量
            if (Math.abs(distanceX) >= Math.abs(distanceY)) {
//                gesture_progress_layout.setVisibility(View.VISIBLE);
//                gesture_volume_layout.setVisibility(View.GONE);
//                gesture_bright_layout.setVisibility(View.GONE);
                GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;
            } else {
                if (mOldX > playerWidth * 3.0 / 5) {// 音量
//                    gesture_volume_layout.setVisibility(View.VISIBLE);
//                    gesture_bright_layout.setVisibility(View.GONE);
//                    gesture_progress_layout.setVisibility(View.GONE);
                    GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
                } else if (mOldX < playerWidth * 2.0 / 5) {// 亮度
//                    gesture_bright_layout.setVisibility(View.VISIBLE);
//                    gesture_volume_layout.setVisibility(View.GONE);
//                    gesture_progress_layout.setVisibility(View.GONE);
                    GESTURE_FLAG = GESTURE_MODIFY_BRIGHT;
                }
            }
        }

        if (GESTURE_FLAG == GESTURE_MODIFY_VOLUME) {
            currentStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                if (distanceY >= DensityUtil.dip2px(this, STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (currentStreamVolume < maxStreamVolume) {// 为避免调节过快，distanceY应大于一个设定值
                        currentStreamVolume++;

                    }
                    mSoundSeekBar.setProgress(currentStreamVolume);
                    //Toast.makeText(MainActivity.this, "Volume up", Toast.LENGTH_LONG).show();
                } else if (distanceY <= -DensityUtil.dip2px(this, STEP_VOLUME)) {// 音量调小
                    if (currentStreamVolume > 0) {
                        currentStreamVolume--;

                        if (currentStreamVolume == 0) {// 静音，设定静音独有的图片
                            //Toast.makeText(MainActivity.this, "Silence", Toast.LENGTH_LONG).show();
                        }
                    }

                    mSoundSeekBar.setProgress(currentStreamVolume);
                    //Toast.makeText(MainActivity.this, "Volume down", Toast.LENGTH_LONG).show();
                }
                int percentage = (currentStreamVolume * 100) / maxStreamVolume;
                //geture_tv_volume_percentage.setText(percentage + "%");
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentStreamVolume, 0);
            }
        }
        else if(GESTURE_FLAG == GESTURE_MODIFY_PROGRESS){
            if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动

                if (distanceX >= DensityUtil.dip2px(this, STEP_PROGRESS)) {// 快退，用步长控制改变速度，可微调

                    //gesture_iv_progress.setImageResource(R.drawable.souhu_player_backward);
                    int newPos = mMediaPlayer.getCurrentPosition() - 500;
                    if (newPos < 0)
                        newPos = 0;
                    mMediaPlayer.seekTo(newPos);
                    mSoundProcessBar.setProgress(newPos);
                    changetimestamp(newPos);
                } else if (distanceX <= -DensityUtil.dip2px(this, STEP_PROGRESS)) {// 快进
                    //gesture_iv_progress.setImageResource(R.drawable.souhu_player_forward);
                    int newPos = 500 + mMediaPlayer.getCurrentPosition();
                    int mPlayEndMsec = mMediaPlayer.getDuration();
                    if (newPos > mPlayEndMsec)
                        newPos = mPlayEndMsec;
                    mMediaPlayer.seekTo(newPos);
                    mSoundProcessBar.setProgress(newPos);
                    changetimestamp(newPos);
                }


                //geture_tv_progress_time.setText(DateTools.getTimeStr(playingTime) + "/" + DateTools.getTimeStr(videoTotalTime));
            }
        }

        firstScroll = false;// 第一次scroll执行完成，修改标志
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 手势里除了singleTapUp，没有其他检测up的方法
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
//            gesture_volume_layout.setVisibility(View.GONE);
//            gesture_bright_layout.setVisibility(View.GONE);
//            gesture_progress_layout.setVisibility(View.GONE);
        }
        return gestureDetector.onTouchEvent(event);
    }


    @Override
    public void onShowPress(MotionEvent e) {}
    public void onLongPress(MotionEvent e) {}
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }


    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch(v.getId()){
            case R.id.Play:
                mMediaPlayer.start();
                break;
            case R.id.Pause:
                mMediaPlayer.pause();
                break;
            case R.id.Stop:
                System.out.println("Stop");
                mMediaPlayer.stop();
                try{
                    mMediaPlayer.prepare();
                }catch(IllegalStateException e){
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
                mMediaPlayer.seekTo(0);
                mSoundProcessBar.setProgress(0);
                break;
//            case R.id.buttonshare:
//                String sharePath=musicname;
//                Uri uri = Uri.fromFile(new File(sharePath));
//                String content="";
//                content=content+"share "+musicname;
//                Intent share = new Intent(Intent.ACTION_SEND);
//                share.setType("audio/*");
//                share.putExtra(Intent.EXTRA_STREAM, uri);
//                share.putExtra(Intent.EXTRA_TEXT,content);
//                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                startActivity(Intent.createChooser(share, "Share Sound File"));
//                break;
//            case R.id.bookmark:
//                bookmarkentity = new DataEntity();
//                int tempal=0;
//                tempal=mMediaPlayer.getCurrentPosition();
//                if(tempal!=0) {
//                    bookmarkentity.setBookmarktime(tempal);
//                    bookmarklist.additem(bookmarkentity);
//                    int transtime=tempal/1000;
//                    int min = transtime/60;
//                    int sec = transtime%60;
//                    String showtime;
//                    if (sec<10) {
//                        showtime=Integer.toString(min)+":0"+Integer.toString(sec);
//                    }else{
//                        showtime=Integer.toString(min)+":"+Integer.toString(sec);
//                    }
//                    Toast.makeText(MainActivity.this, "Added bookmark at "+showtime, Toast.LENGTH_SHORT).show();
//                }
//                break;
            default:
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // TODO Auto-generated method stub
        if(seekBar.equals(mSoundSeekBar)) {
            System.out.println("progress:" + String.valueOf(progress));
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);
        }
        else if(seekBar.equals(mSoundProcessBar))
        {
            if(mMediaPlayer.getCurrentPosition()!=progress) {
                mMediaPlayer.seekTo(progress);
                mSoundProcessBar.setProgress(progress);
            }
            changetimestamp(progress);
        }

    }

    public void ShakeOrNot(float[] dData) {


            long now = System.currentTimeMillis();
            if ((now - lastSense)<=100) return;
            if ((now-lastCapture)>2000)  {
                long delta = (now - lastSense);
                lastSense = now;

                x = dData[0];
                y = dData[1];
                z = dData[2];
                float deltaX = x - lastx;
                float deltaY = y - lasty;
                float deltaZ = z - lastz;
                double vv =  Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / delta * 10000;
                if (vv > 1000) {

                    //sensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
                    lastCapture = System.currentTimeMillis();
                    if(mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();
                        Toast.makeText(MainActivity.this, "Paused", Toast.LENGTH_LONG).show();
                    } else {
                        mMediaPlayer.start();
                        Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_LONG).show();
                    }
                    // sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER);
                }
                lastx = x;
                lasty = y;
                lastz = z;
            }

    }

    private void computeOrientation() {
        if (SensorManager.getRotationMatrix(m_rotationMatrix, null,
                mGravity, mGeomagnetic)) {
            SensorManager.getOrientation(m_rotationMatrix, m_orientation);

        /* 1 radian = 57.2957795 degrees */
        /* [0] : yaw, rotation around z axis
         * [1] : pitch, rotation around x axis
         * [2] : roll, rotation around y axis */
            float yaw = m_orientation[0] * 57.2957795f;
            float pitch = m_orientation[1] * 57.2957795f;
            float roll = m_orientation[2] * 57.2957795f;

        /* append returns an average of the last 10 values */
            m_lastYaw = m_filters[0].append(yaw);
            m_lastPitch = m_filters[1].append(pitch);
            m_lastRoll = m_filters[2].append(roll);

        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(TAG, "onSensorChanged()");
        if (swpocket.isChecked()) return;
        if (event.values == null) {
            Log.w(TAG, "event.values is null");
            return;
        }
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values;
                break;
            default:
                Log.w(TAG, "Unknown sensor type " + sensorType);
                return;
        }
        if (mGravity == null) {
            Log.w(TAG, "mGravity is null");
            return;
        }
        if (mGeomagnetic == null) {
            Log.w(TAG, "mGeomagnetic is null");
            return;
        }


        float R[] = new float[9];
        if (! SensorManager.getRotationMatrix(R, null, mGravity, mGeomagnetic)) {
            Log.w(TAG, "getRotationMatrix() failed");
            return;
        }

        ShakeOrNot(mGravity);

        computeOrientation();
        float deltaRoll = m_lastRoll-previousroll;
        float deltaPitch = m_lastPitch-previouspitch;
        //if ((Math.abs(deltaPitch)*5)<Math.abs(deltaRoll)){
            long now = System.currentTimeMillis();
            if ((now - lastSenseT)<=1000) return;
            if ((now-lastCaptureT)>3000) {
                if(deltaRoll>30){
                    Toast.makeText(this, "Forward 5s",
                            Toast.LENGTH_LONG).show();
                    int newPos = 5000 + mMediaPlayer.getCurrentPosition();
                    int mPlayEndMsec = mMediaPlayer.getDuration();
                    if (newPos > mPlayEndMsec)
                        newPos = mPlayEndMsec;
                    mMediaPlayer.seekTo(newPos);
                    mSoundProcessBar.setProgress(newPos);
                    changetimestamp(newPos);

                    lastCaptureT = now;
                }else if (deltaRoll<-30){
                    Toast.makeText(this, "Backward 5s",
                            Toast.LENGTH_LONG).show();
                    int newPos = mMediaPlayer.getCurrentPosition() - 5000;
                    if (newPos < 0)
                        newPos = 0;
                    mMediaPlayer.seekTo(newPos);
                    mSoundProcessBar.setProgress(newPos);
                    changetimestamp(newPos);
                    lastCaptureT = now;
                }
            }
            lastSenseT = now;
            if (Math.abs(m_lastRoll)<10) previousroll = m_lastRoll;

            if(sw.isChecked()){
                currentStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                float percentage = 0;
                if (m_lastPitch<0){
                    percentage = (m_lastPitch/(float)(-90));
                    currentStreamVolume = (int)(maxStreamVolume*percentage);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentStreamVolume, 0);
                    mSoundSeekBar.setProgress(currentStreamVolume);
                }
                previouspitch = m_lastPitch;
            }





    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    protected void onPause() {
        super.onPause();
//        if (sensorManager != null) {
//            sensorManager.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
//            sensorManager = null;
//        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            //mMediaPlayer.release();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
        super.onBackPressed();
    }



}
