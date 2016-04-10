package mobi.tarantino.ece.model;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.List;

import mobi.tarantino.ece.api.projectoxford.NetClient;
import mobi.tarantino.ece.api.projectoxford.RecognizeResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kolipass on 10.04.16.
 */
public class ProjectOxfordModel {
    public rx.Observable<List<RecognizeResponse>> get(Bitmap content) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        content.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();
        return get(byteArray);
    }

    public rx.Observable<List<RecognizeResponse>> get(final byte[] content) {
        return new NetClient()
                .serverMethods
                .recognize("f04d596f075a4f16bddefc896fc3fc6f",
                        null,
                        RequestBody.create(MediaType.parse("image/jpg"), content))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

    }
}
