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

		HttpServer server = new HttpServer<EventMessageHeader>(EventMessageHeader.FACTORY) {
			@Override public boolean handleHttpRequest(HttpRequest.Input<EventMessageHeader> e, HttpServer.Context ctx) {
				try {
					System.out.println("EventKey["+e.getMessage().getEventKey()+"]");

					EventMessage em = EventMessage.getByInput(e);
					
					for (String name: em.getVariableNameSet()) {
						System.out.println(name + "=" + em.getProperty(name));
					}

					HttpResponse.RESPONSE_OK.send(ctx.getClient().getOutputStream(), (byte[])null);

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
