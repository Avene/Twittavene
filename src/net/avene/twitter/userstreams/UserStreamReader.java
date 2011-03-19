package net.avene.twitter.userstreams;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStream;
import twitter4j.UserStreamAdapter;
import twitter4j.UserStreamListener;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

public class UserStreamReader {
	private static final String TWITTER4J_PROPERTIES = "twitter4j.properties";
	static int onStatusCount = 0;
	static int nextCount = 0;

	public static void main(String[] args) throws TwitterException, IOException {
		System.out.print(System.getProperty("java.class.path"));

		// このファクトリインスタンスは再利用可能でスレッドセーフです
		Twitter twitter = new TwitterFactory().getInstance();
		// twitter.setOAuthConsumer("[consumer key]", "[consumer secret]");
		if (!twitter.isOAuthEnabled()) {
			RequestToken requestToken = twitter.getOAuthRequestToken();
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
				try {
					if (pin.length() > 0) {
						accessToken = twitter.getOAuthAccessToken(requestToken,
								pin);
					} else {
						accessToken = twitter.getOAuthAccessToken();
					}
				} catch (TwitterException te) {
					if (401 == te.getStatusCode()) {
						System.out.println("Unable to get the access token.");
					} else {
						te.printStackTrace();
					}
				}
			}
			// 将来の参照用に accessToken を永続化する
			storeAccessToken(twitter.verifyCredentials().getId(), accessToken);
			// Status status = twitter.updateStatus(args[0]);
			// System.out.println("Successfully updated the status to [" +
			// status.getText() + "].");
			// System.exit(0);
		}
		UserStreamListener listener = new UserStreamAdapter() {
			public void onStatus(Status status) {
				System.out.println("onStatus: " + onStatusCount++);
				System.out.println(status.getUser().getName() + " : "
						+ status.getText());
			}
		};

		UserStream stream = new TwitterStreamFactory().getInstance()
				.getUserStream();
		while (true) {
			stream.next(listener);
			System.out.println("stream.next: " + nextCount++);
		}
	}

	private static void storeAccessToken(int useId, AccessToken accessToken) {
		Properties twitter4jProperties = new Properties();
		try {
			twitter4jProperties.load(new FileInputStream(TWITTER4J_PROPERTIES));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//TODO implement the logic to store these tokens
		System.out.println(accessToken.getToken());
		System.out.println(accessToken.getTokenSecret());
		// twitter4jProperties.setProperty("", value);
	}
}
