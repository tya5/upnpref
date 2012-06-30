package org.tyas.http;

import java.io.File;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.net.ServerSocket;

public class Main
{
	public static void main(String [] args) {
		try {
			ServerSocket sock = new ServerSocket(8080);

			new HttpServer() {
				@Override protected boolean handleHttpRequest(HttpRequest.Input req, HttpServer.Context ctx) {
					try {
						System.out.println("RequestUri: " + req.getRequestUri());

						new HttpResponse(Http.VERSION_1_1, "200", "OK")
							.send(ctx.getClient().getOutputStream(), new File(req.getRequestUri()));

						return true;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
			}.accept(sock).run();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
