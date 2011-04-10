package net.avene.twitter;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.graphics.drawable.Drawable;

public class FlyweightProfileImageMap implements Map<String, Drawable>{
	private static FlyweightProfileImageMap singleton;
	private Map<String, Drawable> internalImageStore = new HashMap<String, Drawable>();

	private FlyweightProfileImageMap() {
	}

	public static FlyweightProfileImageMap getInstance() {
		if (singleton == null) {
			singleton = new FlyweightProfileImageMap();
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

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return internalImageStore.size();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return internalImageStore.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return internalImageStore.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return internalImageStore.containsValue(value);
	}

	@Override
	public Drawable get(Object key) {
		// TODO Auto-generated method stub
		return internalImageStore.get(key);
	}

	@Override
	public Drawable put(String key, Drawable value) {
		// TODO Auto-generated method stub
		return internalImageStore.put(key, value);
	}

	@Override
	public Drawable remove(Object key) {
		// TODO Auto-generated method stub
		return internalImageStore.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Drawable> m) {
		// TODO Auto-generated method stub
		internalImageStore.putAll(m);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		internalImageStore.clear();
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return internalImageStore.keySet();
	}

	@Override
	public Collection<Drawable> values() {
		// TODO Auto-generated method stub
		return internalImageStore.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Drawable>> entrySet() {
		// TODO Auto-generated method stub
		return internalImageStore.entrySet();
	}

}
