package com.apnatime.resume.settings;

import android.app.Application;
import android.content.Context;

public class ResumeApplication extends Application {

    static Context mContext;
    public static final String TAG = ResumeApplication.class
            .getSimpleName();


    private static ResumeApplication mInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mInstance = this;
    }
    public static Context getAppContext()
    {
        return mContext;
    }

    public static synchronized ResumeApplication getInstance() {
        return mInstance;
    }

   /* @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //JobManager.create(this).addJobCreator(new DemoJobCreator());
        MultiDex.install(this);
    }*/
}
