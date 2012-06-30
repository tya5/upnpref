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
import java.io.FilterInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;

public abstract class HttpMessage extends AbsHttpMessage
{
	public static abstract class Const extends AbsHttpMessage
	{
		private Map<String,List<String>> mMap;

		private Const() {
			mMap = new HashMap<String,List<String>>();
		}

		public Const(AbsHttpMessage msg) {
			this();

			for (String key: msg.keySet()) {
				mMap.put(key, new ArrayList<String>(msg.get(key)));
			}
		}

		public Const(Const c) {
			mMap = c.mMap;
		}

		@Override public Set<String> keySet() {
			return mMap.keySet();
		}

		@Override public List<String> get(String name) {
			return mMap.get(name);
		}
	}

	public static abstract class Input extends Const
	{
		private InputStream mInput;

		public InputStream getInputStream() {
			return mInput;
		}

		public Const toConst() {
			final String start = getStartLine();
			
			return new Const(this) {
				@Override public String getStartLine() { return start; }
			};
		}
	}

	private Map<String,List<String>> mMap = new HashMap<String,List<String>>();

	private static final String [] mReservedTab = new String [] {
		Http.HOST,
		Http.LOCATION,
		Http.TRANSFER_ENCODING,
		Http.CONTENT_LENGTH,
		Http.SERVER,
		Http.CACHE_CONTROL,
		Http.CONNECTION,
		Http.KEEP_ALIVE,
	};

	public HttpMessage() {}

	public HttpMessage(AbsHttpMessage msg) {
		for (String key: msg.keySet()) {
			mMap.put(key, new ArrayList<String>(msg.get(key)));
		}
	}

	@Override public Set<String> keySet() {
		return mMap.keySet();
	}

	@Override public List<String> get(String name) {
		return mMap.get(name);
	}

	public Const toConst() {
		final String start = getStartLine();
		
		return new Const(this) {
			@Override public String getStartLine() { return start; }
		};
	}

	private boolean isReserved(String name) {
		name = name == null ? null : name.toUpperCase();

		for (String mask: mReservedTab) {
			if (mask.equals(name.toUpperCase())) {
				return false;
			}
		}

		return true;
	}
		
	private void putImpl(String name, String value) {
		List<String> list = get(name);

		if (list == null) {
			list = new ArrayList<String>();
			mMap.put(name, list);
		}

		list.add(value);
	}

	private void putFirstImpl(String name, String value) {
		List<String> list = get(name);

		if (list == null) {
			list = new ArrayList<String>();
			mMap.put(name, list);
		} else {
			list.clear();
		}

		list.add(value);
	}

	public boolean put(String name, String value) {
		if (! isReserved(name)) return false;
		
		putImpl(name, value);
		return true;
	}

	public boolean putFirst(String name, String value) {
		if (! isReserved(name)) return false;

		putFirstImpl(name, value);
		return true;
	}

	public HttpMessage setInt(String name, int value) {
		putFirst(name, "" + value); return this;
	}

	public HttpMessage remove(String name) {
		mMap.remove(name); return this;
	}

	public HttpMessage setHost(String host, int port) {
		if (port < 0) {
			setHost(host);
		} else {
			putFirstImpl(Http.HOST, host + ":" + port);
		}
		return this;
	}

	public HttpMessage setHost(String host) {
		putFirstImpl(Http.HOST, host); return this;
	}

	public HttpMessage setLocation(String uri) {
		putFirstImpl(Http.LOCATION, uri); return this;
	}

	public HttpMessage setLocation(URI uri) {
		setLocation(uri.toString()); return this;
	}

	public HttpMessage putServerToken(String product) {
		putImpl(Http.SERVER, product); return this;
	}

	public HttpMessage setMaxAge(long max) {
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
		return this;
	}


	/**
	 * Make entity body stream for input.
	 *
	 * @param in InputStream for general message
	 * @return general message
	 */
	public static Input readMessage(InputStream in) throws IOException {
		final String startLine = readLine(in).trim();

		Input inp = new Input() {
				@Override public String getStartLine() { return startLine; }
			};
		Const msg = (Const)inp;
		
		String line = readLine(in);

		while (line.length() > 0) {
			int mid = line.indexOf(':');
			if (mid < 0) break;
			String name = line.substring(0, mid).trim().toUpperCase();
			String [] vals = line.substring(mid + 1).split(",", 0);
			List<String> l = new ArrayList<String>();
			msg.mMap.put(name, l);
			for (String val : vals) l.add(val.trim());
			line = readLine(in);
		}

		if (msg.isChunkedEncoding()) {
			inp.mInput = new FilterInputStream(in) {
					int remain = 0;
					@Override public int read() throws IOException {
						if (remain < 0) {
							return -1;
						}
						if (remain == 0) {
							remain = Integer.decode("0x" + readLine(in).trim());
						}
						int b = in.read();
						remain--;
						if (remain == 0) {
							readLine(in);
						}
						return b;
					}
				};
		} else {
			long tmp = msg.getContentLength();
			if (tmp >= Integer.MAX_VALUE) throw new IOException();
			final int contentSize = (int)tmp;

			inp.mInput = (contentSize <= 0) ? null: new FilterInputStream(in) {
					int ofs = 0;
					@Override public int read() throws IOException {
						if (ofs >= contentSize) {
							return -1;
						}
						
						int b = in.read();
						if (b < 0) {
							ofs = contentSize;
							return -1;
						}

						ofs++;
						return b;
					}
				};
		}

		return inp;
	}

	private static String readLine(InputStream in) throws IOException {
		ByteArrayOutputStream lineBuf = new ByteArrayOutputStream();
		byte readBuf[] = new byte[1];
		
		int	readLen = in.read(readBuf);
		while (0 < readLen) {
			if (readBuf[0] == Http.LF)
				break;
			if (readBuf[0] != Http.CR) 
				lineBuf.write(readBuf[0]);
			readLen = in.read(readBuf);
		}

		return lineBuf.toString();
	}
}
