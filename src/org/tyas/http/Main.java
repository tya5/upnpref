package org.tyas.http;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main
{
	public static void main(String [] args) {
		try {
			System.out.println("Server: start server");
			ServerSocket server = new ServerSocket(8080);

			System.out.println("Client: connect and request to server");
			Socket client = new Socket("localhost", 8080);
			new HttpMessage.Builder<HttpRequestLine>(new HttpRequestLine("GET", "/", HttpMessage.VERSION_1_1))
				.build(HttpRequest.FACTORY)
				.send(client.getOutputStream(), "Hello Client!".getBytes());

			System.out.println("Server: accept request");
			new HttpServer<HttpRequest>(HttpRequest.FACTORY) {
				@Override protected boolean handleHttpRequest(HttpMessage.Input<HttpRequest> req, HttpServer.Context ctx) {
					try {
						ByteArrayOutputStream ba = new ByteArrayOutputStream();
						int d;
						InputStream in = req.getInputStream();
						if (in != null) {
							while ((d = in.read()) >= 0) ba.write(d);
						}
						System.out.println("Server: accept text: " + ba.toString());

						System.out.println("Server: send response");

						String data = "Hello Server!";
						
						new HttpMessage.Builder<HttpStatusLine>(new HttpStatusLine(HttpMessage.VERSION_1_1, "200", "OK"))
							.build(HttpResponse.FACTORY)
							.send(ctx.getClient().getOutputStream(), data.getBytes());

						System.out.println("Server: data='" + data + "'");

						return true;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
			}.accept(server).run();
			
			System.out.println("Client: accept response");
			HttpMessage.Input<HttpResponse> resp = HttpResponse.readMessage(client.getInputStream());
			InputStream content = resp.getInputStream();
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			int data;

			if (content != null) {
				while ((data = content.read()) >= 0) {
					b.write(data);
				}
			}

			System.out.println("Client: data='" + b.toString() + "'");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
