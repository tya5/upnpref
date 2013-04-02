package org.tyas.upnp.gena;

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
		
		// EventServer prepares for Unicast Event Message
		ServerSocket serversock = new ServerSocket(callbackUrl.getPort());
		
		// Client send SubscribeRequest
		Socket sock = new Socket(eventUrl.getHost(), eventUrl.getPort());
		
		GenaSubscribeRequest
			.getSubscribeRequest(eventUrl.getHost(), eventUrl.getPort(), eventUrl.getPath(), callbackUrl.toString(), 5)
			.send(sock.getOutputStream(), (byte[])null);
		
		// Client recv SubscribeResponse
		GenaSubscribeResponse sresp = GenaSubscribeResponse.read(sock.getInputStream());
		final GenaSubscribeId sid = sresp.getSid();
		
		System.out.println("SID=" + sid.toString());
		
		// EventServer accepts Unicast Event Message
		HttpServer server = new HttpServer<GenaEventMessage>(GenaEventMessage.FACTORY) {
			@Override public boolean handleHttpRequest(HttpRequest.Input<GenaEventMessage> ev, HttpServer.Context ctx) {
				try {
					System.out.println("EventKey["+ev.getMessage().getEventKey()+"]");
					
					GenaPropertySet props = GenaPropertySet.read(ev.getInputStream());
					
					for (String name: props.keySet()) {
						System.out.println(name + "=" + props.get(name));
					}
					
					HttpResponse.DEFAULT_200_OK.send(ctx.getClient().getOutputStream(), (byte[])null);
					
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
