package org.tyas.upnp.action;

import org.tyas.http.Http;
import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpResponse;
import org.tyas.http.HttpServer;
import org.tyas.upnp.UpnpServiceType;

import java.net.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Main
{
	public static void main(String [] args) {
		try {
			main1(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main1(String [] args) throws Exception
	{
		System.out.println("Server: start server");
		ServerSocket server = new ServerSocket(8080);

		System.out.println("Client: send action request");
		Document req = new ActionMessage(new UpnpServiceType("Browser", "1"), "Browse")
			.add("in1", "222")
			.add("in2", "123")
			.toDocument();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TransformerFactory
			.newInstance()
			.newTransformer()
			.transform(new DOMSource(req), new StreamResult(out));

		Socket client = new Socket("localhost", 8080);

		new HttpRequest("POST", "/", Http.VERSION_1_1)
			.send(client.getOutputStream(), out.toByteArray());

		System.out.println("Server: accept");
		new HttpServer() {
			@Override protected boolean handleHttpRequest(HttpRequest.Input req, HttpServer.Context ctx) {
				System.out.println("Server: accept action request: "+ req.getInputStream());
				try {
					/*
					ByteArrayOutputStream ba = new ByteArrayOutputStream();
					int d;
					while ((d = req.getInputStream().read()) >= 0) ba.write(d);
					System.out.println("Server: accept text: " + ba.toString());
					*/

					Document reqdoc = DocumentBuilderFactory
						.newInstance()
						.newDocumentBuilder()
						.parse(req.getInputStream());
					
					ActionMessage.Const reqact = ActionMessage.getByDocument(reqdoc);

					System.out.println("Server: send action response");
					Document respdoc = new ActionMessage(reqact.getServiceType(), reqact.getActionName() + "Response")
						.add("out1", "hello")
						.add("out2", "world")
						.toDocument();

					OutputStream out = new HttpResponse(Http.VERSION_1_1, "200", "OK")
						.send(ctx.getClient().getOutputStream());

					TransformerFactory
						.newInstance()
						.newTransformer()
						.transform(new DOMSource(respdoc), new StreamResult(out));

					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}.accept(server).run();

		System.out.println("Client: accept action response");
		HttpMessage.Input inp = HttpMessage.readMessage(client.getInputStream());
		HttpResponse.Input respinp = HttpResponse.getByInput(inp);
		Document resp = DocumentBuilderFactory
			.newInstance()
			.newDocumentBuilder()
			.parse(respinp.getInputStream());
		ActionMessage.Const aresp = ActionMessage.getByDocument(resp);
	}
}