package mobi.tarantino.ece;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.samples.vision.face.facetracker.FaceTrackerActivity;
import com.google.android.gms.samples.vision.face.facetracker.GraphicFaceTrackerFactoryAggregator;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FaceTrackerActivity {
    protected Map<Integer, Face> faces = new HashMap<>();
    private View content;
    private Handler mHandler = new Handler();

    @Override
    public int getLayoutResID() {
        return R.layout.main;
    }

    @NonNull
    @Override
    protected MultiProcessor.Factory<Face> getGraphicFaceTrackerFactory() {
        GraphicFaceTrackerFactoryAggregator aggregator = new GraphicFaceTrackerFactoryAggregator();
        aggregator.put(super.getGraphicFaceTrackerFactory());
        aggregator.put(new CountFaceTrackerFactory());
        return aggregator;
    }

    @Override
    protected void initViews() {
        super.initViews();
        content = findViewById(R.id.content);

    }

    protected void addFace(Integer id, Face face) {
        faces.put(id, face);
        updateContentState();
    }

    protected void removeFace(Integer id) {
        faces.remove(id);
        updateContentState();
    }

    private void updateContentState() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (faces.size()) {
                    case 0:
                        showNonFaceMessage();
                        break;
                    case 1:
                        showContent();
                        break;
                    default:
                        showManyFacesError();
                        break;
                }
            }
        });

    }

    private void showManyFacesError() {
        content.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Two Faces in camera. Please look here", Toast.LENGTH_SHORT).show();
    }

    private void showContent() {
        content.setVisibility(View.VISIBLE);
    }

    private void showNonFaceMessage() {
        content.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "No Face in camera. Please look here", Toast.LENGTH_SHORT).show();
    }

    protected class CountFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new CountFaceTracker();
        }
    }

    private class CountFaceTracker extends Tracker<Face> {
        Face item;
        private int faceId;

        CountFaceTracker() {
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            this.faceId = faceId;
            this.item = item;
            addFace(faceId, item);
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
            removeFace(faceId);
        }


        @Override
        public void onDone() {
            removeFace(faceId);
        }
    }
}
