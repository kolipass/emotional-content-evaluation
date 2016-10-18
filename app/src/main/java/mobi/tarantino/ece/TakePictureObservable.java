package mobi.tarantino.ece;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.face.facetracker.AbstractPictureTakenCallback;
import com.google.android.gms.vision.face.Face;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import mobi.tarantino.ece.model.DetectFaceModel;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by kolipass on 17.10.16.
 */

public class TakePictureObservable extends AbstractPictureTakenCallback {
    private final PublishSubject<File> onTakePhotoSubject = PublishSubject.create();
    private final Long position;
    private final Context context;
    private DetectFaceModel detectFaceModel;
    private String folder;

    public TakePictureObservable(Context context, Long position, String folder) {
        this.context = context;
        detectFaceModel = new DetectFaceModel(context);
        this.position = position;
        this.folder = folder;
    }

    public static void closeSilently(@Nullable Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // Do nothing
        }
    }

    public Observable<File> getPhotoFileObservable() {
        return onTakePhotoSubject.asObservable();
    }

    @Override
    public void onPictureTaken(byte[] jpeg) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, options);
        Canvas canvas = new Canvas(bitmap); // now it should work ok

        SparseArray<Face> faces = detectFaceModel.detect(bitmap);
        if (faces.size() > 0) {
            Face face = faces.get(faces.keyAt(0));

            PointF facePosition = face.getPosition();
            int x = Math.min(Math.round(facePosition.x), 0);
            int y = Math.min(Math.round(facePosition.y), 0);

            int width = Math.min(Math.round(face.getWidth()), 0);
            int height = Math.min(Math.round(face.getHeight()), 0);

            Log.d("onPictureTaken", "x " + x + " y " + y + " width " + width + " height " + height);

            Bitmap face0 = Bitmap.createBitmap(bitmap, x, y, width, height);

            onTakePhotoSubject.onNext(save(face0, position));
        }
    }

    private File save(Bitmap image, long position) {
        final Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 90;

        File folderDir = new File(folder);
        folderDir.mkdirs();

        File file = new File(folderDir, position + ".jpg");

        OutputStream outputStream = null;
        try {
            file.createNewFile();

            Uri saveUri = Uri.fromFile(file);

            outputStream = context.getContentResolver().openOutputStream(saveUri);
            if (outputStream != null) {
                image.compress(format, quality, outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(outputStream);
        }

        return file;
    }

    @Override
    public void onShutter() {

    }
}
