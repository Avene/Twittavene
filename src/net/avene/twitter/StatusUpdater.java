package net.avene.twitter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class StatusUpdater extends TwitterActivity {

	private static final String KEY_STATUS_BODY = "status_body";
	public static final String KEY_POST_TYPE = "post_type";
	public static final int TWEET = 0;
	public static final int REPLY = 1;
	public static final int QUOTE = 2;
	private final String QUOTE_PREFIX = " QT: @";
	private int mType;
	public EditText mBodyText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mType = getIntent().getIntExtra(KEY_POST_TYPE, -1);
		setContentView(R.layout.post);
		setTitle(R.string.post_title);
		mBodyText = (EditText) findViewById(R.id.body);

		if (savedInstanceState != null) {
			populateFields(String.valueOf(savedInstanceState
					.getSerializable(KEY_STATUS_BODY)));
		}
		switch (mType) {
		case 0: // tweet
			((Button) findViewById(R.id.confirm))
					.setOnClickListener(new View.OnClickListener() {
						public void onClick(View view) {
							twitterService.updateStatus(String
									.valueOf(mBodyText.getText()));
							finish();
						}
					});
			break;
		case 1: // reply
			mBodyText.append("@"
					+ getIntent().getCharSequenceExtra(
							Twittavene.KEY_IN_REPLY_TO_SCREEN_NAME) + " ");

			((Button) findViewById(R.id.confirm))
					.setOnClickListener(new View.OnClickListener() {
						public void onClick(View view) {
							twitterService.updateStatus(
									String.valueOf(mBodyText.getText()),
									getIntent()
											.getLongExtra(
													Twittavene.KEY_IN_REPLY_TO_STATUS_ID,
													-1));
							finish();
						}
					});
			break;
		case 2: // quote
			mBodyText.append(this.QUOTE_PREFIX
					+ getIntent().getCharSequenceExtra(
							Twittavene.KEY_IN_REPLY_TO_SCREEN_NAME)
					+ " "
					+ getIntent().getCharSequenceExtra(
							Twittavene.KEY_QUOTED_TEXT));
			mBodyText.setSelection(0);

			((Button) findViewById(R.id.confirm))
					.setOnClickListener(new View.OnClickListener() {
						public void onClick(View view) {
							twitterService.updateStatus(String
									.valueOf(mBodyText.getText()));
							finish();
						}
					});
			break;
		case -1:
			throw new IllegalArgumentException("Status type is undifined in passed intent");
		default:
			throw new IllegalArgumentException("Status type is illegal.");
		}
	}

	private void populateFields(String body) {
		mBodyText.setText(body);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(StatusUpdater.KEY_STATUS_BODY, mBodyText
				.getText().toString());
	}
}
