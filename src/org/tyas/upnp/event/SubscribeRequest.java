package org.tyas.upnp.event;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpRequestLine;
import org.tyas.http.HttpMessageFactory;

import java.util.Set;

public class SubscribeRequest extends HttpRequest
{
	public static final String SUBSCRIBE = "SUBSCRIBE";
	public static final String UNSUBSCRIBE = "UNSUBSCRIBE";
	public static final String CALLBACK = "CALLBACK";
	public static final String TIMEOUT = "TIMEOUT";
	public static final String NT = "NT";
	public static final String SID = "SID";
	public static final String UPNP_EVENT = "upnp:event";

	private SubscribeRequest(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public String getCallback() { return getFirst(CALLBACK); }
	
	public int getTimeout() { return unpackTimeout(getFirst(TIMEOUT)); }
	
	public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(SID)); }
	
	public boolean isSubscribeRequest() {
		return SubscribeRequest.isSubscribeRequest(getStartLine(), keySet());
	}
	
	public boolean isRenewRequest() {
		return SubscribeRequest.isRenewRequest(getStartLine(), keySet());
	}
	
	public boolean isUnsubscribeRequest() {
		return SubscribeRequest.isUnsubscribeRequest(getStartLine(), keySet());
	}

	public static final HttpMessageFactory<HttpRequestLine,SubscribeRequest> FACTORY =
		new HttpMessageFactory<HttpRequestLine,SubscribeRequest>()
	{
		public SubscribeRequest createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			Set<String> keySet = headers.keySet();

			if ((! isSubscribeRequest(startLine, keySet)) &&
			    (! isRenewRequest(startLine, keySet)) &&
			    (! isUnsubscribeRequest(startLine, keySet))) {
				return null;
			}
			return new SubscribeRequest(startLine, headers);
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

	public static SubscribeRequest getSubscribeRequest(String uri, String callback, int timeout) {
		return new HttpMessage.Builder<HttpRequestLine>
			(new HttpRequestLine(SUBSCRIBE, uri, HttpMessage.VERSION_1_1))
			.putFirst(CALLBACK, callback)
			.putFirst(TIMEOUT, packTimeout(timeout))
			.build(FACTORY);
	}

	public static SubscribeRequest getRenewRequest(String uri, SubscribeId sid, int timeout) {
		return new HttpMessage.Builder<HttpRequestLine>
			(new HttpRequestLine(SUBSCRIBE, uri, HttpMessage.VERSION_1_1))
			.putFirst(SID, sid.toString())
			.putFirst(TIMEOUT, packTimeout(timeout))
			.build(FACTORY);
	}

	public static SubscribeRequest getUnsubscribeRequest(String uri, SubscribeId sid) {
		return new HttpMessage.Builder<HttpRequestLine>
			(new HttpRequestLine(UNSUBSCRIBE, uri, HttpMessage.VERSION_1_1))
			.putFirst(SID, sid.toString())
			.build(FACTORY);
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
