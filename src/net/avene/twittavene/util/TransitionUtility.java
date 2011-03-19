package net.avene.twittavene.util;

import net.avene.twitter.StatusUpdater;
import net.avene.twitter.StatusViewer;
import net.avene.twitter.Twittavene;
import net.avene.twitter.UserViewer;
import twitter4j.Status;
import twitter4j.User;
import android.content.Context;
import android.content.Intent;

public class TransitionUtility {
	public static void openPostDialog(Context parentContext) {
		Intent intent = new Intent(parentContext, StatusUpdater.class);
		intent.putExtra(StatusUpdater.KEY_POST_TYPE, StatusUpdater.TWEET);
		parentContext.startActivity(intent);
	}

	public static void openReplyDialog(Context parentContext,
			String inReplyToScreenName, long inReplyToStatusId) {
		Intent intent = new Intent(parentContext, StatusUpdater.class);
		intent.putExtra(Twittavene.KEY_IN_REPLY_TO_SCREEN_NAME,
				inReplyToScreenName);
		intent.putExtra(Twittavene.KEY_IN_REPLY_TO_STATUS_ID, inReplyToStatusId);
		intent.putExtra(StatusUpdater.KEY_POST_TYPE, StatusUpdater.REPLY);
		parentContext.startActivity(intent);
	}

	public static void openReplyDialog(Context parentContext,
			Status inReplyToStatus) {
		openReplyDialog(parentContext, inReplyToStatus.getUser()
				.getScreenName(), inReplyToStatus.getId());
	}

	public static void openQuoteDialog(Context parentContext, Status status) {
		Intent intent = new Intent(parentContext, StatusUpdater.class);
		intent.putExtra(Twittavene.KEY_IN_REPLY_TO_SCREEN_NAME,
				status.getUser().getScreenName());
		intent.putExtra(Twittavene.KEY_QUOTED_TEXT, status.getText());
		intent.putExtra(StatusUpdater.KEY_POST_TYPE, StatusUpdater.QUOTE);
		parentContext.startActivity(intent);
	}

	public static void openStatusViewer(Context parentContext, Status status) {
		Intent intent = new Intent(parentContext, StatusViewer.class);
		intent.putExtra(Twittavene.KEY_STATUS, status);
		parentContext.startActivity(intent);
	}

	public static void openUserViewer(Context parentContext, User user) {
		Intent intent = new Intent(parentContext, UserViewer.class);
		intent.putExtra(Twittavene.KEY_USER, user);
		intent.putExtra(Twittavene.KEY_PROFILE_IMAGE_URL,
				user.getProfileImageURL());
		parentContext.startActivity(intent);
	}

	public static void openUserViewer(Context parentContext, Status status) {
		openUserViewer(parentContext, status.getUser());
	}

}
