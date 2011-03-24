package net.avene.twitter.userstreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class A {

	AsyncTwitter mAsyncTwitter;
	Object lock = new Object();

	// Twitter mAsyncTwitter;

	public A() {
//		mAsyncTwitter = new AsyncTwitterFactory(new TwitterAdapter() {
//			@Override
//			public void updatedStatus(twitter4j.Status status) {
//				System.out.println("Successfully updated the status to ["
//						+ status.getText() + "].");
//				synchronized (lock) {
//					lock.notify();
//				}
//				
//			};
//			
//			@Override
//			public void onException(TwitterException e, twitter4j.TwitterMethod method) {
//				if (method == UPDATE_STATUS) {
//					e.printStackTrace();
//					synchronized (lock) {
//						lock.notify();
//					}
//				} else {
//					synchronized (lock) {
//						lock.notify();
//					}
//					throw new AssertionError("Should not happen");
//				}
//			};
//		}
//		
//		).getInstance();
		 mAsyncTwitter = new AsyncTwitterFactory().getInstance();
		this.connect();
	}

	private void connect() {
		try {
			if (!mAsyncTwitter.getAuthorization().isEnabled()) {
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

	private void postStatus(String body) throws InterruptedException {
		mAsyncTwitter.updateStatus(body);
		synchronized (lock) {
			lock.wait();
		}
	}
//
//	private synchronized void postStatus(StatusUpdate latestStatus) {
//		mAsyncTwitter.updateStatus(latestStatus);
//	}

	public static void main(String[] v) throws InterruptedException {
		System.out.println("A started");
		A a = new A();
		a.postStatus("TwittAvene");
		System.out.println("posted");
	}
}
