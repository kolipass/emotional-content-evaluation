package mobi.tarantino.ece;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.functions.Action1;
import wei.mark.standout.StandOutWindow;

import static wei.mark.standout.StandOutWindow.DISREGARD_ID;

/**
 * Created by kolipass on 16.10.16.
 * window overdraw solution from http://stackoverflow.com/a/34061521
 */

public class QuestionnaireActivity extends AppCompatActivity {
    /**
     * code to post/handler request for permission
     */
    public final static int REQUEST_CODE = 5463;
    @BindView(R.id.test_number)
    TextView testNumberTextView;
    @BindView(R.id.male_radioButton)
    RadioButton maleRadioButton;
    @BindView(R.id.female_radioButton)
    RadioButton femaleRadioButton;
    @BindView(R.id.gender_radioButton)
    RadioGroup genderRadioButton;
    @BindView(R.id.age)
    EditText ageEditText;
    @BindView(R.id.description)
    TextView descriptionTextView;
    @BindView(R.id.start_button)
    Button startButton;
    private Unbinder unbinder;
    private int folderNumber;

    private static int getTestNumber(Context context) {
        return FileUtils.folderCount(context.getExternalCacheDir());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxPermissions.getInstance(this)
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.SYSTEM_ALERT_WINDOW
                )
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                    }
                });


        setContentView(R.layout.activity_questionnaire);
        unbinder = ButterKnife.bind(this);

        folderNumber = getTestNumber(this);
        testNumberTextView.setText(getString(R.string.test_number, String.valueOf(folderNumber)));

    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @OnClick(R.id.start_button)
    void onStartClick() {
        if (genderRadioButton.getCheckedRadioButtonId() != -1) {
            String egeValue = ageEditText.getText().toString();
            if (TextUtils.isEmpty(egeValue)) {
                Toast.makeText(this, "Age required", Toast.LENGTH_SHORT).show();
            } else {
                startTest();
            }
        } else {
            Toast.makeText(this, "Gender required", Toast.LENGTH_SHORT).show();
        }

    }

    public void startTest() {
        if (checkDrawOverlayPermission()) {
            makeFolder(String.valueOf(folderNumber));
            initWindow();
        }
    }

    private void makeFolder(String testNumber) {
        File dir = new File(getTestFolder(testNumber));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File questionnaire = new File(dir, "questionnaire");

        try {
            if (!questionnaire.exists()) {
                questionnaire.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(questionnaire);

            fileWriter.write("[questionnaire]\n");
            String egeValue = ageEditText.getText().toString();
            fileWriter.write("age=" + egeValue + "\n");


            String gender = genderRadioButton.getCheckedRadioButtonId() == R.id.male_radioButton ? "m" : "f";
            fileWriter.write("gender=" + gender);

            if (!TextUtils.isEmpty(descriptionTextView.getText())) {
                fileWriter.write("\ndescription=" + descriptionTextView.getText());
            }

            fileWriter.write("\n[config]");

            fileWriter.write("\ndelay=" + Config.DELAY_SEC);
            fileWriter.write("\ncount=" + Config.COUNT);

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @NonNull
    private String getTestFolder(String testNumber) {
        return getExternalCacheDir() + "/" + testNumber;
    }

    @NonNull
    private String getTestFolder() {
        return getExternalCacheDir() + "/" + String.valueOf(folderNumber);
    }

    public boolean checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                /** if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivityForResult(intent, REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE) {
            /**if so check once again if we have permission */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // continue here - permission was granted
                    initWindow();
                }
            }

        }
    }

    public void initWindow() {
        StandOutWindow.closeAll(this, CameraWindow.class);

        // show a MultiWindow, CameraWindow

        StandOutWindow
                .show(this, CameraWindow.class, StandOutWindow.DEFAULT_ID);

        Bundle bundle = new Bundle();
        bundle.putString(CameraWindow.FOLDER_KEY, getTestFolder());

        StandOutWindow.sendData(this, CameraWindow.class, StandOutWindow.DISREGARD_ID, CameraWindow.FOLDER_CODE,
                bundle, null, DISREGARD_ID);

        finish();
    }


}
