package com.apnatime.resume.database;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.apnatime.resume.model.User;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "ResumeTracker";
	private static final String RESUME_TABLE = "resume";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		db.execSQL("CREATE TABLE "
				+ RESUME_TABLE
				+ "(ID INTEGER PRIMARY KEY AUTOINCREMENT,Name STRING, MobileNo STRING,UserId STRING,Image STRING,Address STRING,Domain STRING,Experience STRING,Birthday STRING,CreatedAt STRING)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + RESUME_TABLE);
		onCreate(db);
	}

	public void insertUserInfo(User user) {

		SQLiteDatabase db = getWritableDatabase();
		long numRows = DatabaseUtils.queryNumEntries(db, RESUME_TABLE);
		System.out.println("count before insert :::: " + numRows);
		ContentValues cv = new ContentValues();
		cv.put("Name", user.getName());
		cv.put("MobileNo", user.getMobileNo());
		cv.put("UserId", user.getId());
		cv.put("Address", user.getAddress());
		cv.put("Image", user.getImage());
		cv.put("Domain", user.getDomain());
		cv.put("Experience", user.getExperience());
		cv.put("Birthday", user.getBirthday());
		cv.put("CreatedAt", user.getTime());
		db.insert(RESUME_TABLE, null, cv);
		long numRows1 = DatabaseUtils.queryNumEntries(db, RESUME_TABLE);
		System.out.println("count after insert :::: " + numRows1);
		db.close();
	}

	public User getUserInfo(String mNo) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM resume WHERE MobileNo='" + mNo + "'", null);
		User user=null;
		System.out.println("cursor1 count ::: " + cursor.getCount());
		if (cursor != null && cursor.getCount() > 0) {
			try {
				if (cursor.moveToFirst()) {
					user=new User();
					String name = cursor.getString(cursor.getColumnIndex("Name"));
					String mobileNo = cursor.getString(cursor.getColumnIndex("MobileNo"));
					String userId = cursor.getString(cursor.getColumnIndex("UserId"));
					String address = cursor.getString(cursor.getColumnIndex("Address"));
					String domain = cursor.getString(cursor.getColumnIndex("Domain"));
					String exp = cursor.getString(cursor.getColumnIndex("Experience"));
					String birthday = cursor.getString(cursor.getColumnIndex("Birthday"));
					String time = cursor.getString(cursor.getColumnIndex("CreatedAt"));
					String image = cursor.getString(cursor.getColumnIndex("Image"));

					user.setName(name);
					user.setAddress(address);
					user.setDomain(domain);
					user.setExperience(exp);
					user.setId(userId);
					user.setMobileNo(mobileNo);
					user.setBirthday(birthday);
					user.setImage(image);
					user.setTime(time);
				}

			} catch (Exception e) {
				return null;
			}
		}
		return  user;
	}
}