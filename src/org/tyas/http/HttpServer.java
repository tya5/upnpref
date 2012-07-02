package org.tyas.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class HttpServer
{
	public interface RequestHandler
	{
		boolean handleHttpRequest(HttpRequest.Input req, Context ctx);
	}

	public interface Context extends Runnable
	{
		ServerSocket getServer();
		Socket getClient();
	}

	private RequestHandler mHandler = new RequestHandler() {
			@Override public boolean handleHttpRequest(HttpRequest.Input req, Context ctx) {
				HttpServer.this.handleHttpRequest(req, ctx);
				return false;
			}
		};

	public HttpServer() {
	}

	public HttpServer(RequestHandler handler) {
		mHandler = handler;
	}

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
				try {
					HttpRequest.Input req;
					InputStream in = sock.getInputStream();

					do {
						HttpMessage.Input msg = HttpMessage.readMessage(in);

						req = HttpRequest.getByInput(msg);

						if (req == null) break;

						if (handler == null) break;

						handler.handleHttpRequest(req, this);
						
					} while (req.isKeepAlive());
					
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
		return accept(server, mHandler);
	}

	protected boolean handleHttpRequest(HttpRequest.Input req, Context ctx) {
		return false;
	}
}
