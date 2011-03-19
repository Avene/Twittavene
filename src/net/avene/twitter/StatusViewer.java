package net.avene.twitter;

import net.avene.twittavene.util.TransitionUtility;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class StatusViewer extends TwitterActivity {
	private Status status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.statusviewer);
		status = (twitter4j.Status) getIntent().getSerializableExtra(
				Twittavene.KEY_STATUS);
		showUserProfile(status);
		// showUserProfileImage(user.getProfileImageURL());
		if (status.getInReplyToStatusId() != -1) {
			createReplyChain(status);
		}
		this.setButtonActions();
	}

	private void setButtonActions() {
		// reply
		((Button) findViewById(R.id.status_view_reply_button))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						TransitionUtility.openReplyDialog(StatusViewer.this,
								status);
					}
				});
		// user detail
		((Button) findViewById(R.id.status_view_user_detail_button))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						TransitionUtility.openUserViewer(StatusViewer.this,
								status);
					}
				});

		// Quote
		((Button) findViewById(R.id.status_view_quote_button))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						TransitionUtility.openQuoteDialog(StatusViewer.this,
								status);
					}
				});
	}

	private void showUserProfile(twitter4j.Status status) {
		User user = status.getUser();
		((TextView) findViewById(R.id.status_view_name))
				.setText(user.getName());
		((TextView) findViewById(R.id.status_view_screen_name)).setText("@"
				+ user.getScreenName());
		((TextView) findViewById(R.id.status_view_created_at)).setText(String
				.valueOf(DateFormat.format("MMM dd, h:mmaa",
						status.getCreatedAt())));
		((TextView) findViewById(R.id.status_view_text)).setText(String
				.valueOf(status.getText()));
	}

	private void createReplyChain(twitter4j.Status status) {
		this.findViewById(R.id.status_view_in_reply_to_header).setVisibility(
				View.VISIBLE);
		this.findViewById(R.id.status_view_in_reply_to_chain).setVisibility(
				View.VISIBLE);
		new ReplyChainGetter().execute(status);
	}

	class ReplyChainGetter extends
			AsyncTask<twitter4j.Status, twitter4j.Status, Void> {

		private Twitter mTwitter;
		private ArrayAdapter<twitter4j.Status> adapter;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.constructTwitterConnection();
			adapter = new ArrayAdapter<twitter4j.Status>(StatusViewer.this,
					R.layout.userviewerstatuses) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					StatusViewHolder holder;

					if (convertView == null) {
						convertView = getLayoutInflater().inflate(
								R.layout.statuses_row_ritch, parent, false);
						holder = new StatusViewHolder(convertView);
						convertView.setTag(holder);
					} else {
						holder = (StatusViewHolder) convertView.getTag();
					}
					twitter4j.Status status = getItem(position);
					User user = status.getUser();

					holder.getName().setText(user.getName());
					holder.getScreenName().setText(user.getScreenName());
					holder.getCreatedAt().setText(
							DateFormat.format("MMM dd, h:mmaa",
									status.getCreatedAt()));
					holder.getBody().setText(status.getText());

					return convertView;

				}

			};
			((ListView) StatusViewer.this
					.findViewById(R.id.status_view_in_reply_to_chain))
					.setAdapter(adapter);
		}

		@Override
		protected Void doInBackground(twitter4j.Status... statuses) {
			long statusId = statuses[0].getInReplyToStatusId();
			if (statusId != -1) {
				try {
					twitter4j.Status status = mTwitter.showStatus(statusId);
					this.addReplyChain(status);

				} catch (TwitterException e) {
					e.printStackTrace();
				}
			} else {
				throw new IllegalArgumentException("status is not Reply");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(twitter4j.Status... statuses) {
			super.onProgressUpdate(statuses);
			adapter.add(statuses[0]);
			Log.d("onProgressUpdate", "Count of replies: " + adapter.getCount());
			// StatusViewer.this.addInReplyToStatus(statuses[0]);
		}

		private void constructTwitterConnection() {
			if (getLastNonConfigurationInstance() instanceof Twitter) {
				mTwitter = (Twitter) getLastNonConfigurationInstance();
			} else {
				mTwitter = new TwitterFactory().getInstance();
			}
		}

		private void addReplyChain(twitter4j.Status status)
				throws TwitterException {
			long inReplyToStatusId = status.getInReplyToStatusId();

			this.publishProgress(status);
			if (inReplyToStatusId != -1) {
				twitter4j.Status inReplyToStatus = mTwitter
						.showStatus(inReplyToStatusId);
				this.addReplyChain(inReplyToStatus);
			}
		}
	}

	public void addInReplyToStatus(twitter4j.Status status) {

	}

}
