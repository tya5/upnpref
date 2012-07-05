package org.tyas.upnp.event;

import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;

public abstract class SubscribeRequest
{
	public static final String SUBSCRIBE = "SUBSCRIBE";
	public static final String UNSUBSCRIBE = "UNSUBSCRIBE";
	public static final String CALLBACK = "CALLBACK";
	public static final String TIMEOUT = "TIMEOUT";
	public static final String NT = "NT";
	public static final String SID = "SID";
	public static final String UPNP_EVENT = "upnp:event";

	public interface Base extends HttpRequest.Base
	{
		String getCallback();
		int getTimeout();
		SubscribeId getSid();
		boolean isSubscribeRequest();
		boolean isRenewRequest();
		boolean isUnsubscribeRequest();
	}

	public static class Const extends HttpRequest.Const implements Base
	{
		private Const(HttpRequest.Const c) {
			super(c);
		}

		@Override public String getCallback() { return getFirst(CALLBACK); }
		@Override public int getTimeout() { return unpackTimeout(getFirst(TIMEOUT)); }
		@Override public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(SID)); }
		@Override public boolean isSubscribeRequest() { return SubscribeRequest.isSubscribeRequest(this); }
		@Override public boolean isRenewRequest() { return SubscribeRequest.isRenewRequest(this); }
		@Override public boolean isUnsubscribeRequest() { return SubscribeRequest.isUnsubscribeRequest(this); }
	}

	public static class Builder extends HttpRequest.Builder implements Base
	{
		private Builder(String method, String uri) {
			super(method, uri, HttpMessage.VERSION_1_1);
		}

		@Override public String getCallback() { return getFirst(CALLBACK); }
		@Override public int getTimeout() { return unpackTimeout(getFirst(TIMEOUT)); }
		@Override public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(SID)); }
		@Override public boolean isSubscribeRequest() { return SubscribeRequest.isSubscribeRequest(this); }
		@Override public boolean isRenewRequest() { return SubscribeRequest.isRenewRequest(this); }
		@Override public boolean isUnsubscribeRequest() { return SubscribeRequest.isUnsubscribeRequest(this); }
	}

	public static SubscribeRequest.Const getByHttpRequest(HttpRequest.Const req) {
		if (isSubscribeRequest(req)) return new Const(req);

		if (isRenewRequest(req)) return new Const(req);

		if (isUnsubscribeRequest(req)) return new Const(req);

		return null;
	}

	public static boolean isSubscribeRequest(HttpRequest.Base req) {
		return
			SUBSCRIBE.equals(req.getMethod()) &&
			req.keySet().contains(CALLBACK) &&
			req.keySet().contains(TIMEOUT);
	}

	public static boolean isRenewRequest(HttpRequest.Base req) {
		return
			SUBSCRIBE.equals(req.getMethod()) &&
			req.keySet().contains(SID) &&
			req.keySet().contains(TIMEOUT);
	}

	public static boolean isUnsubscribeRequest(HttpRequest.Base req) {
		return
			UNSUBSCRIBE.equals(req.getMethod()) &&
			req.keySet().contains(SID);
	}

	public static Builder getSubscribeRequest(String uri, String callback, int timeout) {
		Builder req = new Builder(SUBSCRIBE, uri);

		req.putFirst(CALLBACK, callback);
		req.putFirst(TIMEOUT, packTimeout(timeout));

		return req;
	}

	public static Builder getRenewRequest(String uri, SubscribeId sid, int timeout) {
		Builder req = new Builder(SUBSCRIBE, uri);

		req.putFirst(SID, sid.toString());
		req.putFirst(TIMEOUT, packTimeout(timeout));

		return req;
	}

	public static Builder getUnsubscribeRequest(String uri, SubscribeId sid) {
		Builder req = new Builder(UNSUBSCRIBE, uri);

		req.putFirst(SID, sid.toString());

		return req;
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
