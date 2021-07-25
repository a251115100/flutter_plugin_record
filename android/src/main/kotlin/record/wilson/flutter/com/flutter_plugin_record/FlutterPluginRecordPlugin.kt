package record.wilson.flutter.com.flutter_plugin_record

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*


class FlutterPluginRecordPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.RequestPermissionsResultListener {

    lateinit var channel: MethodChannel
    private lateinit var _result: Result
    private lateinit var call: MethodCall
    private lateinit var voicePlayPath: String
    private var audioUtils: AudioUtils = AudioUtils();


    lateinit var activity: Activity

    companion object {
        //support embedding v1
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val plugin = initPlugin(registrar.messenger())
            plugin.activity = registrar.activity()
            registrar.addRequestPermissionsResultListener(plugin)
        }

        private fun initPlugin(binaryMessenger: BinaryMessenger): FlutterPluginRecordPlugin {
            val channel = createMethodChannel(binaryMessenger)
            val plugin = FlutterPluginRecordPlugin()
            channel.setMethodCallHandler(plugin)
            plugin.channel = channel
            return plugin
        }

        private fun createMethodChannel(binaryMessenger: BinaryMessenger): MethodChannel {
            return MethodChannel(binaryMessenger, "flutter_plugin_record");
        }
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        val methodChannel = createMethodChannel(binding.binaryMessenger)
        methodChannel.setMethodCallHandler(this)
        channel = methodChannel
    }


    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        initActivityBinding(binding)
    }

    private fun initActivityBinding(binding: ActivityPluginBinding) {
        binding.addRequestPermissionsResultListener(this)
        activity = binding.activity
    }


    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        initActivityBinding(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onDetachedFromActivity() {
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        _result = result
        this.call = call
        audioUtils.update(activity, channel, call);
        when (call.method) {
            "init" -> init()
            "initRecordMp3" -> initRecordMp3()
            "start" -> start()
            "stop" -> stop()
            "play" -> play()
            "pause" -> pause()
            "playByPath" -> playByPath()
            "stopPlay" -> stopPlay()
            else -> result.notImplemented()
        }
    }


    private fun initRecord() {
        audioUtils.initRecord();

    }

    private fun stopPlay() {
        audioUtils.stopPlay()
    }

    //暂停播放
    private fun pause() {
        audioUtils.pause()
//        val isPlaying = audioUtils?.pausePlay()
//        val _id = call.argument<String>("id")
//        val m1 = HashMap<String, String>()
//        m1["id"] = _id!!
//        m1["result"] = "success"
//        m1["isPlaying"] = isPlaying.toString()
//        channel.invokeMethod("pausePlay", m1)
    }

    private fun play() {
        audioUtils.play()
//        audioUtils = RecorderUtil(voicePlayPath)
//        audioUtils!!.addPlayStateListener { playState ->
//            print(playState)
//            val _id = call.argument<String>("id")
//            val m1 = HashMap<String, String>()
//            m1["id"] = _id!!
//            m1["playPath"] = voicePlayPath
//            m1["playState"] = playState.toString()
//            channel.invokeMethod("onPlayState", m1)
//        }
//        audioUtils!!.playVoice()
//        Log.d("android voice  ", "play")
//        val _id = call.argument<String>("id")
//        val m1 = HashMap<String, String>()
//        m1["id"] = _id!!
//        channel.invokeMethod("onPlay", m1)
    }

    private fun playByPath() {
        audioUtils.playByPath();
//        val path = call.argument<String>("path")
//        audioUtils = RecorderUtil(path)
//        audioUtils!!.addPlayStateListener { playState ->
//            val _id = call.argument<String>("id")
//            val m1 = HashMap<String, String>()
//            m1["id"] = _id!!
//            m1["playPath"] = path.toString();
//            m1["playState"] = playState.toString()
//            channel.invokeMethod("onPlayState", m1)
//        }
//        audioUtils!!.playVoice()
//
//        Log.d("android voice  ", "play")
//        val _id = call.argument<String>("id")
//        val m1 = HashMap<String, String>()
//        m1["id"] = _id!!
//        channel.invokeMethod("onPlay", m1)
    }

    @Synchronized
    private fun stop() {
        audioUtils.stopRecord()
    }

    @Synchronized
    private fun start() {
        var packageManager = activity.packageManager
        var permission = PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(
            Manifest.permission.RECORD_AUDIO,
            activity.packageName
        )
        if (permission) {
            Log.d("android voice  ", "start")
            audioUtils.startRecord();
        } else {
            checkPermission()
        }

    }



    private fun init() {
        checkPermission()
    }

    private fun initRecordMp3() {
        checkPermission()
    }

    private fun checkPermission() {
        var packageManager = activity.packageManager
        var permission = PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(
            Manifest.permission.RECORD_AUDIO,
            activity.packageName
        )
        if (permission) {
            initRecord()
        } else {
            initPermission()
        }

    }

    private fun initPermission() {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )
        }
    }


