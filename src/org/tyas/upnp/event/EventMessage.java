package org.tyas.upnp.event;

import org.tyas.http.HttpRequest;
import java.util.*;

public abstract class EventMessage
{
	public static final String SID = "SID";
	public static final String SEQ = "SEQ";

	private PropertySet mPropertySet = new PropertySet();

	public Set<String> getVariableNameSet() { return mPropertySet.keySet(); }

	public String getProperty(String variableName) { return mPropertySet.get(variableName); }


	public interface Base extends HttpRequest.Base
	{
		SubscribeId getSid();
		int getEventKey();
		Set<String> getVariableNameSet();
		String getProperty(String variableName);
	}

	private static class PropertySet extends HashMap<String,String>
	{
	}

	public static class Const extends HttpRequest.Const implements Base
	{
		private EventMessage mEv;

		@Override public Set<String> getVariableNameSet() { return mEv.getVariableNameSet(); }
		@Override public String getProperty(String variableName) { return mEv.getProperty(variableName); }
		@Override public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(SID)); }
		@Override public int getEventKey() { return getInt(SEQ, -1); }
	}

	public static class Builder extends HttpRequest.Builder implements Base
	{
		private EventMessage mEv;

		@Override public Set<String> getVariableNameSet() { return mEv.getVariableNameSet(); }
		@Override public String getProperty(String variableName) { return mEv.getProperty(variableName); }
		@Override public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(SID)); }
		@Override public int getEventKey() { return getInt(SEQ, -1); }

		public EventMessage.Builder setSid(SubscribeId sid) {
			putFirst(SID, sid.toString()); return this;
		}

		public EventMessage.Builder setEventKey(int key) {
			putFirst(SEQ, "" + key); return this;
		}

		public EventMessage.Builder putProperty(String variableName, String value) {
			mEv.mPropertySet.put(variableName, value); return this;
		}
	}

	public static EventMessage.Const getByHttpRequest(HttpRequest.Input req) {
	}
}
