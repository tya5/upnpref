package org.tyas.http;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public abstract class HttpMessage implements Http.Message
{
	private Map<String,List<String>> mMap = new HashMap<String,List<String>>();

	public HttpMessage() {}

	public HttpMessage(Http.Message msg) {
		for (String key: msg.keySet()) {
			for (String val: msg.get(key)) put(key, val);
		}
	}

	@Override public List<String> get(String name) {
		return mMap.get(name);
	}

	@Override public String getFirst(String name) {
		List<String> list = get(name);
		return list == null || list.size() == 0 ? null: list.get(0);
	}

	@Override public Set<String> keySet() {
		return mMap.keySet();
	}

	@Override public boolean isKeepAlive() {
		for (String token: get(Http.CONNECTION)) {
			String upper = token.toUpperCase();
			
			if (upper.equals(Http.CLOSE))
				return false;
			
			if (upper.equals(Http.KEEP_ALIVE)) {
				return (get(Http.KEEP_ALIVE) != null);
			}
		}
		return false;
	}

	@Override public boolean isChunkedEncoding() {
		String te = getFirst(Http.TRANSFER_ENCODING);
		return (te != null) && Http.CHUNKED.equals(te.toUpperCase());
	}

	@Override public long getContentLength() {
		String cl = getFirst(Http.CONTENT_LENGTH);
		return cl == null ? -1: Long.decode(cl);
	}

	@Override public String getHost() {
		return getFirst(Http.HOST);
	}

	public void put(String name, String value) {
		List<String> list = get(name);

		if (list == null) {
			list = new ArrayList<String>();
			mMap.put(name, list);
		}

		list.add(value);
	}

	public void putFirst(String name, String value) {
		List<String> list = get(name);

		if (list == null) {
			list = new ArrayList<String>();
			mMap.put(name, list);
		} else {
			list.clear();
		}

		list.add(value);
	}

	public void remove(String name) {
		mMap.remove(name);
	}

	public void setHost(String host, int port) {
		if (port < 0) {
			setHost(host);
		} else {
			putFirst(Http.HOST, host + ":" + port);
		}
	}

	public void setHost(String host) {
		putFirst(Http.HOST, host);
	}

	public void setLocation(String uri) {
		putFirst(Http.LOCATION, uri);
	}

	public void setCacheControl(String name, String value) {
		putFirst(Http.CACHE_CONTROL, name + (value == null ? "": ("=" + value)));
	}

	public void putServerToken(String product) {
		put(Http.SERVER, product);
	}

	public OutputStream send(OutputStream out) throws IOException {
		validate();
		return Http.writeMessage(out, this, 256);
	}

	public void send(OutputStream out, byte [] entity) throws IOException {
		validate();
		Http.writeMessage(out, this, entity);
	}

	public void send(OutputStream out, InputStream in) throws IOException {
		validate();
		Http.writeMessage(out, this, in);
	}

	public void send(OutputStream out, File f) throws IOException {
		validate();
		Http.writeMessage(out, this, f);
	}

	protected void validate() {}
}
