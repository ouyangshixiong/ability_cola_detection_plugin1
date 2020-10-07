package cn.abilitygame.ability_cola_detection_plugin1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.baidu.paddle.lite.PaddlePredictor;
import com.baidu.paddle.lite.Tensor;

import java.util.Date;
import java.util.Vector;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class TargetDetector {

    private static final String TAG = TargetDetector.class.getSimpleName();

    private PaddlePredictor paddlePredictor = null;

    private Vector<String> wordLabels = new Vector<String>();

    private Bitmap inputImage = null;
    private Bitmap outputImage = null;
    private String outputResult = "";

    private String inputColorFormat = "RGB";
    private long[] inputShape = new long[]{1, 3, 300, 300};
    private float[] inputMean = new float[]{0.5f, 0.5f, 0.5f};
    private float[] inputStd = new float[]{0.5f, 0.5f, 0.5f};
    private int warmupIterNum = 1;
    private int inferIterNum = 1;
    private float preprocessTime = 0;
    private float postprocessTime = 0;
    private float inferenceTime = 0;
    private float scoreThreshold = 0.5f;

    public TargetDetector(PaddlePredictor paddlePredictor, Vector<String> wordLabels) {
        this.paddlePredictor = paddlePredictor;
        this.wordLabels = wordLabels;
    }


    public boolean runModel() {
        if (inputImage == null) {
            return false;
        }

        // Set input shape
        Tensor inputTensor = paddlePredictor.getInput(0);
        inputTensor.resize(inputShape);

        // Pre-process image, and feed input tensor with pre-processed data
        Date start = new Date();
        int channels = (int) inputShape[1];
        int width = (int) inputShape[3];
        int height = (int) inputShape[2];
        float[] inputData = new float[channels * width * height];
        if (channels == 3) {
            int[] channelIdx = null;
            if (inputColorFormat.equalsIgnoreCase("RGB")) {
                channelIdx = new int[]{0, 1, 2};
            } else if (inputColorFormat.equalsIgnoreCase("BGR")) {
                channelIdx = new int[]{2, 1, 0};
            } else {
                Log.i(TAG, "Unknown color format " + inputColorFormat + ", only RGB and BGR color format is " +
                        "supported!");
                return false;
            }
            int[] channelStride = new int[]{width * height, width * height * 2};
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color = inputImage.getPixel(x, y);
                    float[] rgb = new float[]{(float) red(color) / 255.0f, (float) green(color) / 255.0f,
                            (float) blue(color) / 255.0f};
                    inputData[y * width + x] = (rgb[channelIdx[0]] - inputMean[0]) / inputStd[0];
                    inputData[y * width + x + channelStride[0]] = (rgb[channelIdx[1]] - inputMean[1]) / inputStd[1];
                    inputData[y * width + x + channelStride[1]] = (rgb[channelIdx[2]] - inputMean[2]) / inputStd[2];
                }
            }
        } else if (channels == 1) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color = inputImage.getPixel(x, y);
                    float gray = (float) (red(color) + green(color) + blue(color)) / 3.0f / 255.0f;
                    inputData[y * width + x] = (gray - inputMean[0]) / inputStd[0];
                }
            }
        } else {
            Log.i(TAG, "Unsupported channel size " + Integer.toString(channels) + ",  only channel 1 and 3 is " +
                    "supported!");
            return false;
        }
        inputTensor.setData(inputData);
        Date end = new Date();
        preprocessTime = (float) (end.getTime() - start.getTime());

        // Warm up
        for (int i = 0; i < warmupIterNum; i++) {
            paddlePredictor.run();
        }
        // Run inference
        start = new Date();
        for (int i = 0; i < inferIterNum; i++) {
            paddlePredictor.run();
        }
        end = new Date();
        inferenceTime = (end.getTime() - start.getTime()) / (float) inferIterNum;

        // Fetch output tensor
        Tensor outputTensor = paddlePredictor.getOutput(0);

        // Post-process
        start = new Date();
        long outputShape[] = outputTensor.shape();
        long outputSize = 1;
        for (long s : outputShape) {
            outputSize *= s;
        }
        outputImage = inputImage;
        outputResult = new String();
        Canvas canvas = new Canvas(outputImage);
        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(1);
        Paint txtPaint = new Paint();
        txtPaint.setTextSize(12);
        txtPaint.setAntiAlias(true);
        int txtXOffset = 4;
        int txtYOffset = (int) (Math.ceil(-txtPaint.getFontMetrics().ascent));
        int imgWidth = outputImage.getWidth();
        int imgHeight = outputImage.getHeight();
        int objectIdx = 0;
        final int[] objectColor = {0xFFFF00CC, 0xFFFF0000, 0xFFFFFF33, 0xFF0000FF, 0xFF00FF00,
                0xFF000000, 0xFF339933};
        for (int i = 0; i < outputSize; i += 6) {
            float score = outputTensor.getFloatData()[i + 1];
            if (score < scoreThreshold) {
                continue;
            }
            int categoryIdx = (int) outputTensor.getFloatData()[i];
            String categoryName = "Unknown";
            if (wordLabels.size() > 0 && categoryIdx >= 0 && categoryIdx < wordLabels.size()) {
                categoryName = wordLabels.get(categoryIdx);
            }
            float rawLeft = outputTensor.getFloatData()[i + 2];
            float rawTop = outputTensor.getFloatData()[i + 3];
            float rawRight = outputTensor.getFloatData()[i + 4];
            float rawBottom = outputTensor.getFloatData()[i + 5];
            float clampedLeft = Math.max(Math.min(rawLeft, 1.f), 0.f);
            float clampedTop = Math.max(Math.min(rawTop, 1.f), 0.f);
            float clampedRight = Math.max(Math.min(rawRight, 1.f), 0.f);
            float clampedBottom = Math.max(Math.min(rawBottom, 1.f), 0.f);
            float imgLeft = clampedLeft * imgWidth;
            float imgTop = clampedTop * imgWidth;
            float imgRight = clampedRight * imgHeight;
            float imgBottom = clampedBottom * imgHeight;
            int color = objectColor[objectIdx % objectColor.length];
            rectPaint.setColor(color);
            txtPaint.setColor(color);
            canvas.drawRect(imgLeft, imgTop, imgRight, imgBottom, rectPaint);
            canvas.drawText(objectIdx + "." + categoryName + ":" + String.format("%.3f", score),
                    imgLeft + txtXOffset, imgTop + txtYOffset, txtPaint);
            outputResult += objectIdx + "." + categoryName + " - " + String.format("%.3f", score) +
                    " [" + String.format("%.3f", rawLeft) + "," + String.format("%.3f", rawTop) + "," + String.format("%.3f", rawRight) + "," + String.format("%.3f", rawBottom) + "]\n";
            objectIdx++;
        }
        end = new Date();
        postprocessTime = (float) (end.getTime() - start.getTime());
        Log.i(TAG, "detect successfully. time cost:" + postprocessTime + " detect result:"+outputResult);
        return true;
    }

    public void setInputImage(Bitmap image) {
        if (image == null) {
            return;
        }
        // Scale image to the size of input tensor
        Bitmap rgbaImage = image.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap scaleImage = Bitmap.createScaledBitmap(rgbaImage, (int) inputShape[3], (int) inputShape[2], true);
        this.inputImage = scaleImage;
    }
}
