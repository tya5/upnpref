package org.tyas.upnp.ssdp;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpRequestLine;
import org.tyas.http.HttpMessageFactory;
import org.tyas.upnp.UpnpUsn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.DatagramPacket;

public class SsdpAdvertisement extends HttpRequest implements Ssdp.RemoteDevicePointer
{
	protected SsdpAdvertisement(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public String getNotificationType() { return getFirst(Ssdp.NT); }

	public String getNotificationSubType() { return getFirst(Ssdp.NTS); }

	public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }

	public int getBootId() { return getInt(Ssdp.BOOTID, 0); }
	
	public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }
	
	public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }

	public URL getDescriptionUrl() {
		try {
			return getLocation().toURL();
		} catch (Exception e) {
			return null;
		}
	}

	public static final SsdpAdvertisement read(InputStream in) throws IOException {
		return HttpMessage.readMessage(in, HttpRequestLine.PARSER, FACTORY).getMessage();
	}

	public static final HttpMessageFactory<HttpRequestLine,SsdpAdvertisement> FACTORY =
		new HttpMessageFactory<HttpRequestLine,SsdpAdvertisement>()
	{
		public SsdpAdvertisement createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			if (! Ssdp.NOTIFY.equals(startLine.getMethod())) return null;
			
			if (! HttpMessage.VERSION_1_1.equals(startLine.getHttpVersion())) return null;
			
			return new SsdpAdvertisement(startLine, headers);
		}
	};
	
	public static class Builder
	{
		public final HttpMessage.Builder<HttpRequestLine> mHttpMessageBuilder =
			new HttpMessage.Builder<HttpRequestLine>
			(new HttpRequestLine(Ssdp.NOTIFY, "*", HttpMessage.VERSION_1_1));

		public Builder setDescriptionUrl(String url) {
			mHttpMessageBuilder.setLocation(url);
			return this;
		}

		public Builder setBootId(int id) {
			mHttpMessageBuilder.setInt(Ssdp.BOOTID, id);
			return this;
		}

		public Builder setConfigId(int id) {
			mHttpMessageBuilder.setInt(Ssdp.CONFIGID, id);
			return this;
		}

		public Builder setSearchPort(int port) {
			mHttpMessageBuilder.setInt(Ssdp.SEARCHPORT, port);
			return this;
		}

		public SsdpAdvertisement build() {
			return mHttpMessageBuilder.build(FACTORY);
		}
	}
}
