package org.tyas.upnp.event;

public class EventMessage extends HttpRequest implements Event.EventMessage
{
	private static class PropertySet extends HashMap<String,String>
	{
	}

	public static class Const extends HttpRequest.Const implements Event.EventMessage
	{
		private PropertySet mPropertySet = new PropertySet();
	
		@Override public Set<String> getVariableNameSet() { return mPropertySet.keySet(); }

		@Override public String getProperty(String variableName) { return mPropertySet.get(variableName); }

		@Override public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(Event.SID)); }

		@Override public int getEventKey() { return getInt(Event.SEQ, -1); }
	}

	private PropertySet mPropertySet = new PropertySet();
	
	@Override public Set<String> getVariableNameSet() { return mPropertySet.keySet(); }

	@Override public String getProperty(String variableName) { return mPropertySet.get(variableName); }

	@Override public SubscribeId getSid() { return SubscribeId.getBySid(getFirst(Event.SID)); }

	@Override public int getEventKey() { return getInt(Event.SEQ, -1); }

	public EventMessage setSid(SubscribeId sid) {
		putFirst(Event.SID, sid.toString()); return this;
	}

	public EventMessage setEventKey(int key) {
		putFirst(Event.SEQ, "" + key); return this;
	}

	public EventMessage putProperty(String variableName, String value) {
		mPropertySet.put(variableName, value); return this;
	}

	public static EventMessage.Const getByHttpRequest(HttpRequest.Input req) {
	}
}
