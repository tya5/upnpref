package org.tyas.upnp.ssdp;


public interface SsdpConstant
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
	public static final String LOCATION = "LOCATION";
	public static final String BOOTID = "BOOTID.UPNP.ORG";
	public static final String CONFIGID = "CONFIGID.UPNP.ORG";
	public static final String SEARCHPORT = "SEARCHPORT.UPNP.ORG";
	public static final String NEXTBOOTID = "NEXTBOOTID.UPNP.ORG";

	public static final String MULTICAST_HOST = "239.255.255.250";
	//public static final String MULTICAST_HOST = "239.255.255.249";
	public static final int MULTICAST_PORT = 1900;
	public static final int DEFAULT_PORT = 1900;

	public static final String ST_ALL = "ssdp:all";
	public static final String MAN_DISCOVER = "\"ssdp:discover\"";
	public static final String ROOT_DEVICE = "upnp:rootdevice";
}
