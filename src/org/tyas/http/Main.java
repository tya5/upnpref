package org.tyas.http;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

public class Main
{
	public static void main(String [] args) {
		final HttpRequestLine REQUEST_GET = new HttpRequestLine("GET", "/", HttpMessage.VERSION_1_1);

		try {
			InetAddress local = InetAddress.getLocalHost();

			System.out.println("Server: start server");
			ServerSocket server = new ServerSocket(8080, 1, local);

			System.out.println("Client: connect and request to server");
			Socket client = new Socket(local, 8080);
			HttpRequest.Builder builder = new HttpRequest.Builder();
			builder.setStartLine(REQUEST_GET);
			builder.putHost(local.getHostAddress(), 8080);
			builder.build().send(client.getOutputStream(), "Hello Client!".getBytes());
			builder = null;

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
						
						HttpResponse.Builder builder = new HttpResponse.Builder();
						builder.setStartLine(HttpStatusLine.DEFAULT_200_OK);
						builder.build().send(ctx.getClient().getOutputStream(), data.getBytes());
						builder = null;

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
