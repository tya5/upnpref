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
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.DatagramPacket;

public abstract class HttpMessage
{
	public static final char LF = '\n';
	public static final char CR = '\r';
	public static final String CRLF = "\r\n";

	public static final String VERSION_1_0 = "HTTP/1.0";
	public static final String VERSION_1_1 = "HTTP/1.1";

	public static final String HOST = "HOST";
	public static final String LOCATION = "LOCATION";
	public static final String TRANSFER_ENCODING = "TRANSFER-ENCODING";
	public static final String CONTENT_LENGTH = "CONTENT-LENGTH";
	public static final String SERVER = "SERVER";
	public static final String CACHE_CONTROL = "CACHE-CONTROL";
	public static final String CONNECTION = "CONNECTION";
	public static final String KEEP_ALIVE = "KEEP-ALIVE";
	public static final String CLOSE = "CLOSE";
	public static final String CHUNKED = "chunked";
	public static final String MAX_AGE = "max-age";

	private static final String [] mReservedTab = new String [] {
		HOST,
		LOCATION,
		TRANSFER_ENCODING,
		CONTENT_LENGTH,
		SERVER,
		CACHE_CONTROL,
		CONNECTION,
		KEEP_ALIVE,
	};

	private Map<String,List<String>> mMap;

	public HttpMessage() {
		mMap = new HashMap<String,List<String>>();
	}

	public HttpMessage(Map<String,List<String>> map) {
		mMap = map;
	}

	public HttpMessage(HttpMessage msg) {
		this();
		for (String key: msg.keySet()) {
			mMap.put(key, new ArrayList<String>(msg.get(key)));
		}
	}

	public abstract String getStartLine();

	public Set<String> keySet() { return mMap.keySet(); }

	public List<String> get(String name) { return mMap.get(name); }

	public boolean isReserved(String name) {
		name = name == null ? null : name.toUpperCase();

		for (String mask: mReservedTab) {
			if (mask.equals(name.toUpperCase())) {
				return false;
			}
		}

		return true;
	}

	public String getFirst(String name) {
		List<String> list = get(name);
		return list == null || list.size() == 0 ? null: list.get(0);
	}

