package mobi.tarantino.ece.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by kolipass on 10.04.16.
 */
public class DetectFaceModel {

    private final FaceDetector detector;

    public DetectFaceModel(Context appContext) {
        detector = new FaceDetector.Builder(appContext.getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
    }

    public SparseArray<Face> detect(Bitmap bitmap) {
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        return detector.detect(frame);
    }

    public void release() {
        detector.release();
    }
}
