package org.tyas.upnp.event;

public class SubscribeRequest extends HttpRequest implements Event.SubscribeRequest
{
	public static class Const extends HttpRequest.Const implements Event.SubscribeRequest
	{
		private Const(HttpRequest.Const c) {
			super(c);
		}

		@Override public String getCallback() { return getFirst(Event.CALLBACK); }

		@Override public int getTimeout() { return Event.unpackTimeout(getFirst(Event.TIMEOUT, 0)); }

		@Override public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(Event.SID)); }

		@Override public boolean isSubscribeRequest() { return isSubscribeRequest(this); }

		@Override public boolean isRenewRequest() { return isRenewRequest(this); }

		@Override public boolean isUnsubscribeRequest() { return isUnsubscribeRequest(this); }
	}

	private SubscribeRequest(String method, String uri) {
		super(method, uri, Http.VERSION_1_1);
	}

	@Override public String getCallback() { return getFirst(Event.CALLBACK); }

	@Override public int getTimeout() { return Event.unpackTimeout(getFirst(Event.TIMEOUT, 0)); }

	@Override public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(Event.SID)); }

	@Override public boolean isSubscribeRequest() { return isSubscribeRequest(this); }

	@Override public boolean isRenewRequest() { return isRenewRequest(this); }

	@Override public boolean isUnsubscribeRequest() { return isUnsubscribeRequest(this); }

	public static SubscribeRequest.Const getByHttpRequest(HttpRequest.Const req) {
		if (isSubscribeRequest(req)) return new Const(req);

		if (isRenewRequest(req)) return new Const(req);

		if (isUnsubscribeRequest(req)) return new Const(req);

		return null;
	}

	public static boolean isSubscribeRequest(Http.Request req) {
		return
			Event.SUBSCRIBE.equals(req.getMethod()) &&
			req.keySet().contains(Event.CALLBACK) &&
			req.keySet().contains(Event.TIMEOUT);
	}

	public static boolean isRenewRequest(Http.Request req) {
		return
			Event.SUBSCRIBE.equals(req.getMethod()) &&
			req.keySet().contains(Event.SID) &&
			req.keySet().contains(Event.TIMEOUT);
	}

	public static boolean isUnsubscribeRequest(Http.Request req) {
		return
			Event.UNSUBSCRIBE.equals(req.getMethod()) &&
			req.keySet().contains(Event.SID);
	}

	public static SubscribeRequest getSubscribeRequest(String uri, String callback, int timeout) {
		SubscribeRequest req = new SubscribeRequest(Event.SUBSCRIBE, uri);

		req.putFirst(Event.CALLBACK, callback);
		req.putFirst(Event.TIMEOUT, Event.packTimeout(timeout));

		return req;
	}

	public static SubscribeRequest getRenewRequest(String uri, SubscribeId sid, int timeout) {
		SubscribeRequest req = new SubscribeRequest(Event.SUBSCRIBE, uri);

		req.putFirst(Event.SID, sid.toString());
		req.putFirst(Event.TIMEOUT, Event.packTimeout(timeout));

		return req;
	}

	public static SubscribeRequest getUnsubscribeRequest(String uri, SubscribeId sid) {
		SubscribeRequest req = new SubscribeRequest(Event.UNSUBSCRIBE, uri);

		req.putFirst(Event.SID, sid.toString());

		return req;
	}
}
