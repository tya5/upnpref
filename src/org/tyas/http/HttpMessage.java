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
import java.net.URI;
import java.net.URL;

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

	@Override public int getInt(String name, int defaultValue) {
		try {
			return Integer.decode(getFirst(name));
		} catch (Exception e) {
			return defaultValue;
		}
	}

	@Override public long getLong(String name, long defaultValue) {
		try {
			return Long.decode(getFirst(name));
		} catch (Exception e) {
			return defaultValue;
		}
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
		String host = getFirst(Http.HOST);

		if (host == null) return null;

		try {
			return host.split(":")[0];
		} catch (Exception e) {
			return "";
		}
	}

	@Override public int getPort() {
		try {
			return Integer.decode(getFirst(Http.HOST).split(":")[1]);
		} catch (Exception e) {
			return -1;
		}
	}

	@Override public long getMaxAge() {
		List<String> list = get(Http.CACHE_CONTROL);

		if (list == null) return -1;

		for (String item: list) {
			int idx = item.indexOf(Http.MAX_AGE);
			if (idx >= 0) {
				idx = item.indexOf('=', idx + Http.MAX_AGE.length());
				if (idx >= 0) {
					try {
						return Long.decode(item.substring(idx + 1).trim());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return -1;
	}

	@Override public URI getLocation() {
		try {
			return new URI(getFirst(Http.LOCATION));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setMaxAge(long max) {
		List<String> list = get(Http.CACHE_CONTROL);

		if (list != null) {
			for (String item: list) {
				int idx = item.indexOf(Http.MAX_AGE);
				if (idx >= 0) {
					list.remove(item);
				}
			}
		}
		put(Http.CACHE_CONTROL, Http.MAX_AGE + "=" + max);
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

	public void setInt(String name, int value) {
		putFirst(name, "" + value);
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

	public void setLocation(URI uri) {
		setLocation(uri.toString());
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
