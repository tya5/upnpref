package org.tyas.upnp.ssdp;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpResponse;
import org.tyas.http.HttpStatusLine;
import org.tyas.http.HttpMessageFactory;
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

	public static final HttpMessageFactory<HttpStatusLine,SsdpSearchResponse> FACTORY =
		new HttpMessageFactory<HttpStatusLine,SsdpSearchResponse>()
	{
		public SsdpSearchResponse createMessage(HttpStatusLine startLine, HttpHeaders headers) {
			return new SsdpSearchResponse(startLine, headers);
		}
	};

	public static class Builder
	{
		public final HttpMessage.Builder<HttpStatusLine> mHttpMessageBuilder =
			new HttpMessage.Builder<HttpStatusLine>
			(new HttpStatusLine(HttpMessage.VERSION_1_1, "200", "OK"));

		public SsdpSearchResponse build() {
			return mHttpMessageBuilder.build(FACTORY);
		}

		public Builder setDescriptionUrl(String url) {
			mHttpMessageBuilder.setLocation(url); return this;
		}

		public Builder setSearchTarget(String target) {
			mHttpMessageBuilder.putFirst(Ssdp.ST, target); return this;
		}

		public Builder setUniqueServiceName(String usn) {
			mHttpMessageBuilder.putFirst(Ssdp.USN, usn); return this;
		}
	
		public Builder setBootId(int id) {
			mHttpMessageBuilder.setInt(Ssdp.BOOTID, id); return this;
		}

		public Builder setConfigId(int id) {
			mHttpMessageBuilder.setInt(Ssdp.CONFIGID, id); return this;
		}

		public Builder setSearchPort(int port) {
			mHttpMessageBuilder.setInt(Ssdp.SEARCHPORT, port); return this;
		}
	}
}
