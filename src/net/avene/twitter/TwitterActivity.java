package net.avene.twitter;

import net.avene.twittavene.service.TwitterService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class TwitterActivity extends Activity {

	protected TwitterService twitterService;

	private ServiceConnection twitterServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			TwitterActivity.this.twitterService = ((TwitterService.LocalBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			TwitterActivity.this.twitterService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.bindService(new Intent(this, TwitterService.class),
				twitterServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		this.unbindService(twitterServiceConnection);
	}
}
