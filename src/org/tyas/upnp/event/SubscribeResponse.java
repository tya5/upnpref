package org.tyas.upnp.event;

public class SubscribeResponse extends HttpResponse
{
	public static final String SID = "SID";
	public static final String TIMEOUT = "TIMEOUT";

	private SubscribeResponse(HttpStatusLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(SID)); }
}
