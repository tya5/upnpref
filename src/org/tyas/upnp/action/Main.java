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
		InputStream client = new ActionMessage(new UpnpServiceType("Browser", "1"), "Browse")
			.add("in1", "222")
			.add("in2", "123")
			.sendByHttpRequest("localhost", 8080, "/");

		System.out.println("Server: accept");
		new HttpServer() {
			@Override protected boolean handleHttpRequest(HttpRequest.Input req, HttpServer.Context ctx) {
				try {
					System.out.println("Server: accept action request");
					ActionMessage.Const reqact = ActionMessage.readDocument(req.getInputStream());
					System.out.println("Server: accept " + req.getFirst(ActionMessage.SOAPACTION));

					System.out.println("Server: send action response");
					new ActionMessage(reqact.getServiceType(), reqact.getActionName() + "Response")
						.add("out1", "hello")
						.add("out2", "world")
						.sendByHttpResponse(ctx.getClient().getOutputStream());

					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}.accept(server).run();

		System.out.println("Client: accept action response");
		HttpMessage.Input inp = HttpMessage.readMessage(client);
		HttpResponse.Input respinp = HttpResponse.getByInput(inp);
		ActionMessage.Const resp = ActionMessage.readDocument(inp.getInputStream());
	}
}