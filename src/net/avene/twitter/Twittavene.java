package net.avene.twitter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import net.avene.sqlite.StatusDbAdapter;
import net.avene.twittavene.service.UserStreamService;
import net.avene.twittavene.util.TransitionUtility;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class Twittavene extends ListActivity {
	public static final String TWITTER4J_PROPERTIES = "twitter4j.properties";
	public static final int ACTIVITY_POST = 0;
	public static final String KEY_TWITTER = "twitter4j.AsyncTwitter";
	public static final String KEY_STATUS_BODY = "status_body";
	public static final String KEY_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
	public static final String KEY_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
	public static final String KEY_STATUS = "status";
	public static final String KEY_PROFILE_IMAGE_URL = "profile_image_url";
	public static final int ACTIVITY_REPLY = 1;
	public static final String KEY_USER = "user";
	public static final String KEY_QUOTED_TEXT = "quated_text";
	private String screenName = "Avene_Avene";

	static int onStatusCount = 0;
	static int nextCount = 0;
	private static StatusDbAdapter mDbHelper = null;
	private AsyncTwitter mAsyncTwitter;
	private int mStatusLayoutId = R.layout.statuses_row_ritch;
	protected UserStreamService userStreamService = null;
	private Cursor cursor;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Status status = (Status) intent
					.getSerializableExtra(Twittavene.KEY_STATUS);
			System.out.println(status.getUser().getScreenName() + ": "
					+ status.getText());
			// mDbHelper.insertStatus(status);
			updateTL();
		}
	};

	private ServiceConnection userStreamServideConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			Twittavene.this.userStreamService = ((UserStreamService.LocalBinder) rawBinder)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Twittavene.this.userStreamService = null;
		}
	};

	/** Called when the activity is first created. */

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.statuses_list);
		if (mDbHelper == null) {
			mDbHelper = new StatusDbAdapter(this);
		}
		mDbHelper.open();
		mDbHelper.deleteOldStatuses();

		this.constructTwitterConnection();
		if (bindService(new Intent(this, UserStreamService.class),
				userStreamServideConnection, Context.BIND_AUTO_CREATE)) {
			Toast.makeText(this, "Successfully binding to UserStreamService",
					Toast.LENGTH_SHORT).show();
		}
		this.enableLongClick();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.prepareCursor();
		registerReceiver(receiver, new IntentFilter(
				UserStreamService.BROADCAST_ACTION));
		this.updateTL();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopManagingCursor(cursor);
		cursor.close();
		cursor = null;
		unregisterReceiver(receiver);
		// this.cursor.close();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unbindService(userStreamServideConnection);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return (AsyncTwitter) this.mAsyncTwitter;
	}

	private void constructTwitterConnection() {
		if (getLastNonConfigurationInstance() instanceof AsyncTwitter) {
			mAsyncTwitter = (AsyncTwitter) getLastNonConfigurationInstance();
		} else {
			mAsyncTwitter = new AsyncTwitterFactory().getInstance();
			mAsyncTwitter.addListener(new TwitterAdapter() {
				@Override
				public void gotHomeTimeline(ResponseList<Status> statuses) {
					super.gotHomeTimeline(statuses);
					for (Status status : statuses) {
						mDbHelper.insertStatus(status);
					}
				}

				@Override
				public void gotUserDetail(User user) {
					super.gotUserDetail(user);
					openUserViewer(user);
				}

				@Override
				public void gotShowStatus(Status status) {
					super.gotShowStatus(status);
					openStatusViewer(status);
				}
			});
		}

		try {
			if (!mAsyncTwitter.getAuthorization().isEnabled()) {
				RequestToken requestToken = mAsyncTwitter
						.getOAuthRequestToken();
				AccessToken accessToken = null;
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				while (null == accessToken) {
					System.out
							.println("Open the following URL and grant access to your account:");
					System.out.println(requestToken.getAuthorizationURL());
					System.out
							.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
					String pin = br.readLine();
					if (pin.length() > 0) {
						accessToken = mAsyncTwitter.getOAuthAccessToken(
								requestToken, pin);
					} else {
						accessToken = mAsyncTwitter.getOAuthAccessToken();
					}
				}
				// save AccessToken into External Resource
				storeAccessToken(mAsyncTwitter.getId(), accessToken);

			}
		} catch (TwitterException te) {
			if (401 == te.getStatusCode()) {
				System.out.println("Unable to get the access token.");
			} else {
				te.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(getApplication()).inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		System.out.println(item.getItemId());
		switch (item.getOrder()) {
		case 0: // option: scrollToLatest
			this.scrollToLatest();
			break;
		case 1: // option: stop
			
			this.mAsyncTwitter.showUser(screenName);
			break;
		case 2: // option: clear
			this.clearTL();
			this.updateTL();
			break;
		case 3: // option: rotate
			switch (this.getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE: // landscape -> portrait
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			case Configuration.ORIENTATION_PORTRAIT: // portrait -> landscape
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			default:
				throw new IllegalStateException(
						"Screen Orientation is Illegal: "
								+ this.getResources().getConfiguration().orientation);
			}
			break;
		case 4: // option: layout change
			switch (mStatusLayoutId) {
			case R.layout.statuses_row_ritch: // rich -> simple
				mStatusLayoutId = R.layout.statuses_row_simple;
				this.getListView().invalidate();
				break;
			case R.layout.statuses_row_simple: // simple -> rich
				mStatusLayoutId = R.layout.statuses_row_ritch;
				this.getListView().invalidate();
				break;
			default:
				throw new IllegalStateException(
						"Statuses List Type is Illegal: " + mStatusLayoutId);
			}
			break;
		case 5: // option: post
			TransitionUtility.openPostDialog(this);
			break;
		default:
			throw new IllegalArgumentException(
					"the selected option menu has invalid: the order "
							+ item.getOrder() + " is invalid.");
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		long statusId = mDbHelper.fetchStatusId(id);
		Log.d(this.getClass().getName(), String.valueOf(statusId));
		this.mAsyncTwitter.showStatus(statusId);

	}

	private void prepareCursor() {

//		String[] from = new String[] { StatusDbAdapter.KEY_NAME,
//				StatusDbAdapter.KEY_SCREEN_NAME, StatusDbAdapter.KEY_TEXT,
//				StatusDbAdapter.KEY_CREATED_AT, StatusDbAdapter.KEY_PROFILE_IMAGE_URL};
//		int[] to = new int[] { R.id.status_name, R.id.status_screen_name,
//				R.id.status_body, R.id.status_created_at };

		cursor = mDbHelper.fetchAllStatuses();
		startManagingCursor(cursor);
//
//		SimpleCursorAdapter notes = new SimpleCursorAdapter(this,
//				mStatusLayoutId, cursor, from, to);
//		this.setListAdapter(notes);
		this.setListAdapter(new StatusCursorAdapter(this, cursor, mStatusLayoutId));

	}

	private void enableLongClick() {
		this.getListView().setLongClickable(true);
		this.getListView().setOnItemLongClickListener(
				new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View arg1, int position, long id) {
						long statusId = mDbHelper.fetchStatusId(id);
						String screenName = mDbHelper.fetchStatusScreenName(id);
						Log.d(this.getClass().getName(),
								String.valueOf(statusId));
						TransitionUtility.openReplyDialog(Twittavene.this,
								screenName, statusId);
						return false;
					}
				});
	}

	private void updateTL() {
		cursor.requery();
	}

	private void openUserViewer(User user) {
		TransitionUtility.openUserViewer(this, user);
	}

	private void openStatusViewer(Status status) {
		TransitionUtility.openStatusViewer(this, status);
	}

	private void clearTL() {
		mDbHelper.deleteStatuses();
	}

	private void scrollToLatest() {
		ListView listView = this.getListView();
		listView.setSelection(listView.getCount());
	}

	private void storeAccessToken(long useId, AccessToken accessToken) {
		Properties twitter4jProperties = new Properties();
		try {
			twitter4jProperties.load(new FileInputStream(TWITTER4J_PROPERTIES));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO implement the logic to store these tokens
		System.out.println(accessToken.getToken());
		System.out.println(accessToken.getTokenSecret());
		// twitter4jProperties.setProperty("", value);
	}
}