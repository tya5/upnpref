package org.tyas.upnp.ssdp;

import java.net.URL;

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
	public static final String BOOTID = "BOOTID.UPNP.ORG";
	public static final String CONFIGID = "CONFIGID.UPNP.ORG";
	public static final String SEARCHPORT = "SEARCHPORT.UPNP.ORG";
	
	public static final int DEFAULT_SSDP_PORT = 1900;

	public static final String NTS_ALIVE = "ssdp:alive";
	public static final String NTS_BYEBYE = "ssdp:byebye";
	public static final String NTS_UPDATE = "ssdp:update";
	public static final String MAN_DISCOVER = "\"ssdp:discover\"";

	public interface Advertisement
	{
		String getHost();
		long getMaxAge();
		URL getDescriptionUrl();
		String getNotificationType();
		String getNotificationSubType();
		//String getServerName();
		String getUniqueServiceName();
		int getBootId();
		int getConfigId();
		int getSearchPort();
	}

	public interface SearchRequest
	{
		String getHost();
		int getMaxWaitTime();
		String getSearchTarget();
	}

	public interface SearchResponse
	{
		long getMaxAge();
		//Date getDate();
		URL getDescriptionUrl();
		//String getServerName();
		String getSearchTarget();
		String getUniqueServiceName();
		int getBootId();
		int getConfigId();
		int getSearchPort();
	}
}
