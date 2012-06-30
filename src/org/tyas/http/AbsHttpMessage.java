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

public abstract class AbsHttpMessage implements Http.Message
{
	public abstract String getStartLine();

	public abstract Set<String> keySet();
	
	public abstract List<String> get(String name);

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

	public boolean isChunkedEncoding() {
		String te = getFirst(Http.TRANSFER_ENCODING);
		return (te != null) && Http.CHUNKED.equals(te.toUpperCase());
	}

	public long getContentLength() {
		String cl = getFirst(Http.CONTENT_LENGTH);
		return cl == null ? -1: Long.decode(cl);
	}

	public String getHost() {
		String host = getFirst(Http.HOST);

		if (host == null) return null;

		try {
			return host.split(":")[0];
		} catch (Exception e) {
			return "";
		}
	}

	public int getPort() {
		try {
			return Integer.decode(getFirst(Http.HOST).split(":")[1]);
		} catch (Exception e) {
			return -1;
		}
	}

	public long getMaxAge() {
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

	public URI getLocation() {
		try {
			return new URI(getFirst(Http.LOCATION));
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
	public static OutputStream writeMessage(OutputStream out, AbsHttpMessage msg, final int maxChunkSize)
		throws IOException
	{
		writeMessageHeaders(out, msg.getStartLine(), msg, Http.CHUNKED, null);
		
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

	public static void writeMessage(OutputStream out, AbsHttpMessage msg, byte [] entity)
		throws IOException
	{
		String contLen = null;
		if ((entity != null) && (entity.length > 0)) {
			contLen = String.format("%x", entity.length);
		}
		writeMessageHeaders(out, msg.getStartLine(), msg, null, contLen);
		if ((entity != null) && (entity.length > 0)) {
			out.write(entity);
		}
	}

	public static void writeMessage(OutputStream raw, AbsHttpMessage msg, InputStream in)
		throws IOException
	{
		OutputStream out = writeMessage(raw, msg, 256);
		int b = in.read();
		while (b >= 0) {
			out.write(b);
			b = in.read();
		}
	}

	public static void writeMessage(OutputStream raw, AbsHttpMessage msg, File f)
		throws IOException
	{
		FileInputStream in = new FileInputStream(f);
		writeMessage(raw, msg, in);
		in.close();
	}

	private static void writeMessageHeaders(OutputStream out, String startLine, AbsHttpMessage msg, String transEnc, String contLen)
		throws IOException
	{
		out.write((startLine + "\r\n").getBytes());

		for (String key: msg.keySet()) {
			if (Http.TRANSFER_ENCODING.equals(key)) continue;
			if (Http.CONTENT_LENGTH.equals(key)) continue;
			String values = join(msg.get(key), ",");
			out.write((key + ":" + values + "\r\n").getBytes());
		}

		if (transEnc != null) {
			out.write((Http.TRANSFER_ENCODING + ":" + transEnc + Http.CRLF).getBytes());
		}
		if (contLen != null) {
			out.write((Http.CONTENT_LENGTH + ":" + contLen + Http.CRLF).getBytes());
		}

		out.write("\r\n".getBytes());
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

	private static String join(List<String> texts, String delim) {
		if ((texts == null) || (texts.size() <= 0)) return "";

		String out = texts.get(0);
		for (int ii = 1; ii < texts.size(); ii++) {
			out += delim + texts.get(ii);
		}

		return out;
	}
}
