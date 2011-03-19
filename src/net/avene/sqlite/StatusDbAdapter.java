package net.avene.sqlite;

import twitter4j.Status;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;

public class StatusDbAdapter {

	public static final String KEY_CREATED_AT = "createdAt";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ID = "id";
	public static final String KEY_NAME = "user";
	public static final String KEY_SCREEN_NAME = "userId";
	public static final String KEY_TEXT = "text";
	public static final String KEY_SOURCE = "source";
	public static final String KEY_IS_TRUNCATED = "isTruncated";
	public static final String KEY_IN_REPLY_TO_STATUS_ID = "inReplyToStatusId";
	public static final String KEY_IN_REPLY_TO_USER_ID = "inReplyToStatusId";
	public static final String KEY_IS_FAVORITED = "isFavorited";
	public static final String KEY_IN_REPLY_TO_SCREEEN_NAME = "inReplyToScreenName";
	public static final String KEY_GEO_LOCATION = "geoLocation";
	public static final String KEY_PLACE = "place";
	public static final String KEY_RETWEETCOUNT = "retweetCount";
	public static final String KEY_WAS_RETWEETED_BY_ME = "wasRetweetedByMe";
	public static final String KEY_PROFILE_IMAGE_URL = "profileImageURL";

	private static final String TAG = "StatusDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table statuses (_id integer primary key autoincrement, "
			+ "createdAt, "
			+ "id, "
			+ "user, "
			+ "userId, "
			+ "text, "
			+ "source, "
			+ "isTruncated, "
			+ "inReplyToStatusId, "
			+ "inReplyToUserId, "
			+ "isFavorited, "
			+ "inReplyToScreenName, "
			+ "geoLocation, "
			+ "place, "
			+ "retweetCount, "
			+ "wasRetweetedByMe, "
			+ "profileImageURL"
			// These parameters are array or any Object in the Class status, not
			// include in the statuses table temporally
			// + "contributors, "
			// + "annotations, "
			// + "retweetedStatus, "
			// + "userMentions, "
			// + "urls, "
			// + "hashtags, "
			+ ");";

	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "statuses";
	private static final int DATABASE_VERSION = 4;
	private static final String DATABASE_DROP = "DROP TABLE IF EXISTS statuses";
	private static final String DATABASE_DELETE = "DELETE FROM statuses";
	private static final String DATABASE_DELETE_OLD = "DELETE FROM statuses "
			+ "WHERE _id < " + "((select max(_id) from statuses) - 10) ";

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// this.onCreate(getWritableDatabase());
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_DROP);
			// db.execSQL(DATABASE_DELETE);
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS statuses");
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public StatusDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public StatusDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		// this.deleteStatuses();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long insertStatus(Status status) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(
				KEY_CREATED_AT,
				String.valueOf(DateFormat.format("MMM dd, h:mmaa",
						status.getCreatedAt())));
		initialValues.put(KEY_ID, status.getId());
		initialValues.put(KEY_NAME, status.getUser().getName());
		initialValues.put(KEY_SCREEN_NAME, status.getUser().getScreenName());
		initialValues.put(KEY_TEXT, status.getText());
		// initialValues.put(KEY_TEXT, "status" + statusCount++);
		initialValues.put(KEY_SOURCE, status.getSource());
		initialValues.put(KEY_IS_TRUNCATED, status.isTruncated());
		initialValues.put(KEY_IN_REPLY_TO_STATUS_ID,
				status.getInReplyToStatusId());
		initialValues.put(KEY_IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
		initialValues.put(KEY_IS_FAVORITED, status.isFavorited());
		initialValues.put(KEY_IN_REPLY_TO_SCREEEN_NAME,
				status.getInReplyToScreenName());
		// initialValues.put(KEY_GEO_LOCATION,
		// status.getGeoLocation().toString());
		// initialValues.put(KEY_PLACE, status.getPlace().toString());
		initialValues.put(KEY_RETWEETCOUNT, status.getRetweetCount());
		initialValues.put(KEY_WAS_RETWEETED_BY_ME, status.isRetweetedByMe());
		initialValues.put(KEY_PROFILE_IMAGE_URL, status.getUser().getProfileImageURL().toString());
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public void deleteStatuses() {
		this.mDb.execSQL(DATABASE_DELETE);
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllStatuses() {

		Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_CREATED_AT, KEY_SCREEN_NAME, KEY_NAME, KEY_TEXT, KEY_ID, KEY_PROFILE_IMAGE_URL}, null,
				null, null, null, null);
		return cursor;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotes(String[] columnNames) {
		Cursor cursor = mDb.query(DATABASE_TABLE, columnNames, null, null,
				null, null, null);
		return cursor;
	}

	public long fetchStatusId(long rowId) throws SQLException {

		Cursor mCursor = mDb.query(true, DATABASE_TABLE,
				new String[] { KEY_ID }, KEY_ROWID + "=" + rowId, null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
			return mCursor.getLong(mCursor.getColumnIndex(KEY_ID));
		}
		return (Long) null;

	}

	public void deleteOldStatuses() {
		this.mDb.execSQL(DATABASE_DELETE_OLD);
	}

	public String fetchStatusScreenName(long rowId) {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE,
				new String[] { KEY_SCREEN_NAME }, KEY_ROWID + "=" + rowId,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
			for (String s : mCursor.getColumnNames()) {
				System.out.println(s);
				System.out.println(mCursor.getColumnCount());
			}
			return mCursor.getString(mCursor.getColumnIndex(KEY_SCREEN_NAME));
		}
		return null;
	}

}
