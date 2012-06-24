package org.tyas.http;

import java.io.File;
import java.io.OutputStream;
import java.io.FileInputStream;

public class MyHttpServer
{
	public static void main(String [] args) {
		try {
			HttpServer server = new HttpServer(8081) {
					@Override public boolean handleHttpRequest(Http.InputRequest req, HttpServer.Context ctx) {
						try {
							System.out.println("RequestUri: " + req.getRequestUri());

							HttpHeaders headers = new HttpHeaders();

							new HttpResponse(Http.VERSION_1_1, "200", "OK", headers)
								.send(ctx.getClient().getOutputStream(), new File(req.getRequestUri()));

							return true;
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}
				};

			HttpServer.Context ctx = server.acceptHttpRequest();
			ctx.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
