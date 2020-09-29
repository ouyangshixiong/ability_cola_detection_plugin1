package cn.abilitygame.ability_cola_detection_plugin1;

import android.content.Context;
import android.util.Log;

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

  private static final String modelFile = "cola_opt.nb";

  private static final String modePath = "models";

  private static final String labelFile = "cola_label.txt";

  private static final String labelPath = "labels";

  private MethodChannel channel;

  private Context context;

  private PaddlePredictor predictor;

  private Vector<String> wordLabels = new Vector<String>();

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
      boolean loaded = false;
      try{
        loaded = loadModel();
        result.success(loaded);
      }catch(Exception e){
        result.error("500", "Can not load model", e.toString());
      }
    } else if (call.method.equals("loadLabel")) {
      boolean loaded = false;
      try {
        loaded = loadLabel();
        result.success(loaded);
      } catch(Exception e) {
        result.error("501", "Can not load labels", e.toString());
      }
    } else if (call.method.equals("version")) {
      if( predictor != null ){
        String version = predictor.getVersion();
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

  ////////////////////业务逻辑代码/////////////////////////
  private boolean loadModel(){
    String cachedModelPath = copyFromAssetsToCache(modePath, modelFile);
    MobileConfig config = new MobileConfig();
    config.setModelFromFile(cachedModelPath + "/" + modelFile);
    config.setPowerMode(PowerMode.LITE_POWER_HIGH);
    config.setThreads(1);
    predictor = PaddlePredictor.createPaddlePredictor(config);
    return true;
  }

  private boolean loadLabel(){
    wordLabels.clear();
    try{
      BufferedReader br = new BufferedReader(new InputStreamReader( context.getAssets().open( labelPath + "/" + labelFile)));
      String line = null;
      while( (line = br.readLine()) != null ) {
        wordLabels.add(line);
      }
      Log.i(TAG, "label size:" + wordLabels.size());
      return true;
    }catch( Exception e ){
      Log.e(TAG, e.getMessage());
    }
    return false;
  }

  private String copyFromAssetsToCache(String path, String fileName) {
    String cachePath = context.getCacheDir() + "/" + path;
    File desDir = new File(cachePath);
    try {
      if (!desDir.exists()) {
        desDir.mkdir();
      }
      InputStream stream = context.getAssets().open(path + "/" + fileName);
      OutputStream output = new BufferedOutputStream(new FileOutputStream(cachePath + "/" + fileName));
      byte data[] = new byte[1024];
      int count;
      while ((count = stream.read(data)) != -1) {
        output.write(data, 0, count);
      }
      output.flush();
      output.close();
      stream.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return desDir.getPath();
  }
}
