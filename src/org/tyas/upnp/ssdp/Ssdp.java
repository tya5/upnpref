package org.tyas.upnp.ssdp;

import java.net.URL;
import java.net.DatagramPacket;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.tyas.http.Http;
import org.tyas.upnp.UpnpUsn;

public class Ssdp
{
	public static final String REQUEST_URI = "*";
	public static final String NOTIFY = "NOTIFY";
	public static final String M_SEARCH = "M-SEARCH";

	public static final String ST = "ST";
	public static final String MX = "MX";
	public static final String NT = "NT";
	public static final String NTS = "NTS";
	public static final String USN = "USN";
	public static final String MAN = "MAN";
	public static final String BOOTID = "BOOTID.UPNP.ORG";
	public static final String CONFIGID = "CONFIGID.UPNP.ORG";
	public static final String SEARCHPORT = "SEARCHPORT.UPNP.ORG";

	public static final String MULTICAST_HOST = "239.255.255.250";
	public static final int DEFAULT_PORT = 1900;

	public static final String ST_ALL = "ssdp:all";
	public static final String NTS_ALIVE = "ssdp:alive";
	public static final String NTS_BYEBYE = "ssdp:byebye";
	public static final String NTS_UPDATE = "ssdp:update";
	public static final String MAN_DISCOVER = "\"ssdp:discover\"";
	public static final String ROOT_DEVICE = "upnp:rootdevice";

	public interface RemoteDevicePointer
	{
		long getMaxAge();
		URL getDescriptionUrl();
		UpnpUsn getUniqueServiceName();
		int getBootId();
		int getConfigId();
		int getSearchPort();
		//String getServerName();
	}

	public interface Advertisement extends RemoteDevicePointer, Http.Request
	{
		String getHost();
		String getNotificationType();
		String getNotificationSubType();
		DatagramPacket toDatagramPacket() throws IOException;
	}

	public interface SearchRequest extends Http.Request
	{
		String getHost();
		int getMaxWaitTime();
		String getSearchTarget();
		String getMan();
		DatagramPacket toDatagramPacket() throws IOException;
	}

	public interface SearchResponse extends RemoteDevicePointer, Http.Response
	{
		//Date getDate();
		String getSearchTarget();
		DatagramPacket toDatagramPacket() throws IOException;
	}

	public static DatagramPacket toDatagramPacket(Http.Message msg) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		msg.send(out);
		byte [] data = out.toByteArray();
		return new DatagramPacket(data, data.length);
	}
}
