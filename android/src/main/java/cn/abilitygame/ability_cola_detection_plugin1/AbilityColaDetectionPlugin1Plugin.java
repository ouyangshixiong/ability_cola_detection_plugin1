package cn.abilitygame.ability_cola_detection_plugin1;

import android.content.Context;

import com.baidu.paddle.lite.MobileConfig;
import com.baidu.paddle.lite.PaddlePredictor;
import com.baidu.paddle.lite.PowerMode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** AbilityColaDetectionPlugin1Plugin */
public class AbilityColaDetectionPlugin1Plugin implements FlutterPlugin, MethodCallHandler {

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
    if (call.method.equals("version")) {
//      result.success("Android " + android.os.Build.VERSION.RELEASE);
      String modelFile = "cola_opt.nb";
      String modelPath = copyFromAssetsToCache("models", modelFile);
      //      File testFile = new File(context.getCacheDir(), "models");
      for(File aFile : new File(context.getCacheDir(), "models").listFiles()){
        System.out.println("filename:" + aFile.getName());
      }
      MobileConfig config = new MobileConfig();
//      config.setModelDir(modelPath);
      config.setModelFromFile(modelPath + "/" + modelFile);
      config.setPowerMode(PowerMode.LITE_POWER_HIGH);
      config.setThreads(1);
      PaddlePredictor predictor = PaddlePredictor.createPaddlePredictor(config);
      String version = predictor.getVersion();
      result.success(version);
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

  ////////////////////自己加的代码

  public String copyFromAssetsToCache(String modelPath, String fileName) {
    String newPath = context.getCacheDir() + "/" + modelPath;
    // String newPath = "/sdcard/" + modelPath;
    File desDir = new File(newPath);

    try {
      if (!desDir.exists()) {
        desDir.mkdir();
      }
      InputStream stream = context.getAssets().open(modelPath + "/" + fileName);
      OutputStream output = new BufferedOutputStream(new FileOutputStream(newPath + "/" + fileName));

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
