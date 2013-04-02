package org.tyas.upnp.gena;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpRequestLine;
import java.util.Set;

public class GenaSubscribeRequest extends HttpRequest
{
	private static final String SUBSCRIBE = "SUBSCRIBE";
	private static final String UNSUBSCRIBE = "UNSUBSCRIBE";
	private static final String CALLBACK = "CALLBACK";
	private static final String TIMEOUT = "TIMEOUT";
	private static final String NT = "NT";
	private static final String SID = "SID";
	private static final String UPNP_EVENT = "upnp:event";

	private GenaSubscribeRequest(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public String getCallback() { return getFirst(CALLBACK); }
	
	public int getTimeout() { return unpackTimeout(getFirst(TIMEOUT)); }
	
	public GenaSubscribeId getSid() { return GenaSubscribeId.getBySid(getFirst(SID)); }
	
	public boolean isSubscribeRequest() {
		return GenaSubscribeRequest.isSubscribeRequest(getStartLine(), keySet());
	}
	
	public boolean isRenewRequest() {
		return GenaSubscribeRequest.isRenewRequest(getStartLine(), keySet());
	}
	
	public boolean isUnsubscribeRequest() {
		return GenaSubscribeRequest.isUnsubscribeRequest(getStartLine(), keySet());
	}

	public static final HttpMessage.Factory<HttpRequestLine,GenaSubscribeRequest> FACTORY =
		new HttpMessage.Factory<HttpRequestLine,GenaSubscribeRequest>()
	{
		public GenaSubscribeRequest createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			Set<String> keySet = headers.keySet();

			if ((! isSubscribeRequest(startLine, keySet)) &&
			    (! isRenewRequest(startLine, keySet)) &&
			    (! isUnsubscribeRequest(startLine, keySet))) {
				return null;
			}
			return new GenaSubscribeRequest(startLine, headers);
		}
	};

	public static boolean isSubscribeRequest(HttpRequestLine startLine, Set<String> headersKeySet) {
		return
			SUBSCRIBE.equals(startLine.getMethod()) &&
			headersKeySet.contains(CALLBACK) &&
			headersKeySet.contains(TIMEOUT);
	}

	public static boolean isRenewRequest(HttpRequestLine startLine, Set<String> headersKeySet) {
		return
			SUBSCRIBE.equals(startLine.getMethod()) &&
			headersKeySet.contains(SID) &&
			headersKeySet.contains(TIMEOUT);
	}

	public static boolean isUnsubscribeRequest(HttpRequestLine startLine, Set<String> headersKeySet) {
		return
			UNSUBSCRIBE.equals(startLine.getMethod()) &&
			headersKeySet.contains(SID);
	}

	public static GenaSubscribeRequest getSubscribeRequest(String host, int port, String path, String callback, int timeout) {
		HttpRequest.Builder b = new HttpRequest.Builder
			(new HttpRequestLine(SUBSCRIBE, path, HttpMessage.VERSION_1_1), host, port);
		
		b.mMap.setFirst(CALLBACK, callback);
		b.mMap.setFirst(TIMEOUT, packTimeout(timeout));
		
		return b.build(FACTORY);
	}

	public static GenaSubscribeRequest getRenewRequest(String host, int port, String path, GenaSubscribeId sid, int timeout) {
		HttpRequest.Builder b = new HttpRequest.Builder
			(new HttpRequestLine(SUBSCRIBE, path, HttpMessage.VERSION_1_1), host, port);
		
		b.mMap.setFirst(SID, sid.toString());
		b.mMap.setFirst(TIMEOUT, packTimeout(timeout));
		
		return b.build(FACTORY);
	}

	public static GenaSubscribeRequest getUnsubscribeRequest(String host, int port, String path, GenaSubscribeId sid) {
		HttpRequest.Builder b = new HttpRequest.Builder
			(new HttpRequestLine(SUBSCRIBE, path, HttpMessage.VERSION_1_1), host, port);
		
		b.mMap.setFirst(SID, sid.toString());
		
		return b.build(FACTORY);
	}

	public static int unpackTimeout(String timeout) {
		String pfx = "Second-";
		int idx = timeout.indexOf(pfx);
		return Integer.decode(timeout.substring(pfx.length()));
	}

	public static String packTimeout(int timeout) {
		return "Second-" + timeout;
	}
}
