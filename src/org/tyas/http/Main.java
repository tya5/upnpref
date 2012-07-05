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
			new HttpRequest.Builder("GET", "/", HttpMessage.VERSION_1_1)
				.send(client.getOutputStream(), "Hello Client!".getBytes());

			System.out.println("Server: accept request");
			new HttpServer() {
				@Override protected boolean handleHttpRequest(HttpRequest.Input req, HttpServer.Context ctx) {
					try {
						ByteArrayOutputStream ba = new ByteArrayOutputStream();
						int d;
						while ((d = req.getInputStream().read()) >= 0) ba.write(d);
						System.out.println("Server: accept text: " + ba.toString());

						System.out.println("Server: send response");

						String data = "Hello Server!";
						
						new HttpResponse.Builder(HttpMessage.VERSION_1_1, "200", "OK")
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
			HttpMessage.Input msg = HttpMessage.readMessage(client.getInputStream());
			HttpResponse.Input resp = HttpResponse.getByInput(msg);
			InputStream content = resp.getInputStream();
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			int data;

			while ((data = content.read()) >= 0) {
				b.write(data);
			}

			System.out.println("Client: data='" + b.toString() + "'");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
