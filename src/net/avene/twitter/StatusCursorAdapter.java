package net.avene.twitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

	private Map<String, Drawable> mProfileImageMap = new HashMap<String, Drawable>();
	
	

	private StatusCursorAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}

	public StatusCursorAdapter(Context context, Cursor c, int layoutId) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		createView(context, view, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = ((Activity) context).getLayoutInflater().inflate(
				R.layout.statuses_row_ritch, parent, false);
		this.createView(context, convertView, cursor);
		return convertView;
	}

	private View createView(Context context, View view, Cursor cursor) {
		StatusViewHolder holder = new StatusViewHolder(view);
		view.setTag(holder);
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

		String profileImageUrl = cursor.getString(cursor
				.getColumnIndex(StatusDbAdapter.KEY_PROFILE_IMAGE_URL));
		if (mProfileImageMap.containsKey(profileImageUrl)) {
			holder.getIcon().setImageDrawable(
					mProfileImageMap.get(profileImageUrl));
		} else {
			holder.getIcon().setImageDrawable(
					context.getResources().getDrawable(R.drawable.icon));
			new ProfileImageGetTask().execute(cursor.getString(cursor
					.getColumnIndex(StatusDbAdapter.KEY_PROFILE_IMAGE_URL)),
					holder);
		}
		return view;
	}

	private class ProfileImageGetTask extends AsyncTask<Object, Object, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			String profileImageUrl = (String) params[0];
			StatusViewHolder holder = (StatusViewHolder) params[1];

			try {
				Drawable profileImageDrawable = Drawable.createFromStream(
						new URL(profileImageUrl).openStream(), "");
				mProfileImageMap.put(profileImageUrl, profileImageDrawable);
				this.publishProgress(holder, profileImageDrawable);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
