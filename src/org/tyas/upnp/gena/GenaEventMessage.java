package org.tyas.upnp.gena;

import java.io.OutputStream;
import java.io.IOException;

import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpRequestLine;
import org.tyas.http.HttpHeaders;
import org.tyas.upnp.UpnpUsn;
import org.tyas.upnp.UpnpServiceId;

public class GenaEventMessage extends HttpRequest
{
	private static final String NT = "NT";
	private static final String NTS = "NTS";
	private static final String SID = "SID";
	private static final String SEQ = "SEQ";
	private static final String USN = "USN";
	private static final String LVL = "LVL";
	private static final String SVCID = "SVCID";
	private static final String NOTIFY = "NOTIFY";
	private static final String BOOTID = "BOOTID.UPNP.ORG";
	private static final String NT_VALUE = "upnp:event";
	private static final String NTS_VALUE = "upnp:propchange";

	public static final HttpMessage.Factory<HttpRequestLine,GenaEventMessage> FACTORY =
		new HttpMessage.Factory<HttpRequestLine,GenaEventMessage>()
	{
		public GenaEventMessage createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			return new GenaEventMessage(startLine, headers);
		}
	};

	private GenaEventMessage(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public GenaSubscribeId getSid() {
		return GenaSubscribeId.getBySid(getFirst(SID));
	}

	public int getEventKey() {
		// FIXME: range should be from 0-4294967295
		return getInt(SEQ, -1);
	}

	public GenaEventLevel getEventLevel() {
		return GenaEventLevel.getByString(getFirst(LVL));
	}

	public UpnpUsn getUsn() {
		return UpnpUsn.getByString(getFirst(USN));
	}

	public UpnpServiceId getServiceId() {
		return UpnpServiceId.getByString(getFirst(SVCID));
	}

	public int getBootId() {
		return getInt(BOOTID, -1);
	}

	public void send(OutputStream out, GenaPropertySet props) throws IOException {
		super.send(out, props.toByteArray());
	}
	
	private static abstract class BaseBuilder extends HttpRequest.Builder
	{
		public BaseBuilder(HttpRequestLine startLine, String host, int port) {
			super(startLine, host, port);
			
			mMap.setFirst(NT, NT_VALUE);
			mMap.setFirst(NTS, NTS_VALUE);
			setEventKey(0);
		}
		
		public void setEventKey(int key) {
			putInt(SEQ, key);
		}

		public void incrementEventKey() {
			int seq = mMap.getInt(SEQ, 0);
			setEventKey(seq + 1);
		}
	}

	public static class UnicastBuilder extends BaseBuilder
	{
		private final GenaSubscribeId sid;
		
		public UnicastBuilder(String host, int port, String path, GenaSubscribeId sid) {
			super(new HttpRequestLine(NOTIFY, path, HttpMessage.VERSION_1_1), host, port);
			
			this.sid = sid;
		}

		public GenaEventMessage build() {
			mMap.setFirst(SID, sid.toString());
			
			return build(FACTORY);
		}
	}

	public static class MulticastBuilder extends BaseBuilder
	{
		private static final HttpRequestLine REQUEST_LINE = new HttpRequestLine(NOTIFY, "*", HttpMessage.VERSION_1_1);
		
		private final UpnpUsn usn;
		private final UpnpServiceId service;
		
		public MulticastBuilder(String host, int port, UpnpUsn usn, UpnpServiceId service) {
			super(REQUEST_LINE, host, port);
			
			this.usn = usn;
			this.service = service;
			
			setEventLevel(GenaEventLevel.GENERAL);
			setBootId(0);
		}
		
		public GenaEventMessage build() {
			mMap.setFirst(USN, usn.toString());
			mMap.setFirst(SVCID, service.toString());
			
			return build(FACTORY);
		}
		
		public void setEventLevel(GenaEventLevel level) {
			mMap.setFirst(LVL, level.toString());
		}
		
		public void setBootId(int bootid) {
			putInt(BOOTID, bootid);
		}
	}
}
