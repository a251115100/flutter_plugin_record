package record.wilson.flutter.com.flutter_plugin_record;


import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import me.shetj.player.PlayerListener;
import me.shetj.recorder.core.SimRecordListener;
import me.shetj.recorder.mixRecorder.MixRecorder;

public class AudioUtils {
    MixRecorder recorder;
    String recordPath;

    String playPath;

    MethodChannel channel;
    Activity activity;
    MethodCall call;

    List<Integer> volumeSet = new LinkedList<>();
    Handler handler;

    public AudioUtils() {
        handler = new Handler(Looper.getMainLooper());
    }


    public void update(Activity activity, MethodChannel channel, MethodCall call) {
        this.activity = activity;
        this.channel = channel;
        this.call = call;
    }


    public void initRecord() {
        if (recorder != null) {
            playStopRunnable.run();
            recorder.stop();
            recorder = null;
        }
        recorder = new MixRecorder();
        File dir = new File(activity.getCacheDir(), "audio" + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + ".mp3";
        recorder.setRecordListener(new SimRecordListener() {
            @Override
            public void autoComplete(@NonNull String file, long time) {
                super.autoComplete(file, time);
                Log.e("recorder:", "autoComplete:" + file + "  " + time);
                onRecordSuccess(file, time);
            }

            @Override
            public void onError(@NonNull Exception e) {
                super.onError(e);
                Log.e("recorder:", "onError:" + e);
            }

            @Override
            public void onRecording(long time, int volume) {
                super.onRecording(time, volume);
                volumeSet.add(volume);
                if (volumeSet.size() >= 3) {
                    onVolume();
                }
                Log.e("recorder:", "onRecording:" + time + "   " + volume);
            }

            @Override
            public void onStart() {
                super.onStart();
                Log.e("recorder:", "onStart:");

            }

            @Override
            public void onSuccess(@NonNull String file, long time) {
                super.onSuccess(file, time);
                Log.e("recorder:", "autoComplete:" + file + "  " + time);
                onRecordSuccess(file, time);
            }
        });
        recorder.setBackgroundMusicListener(new PlayerListener() {
            @Override
            public void onStart(int i) {
                Log.e("play:", "onStart:" + i);
                playCompletion();
            }

            @Override
            public void onPause() {
                Log.e("play:", "onPause:");
            }

            @Override
            public void onResume() {
                Log.e("play:", "onResume:");
            }

            @Override
            public void onStop() {
                Log.e("play:", "onStop:");
//                handler.post(playStopRunnable);
            }

            @Override
            public void onCompletion() {
//                Log.e("play:", "onCompletion:");
//                playCompletion();
            }

            @Override
            public void onError(@NonNull Exception e) {
                Log.e("play:", "onError:" + e.getMessage());
                handler.post(playStopRunnable);
            }

            @Override
            public void onProgress(int i, int i1) {
//                Log.e("play:", "onProgress:" + i + " : " + i1);
                int delayed = i1 - i;
                if (delayed < 1000) {
                    handler.postDelayed(playStopRunnable, delayed + 100);
                }
            }
        });
        recordPath = new File(dir, fileName).getAbsolutePath();
        recorder.setOutputFile(recordPath, false);
        recorder.setMaxTime(1000 * 60, null);
        initSuccess();
    }

    private Runnable playStopRunnable = new Runnable() {
        @Override
        public void run() {
            playStop();
        }
    };

    private void initSuccess() {
        Log.d("android voice  ", "init");
        String id = call.argument("id");
        Map<String, String> m1 = new HashMap<>();
        m1.put("id", id);
        m1.put("result", "success");
        channel.invokeMethod("onInit", m1);
    }

    public void startRecord() {
        if (recorder == null) {
            initRecord();
        }
        recorder.start();
        String id = call.argument("id");
        Map<String, String> m1 = new HashMap<>();
        m1.put("id", id);
        m1.put("result", "success");
        channel.invokeMethod("onStart", m1);

    }

    public void stopRecord() {
        if (recorder != null) {
            recorder.stop();
        }
    }

    private void playStop() {
        if (TextUtils.isEmpty(playPath) || channel == null) {
            return;
        }
        String id = call.argument("id");
        final Map<String, String> m1 = new HashMap<>();
        m1.put("id", id);
        m1.put("result", "success");
        m1.put("playPath", playPath);
        m1.put("playState", "complete");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                channel.invokeMethod("onPlayState", m1);
                Log.e("play:", "playStop");
            }
        });

    }

    private void playCompletion() {
        if (TextUtils.isEmpty(playPath)) {
            return;
        }
        String id = call.argument("id");
        final Map<String, String> m1 = new HashMap<>();
        m1.put("id", id);
        m1.put("result", "success");
        m1.put("playPath", playPath);
        m1.put("playState", "start");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                channel.invokeMethod("onPlayState", m1);
                Log.e("play:", "playStart");
            }
        });

    }

    private void onRecordSuccess(String file, Long audioTime) {
        double time = audioTime / 1000.0;
        DecimalFormat df = new DecimalFormat("######0.0");
        String id = call.argument("id");
        final Map<String, String> m1 = new HashMap<>();
        m1.put("id", id);
        m1.put("result", "success");
        m1.put("voicePath", file);
        m1.put("audioTimeLength", df.format(time));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                channel.invokeMethod("onStop", m1);
            }
        });
    }

    public void stopPlay() {
        if (recorder != null) {
            recorder.stop();
        }

    }

    public void pause() {
        if (recorder != null) {
            recorder.pauseMusic();
        }

    }

    public void playByPath() {
        String path = call.argument("path");
        playPath = path;
        recorder.setBackgroundMusic(path, false);
        recorder.startPlayMusic();
        Log.e("play:", "playByPath:" + path);
        String id = call.argument("id");
        Map<String, String> m1 = new HashMap<>();
        m1.put("id", id);
        channel.invokeMethod("onPlay", m1);
    }

    public void play() {
        if (!TextUtils.isEmpty(recordPath) && recorder != null) {
            playPath = recordPath;
            recorder.setBackgroundMusic(recordPath, false);
            recorder.startPlayMusic();
            String id = call.argument("id");
            Map<String, String> m1 = new HashMap<>();
            m1.put("id", id);
            channel.invokeMethod("onPlay", m1);
        }
    }


    private void onVolume() {
        int db = 0;
        int size = volumeSet.size();
        if (size == 0) {
            return;
        }
        for (Integer integer : volumeSet) {
            db += integer;
        }
        db = db / size;
        volumeSet.clear();

        DecimalFormat df = new DecimalFormat("######0.0");
        String id = call.argument("id");
        final Map<String, String> m1 = new HashMap<>();
        m1.put("id", id);
        m1.put("result", "success");
        m1.put("amplitude", df.format(db / 1000.0));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                channel.invokeMethod("onAmplitude", m1);
            }
        });


    }
}
