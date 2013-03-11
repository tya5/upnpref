package org.tyas.upnp.ssdp;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpResponse;
import org.tyas.http.HttpStatusLine;
import org.tyas.upnp.UpnpUsn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.DatagramPacket;

public class SsdpSearchResponse extends HttpResponse implements Ssdp.RemoteDevicePointer
{
	private SsdpSearchResponse(HttpStatusLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public URL getDescriptionUrl() {
		try {
			return getLocation().toURL();
		} catch (Exception e) {
			return null;
		}
	}

	public String getSearchTarget() { return getFirst(Ssdp.ST); }

	public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }

	public int getBootId() { return getInt(Ssdp.BOOTID, 0); }

	public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }

	public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }

	public static SsdpSearchResponse read(InputStream in) throws IOException {
		return HttpMessage.readMessage(in, HttpStatusLine.PARSER, FACTORY).getMessage();
	}

	public static final HttpMessage.Factory<HttpStatusLine,SsdpSearchResponse> FACTORY =
		new HttpMessage.Factory<HttpStatusLine,SsdpSearchResponse>()
	{
		public SsdpSearchResponse createMessage(HttpStatusLine startLine, HttpHeaders headers) {
			return new SsdpSearchResponse(startLine, headers);
		}
	};

	public static class Builder extends HttpResponse.Builder
	{
		private final HttpStatusLine STATUS_LINE = new HttpStatusLine(HttpMessage.VERSION_1_1, "200", "OK");

		public Builder() {
			super.setStartLine(STATUS_LINE);
		}

		public SsdpSearchResponse build() {
			return build(FACTORY);
		}

		public void setDescriptionUrl(String url) {
			setLocation(url);
		}

		public void setSearchTarget(String target) {
			mMap.putFirst(Ssdp.ST, target);
		}

		public void setUniqueServiceName(String usn) {
			mMap.putFirst(Ssdp.USN, usn);
		}
	
		public void setBootId(int id) {
			putInt(Ssdp.BOOTID, id);
		}

		public void setConfigId(int id) {
			putInt(Ssdp.CONFIGID, id);
		}

		public void setSearchPort(int port) {
			putInt(Ssdp.SEARCHPORT, port);
		}
	}
}
