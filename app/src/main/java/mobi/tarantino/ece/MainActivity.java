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

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.id;

public class MainActivity extends FaceTrackerActivity implements CountFaceTracker.Listener {
    protected Map<Integer, Face> faces = new HashMap<>();
    private View content;
    private Handler mHandler = new Handler();
    private Toast toast;

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

    @Override
    public void addFace(int faceId, Face face) {
        faces.put(id, face);
        updateContentState();
    }

    @Override
    public void removeFace(int id) {
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
        makeToast("Two Faces in camera. Please look here");
    }

    private void showContent() {
        cancelToast();
        content.setVisibility(View.VISIBLE);
    }

    private void showNonFaceMessage() {
        content.setVisibility(View.INVISIBLE);

        makeToast("No Face in camera. Please look here");
    }

    private void makeToast(String text) {
        cancelToast();
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    protected class CountFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new CountFaceTracker(MainActivity.this);
        }
    }
}
