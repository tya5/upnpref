package org.tyas.upnp.event;

import org.tyas.http.*;

public class EventMessageHeader extends HttpRequest
{
	public static final String NT = "NT";
	public static final String NTS = "NTS";
	public static final String SID = "SID";
	public static final String SEQ = "SEQ";
	public static final String NT_VALUE = "upnp:event";
	public static final String NTS_VALUE = "upnp:propchange";

	public static final HttpMessageFactory<HttpRequestLine,EventMessageHeader> FACTORY =
		new HttpMessageFactory<HttpRequestLine,EventMessageHeader>()
	{
		public EventMessageHeader createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			return new EventMessageHeader(startLine, headers);
		}
	};
	
	private EventMessageHeader(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public SubscribeId getSid() {
		return SubscribeId.getBySid(getFirst(SID));
	}

	public int getEventKey() {
		return getInt(SEQ, 0);
	}

	public static class Builder
	{
		public final HttpMessage.Builder<HttpRequestLine> mHttpMessageBuilder;

		public Builder(String deliveryPath) {
			mHttpMessageBuilder = new HttpMessage.Builder<HttpRequestLine>
				(new HttpRequestLine("NOTIFY", deliveryPath, HttpMessage.VERSION_1_1));
		}

		public EventMessageHeader build() {
			return mHttpMessageBuilder
				.putFirst(NT, NT_VALUE)
				.putFirst(NTS, NTS_VALUE)
				.build(FACTORY);
		}

		public Builder setSid(SubscribeId sid) {
			mHttpMessageBuilder.putFirst(SID, sid.toString()); return this;
		}

		public Builder setEventKey(int eventKey) {
			mHttpMessageBuilder.setInt(SEQ, eventKey); return this;
		}
	}
}