package org.tyas.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class Http
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

	public interface Headers
	{
		List<String> get(String name);
		String getFirst(String name);
		Set<String> keySet();
		boolean isKeepAlive();
		boolean isChunkedEncoding();
		long getContentLength();
		String getHost()
	}

	public interface Message
	{
		String getStartLine();
		Headers getHeaders();
	}

	public interface InputMessage extends Message
	{
		/**
		 * Get InputStream for entity body
		 */
		InputStream getInputStream();
	}

	public interface Request extends Message
	{
		String getVersion();
		String getMethod();
		String getRequestUri();
	}

	public interface Response extends Message
	{
		String getVersion();
		String getStatusCode();
		String getReasonPhrase();
	}

	public interface InputRequest extends InputMessage, Request {}

	public interface InputResponse extends InputMessage, Response {}

	/**
	 * Make entity body stream for input.
	 *
	 * @param in InputStream for general message
	 * @return general message
	 */
	public static InputMessage readMessage(InputStream in) throws IOException {
		final HttpHeaders headers = new HttpHeaders();
		final String startLine = readLine(in).trim();
		String line;

		line = readLine(in);
		while (line.length() > 0) {
			int mid = line.indexOf(':');
			if (mid < 0) break;
			String name = line.substring(0, mid).trim().toUpperCase();
			String [] vals = line.substring(mid + 1).split(" ", 0);
			for (String val : vals) headers.put(name, val);
			line = readLine(in);
		}

		if (headers.isChunkedEncoding()) {
			in = new FilterInputStream(in) {
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
			final int contentSize = headers.getContentLength();

			in = (contentSize <= 0) ? null: new FilterInputStream(in) {
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

		final InputStream body = in;

		return new InputMessage() {
			@Override public String getStartLine() { return startLine; }
			@Override public Headers getHeaders() { return headers; }
			@Override public InputStream getInputStream() { return body; }
		};
	}

	/**
	 * Make entity body stream for output.
	 *
	 * @param msg message header
	 * @param out OutputStream for general message
	 * @return OutputStream for entity body
	 */
	public static OutputStream writeMessage(OutputStream out, Message msg, final int maxChunkSize) throws IOException {
		HttpHeaders headers = new HttpHeaders(msg.getHeaders());
		headers.putFirst(Http.TRANSFER_ENCODING, "chunked");
		writeMessageHeader(out, msg.getStartLine(), headers);
		
		return new FilterOutputStream(out) {
			int ofs = 0;
			@Override public void write(int b) throws IOException {
				if (ofs == 0) {
					out.write(String.format("%x\r\n", maxChunkSize).getBytes("UTF8"));
				}
				out.write(b);
				ofs++;
				if (ofs >= maxChunkSize) {
					out.write("\r\n".getBytes("UTF8"));
					ofs = 0;
				}
			}
		};
	}

	public static void writeMessage(OutputStream out, Message msg, byte [] entity) throws IOException {
		HttpHeaders headers = new HttpHeaders(msg.getHeaders());
		headers.remove(Http.TRANSFER_ENCODING);
		headers.remove(Http.CONTENT_LENGTH);
		if ((entity != null) && (entity.length > 0)) {
			headers.put(Http.CONTENT_LENGTH, String.format("%x", entity.length));
		}
		writeMessageHeader(out, msg.getStartLine(), headers);
		if ((entity != null) && (entity.length > 0)) {
			out.write(entity);
		}
	}

	public static void writeMessage(OutputStream raw, Message msg, InputStream in) throws IOException {
		OutputStream out = writeMessage(raw, msg, 256);
		int b = in.read();
		while (b >= 0) {
			out.write(b);
			b = in.read();
		}
	}

	public static void writeMessage(OutputStream raw, Message msg, File f) throws IOException {
		FileInputStream in = new FileInputStream(f);
		writeMessage(raw, msg, in);
		in.close();
	}

	private static void writeMessageHeader(OutputStream out, String startLine, Headers headers) throws IOException {
		out.write((startLine + "\r\n").getBytes("UTF8"));

		for (String key: headers.keySet()) {
			String values = "";
			for (String val: headers.get(key)) values += " " + val;
			out.write((key + ":" + values + "\r\n").getBytes("UTF8"));
		}
		out.write("\r\n".getBytes("UTF8"));
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
