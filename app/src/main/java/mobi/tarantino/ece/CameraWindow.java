package mobi.tarantino.ece;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.face.facetracker.AbstractPictureTakenCallback;
import com.google.android.gms.samples.vision.face.facetracker.GraphicFaceTrackerFactoryAggregator;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

import static android.R.attr.id;


public class CameraWindow extends StandOutWindow implements CountFaceTracker.Listener {
    public static final int FOLDER_CODE = 1;
    public static final String FOLDER_KEY = "folder";
    private static final String TAG = "FaceTracker";
    protected SparseArray<Face> faces = new SparseArray<>();
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.preview)
    CameraSourcePreview mPreview;
    @BindView(R.id.faceOverlay)
    GraphicOverlay mGraphicOverlay;
    private CameraSource mCameraSource = null;
    private Unbinder unbinder;
    private String folder;

    @Override
    public String getAppName() {
        return "CameraWindow";
    }

    @Override
    public int getAppIcon() {
        return android.R.drawable.ic_menu_close_clear_cancel;
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        // create a new layout from body.xml
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        unbinder = ButterKnife.bind(this, inflater.inflate(R.layout.window, frame, true));

        createCameraSource();
        startCameraSource();

    }

    // the window will be centered
    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(id, 250, 300,
                StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER);
    }

    // move the window by dragging the view
    @Override
    public int getFlags(int id) {
        return super.getFlags(id) | StandOutFlags.FLAG_BODY_MOVE_ENABLE
                | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return "Click to close the CameraWindow";
    }

    @Override
    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, CameraWindow.class, id);
    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(getGraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(480, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    @NonNull
    protected MultiProcessor.Factory<Face> getGraphicFaceTrackerFactory() {
        GraphicFaceTrackerFactoryAggregator aggregator = new GraphicFaceTrackerFactoryAggregator();
        aggregator.put(new GraphicFaceTrackerFactory());
        aggregator.put(new CountFaceTrackerFactory());
        return aggregator;
    }

    public void getPhoto(AbstractPictureTakenCallback shutter) {
        if (mCameraSource != null) {
            mCameraSource.takePicture(shutter, shutter);
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mPreview.stop();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        unbinder.unbind();
    }

    @Override
    public void onReceiveData(int id, int requestCode, Bundle data,
                              Class<? extends StandOutWindow> fromCls, int fromId) {
        switch (requestCode) {
            case FOLDER_CODE:

                folder = data.getString(FOLDER_KEY);

                textView.setText(folder);

                Observable.interval(Config.DELAY_SEC, TimeUnit.SECONDS, Schedulers.io())
                        .take(Config.COUNT)
                        .filter(new Func1<Long, Boolean>() {
                            @Override
                            public Boolean call(Long aLong) {
                                return faces.size() == 1;
                            }
                        })
                        .doOnNext(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                Log.d("Window", "Long: " + aLong);
                            }
                        })
                        .flatMap(new Func1<Long, Observable<File>>() {
                            @Override
                            public Observable<File> call(Long position) {
                                TakePictureObservable takePictureObservable = new TakePictureObservable(getBaseContext(), position, folder);
                                getPhoto(takePictureObservable);
                                return takePictureObservable
                                        .getPhotoFileObservable();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<File>() {
                                       @Override
                                       public void call(File file) {
                                           textView.setText(file.getName());
                                       }
                                   }, new Action1<Throwable>() {
                                       @Override
                                       public void call(Throwable throwable) {
                                           throwable.printStackTrace();
                                           textView.setText(throwable.getLocalizedMessage());

                                       }
                                   }, new Action0() {
                                       @Override
                                       public void call() {
                                           textView.setText("finish");
                                       }
                                   }
                        );
                break;
        }
    }


    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {

            Log.d(TAG, "Google Api UNAvailability");
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void addFace(int faceId, Face face) {
        faces.put(id, face);
    }

    @Override
    public void removeFace(int faceId) {
        faces.remove(id);
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    protected class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    protected class CountFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new CountFaceTracker(CameraWindow.this);
        }
    }

}
