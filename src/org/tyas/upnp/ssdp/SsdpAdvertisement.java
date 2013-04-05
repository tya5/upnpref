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

public class SsdpAdvertisement extends HttpRequest implements SsdpConstant
{
	public static enum Nts
	{		
		ALIVE("ssdp:alive", new String [] {
				HttpRequest.HOST,
				HttpMessage.CACHE_CONTROL,
				LOCATION,
				NT,
				NTS,
				HttpMessage.SERVER,
				USN,
				BOOTID,
				CONFIGID,
			}),
			BYEBYE("ssdp:byebye", new String [] {
					HttpRequest.HOST,
					NT,
					NTS,
					USN,
					BOOTID,
					CONFIGID,
				}),
			UPDATE("ssdp:update", new String [] {
					HttpRequest.HOST,
					LOCATION,
					NT,
					NTS,
					USN,
					BOOTID,
					CONFIGID,
					NEXTBOOTID,
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

	public String getNotificationType() { return getFirst(NT); }

	public Nts getNotificationSubType() { return Nts.getByStringValue(getFirst(NTS)); }

	public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(USN)); }

	public int getBootId() { return getInt(BOOTID, 0); }
	
	public int getConfigId() { return getInt(CONFIGID, 0); }
	
	public int getSearchPort() { return getInt(SEARCHPORT, -1); }

	public URL getDescriptionUrl() {
		try {
			return new URI(getFirst(LOCATION)).toURL();
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
		private static final HttpRequestLine REQUEST_LINE = new HttpRequestLine(NOTIFY, "*", HttpMessage.VERSION_1_1);

		public Builder(String host, int port) {
			super(REQUEST_LINE, "239.255.255.255", 1900);
			putMaxAge(1800);
		}

		public void setDescriptionUrl(String url) {
			mMap.setFirst(LOCATION, url);
		}

		public void setBootId(int id) {
			putInt(BOOTID, id);
		}

		public void setConfigId(int id) {
			putInt(CONFIGID, id);
		}

		public void setSearchPort(int port) {
			putInt(SEARCHPORT, port);
		}

		public void setNotificationType(String value) {
			mMap.setFirst(NT, value);
		}

		public void setUsn(String usn) {
			mMap.setFirst(USN, usn);
		}
		
		public void setNotificationSubType(Nts nts) {
			mMap.setFirst(NTS, nts.getStringValue());
		}

		public SsdpAdvertisement build() {
			return build(FACTORY);
		}

		public SsdpAdvertisement buildStrict() {
			Nts nts = Nts.getByStringValue(mMap.getFirst(NTS));

			if ((nts == null) || (! nts.containsAllRequired(mMap))) {
				return null;
			}

			return build();
		}
	}
}
