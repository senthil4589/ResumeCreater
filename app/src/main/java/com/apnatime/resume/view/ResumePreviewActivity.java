package com.apnatime.resume.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apnatime.resume.R;
import com.apnatime.resume.database.DatabaseHelper;
import com.apnatime.resume.model.User;
import com.apnatime.resume.settings.PreferenceSettings;

import java.io.File;
import java.io.FileOutputStream;

public class ResumePreviewActivity extends AppCompatActivity {

    TextView mNameTxtView, mMobileNoTxtView,mDomainExpTxtView,mAddressTxtView,mDobTxtView;
    ImageView mProfileImgView;
    Toolbar mToolbar;
    RelativeLayout mRootLay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_preview);
        initViews();

    }

    private void initViews(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Resume Creator");
        mNameTxtView=findViewById(R.id.name);
        mMobileNoTxtView=findViewById(R.id.mobile_no);
        mDomainExpTxtView=findViewById(R.id.domain_exp);
        mAddressTxtView=findViewById(R.id.address);
        mDobTxtView=findViewById(R.id.dob);
        mProfileImgView=findViewById(R.id.image);
        mRootLay=findViewById(R.id.root_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfoFromDB();
    }

    private void getUserInfoFromDB(){
        DatabaseHelper db=new DatabaseHelper(ResumePreviewActivity.this);

        User user=db.getUserInfo(PreferenceSettings.getMobileNo());
        db.close();
        if(user!=null){
            mNameTxtView.setText(user.getName());
            mDobTxtView.setText("DOB "+" : "+user.getBirthday());
            mMobileNoTxtView.setText(user.getMobileNo());
            mAddressTxtView.setText("Address "+" : "+user.getAddress());
            mDomainExpTxtView.setText(user.getExperience()+" years of "+user.getDomain()+" development experience");

            try{
                byte[] bytarray = Base64.decode(user.getImage(), Base64.DEFAULT);
                Bitmap bmimage = BitmapFactory.decodeByteArray(bytarray, 0,
                        bytarray.length);
                mProfileImgView.setImageBitmap(bmimage);
            }catch (Exception e){

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share:
                File file =store(getScreenShot(mRootLay),"resume_image");
                shareImage(file);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public File store(Bitmap bm, String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, fileName+ ".png");
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void shareImage(File file){
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ResumePreviewActivity.this, "No App Available", Toast.LENGTH_SHORT).show();
        }
    }
}
