package com.apnatime.resume.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.apnatime.resume.R;
import com.apnatime.resume.database.DatabaseHelper;
import com.apnatime.resume.model.User;
import com.apnatime.resume.settings.PreferenceSettings;
import com.apnatime.resume.utils.Constants;
import com.apnatime.resume.utils.LocationAddress;
import com.apnatime.resume.utils.ResizableImageView;
import com.apnatime.resume.utils.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class GetUserInfoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{
    public static int CAMERA_CODE=101;
    public static int GALLERY_CODE=102;
    private Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds

    EditText mNameEditTxt,mDobEditTxt,mTotalExperienceEditTxt,mAddressEditTxt;
    ImageView mDatePickerImgView;
    Spinner mDomainSpinner;
    Button mCreateBtn;
    Toolbar mToolbar;
    ProgressBar mProgressBar;
    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener mDatePicker;

    ImageView mGalleryImgView,mCameraImgView,mCloseImgView;
    ResizableImageView mProfileImgView;
    RelativeLayout mUploadImgLayout,mQAImageLayout;

    InputStream fileInputStream=null;
    byte[] imgByteSize;
    int month,year,date;
    String mMobileNo, mUserId,encodedImageString="";


    // Firebase Database
   // private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setStatusBarColor(this);
        setContentView(R.layout.activity_get_user_info);
        initViews();


      //  handleClickEvent();

    }

    private void handleClickEvent(){
        mDatePickerImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(GetUserInfoActivity.this, mDatePicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });




        mCameraImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCamera(GetUserInfoActivity.this, CAMERA_CODE);
            }
        });

        mGalleryImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openGallery(GetUserInfoActivity.this, GALLERY_CODE);
            }
        });

        mCloseImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileInputStream=null;
                mUploadImgLayout.setVisibility(View.VISIBLE);
                mQAImageLayout.setVisibility(View.GONE);
            }
        });


        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=mNameEditTxt.getText().toString();
                String address=mAddressEditTxt.getText().toString();
                int index=mDomainSpinner.getSelectedItemPosition();
                String experience=mTotalExperienceEditTxt.getText().toString();
                String birthday=mDobEditTxt.getText().toString();

                if(name.isEmpty()){
                    Utility.showToastMessage(GetUserInfoActivity.this,getResources().getString(R.string.name_empty));
                }else if(address.isEmpty()){
                    Utility.showToastMessage(GetUserInfoActivity.this,getResources().getString(R.string.address_empty));
                }else if(experience.isEmpty()){
                    Utility.showToastMessage(GetUserInfoActivity.this,getResources().getString(R.string.exp_empty));
                }else if(birthday.isEmpty()){
                    Utility.showToastMessage(GetUserInfoActivity.this,getResources().getString(R.string.birthday_empty));
                }else if(index==0){
                    Utility.showToastMessage(GetUserInfoActivity.this,getResources().getString(R.string.domain_empty));
                }else if(fileInputStream==null){
                    Utility.showToastMessage(GetUserInfoActivity.this,getResources().getString(R.string.image_empty));
                }else{
                    mCreateBtn.setEnabled(false);
                    mProgressBar.setVisibility(View.VISIBLE);

                    // Creating user object
                    User user=new User();
                    user.setName(name);
                    user.setAddress(address);
                    user.setMobileNo(mMobileNo);
                    user.setId(mUserId);
                    user.setBirthday(birthday);
                    user.setExperience(experience);
                    user.setImage(encodedImageString);
                    user.setTime(Utility.getCurrentTime());
                    user.setDomain(mDomainSpinner.getSelectedItem().toString());

                    // Storing in local database sqlite
                    DatabaseHelper db=new DatabaseHelper(GetUserInfoActivity.this);
                    db.insertUserInfo(user);
                    db.close();

                    // Storing in firebase database
                   // mDatabase.child("users").child(mUserId).setValue(user);

                    PreferenceSettings.setLoginStatus(true);
                    PreferenceSettings.setMobileNo(mMobileNo);
                    PreferenceSettings.setUserId(mUserId);

                    Intent intent=new Intent(GetUserInfoActivity.this,ResumePreviewActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        });
    }

    private void initViews(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Resume Creator");
        mNameEditTxt=findViewById(R.id.name);
        mDobEditTxt=findViewById(R.id.birth_day_date);
        mTotalExperienceEditTxt=findViewById(R.id.years_of_exp);
        mAddressEditTxt=findViewById(R.id.address);
        mDatePickerImgView=findViewById(R.id.date_picker_image);
        mCreateBtn=findViewById(R.id.submit);
        mProgressBar=findViewById(R.id.progressBar);
        mDomainSpinner=findViewById(R.id.domain_spinner);

        mGalleryImgView=(ImageView) findViewById(R.id.gallery_image);
        mCameraImgView=(ImageView) findViewById(R.id.camera_image);
        mProfileImgView=(ResizableImageView) findViewById(R.id.qa_image);
        mCloseImgView=(ImageView) findViewById(R.id.close_image);

        mUploadImgLayout=(RelativeLayout) findViewById(R.id.upload_layout);
        mQAImageLayout=(RelativeLayout) findViewById(R.id.qa_image_layout);

        // we build google api client
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();


        EasyImage.configuration(this)
                .setImagesFolderName("IssueTracking") // images folder name, default is "EasyImage"
                .saveInAppExternalFilesDir() // if you want to use root internal memory for storying images
                .saveInRootPicturesDirectory();


        mDatePicker= new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year1, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year1);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
               /* date=dayOfMonth;
                month=monthOfYear;
                year=year1;*/
                if(getAge(year1,monthOfYear,dayOfMonth) >= 18){
                    updateLabel();
                }else{
                    Utility.showToastMessage(GetUserInfoActivity.this,"Age should be 18+");
                }

            }

        };
        String[] stringArray = {"Choose your domain","Android","IOS","Java",".Net","UX","Cloud","DevOps"};
        ArrayAdapter<CharSequence> adapter =new  ArrayAdapter<CharSequence>(GetUserInfoActivity.this,  R.layout.simple_spinner_item, stringArray) ;
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDomainSpinner.setAdapter(adapter);

        mMobileNo=getIntent().getStringExtra(Constants.MOBILE_NUMBER);
        mUserId=getIntent().getStringExtra(Constants.USER_ID);

      //  mDatabase = FirebaseDatabase.getInstance().getReference();

        handleClickEvent();
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mDobEditTxt.setText(sdf.format(myCalendar.getTime()));
    }

    private int getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }
        System.out.println("age ::: "+age);
        return age;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            //locationTv.setText("You need to install Google Play Services to use the App properly");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude,
                    getApplicationContext(), new GeocoderHandler());
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
        }
    }

       private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            System.out.println("inside handleMessage");
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            mAddressEditTxt.setText(locationAddress);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, GetUserInfoActivity.this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                Utility.showToastMessage(GetUserInfoActivity.this,"Please try to upload again");
            }
            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                try {
                    System.out.println("image path in before compress :" + imageFile.getPath());
                    Bitmap bitmap = Utility.compressImage(imageFile.getPath());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    byte[] b = stream.toByteArray();
                    encodedImageString = Base64.encodeToString(b, Base64.DEFAULT);
                    mProfileImgView.setImageBitmap(bitmap);

                    mQAImageLayout.setVisibility(View.VISIBLE);
                    //mLinkInfoLayout.setVisibility(View.VISIBLE);
                    mUploadImgLayout.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);

                    fileInputStream = new FileInputStream(imageFile);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