//    //自定义路径
//    private inner class MessageRecordListenerByPath : AudioHandler.RecordListener {
//        var wavPath = ""
//
//        constructor(wavPath: String) {
//            this.wavPath = wavPath
//        }
//
//
//        override fun onStop(recordFile: File?, audioTime: Double?) {
//            if (recordFile != null) {
//                voicePlayPath = recordFile.path
//                if (recordMp3) {
//
//                    val callback: IConvertCallback = object : IConvertCallback {
//                        override fun onSuccess(convertedFile: File) {
//
//                            Log.d("android", "  ConvertCallback ${convertedFile.path}")
//
//                            val _id = call.argument<String>("id")
//                            val m1 = HashMap<String, String>()
//                            m1["id"] = _id!!
//                            m1["voicePath"] = convertedFile.path
//                            m1["audioTimeLength"] = audioTime.toString()
//                            m1["result"] = "success"
//                            activity.runOnUiThread { channel.invokeMethod("onStop", m1) }
//                        }
//
//                        override fun onFailure(error: java.lang.Exception) {
//                            Log.d("android", "  ConvertCallback $error")
//                        }
//                    }
//                    AndroidAudioConverter.with(activity.applicationContext)
//                        .setFile(recordFile)
//                        .setFormat(AudioFormat.MP3)
//                        .setCallback(callback)
//                        .convert()
//
//                } else {
//                    val _id = call.argument<String>("id")
//                    val m1 = HashMap<String, String>()
//                    m1["id"] = _id!!
//                    m1["voicePath"] = voicePlayPath
//                    m1["audioTimeLength"] = audioTime.toString()
//                    m1["result"] = "success"
//                    activity.runOnUiThread { channel.invokeMethod("onStop", m1) }
//
//                }
//            }
//
//        }
//
//
//        override fun getFilePath(): String {
//            return wavPath;
//        }
//
//        private val fileName: String
//        private val cacheDirectory: File
//
//
//        init {
//            cacheDirectory = FileTool.getIndividualAudioCacheDirectory(activity)
//            fileName = UUID.randomUUID().toString()
//        }
//
//        override fun onStart() {
//            LogUtils.LOGE("MessageRecordListener onStart on start record")
//        }
//
//        override fun onVolume(db: Double) {
//            LogUtils.LOGE("MessageRecordListener onVolume " + db / 100)
//            val _id = call.argument<String>("id")
//            val m1 = HashMap<String, Any>()
//            m1["id"] = _id!!
//            m1["amplitude"] = db / 100
//            m1["result"] = "success"
//
//            activity.runOnUiThread { channel.invokeMethod("onAmplitude", m1) }
//
//
//        }
//
//        override fun onError(error: Int) {
//            LogUtils.LOGE("MessageRecordListener onError $error")
//        }
//    }
//
//
//    private inner class MessageRecordListener : AudioHandler.RecordListener {
//        override fun onStop(recordFile: File?, audioTime: Double?) {
//            LogUtils.LOGE("MessageRecordListener onStop $recordFile")
//            if (recordFile != null) {
//                voicePlayPath = recordFile.path
//                if (recordMp3) {
//                    val callback: IConvertCallback = object : IConvertCallback {
//                        override fun onSuccess(convertedFile: File) {
//
//                            Log.d("android", "  ConvertCallback ${convertedFile.path}")
//
//                            val _id = call.argument<String>("id")
//                            val m1 = HashMap<String, String>()
//                            m1["id"] = _id!!
//                            m1["voicePath"] = convertedFile.path
//                            m1["audioTimeLength"] = audioTime.toString()
//                            m1["result"] = "success"
//                            activity.runOnUiThread { channel.invokeMethod("onStop", m1) }
//                        }
//
//                        override fun onFailure(error: java.lang.Exception) {
//                            Log.d("android", "  ConvertCallback $error")
//                        }
//                    }
//                    AndroidAudioConverter.with(activity.applicationContext)
//                        .setFile(recordFile)
//                        .setFormat(AudioFormat.MP3)
//                        .setCallback(callback)
//                        .convert()
//
//                } else {
//                    val _id = call.argument<String>("id")
//                    val m1 = HashMap<String, String>()
//                    m1["id"] = _id!!
//                    m1["voicePath"] = voicePlayPath
//                    m1["audioTimeLength"] = audioTime.toString()
//                    m1["result"] = "success"
//                    activity.runOnUiThread { channel.invokeMethod("onStop", m1) }
//
//                }
//            }
//
//        }
//
//
//        override fun getFilePath(): String {
//            val file = File(cacheDirectory, fileName)
//            return file.absolutePath
//        }
//
//        private val fileName: String
//        private val cacheDirectory: File
//
//
//        init {
//            cacheDirectory = FileTool.getIndividualAudioCacheDirectory(activity)
//            fileName = UUID.randomUUID().toString()
//        }
//
//        override fun onStart() {
//            LogUtils.LOGE("MessageRecordListener onStart on start record")
//        }
//
//        override fun onVolume(db: Double) {
//            LogUtils.LOGE("MessageRecordListener onVolume " + db / 100)
//            val _id = call.argument<String>("id")
//            val m1 = HashMap<String, Any>()
//            m1["id"] = _id!!
//            m1["amplitude"] = db / 100
//            m1["result"] = "success"
//
//            activity.runOnUiThread { channel.invokeMethod("onAmplitude", m1) }
//
//
//        }
//
//        override fun onError(error: Int) {
//            LogUtils.LOGE("MessageRecordListener onError $error")
//        }
//    }


    // 权限监听回调
    override fun onRequestPermissionsResult(
        p0: Int,
        p1: Array<out String>?,
        p2: IntArray?
    ): Boolean {
        if (p0 == 1) {
            if (p2?.get(0) == PackageManager.PERMISSION_GRANTED) {
                initRecord()
                return true
            } else {

                Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
            return false
        }

        return false
    }


}
