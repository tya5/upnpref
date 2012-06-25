package org.tyas.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class HttpServer extends ServerSocket
{
	public interface RequestHandler
	{
		boolean handleHttpRequest(Http.InputRequest req, Context ctx);
	}

	public interface Context extends Runnable
	{
		HttpServer getServer();
		Socket getClient();
	}

	private final RequestHandler mDefaultHandler = new RequestHandler() {
			@Override public boolean handleHttpRequest(Http.InputRequest req, Context ctx) {
				HttpServer.this.handleHttpRequest(req, ctx);
				return false;
			}
		};

	public HttpServer() throws IOException {
		super();
	}

	public HttpServer(int port) throws IOException {
		super(port);
	}

	public HttpServer(int port, int backlog) throws IOException {
		super(port, backlog);
	}

	public HttpServer(int port, int backlog, InetAddress localAddress) throws IOException {
		super(port, backlog, localAddress);
	}

	public Context acceptHttpRequest(final RequestHandler handler) throws IOException {
		final Socket sock = accept();
		
		return new Context() {
			@Override public HttpServer getServer() {
				return HttpServer.this;
			}
			@Override public Socket getClient() {
				return sock;
			}
			@Override public void run() {
				InputStream in;
				Http.InputRequest req;
				
				try {
					in = sock.getInputStream();
					
					do {
						req = HttpRequest.parse(in);
						if (! handler.handleHttpRequest(req, this)) {
							;
						}
					} while (req.getRequest().isKeepAlive());
					
				} catch (IOException e) {
					e.printStackTrace();

				} finally {
					try {
						sock.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	public Context acceptHttpRequest() throws IOException {
		return acceptHttpRequest(mDefaultHandler);
	}

	public boolean handleHttpRequest(Http.InputRequest req, Context ctx) {
		return false;
	}
}
