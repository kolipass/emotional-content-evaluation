package com.google.android.gms.samples.vision.face.facetracker;

import com.google.android.gms.vision.CameraSource;

/**
 * Created by kolipass on 20.03.16.
 */
public abstract class AbstractPictureTakenCallback implements CameraSource.ShutterCallback, CameraSource.PictureCallback {
    @Override
    public void onPictureTaken(byte[] bytes) {
    }

    @Override
    public void onShutter() {
    }
}
