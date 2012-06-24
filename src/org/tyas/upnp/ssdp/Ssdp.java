package org.tyas.upnp.ssdp;

public class Ssdp
{
	public static final String REQUEST_URI = "*";
	public static final String NOTIFY = "NOTIFY";
	public static final String MSEARCH = "M-SEARCH";
	public static final int DEFAULT_SSDP_PORT = 1900;

	public static final String NTS_ALIVE = "ssdp:alive";
	public static final String NTS_BYEBYE = "ssdp:byebye";
	public static final String NTS_UPDATE = "ssdp:update";
	public static final String MAN_DISCOVER = "\"ssdp:discover\"";

	public interface Advertisement
	{
		String getHost();
		int getPort();
		int getMaxAge();
		String getDescriptionUrl();
		String getNotificationType();
		String getNotificationSubType();
		String getServerName();
		String getUniqueServiceName();
		int getBootId();
		int getConfigId();
		int getSearchPort();
	}

	public interface SearchRequest
	{
		String getHost();
		int getPort();
		int getMaxWaitTime();
		String getSearchTarget();
	}

	public interface SearchResponse
	{
		int getMaxAge();
		Date getDate();
		String getDescriptionUrl();
		String getServerName();
		String getSearchTarget();
		String getUniqueServiceName();
		int getBootId();
		int getConfigId();
		int getSearchPort();
	}
}
