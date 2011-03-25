package net.avene.twitter;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.Drawable;

public class FlyweightProfileImageStore {
	private static FlyweightProfileImageStore singleton;
	private Map<String, Drawable> internalImageStore = new HashMap<String, Drawable>();

	private FlyweightProfileImageStore() {
	}

	public static FlyweightProfileImageStore getInstance() {
		if (singleton == null) {
			singleton = new FlyweightProfileImageStore();
		}
		return singleton;
	}

	public Drawable getProfileImage(String profileImageUrl) {
		if (!internalImageStore.containsKey(profileImageUrl.toString())) {
			fetchProfileImage(profileImageUrl);
		}
		return internalImageStore.get(profileImageUrl.toString());
	}

	private void fetchProfileImage(String profileImageUrl) {
		try {
			internalImageStore.put(
					profileImageUrl.toString(),
					Drawable.createFromStream(
							new URL(profileImageUrl).openStream(), ""));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
