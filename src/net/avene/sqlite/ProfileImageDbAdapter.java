package net.avene.sqlite;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import twitter4j.User;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ProfileImageDbAdapter {
	private static final String KEY_ROWID = "_id";
	private static final String KEY_URL = "url";
	private static final String KEY_IMAGE = "image";
	private static final String TABLE_NAME = "profileImages";

	private static final String TABLE_CREATE = "create table " + TABLE_NAME
			+ " (" + KEY_ROWID + " integer primary key,"
			+ KEY_URL + ", " + KEY_IMAGE + ");";

	private static final String TABLE_DROP = "drop table if exists profileImages";
	private SQLiteDatabase mDb;
	private DbHelper mDbHelper;
	private static ProfileImageDbAdapter singleton;

	private class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context) {
			super(context, TABLE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(TABLE_CREATE);
			mDb = db;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
	}

	private ProfileImageDbAdapter() {

	}

	public static ProfileImageDbAdapter getInstance() {
		if (singleton == null) {
			singleton = new ProfileImageDbAdapter();
		}
		return singleton;
	}

	public void open(Context context) {
		mDbHelper = new DbHelper(context);
		mDb = mDbHelper.getWritableDatabase();
	}

	public void close() {
		mDbHelper.close();
	}

	public void insertProfileImage(long userId, URL profileImageURL) {
		ContentValues values = new ContentValues();
		values.put(KEY_ROWID, userId);

		InputStream is;
		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			is = profileImageURL.openStream();
			while (true) {
				Integer i = is.read();
				if (i == -1) {
					break;
				}
				list.add(i);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		byte[] b = new byte[list.size()];
		for (int i = 0; i < b.length; i++) {
			b[i] = list.get(i).byteValue();
		}

		values.put(KEY_IMAGE, b);
		values.put(KEY_URL, profileImageURL.toString());
		try {
			mDb.insertOrThrow(TABLE_NAME, null, values);
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}
	}

	public synchronized Drawable fetchProfileImage(Context context, long userId, URL profileImageURL) {
		open(context);
		Cursor cursor = mDb.query(TABLE_NAME, new String[] { KEY_IMAGE },
				KEY_ROWID + "=" + userId, null, null, null, null);
		if (cursor.getCount() == 0) {
			Log.d("ProfileImageAdapter",
					"icon not found in db: fetch from the Web");
			this.insertProfileImage(userId, profileImageURL);

			cursor.requery();
		}
		cursor.moveToFirst();
		Drawable profileImageDrawable = Drawable.createFromStream(
				new ByteArrayInputStream(cursor.getBlob(cursor
						.getColumnIndex(KEY_IMAGE))), "profileImageDb");
		cursor.close();
		close();
		return profileImageDrawable;
	}

	public Drawable fetchProfileImage(Context context, User user) {
		return this.fetchProfileImage(context, user.getId(), user.getProfileImageURL());
	}
}
