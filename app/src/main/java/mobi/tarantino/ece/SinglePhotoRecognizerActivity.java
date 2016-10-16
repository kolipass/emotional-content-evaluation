package mobi.tarantino.ece;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.face.Face;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mobi.tarantino.ece.api.projectoxford.RecognizeResponse;
import mobi.tarantino.ece.model.DetectFaceModel;
import mobi.tarantino.ece.model.ProjectOxfordModel;
import rx.Observer;
import rx.functions.Action1;

public class SinglePhotoRecognizerActivity extends AppCompatActivity {

    private static final int TAKE_CAMERA_PHOTO_CODE = 3454;
    private static final int TAKE_LIBRARY_PHOTO_CODE = 3455;

    private DetectFaceModel detectFaceModel;
    private ImageView imageView;
    private RecyclerView result;
    private ResultAdapter resultAdapter;

    public static int fileCount(File folder, final String extension) {
        final List<File> files = new ArrayList<>();
        Collections.addAll(files, folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.toString().contains(extension);
            }
        }));
        return files.size();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxPermissions.getInstance(this)
                .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                    }
                });

        setContentView(R.layout.activity_single_recognize);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.photo);
        result = (RecyclerView) findViewById(R.id.result);
        resultAdapter = new ResultAdapter();
        result.setLayoutManager(new LinearLayoutManager(this));
        result.setAdapter(resultAdapter);

        detectFaceModel = new DetectFaceModel(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        detectFaceModel.release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_photo_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_camera:
                makeShot();
                return true;
            case R.id.menu_select_gallery:
                Intent libraryIntent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
                startActivityForResult(libraryIntent, TAKE_LIBRARY_PHOTO_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        switch (requestCode) {
            case TAKE_CAMERA_PHOTO_CODE:
                detect(getLastShotUri());
                break;
            case TAKE_LIBRARY_PHOTO_CODE:
                detect(result.getData());
                break;
            default:
                break;
        }
    }

    protected void makeShot() {
        Uri tempShotUri = getTempShotUri();

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempShotUri);
        startActivityForResult(cameraIntent, TAKE_CAMERA_PHOTO_CODE);
    }

    private Uri getTempShotUri() {

        String extension = ".jpg";
        String filename = fileCount(getExternalCacheDir(), extension) + extension;

        File tempshot = new File(getExternalCacheDir(), filename);
        if (!tempshot.exists()) {
            try {
                tempshot.createNewFile();
            } catch (IOException ignored) {
            }
        }

        return Uri.fromFile(tempshot);
    }

    private Uri getLastShotUri() {
        String extension = ".jpg";
        String filename = (fileCount(getExternalCacheDir(), extension) - 1) + extension;

        return Uri.fromFile(new File(getExternalCacheDir(), filename));
    }

    protected void detect(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            SparseArray<Face> faces = detectFaceModel.detect(bitmap);
            if (faces.size() > 0) {
                Face face = faces.get(0);

                PointF position = face.getPosition();
                int x = Math.round(position.x);
                int y = Math.round(position.y);

                int width = Math.round(face.getWidth());
                int height = Math.round(face.getHeight());

                Bitmap face0 = Bitmap.createBitmap(bitmap, x, y, width, height);

                imageView.setImageBitmap(face0);

                final ProgressDialog dialog = ProgressDialog.show(this, "sending", "sending foto to oxford");
                new ProjectOxfordModel().get(face0).subscribe(new Observer<List<RecognizeResponse>>() {
                    @Override
                    public void onCompleted() {
                        dialog.hide();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.hide();
                        Toast.makeText(SinglePhotoRecognizerActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<RecognizeResponse> recognizeResponses) {
                        for (RecognizeResponse response : recognizeResponses) {
                            resultAdapter.setValues(response);
                        }

                    }
                });
            } else {
                Toast.makeText(this, "No face", Toast.LENGTH_SHORT).show();
            }


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}