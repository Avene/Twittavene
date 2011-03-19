package net.avene.twittavene.service;

import net.avene.sqlite.StatusDbAdapter;
import net.avene.twitter.Twittavene;
import twitter4j.TwitterException;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStream;
import twitter4j.UserStreamAdapter;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class UserStreamService extends Service {
	private LocalBinder binder = new LocalBinder();
	private boolean flag = true;
	static int onStatusCount = 0;
	public static final String BROADCAST_ACTION = "net.avene.twitter.UserStreamUpdateEvent";
	private Intent broadcast = new Intent(BROADCAST_ACTION);
	private static UserStreamReader mUserStreamReader = null;
	private static StatusDbAdapter mDbHelper = null;

	@Override
	public IBinder onBind(Intent arg0) {
		if (mDbHelper == null) {
			mDbHelper = new StatusDbAdapter(this);
		}
		mDbHelper.open();
		return binder;
	}

	public class LocalBinder extends Binder {
		public UserStreamService getService() {
			return (UserStreamService.this);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		startReadingUserStream();
	}

	private void startReadingUserStream() {
		if(mUserStreamReader == null){
			mUserStreamReader = new UserStreamReader();
		}
		if(!mUserStreamReader.getStatus().equals(AsyncTask.Status.RUNNING)){
			mUserStreamReader.execute();
		}
	}

	class UserStreamReader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			UserStream stream;
			flag = true;
			try {
				stream = new TwitterStreamFactory().getInstance()
						.getUserStream();
				while (flag) {
					try {
						stream.next(new UserStreamAdapter() {
							@Override
							public void onStatus(twitter4j.Status status) {
								super.onStatus(status);

								Log.d(this.getClass().getName(), "onStatus: "
										+ onStatusCount++);
								broadcast.putExtra(Twittavene.KEY_STATUS,
										status);
								mDbHelper.insertStatus(status);
								sendBroadcast(broadcast);
							}
						});
					} catch (TwitterException e) {
						e.printStackTrace();
						stream = new TwitterStreamFactory().getInstance()
								.getUserStream();
					}
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
