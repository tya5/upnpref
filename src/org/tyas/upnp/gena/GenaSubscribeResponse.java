package org.tyas.upnp.gena;

import java.io.InputStream;
import java.io.IOException;

import org.tyas.http.HttpMessage;
import org.tyas.http.HttpResponse;
import org.tyas.http.HttpStatusLine;
import org.tyas.http.HttpHeaders;

public class GenaSubscribeResponse extends HttpResponse
{
	public static final String SID = "SID";
	public static final String TIMEOUT = "TIMEOUT";

	private GenaSubscribeResponse(HttpStatusLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public GenaSubscribeId getSid() {
		return GenaSubscribeId.getBySid(getFirst(SID));
	}

	public int getTimeout() {
		return GenaSubscribeRequest.unpackTimeout(getFirst(TIMEOUT));
	}

	public static GenaSubscribeResponse read(InputStream in) throws IOException {
		return HttpMessage.readMessage(in, HttpStatusLine.PARSER, FACTORY).getMessage();
	}

	public static final HttpMessage.Factory<HttpStatusLine,GenaSubscribeResponse> FACTORY =
		new HttpMessage.Factory<HttpStatusLine,GenaSubscribeResponse>()
	{
		public GenaSubscribeResponse createMessage(HttpStatusLine startLine, HttpHeaders headers) {
			return new GenaSubscribeResponse(startLine, headers);
		}
	};

	// Used for Subscribe Request and Renew Request.
	// Use HttpResponse for Unsubscribe Request.
	public static class Builder extends HttpResponse.Builder
	{
		private final GenaSubscribeId sid;
		
		public Builder(HttpStatusLine startLine, GenaSubscribeId sid) {
			super(startLine);
			
			this.sid = sid;
			setTimeout(1800);
		}

		public GenaSubscribeResponse build() {
			mMap.setFirst(SID, sid.toString());
			
			return build(FACTORY);
		}

		public void setTimeout(int timeout) {
			mMap.setFirst(TIMEOUT, GenaSubscribeRequest.packTimeout(timeout));
		}
	}
}
