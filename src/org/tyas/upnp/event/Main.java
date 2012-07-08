package org.tyas.upnp.event;

import org.tyas.http.*;
import java.net.*;

public class Main
{
	public static void main(String [] args) {
		try {
			listen(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listen(String [] args) throws Exception {
		URL eventUrl = null;
		URL callbackUrl = null;

		ServerSocket serversock = new ServerSocket(callbackUrl.getPort());
		Socket sock = new Socket(eventUrl.getHost(), eventUrl.getPort());

		SubscribeRequest
			.getSubscribeRequest(eventUrl.getPath(), callbackUrl.toString(), 5)
			.send(sock.getOutputStream(), (byte[])null);

		HttpMessage.Input msg = HttpMessage.readMessage(sock.getInputStream());
		HttpResponse.Input resp = HttpResponse.getByInput(msg);
		final SubscribeId sid = SubscribeId.getBySid(resp.getFirst("SID"));

		System.out.println("SID=" + sid.toString());

		HttpServer server = new HttpServer() {
				@Override public boolean handleHttpRequest(HttpRequest.Input req, HttpServer.Context ctx) {
					try {
						EventMessage e = EventMessage.getByHttpRequest(req, sid);

						if (e == null) return false;

						System.out.println("EventKey["+e.getEventKey()+"]");

						for (String name: e.getVariableNameSet()) {
							System.out.println(name + "=" + e.getProperty(name));
						}
					
						new HttpResponse.Builder(HttpMessage.VERSION_1_1, "200", "OK")
							.send(ctx.getClient().getOutputStream(), (byte[])null);

						return true;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
			};

		while (true) {
			server.accept(serversock).run();
		}
	}
}
