package net.avene.twitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import net.avene.sqlite.ProfileImageDbAdapter;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class UserViewer extends TwitterActivity {

	private Twitter mAsyncTwitter;
	private ProfileImageDbAdapter mDbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbAdapter = ProfileImageDbAdapter.getInstance();
//		mDbAdapter.open();
		this.setContentView(R.layout.userviewer);
		User user = (User) getIntent()
				.getSerializableExtra(Twittavene.KEY_USER);
		showUserProfile(user);
//		showUserProfileImage(user.getProfileImageURL());
		showUserProfileImage(user);
		constructTwitterConnection();
		new UserDetailsGetter().execute(user.getScreenName());
	}

	private void showUserProfile(User user) {
		((TextView) findViewById(R.id.user_view_name)).setText(user.getName());
		System.out.println(findViewById(R.id.user_view_screen_NAME).getClass()
				.toString());
		((TextView) findViewById(R.id.user_view_screen_NAME)).setText("@"
				+ user.getScreenName());
		((TextView) findViewById(R.id.user_view_bio)).setText(user
				.getDescription());
		((TextView) findViewById(R.id.user_view_followings)).append(System
				.getProperty("line.separator")
				+ String.valueOf(user.getFriendsCount()));
		((TextView) findViewById(R.id.user_view_followers)).append(System
				.getProperty("line.separator")
				+ String.valueOf(user.getFollowersCount()));
	}

	private void showUserProfileImage(User user) {
		((ImageView) findViewById(R.id.user_view_icon))
				.setImageDrawable(mDbAdapter.fetchProfileImage(this, user));
	}

	public void addStatus(ArrayList<String> statuses) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.userviewerstatuses, statuses);
		((ListView) this.findViewById(R.id.user_view_statuses))
				.setAdapter(adapter);
		Log.d("userViewer", "addStatuses");

	}

	private void constructTwitterConnection() {
		if (getLastNonConfigurationInstance() instanceof Twitter) {
			mAsyncTwitter = (Twitter) getLastNonConfigurationInstance();
		} else {
			mAsyncTwitter = new TwitterFactory().getInstance();
		}
	}

	class UserDetailsGetter
			extends
			AsyncTask<String, ResponseList<twitter4j.Status>, ResponseList<twitter4j.Status>> {
		@Override
		protected ResponseList<twitter4j.Status> doInBackground(
				String... screenName) {
			try {
				return mAsyncTwitter.getUserTimeline(screenName[0]);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			Log.d("userViewer", screenName[0]);
			return null;
		}

		@Override
		protected void onPostExecute(ResponseList<twitter4j.Status> result) {
			super.onPostExecute(result);
			ArrayList<String> texts = new ArrayList<String>();
			for (twitter4j.Status s : result) {
				texts.add(s.getText());
			}
			UserViewer.this.addStatus(texts);
		}
	}
}
