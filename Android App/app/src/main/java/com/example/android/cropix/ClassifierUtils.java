package com.example.android.cropix;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import androidx.core.content.FileProvider;

public class ClassifierUtils {
    public Interpreter initModel(InputStream inputStream) throws IOException {
        // TODO: 11-02-2021  File file = new File("/storage/emulated/0/Android/data/com.example.android.cam/files/Model/cropix_model.tflite");
        // TODO: 11-02-2021  Uri modelUri = FileProvider.getUriForFile(this, "com.example.android.cam.fileprovider", file);
        // TODO: 11-02-2021  InputStream inputStream = getContentResolver().openInputStream(modelUri);
        byte[] model = new byte[inputStream.available()];
        inputStream.read(model);
        ByteBuffer buffer = ByteBuffer.allocateDirect(model.length)
                .order(ByteOrder.nativeOrder());
        buffer.put(model);
        Interpreter interpreter = new Interpreter(buffer);
        return interpreter;
    }

    public ByteBuffer preprocessImage(Bitmap bit){
        Bitmap bitmap = Bitmap.createScaledBitmap(bit, 227, 227, true);
        ByteBuffer input = ByteBuffer.allocateDirect(227 * 227 * 3 * 4).order(ByteOrder.nativeOrder());
        for (int y = 0; y < 227; y++) {
            for (int x = 0; x < 227; x++) {
                int px = bitmap.getPixel(x, y);

                // Get channel values from the pixel value.
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);

                // Normalize channel values to [-1.0, 1.0]. This requirement depends
                // on the model. For example, some models might require values to be
                // normalized to the range [0.0, 1.0] instead.
                float rf = (r) / 255.0f;
                float gf = (g) / 255.0f;
                float bf = (b) / 255.0f;

                input.putFloat(rf);
                input.putFloat(gf);
                input.putFloat(bf);
            }
        }
        return  input;
    }

    public String Classify(ByteBuffer input, Interpreter interpreter, BufferedReader reader){
        String final_result = null;
        int bufferSize = 10 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        interpreter.run(input, modelOutput);
        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        try {
            float max_prob = 0;
            for (int i = 0; i < probabilities.capacity(); i++) {
                String label = reader.readLine();
                float probability = probabilities.get(i);
                if(probability > max_prob) {
                    final_result = label;
                    max_prob = probability;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return final_result;
    }
}
