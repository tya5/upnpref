package org.tyas.upnp.event;

public class Event
{
	public static final String SUBSCRIBE - "SUBSCRIBE";
	public static final String SUBSCRIBE - "UNSUBSCRIBE";
	public static final String CALLBACK = "CALLBACK";
	public static final String TIMEOUT = "TIMEOUT";
	public static final String NT = "NT";
	public static final String SID = "SID";
	public static final String UPNP_EVENT = "upnp:event";

	public interface SubscribeRequest extends Http.Request
	{
		String getCallback();
		int getTimeout();
		SubscribeId getSid();
		boolean isSubscribeRequest();
		boolean isRenewRequest();
		boolean isUnsubscribeRequest();
	}

	public interface EventMessage extends Http.Request
	{
		SubscribeId getSid();
		int getEventKey();
		Set<String> getVariableNameSet();
		String getProperty(String variableName);
	}

	public static int unpackTimeout(String timeout) {
		String pfx = "Second-";
		int idx = timeout.indexOf(pfx);
		return Integer.decode(timeout.substring(pfx.length));
	}

	public static String packTimeout(int timeout) {
		return "Second-" + timeout;
	}
}
