package org.tyas.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class HttpServer
{
	public interface RequestHandler
	{
		boolean handleHttpRequest(Http.InputRequest req, Context ctx);
	}

	public interface Context extends Runnable
	{
		ServerSocket getServer();
		Socket getClient();
	}

	private final RequestHandler mDefaultHandler = new RequestHandler() {
			@Override public boolean handleHttpRequest(Http.InputRequest req, Context ctx) {
				HttpServer.this.handleHttpRequest(req, ctx);
				return false;
			}
		};

	public Context accept(final ServerSocket server, final RequestHandler handler) throws IOException {
		final Socket sock = server.accept();
		
		return new Context() {
			@Override public ServerSocket getServer() {
				return server;
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

	public Context accept(ServerSocket server) throws IOException {
		return accept(server, mDefaultHandler);
	}

	protected boolean handleHttpRequest(Http.InputRequest req, Context ctx) {
		return false;
	}
}
