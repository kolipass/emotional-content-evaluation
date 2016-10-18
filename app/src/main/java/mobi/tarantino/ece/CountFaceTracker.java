package mobi.tarantino.ece;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by kolipass on 17.10.16.
 */
public class CountFaceTracker extends Tracker<Face> {
    Face item;
    private int faceId;
    private Listener listener;

    public CountFaceTracker(Listener listener) {
        this.listener = listener;
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item) {
        this.faceId = faceId;
        this.item = item;
        listener.addFace(faceId, item);
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        //todo
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        listener.removeFace(faceId);
    }


    @Override
    public void onDone() {
        listener.removeFace(faceId);
    }

    public interface Listener {
        void addFace(int faceId, Face item);

        void removeFace(int faceId);
    }

}