	public int getInt(String name, int defaultValue) {
		try {
			return Integer.decode(getFirst(name));
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public long getLong(String name, long defaultValue) {
		try {
			return Long.decode(getFirst(name));
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public boolean isKeepAlive() {
		List<String> con = get(CONNECTION);
		if (con != null) {
			for (String token: con) {
				String upper = token.toUpperCase();
			
				if (upper.equals(CLOSE))
					return false;
			
				if (upper.equals(KEEP_ALIVE)) {
					return (get(KEEP_ALIVE) != null);
				}
			}
		}
		return false;
	}

	public boolean isChunkedEncoding() {
		String te = getFirst(TRANSFER_ENCODING);
		return (te != null) && CHUNKED.equals(te.toUpperCase());
	}

	public long getContentLength() {
		String cl = getFirst(CONTENT_LENGTH);
		return cl == null ? -1: Long.decode(cl);
	}

	public String getHost() {
		String host = getFirst(HOST);

		if (host == null) return null;

		try {
			return host.split(":")[0];
		} catch (Exception e) {
			return "";
		}
	}

	public int getPort() {
		try {
			return Integer.decode(getFirst(HOST).split(":")[1]);
		} catch (Exception e) {
			return -1;
		}
	}

	public long getMaxAge() {
		List<String> list = get(CACHE_CONTROL);

		if (list == null) return -1;

		for (String item: list) {
			int idx = item.indexOf(MAX_AGE);
			if (idx >= 0) {
				idx = item.indexOf('=', idx + MAX_AGE.length());
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

	public URI getLocation() {
		try {
			return new URI(getFirst(LOCATION));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public OutputStream send(OutputStream out) throws IOException {
		validate();
		return writeMessage(out, this, 256);
	}

	public void send(OutputStream out, byte [] entity) throws IOException {
		validate();
		writeMessage(out, this, entity);
	}

	public void send(OutputStream out, InputStream in) throws IOException {
		validate();
		writeMessage(out, this, in);
	}

	public void send(OutputStream out, File f) throws IOException {
		validate();
		writeMessage(out, this, f);
	}

	protected void validate() {}

	/**
	 * Make entity body stream for output.
	 *
	 * @param msg message header
	 * @param out OutputStream for general message
	 * @return OutputStream for entity body
	 */
	public static OutputStream writeMessage(OutputStream out, HttpMessage msg, final int maxChunkSize)
		throws IOException
	{
		writeMessageHeaders(out, msg.getStartLine(), msg, CHUNKED, null);
		
		return new FilterOutputStream(out) {
			int ofs = 0;
			@Override public void write(int b) throws IOException {
				if (ofs == 0) {
					out.write(String.format("%x\r\n", maxChunkSize).getBytes());
				}
				out.write(b);
				ofs++;
				if (ofs >= maxChunkSize) {
					out.write("\r\n".getBytes());
					ofs = 0;
				}
			}
		};
	}

	public static void writeMessage(OutputStream out, HttpMessage msg, byte [] entity)
		throws IOException
	{
		String contLen = null;
		if ((entity != null) && (entity.length > 0)) {
			contLen = "" + entity.length;
		}
		writeMessageHeaders(out, msg.getStartLine(), msg, null, contLen);
		if ((entity != null) && (entity.length > 0)) {
			out.write(entity);
		}
	}

	public static void writeMessage(OutputStream raw, HttpMessage msg, InputStream in)
		throws IOException
	{
		OutputStream out = writeMessage(raw, msg, 256);
		int b = in.read();
		while (b >= 0) {
			out.write(b);
			b = in.read();
		}
	}

	public static void writeMessage(OutputStream raw, HttpMessage msg, File f)
		throws IOException
	{
		FileInputStream in = new FileInputStream(f);
		writeMessage(raw, msg, in);
		in.close();
	}

	private static void writeMessageHeaders(OutputStream out, String startLine, HttpMessage msg, String transEnc, String contLen)
		throws IOException
	{
		out.write((startLine + "\r\n").getBytes());

		for (String key: msg.keySet()) {
			if (TRANSFER_ENCODING.equals(key)) continue;
			if (CONTENT_LENGTH.equals(key)) continue;
			String values = join(msg.get(key), ",");
			out.write((key + ":" + values + "\r\n").getBytes());
		}

		if (transEnc != null) {
			out.write((TRANSFER_ENCODING + ":" + transEnc + CRLF).getBytes());
		}
		if (contLen != null) {
			out.write((CONTENT_LENGTH + ":" + contLen + CRLF).getBytes());
		}

		out.write("\r\n".getBytes());
	}

	private static String join(List<String> texts, String delim) {
		if ((texts == null) || (texts.size() <= 0)) return "";

		String out = texts.get(0);
		for (int ii = 1; ii < texts.size(); ii++) {
			out += delim + texts.get(ii);
		}

		return out;
	}

	public DatagramPacket toDatagramPacket() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		send(out);
		byte [] data = out.toByteArray();
		return new DatagramPacket(data, data.length);
	}

	public interface Base
	{
		String getStartLine();
		Set<String> keySet();
		List<String> get(String name);
		String getFirst(String name);
		int getInt(String name, int defaultValue);
		long getLong(String name, long defaultValue);
		boolean isKeepAlive();
		boolean isChunkedEncoding();
		long getContentLength();
		String getHost();
		int getPort();
		long getMaxAge();
		URI getLocation();
		OutputStream send(OutputStream out) throws IOException;
		void send(OutputStream out, byte [] entity) throws IOException;
		void send(OutputStream out, InputStream in) throws IOException;
		void send(OutputStream out, File f) throws IOException;
		DatagramPacket toDatagramPacket() throws IOException;
	}

	public static abstract class Const extends HttpMessage implements Base
	{
		private Const() { super(); }

		public Const(HttpMessage msg) { super(msg); }

		public Const(Const c) { super(((HttpMessage)c).mMap); }
	}

	public static abstract class Input extends Const implements Base
	{
		private InputStream mInput;

		public InputStream getInputStream() { return mInput; }
	}

	public static abstract class Builder extends HttpMessage
	{
		public Builder() { super(); }

		public Builder(HttpMessage msg) { super(msg); }

		private void putImpl(String name, String value) {
			List<String> list = get(name);

			if (list == null) {
				list = new ArrayList<String>();
				((HttpMessage)this).mMap.put(name, list);
			}

			list.add(value);
		}

		private void putFirstImpl(String name, String value) {
			List<String> list = get(name);

			if (list == null) {
				list = new ArrayList<String>();
				((HttpMessage)this).mMap.put(name, list);
			} else {
				list.clear();
			}

			list.add(value);
		}

		public HttpMessage.Builder put(String name, String value) {
			if (! isReserved(name)) return null;
		
			putImpl(name, value);
			return this;
		}

		public HttpMessage.Builder putFirst(String name, String value) {
			if (! isReserved(name)) return null;

			putFirstImpl(name, value);
			return this;
		}

		public HttpMessage.Builder setInt(String name, int value) {
			putFirst(name, "" + value); return this;
		}

		public HttpMessage.Builder remove(String name) {
			((HttpMessage)this).mMap.remove(name); return this;
		}

		public HttpMessage.Builder setHost(String host, int port) {
			if (port < 0) {
				setHost(host);
			} else {
				putFirstImpl(HOST, host + ":" + port);
			}
			return this;
		}

		public HttpMessage.Builder setHost(String host) {
			putFirstImpl(HOST, host); return this;
		}

		public HttpMessage.Builder setLocation(String uri) {
			putFirstImpl(LOCATION, uri); return this;
		}

		public HttpMessage.Builder setLocation(URI uri) {
			setLocation(uri.toString()); return this;
		}

		public HttpMessage.Builder putServerToken(String product) {
			putImpl(SERVER, product); return this;
		}

		public HttpMessage.Builder setMaxAge(long max) {
			List<String> list = get(CACHE_CONTROL);

			if (list != null) {
				for (String item: list) {
					int idx = item.indexOf(MAX_AGE);
					if (idx >= 0) {
						list.remove(item);
					}
				}
			}
			put(CACHE_CONTROL, MAX_AGE + "=" + max);
			return this;
		}
	}

	private static class FixedLengthInputStream extends FilterInputStream
	{
		private long ofs = 0;
		private long length;

		FixedLengthInputStream(InputStream in, long length) {
			super(in);
			this.length = length;
		}
		
		@Override public int read() throws IOException {
			if (ofs >= length) return -1;
						
			int b = in.read();
			if (b < 0) {
				ofs = length;
				return -1;
			}

			ofs++;
			return b;
		}

		@Override public int read(byte[] buffer, int offset, int count) throws IOException {
			if (ofs >= length) return -1;

			long readable = length - ofs;

			if (count > readable) count = (readable <= Integer.MAX_VALUE ? (int)readable: Integer.MAX_VALUE);

			int result = in.read(buffer, offset, count);

			if (result >= 0) ofs += result;

			return result;
		}

		@Override public boolean markSupported() { return false; }

		@Override public long skip(long byteCount) throws IOException {
			if (ofs >= length) return 0;

			long skipable = length - ofs;

			if (byteCount > skipable) byteCount = skipable;

			long result = in.skip(byteCount);

			ofs += result;

			return result;
		}
	}

	private static class ChunkedInputStream extends FilterInputStream
	{
		private int remain = 0;

		ChunkedInputStream(InputStream in) {
			super(in);
		}
		
		@Override public int read() throws IOException {
			if (remain < 0) return -1;

			if (remain == 0) {
				remain = Integer.decode("0x" + readLine(in).trim());
				if (remain == 0) {
					remain = -1;
					return -1;
				}
			}

			int b = in.read();

			if (b < 0) {
				remain = -1;
				return -1;
			}
			
			remain--;
			
			if (remain == 0) readLine(in);

			return b;
		}

		@Override public int read(byte[] buffer, int offset, int count) throws IOException {
			if (remain < 0) return -1;

			if (remain == 0) {
				remain = Integer.decode("0x" + readLine(in).trim());
				if (remain == 0) {
					remain = -1;
					return -1;
				}
			}
			
			int result = in.read(buffer, offset, remain < count ? remain: count);

			if (result >= 0) remain -= result;

			if (remain == 0) readLine(in);

			return result;
		}

		@Override public boolean markSupported() { return false; }

		@Override public long skip(long byteCount) throws IOException {
			if (remain < 0) return 0;

			if (remain == 0) {
				remain = Integer.decode("0x" + readLine(in).trim());
				if (remain == 0) {
					remain = -1;
					return 0;
				}
			}
			
			long result = in.skip(remain < byteCount ? remain: byteCount);

			if (result >= 0) remain -= (int)result;

			if (remain == 0) readLine(in);

			return result;
		}
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
		String line = readLine(in);

		while (line.length() > 0) {
			int mid = line.indexOf(':');
			if (mid < 0) break;
			String name = line.substring(0, mid).trim().toUpperCase();
			String [] vals = line.substring(mid + 1).split(",", 0);
			List<String> l = new ArrayList<String>();
			((HttpMessage)inp).mMap.put(name, l);
			for (String val : vals) l.add(val.trim());
			line = readLine(in);
		}

		if (inp.isChunkedEncoding()) {
			inp.mInput = new ChunkedInputStream(in);
		} else {
			long tmp = inp.getContentLength();
			inp.mInput = (tmp <= 0) ? null: new FixedLengthInputStream(in, tmp);
		}

		return inp;
	}

	private static String readLine(InputStream in) throws IOException {
		ByteArrayOutputStream lineBuf = new ByteArrayOutputStream();
		byte readBuf[] = new byte[1];
		
		int	readLen = in.read(readBuf);
		while (0 < readLen) {
			if (readBuf[0] == LF)
				break;
			if (readBuf[0] != CR) 
				lineBuf.write(readBuf[0]);
			readLen = in.read(readBuf);
		}

		return lineBuf.toString();
	}
}
