package org.tyas.http;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class HttpServer<R extends HttpRequest>
{
	public interface RequestHandler<R extends HttpRequest>
	{
		boolean handleHttpRequest(HttpMessage.Input<R> req, Context ctx);
	}

	public interface Context extends Runnable
	{
		ServerSocket getServer();
		Socket getClient();
	}

	private final HttpMessageFactory<HttpRequestLine,R> mFactory;

	private RequestHandler<R> mHandler = new RequestHandler<R>() {
			@Override public boolean handleHttpRequest(HttpMessage.Input<R> req, Context ctx) {
				HttpServer.this.handleHttpRequest(req, ctx);
				return false;
			}
		};

	public HttpServer(HttpMessageFactory<HttpRequestLine,R> factory) {
		mFactory = factory;
	}

	public HttpServer(HttpMessageFactory<HttpRequestLine,R> factory, RequestHandler<R> handler) {
		mFactory = factory;
		mHandler = handler;
	}

	public Context accept(final ServerSocket server, final RequestHandler<R> handler) throws IOException {
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
					HttpMessage.Input<R> req;
					InputStream in = sock.getInputStream();

					do {
						req = HttpMessage.readMessage(in, HttpRequestLine.PARSER, mFactory);
						
						if (req == null) break;
						
						if (handler == null) break;
						
						handler.handleHttpRequest(req, this);
						
					} while (req.getMessage().isKeepAlive());
					
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

	protected boolean handleHttpRequest(HttpMessage.Input<R> req, Context ctx) {
		return false;
	}
}
