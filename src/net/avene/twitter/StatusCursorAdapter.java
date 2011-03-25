package net.avene.twitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.avene.sqlite.ProfileImageDbAdapter;
import net.avene.sqlite.StatusDbAdapter;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class StatusCursorAdapter extends CursorAdapter {
	private int layoutId;
	private Context mContext;
	private Map<Long, Drawable> mProfileImageMap = new HashMap<Long, Drawable>();

	private StatusCursorAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}

	public StatusCursorAdapter(Context context, Cursor c, int layoutId) {
		super(context, c);
		this.layoutId = layoutId;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		createView(context, view, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = ((Activity) context).getLayoutInflater().inflate(
				R.layout.statuses_row_ritch, parent, false);
		// StatusViewHolder holder = new StatusViewHolder(convertView);
		// convertView.setTag(holder);
		//
		// holder.getName().setText(
		// cursor.getString(cursor
		// .getColumnIndex(StatusDbAdapter.KEY_NAME)));
		// holder.getScreenName().setText(
		// cursor.getString(cursor
		// .getColumnIndex(StatusDbAdapter.KEY_SCREEN_NAME)));
		// holder.getCreatedAt().setText(
		// cursor.getString(cursor
		// .getColumnIndex(StatusDbAdapter.KEY_CREATED_AT)));
		// holder.getBody().setText(
		// cursor.getString(cursor
		// .getColumnIndex(StatusDbAdapter.KEY_TEXT)));
		// ProfileImageDbAdapter imageDbAdapter = ProfileImageDbAdapter
		// .getInstance();
		// // imageDbAdapter.open();
		// try {
		// holder.getIcon()
		// .setImageDrawable(
		// imageDbAdapter.fetchProfileImage(context,
		// cursor.getLong(cursor
		// .getColumnIndex(StatusDbAdapter.KEY_ID)),
		// new URL(
		// cursor.getString(cursor
		// .getColumnIndex(StatusDbAdapter.KEY_PROFILE_IMAGE_URL)))));
		// } catch (MalformedURLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } finally {
		// // imageDbAdapter.close();
		// }
		this.createView(context, convertView, cursor);
		return convertView;
	}

	private View createView(Context context, View view, Cursor cursor) {
		StatusViewHolder holder = new StatusViewHolder(view);
		view.setTag(holder);
		this.mContext = context;
		holder.getName().setText(
				cursor.getString(cursor
						.getColumnIndex(StatusDbAdapter.KEY_NAME)));
		holder.getScreenName().setText(
				cursor.getString(cursor
						.getColumnIndex(StatusDbAdapter.KEY_SCREEN_NAME)));
		holder.getCreatedAt().setText(
				cursor.getString(cursor
						.getColumnIndex(StatusDbAdapter.KEY_CREATED_AT)));
		holder.getBody().setText(
				cursor.getString(cursor
						.getColumnIndex(StatusDbAdapter.KEY_TEXT)));
		long userId = cursor.getLong(cursor
				.getColumnIndex(StatusDbAdapter.KEY_ID));
		// if (mProfileImageMap.containsKey(userId)) {
		// Log.d("ImageGetTask",
		// "icon found in internal map: "
		// + cursor.getString(cursor
		// .getColumnIndex(StatusDbAdapter.KEY_SCREEN_NAME)));
		// holder.getIcon().setImageDrawable(mProfileImageMap.get(userId));
		// return view;
		// } else {
		// holder.getIcon().setImageDrawable(
		// Drawable.createFromPath("res/drawable-mdpi/icon.png"));
		// try {
		// URL profileImageURL = new URL(cursor.getString(cursor
		// .getColumnIndex(StatusDbAdapter.KEY_PROFILE_IMAGE_URL)));
		// new ProfileImageGetTask().execute(context, userId,
		// profileImageURL, view);
		// } catch (MalformedURLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		new ProfileImageGetTask()
				.execute(
						cursor.getString(cursor
								.getColumnIndex(StatusDbAdapter.KEY_PROFILE_IMAGE_URL)),
						holder);

		return view;
	}

	private class ProfileImageGetTask extends AsyncTask<Object, Object, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			String profileImageUrl = (String) params[0];
			StatusViewHolder holder = (StatusViewHolder) params[1];

			try {
				Drawable profileImageDrawable = FlyweightProfileImageStore
						.getInstance().getProfileImage(profileImageUrl);
				// mProfileImageMap.put(userId, drawable);
				this.publishProgress(holder, profileImageDrawable);
			} finally {
				// imageDbAdapter.close();
			}

			return (Void) null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			StatusViewHolder holder = (StatusViewHolder) values[0];
			holder.getIcon().setImageDrawable((Drawable) values[1]);
			Log.d("ImageGetTask", "set icon done");
		}
	}
}
