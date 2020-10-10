package cn.abilitygame.ability_cola_detection_plugin1;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.graphics.Bitmap;

import com.baidu.paddle.lite.MobileConfig;
import com.baidu.paddle.lite.PaddlePredictor;
import com.baidu.paddle.lite.PowerMode;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Target;
import java.nio.ByteBuffer;
import java.util.Vector;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** AbilityColaDetectionPlugin1Plugin */
public class AbilityColaDetectionPlugin1Plugin implements FlutterPlugin, MethodCallHandler {

  private static final String TAG = AbilityColaDetectionPlugin1Plugin.class.getSimpleName();

  private MethodChannel channel;

  private Context context;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "abilitygame.cn/cola_detection");
    channel.setMethodCallHandler(this);
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "abilitygame.cn/cola_detection");
    channel.setMethodCallHandler(new AbilityColaDetectionPlugin1Plugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("loadModel")){
      try{
        boolean loaded = ModelFactory.loadModel(context);
        result.success(loaded);
      }catch(Exception e){
        result.error("500", "Can not load model", e.toString());
      }
    } else if (call.method.equals("loadLabel")) {
      try {
        boolean loaded = ModelFactory.loadLabel(context);
        result.success(loaded);
      } catch(Exception e) {
        result.error("501", "Can not load labels", e.toString());
      }
    } else if (call.method.equals("detectCola")) {
      if( call.arguments instanceof byte[] ){
        if( ModelFactory.getPredictor() == null || ModelFactory.getWordLabels() == null || ModelFactory.getWordLabels().isEmpty() ){
          result.error("505", "Model and Labels haven't been initialized!",
                  "predictor:" + ModelFactory.getPredictor() + " wordLabels:" + ModelFactory.getWordLabels());
        }else{
          byte[] imageBytes = (byte[])call.arguments;
          Log.i(TAG, "receive image byte[] from flutter, size:" + imageBytes.length);
//          Bitmap bitMap = Bitmap.createBitmap(1080,1080, Bitmap.Config.ARGB_8888);
          Bitmap bitMap = BitmapFactory.decodeByteArray(imageBytes,0, imageBytes.length);
//          bitMap.copyPixelsFromBuffer(ByteBuffer.wrap(imageBytes));
          TargetDetector detector = new TargetDetector(ModelFactory.getPredictor(), ModelFactory.getWordLabels());
          detector.setInputImage(bitMap);
          boolean rs = detector.runModel();
          if( rs ) {
            result.success(rs);
          } else {
            result.error("506","Detect failed", "");
          }
        }
      }else{
        Log.e(TAG, "imageBytes transfer error from flutter to android native!");
      }
    } else if (call.method.equals("version")) {
      if( ModelFactory.getPredictor() != null ){
        String version = ModelFactory.getPredictor().getVersion();
        result.success(version);
      }else{
        result.error("502", "Can not get model version",
                "Can not get model version, pls call loadModel() first!");
      }
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    context = null;
    channel.setMethodCallHandler(null);
    channel = null;
  }


}
