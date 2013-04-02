package org.tyas.upnp.ssdp;

import org.tyas.http.HttpHeaders;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpRequestLine;
import org.tyas.upnp.UpnpUsn;

import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class SsdpAdvertisement extends HttpRequest implements Ssdp.RemoteDevicePointer
{
	public static enum Nts
	{		
		ALIVE("ssdp:alive", new String [] {
				HttpRequest.HOST,
				HttpMessage.CACHE_CONTROL,
				Ssdp.LOCATION,
				Ssdp.NT,
				Ssdp.NTS,
				HttpMessage.SERVER,
				Ssdp.USN,
				Ssdp.BOOTID,
				Ssdp.CONFIGID,
			}),
			BYEBYE("ssdp:byebye", new String [] {
					HttpRequest.HOST,
					Ssdp.NT,
					Ssdp.NTS,
					Ssdp.USN,
					Ssdp.BOOTID,
					Ssdp.CONFIGID,
				}),
			UPDATE("ssdp:update", new String [] {
					HttpRequest.HOST,
					Ssdp.LOCATION,
					Ssdp.NT,
					Ssdp.NTS,
					Ssdp.USN,
					Ssdp.BOOTID,
					Ssdp.CONFIGID,
					Ssdp.NEXTBOOTID,
				});

		private final String mStringValue;
		private final String [] mRequiredHeaderKeys;

		private Nts(String value, String [] required) {
			mStringValue = value;
			mRequiredHeaderKeys = required;
		}

		public String getStringValue() { return mStringValue; }

		public boolean containsAllRequired(HttpHeaders headers) {
			Set<String> keys = headers.keySet();

			for (String key: mRequiredHeaderKeys) {
				if (! keys.contains(key)) return false;
			}

			return true;
		}

		public static Nts getByStringValue(String value) {
			if (value == null) return null;

			for (Nts nts: values()) {
				if (value.equals(nts.getStringValue())) {
					return nts;
				}
			}
			return null;
		}
	}

	protected SsdpAdvertisement(HttpRequestLine startLine, HttpHeaders headers) {
		super(startLine, headers);
	}

	public String getNotificationType() { return getFirst(Ssdp.NT); }

	public Nts getNotificationSubType() { return Nts.getByStringValue(getFirst(Ssdp.NTS)); }

	public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }

	public int getBootId() { return getInt(Ssdp.BOOTID, 0); }
	
	public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }
	
	public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }

	public URL getDescriptionUrl() {
		try {
			return new URI(getFirst(Ssdp.LOCATION)).toURL();
		} catch (Exception e) {
			return null;
		}
	}

	public static final SsdpAdvertisement read(InputStream in) throws IOException {
		return HttpRequest.readMessage(in, FACTORY).getMessage();
	}

	public static final HttpMessage.Factory<HttpRequestLine,SsdpAdvertisement> FACTORY =
		new HttpMessage.Factory<HttpRequestLine,SsdpAdvertisement>()
	{
		public SsdpAdvertisement createMessage(HttpRequestLine startLine, HttpHeaders headers) {
			return new SsdpAdvertisement(startLine, headers);
		}
	};
	
	public static class Builder extends HttpRequest.Builder
	{
		private static final HttpRequestLine REQUEST_LINE = new HttpRequestLine(Ssdp.NOTIFY, "*", HttpMessage.VERSION_1_1);

		public Builder(String host, int port) {
			super(REQUEST_LINE, "239.255.255.255", 1900);
			putMaxAge(1800);
		}

		public void setDescriptionUrl(String url) {
			mMap.setFirst(Ssdp.LOCATION, url);
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

		public void setNotificationSubType(Nts nts) {
			mMap.setFirst(Ssdp.NTS, nts.getStringValue());
		}

		public SsdpAdvertisement build() {
			return build(FACTORY);
		}

		public SsdpAdvertisement buildStrict() {
			Nts nts = Nts.getByStringValue(mMap.getFirst(Ssdp.NTS));

			if ((nts == null) || (! nts.containsAllRequired(mMap))) {
				return null;
			}

			return build();
		}
	}
}
