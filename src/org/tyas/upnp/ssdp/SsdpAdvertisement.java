package org.tyas.upnp.ssdp;

import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.upnp.UpnpUsn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.DatagramPacket;

public abstract class SsdpAdvertisement
{
	public interface Base extends Ssdp.RemoteDevicePointer, HttpRequest.Base
	{
		String getNotificationType();
		String getNotificationSubType();
	}
	
	public static class Const extends HttpRequest.Const implements Base
	{
		private Const(HttpRequest.Const c) {
			super(c);
		}

		@Override public URL getDescriptionUrl() {
			try {
				return getLocation().toURL();
			} catch (Exception e) {
				return null;
			}
		}

		@Override public String getNotificationType() { return getFirst(Ssdp.NT); }

		@Override public String getNotificationSubType() { return getFirst(Ssdp.NTS); }

		@Override public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }

		@Override public int getBootId() { return getInt(Ssdp.BOOTID, 0); }
	
		@Override public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }
	
		@Override public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }
	}

	public static class Builder extends HttpRequest.Builder implements Base
	{
		public Builder() {
			super(Ssdp.NOTIFY, "*", HttpMessage.VERSION_1_1);
		}

		@Override public URL getDescriptionUrl() {
			try {
				return getLocation().toURL();
			} catch (Exception e) {
				return null;
			}
		}

		@Override public String getNotificationType() { return getFirst(Ssdp.NT); }

		@Override public String getNotificationSubType() { return getFirst(Ssdp.NTS); }

		@Override public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }

		@Override public int getBootId() { return getInt(Ssdp.BOOTID, 0); }
	
		@Override public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }
	
		@Override public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }

		public Builder setDescriptionUrl(String url) {
			setLocation(url);
			return this;
		}

		public Builder setBootId(int id) {
			setInt(Ssdp.BOOTID, id);
			return this;
		}

		public Builder setConfigId(int id) {
			setInt(Ssdp.CONFIGID, id);
			return this;
		}

		public Builder setSearchPort(int port) {
			setInt(Ssdp.SEARCHPORT, port);
			return this;
		}
	}

	public static SsdpAdvertisement.Const getByHttpRequest(HttpRequest.Const req) {
		if (! Ssdp.NOTIFY.equals(req.getMethod())) return null;

		if (! HttpMessage.VERSION_1_1.equals(req.getVersion())) return null;

		return new SsdpAdvertisement.Const(req);
	}
}
