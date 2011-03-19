package net.avene.twittavene.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import net.avene.twitter.Twittavene;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TwitterService extends Service {

	private LocalBinder binder = new LocalBinder();

	public class LocalBinder extends Binder {
		public TwitterService getService() {
			return TwitterService.this;
		}
	}

	private AsyncTwitter mAsyncTwitter;

	@Override
	public void onCreate() {
		super.onCreate();
		constructTwitterConnection();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		constructTwitterConnection();
		return binder;
	}

	public void updateStatus(String status) {
		mAsyncTwitter.updateStatus(status);
	}

	public void updateStatus(String status, Long inReplyToStatusId) {
		mAsyncTwitter.updateStatus(status, inReplyToStatusId);
	}

	public void updateStatus(Intent intent, String status) {
		mAsyncTwitter
				.updateStatus(
						String.valueOf(
								intent.getCharSequenceExtra(Twittavene.KEY_STATUS_BODY))
								.trim(), Long.valueOf(intent.getLongExtra(
								(Twittavene.KEY_IN_REPLY_TO_STATUS_ID), -1)));
	}

	public void getUserTimeline(int userId) {
		mAsyncTwitter.getUserTimeline(userId);
	}

	public AsyncTwitter getAsyncTwitter() {
		return this.mAsyncTwitter;
	}

	private void constructTwitterConnection() {
		if (mAsyncTwitter == null) {
			mAsyncTwitter = new AsyncTwitterFactory(new TwitterAdapter() {
				@Override
				public void gotHomeTimeline(ResponseList<Status> statuses) {
					super.gotHomeTimeline(statuses);
					// for (Status status : statuses) {
					// mDbHelper.insertStatus(status);
					// }
				}

				@Override
				public void gotUserDetail(User user) {
					super.gotUserDetail(user);
					// openUserViewer(user);
				}

				@Override
				public void gotShowStatus(Status status) {
					super.gotShowStatus(status);
					// openStatusViewer(status);
				}
			}).getInstance();
		}

		try {
			if (!mAsyncTwitter.isOAuthEnabled()) {
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

	private void storeAccessToken(int useId, AccessToken accessToken) {
		Properties twitter4jProperties = new Properties();
		try {
			twitter4jProperties.load(new FileInputStream(
					Twittavene.TWITTER4J_PROPERTIES));
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
