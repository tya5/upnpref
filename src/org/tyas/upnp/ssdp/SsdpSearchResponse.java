package org.tyas.upnp.ssdp;

import org.tyas.http.HttpMessage;
import org.tyas.http.HttpResponse;
import org.tyas.upnp.UpnpUsn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.DatagramPacket;

public class SsdpSearchResponse
{
	private SsdpSearchResponse() {}

	public interface Base extends Ssdp.RemoteDevicePointer, HttpResponse.Base
	{
		//Date getDate();
		String getSearchTarget();
	}

	public static class Const extends HttpResponse.Const implements Base
	{
		private Const(HttpResponse.Const c) {
			super(c);
		}

		@Override public URL getDescriptionUrl() {
			try {
				return getLocation().toURL();
			} catch (Exception e) {
				return null;
			}
		}

		@Override public String getSearchTarget() { return getFirst(Ssdp.ST); }

		@Override public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }

		@Override public int getBootId() { return getInt(Ssdp.BOOTID, 0); }

		@Override public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }

		@Override public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }
	}

	public static class Builder extends HttpResponse.Builder implements Base
	{
		public Builder() {
			super(HttpMessage.VERSION_1_1, "200", "OK");
		}

		@Override public URL getDescriptionUrl() {
			try {
				return getLocation().toURL();
			} catch (Exception e) {
				return null;
			}
		}

		@Override public String getSearchTarget() { return getFirst(Ssdp.ST); }

		@Override public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }

		@Override public int getBootId() { return getInt(Ssdp.BOOTID, 0); }

		@Override public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }

		@Override public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }

		public Builder setDescriptionUrl(String url) {
			setLocation(url); return this;
		}

		public Builder setSearchTarget(String target) {
			putFirst(Ssdp.ST, target); return this;
		}

		public Builder setUniqueServiceName(String usn) {
			putFirst(Ssdp.USN, usn); return this;
		}
	
		public Builder setBootId(int id) {
			setInt(Ssdp.BOOTID, id); return this;
		}

		public Builder setConfigId(int id) {
			setInt(Ssdp.CONFIGID, id); return this;
		}

		public Builder setSearchPort(int port) {
			setInt(Ssdp.SEARCHPORT, port); return this;
		}
	}

	public static Const getByHttpResponse(HttpResponse.Const resp) {
		return new Const(resp);
	}
}
