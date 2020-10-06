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

public class ModelFactory {
    private static final String TAG = ModelFactory.class.getSimpleName();

    private static final String modelFile = "cola_opt.nb";

    private static final String modePath = "models";

    private static final String labelFile = "cola_label.txt";

    private static final String labelPath = "labels";

    private static Vector<String> wordLabels = new Vector<String>();

    private static PaddlePredictor predictor;

    public static boolean loadModel(Context context){
        String cachedModelPath = copyFromAssetsToCache(context, modePath, modelFile);
        MobileConfig config = new MobileConfig();
        config.setModelFromFile(cachedModelPath + "/" + modelFile);
        config.setPowerMode(PowerMode.LITE_POWER_HIGH);
        config.setThreads(1);
        predictor = PaddlePredictor.createPaddlePredictor(config);
        return true;
    }

    public static boolean loadLabel(Context context){
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

    private static String copyFromAssetsToCache(Context context, String path, String fileName) {
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

    public static Vector<String> getWordLabels() {
        return wordLabels;
    }

    public static PaddlePredictor getPredictor() {
        return predictor;
    }
}
