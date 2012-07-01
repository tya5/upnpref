package org.tyas.upnp.action;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Main
{
	public static void main(String [] args) 
	{
		ServerSocket server = new ServerSocket(8080);

		Document req = new ActionRequest(new UpnpServiceType("Browser", "1"), "Browse")
			.add("arg1", "222")
			.add("arg2", "123")
			.toDocument();

		Socket client = new Socket("localhost", 8080);

		OutputStream out = new HttpRequest()
			.send(sock.OutputStream());

		TransformerFactory
			.newInstance()
			.newTransformer()
			.transform(new DOMSource(doc), new StreamResult(req));

		new HttpServer() {
		}.accept(server).run();

		HttpMessage.Input inp = HttpMessage.readMessage(client.getInputStream());
		HttpResponse.Input respinp = HttpResponse.getByInput(inp);
		Document resp = DocumentBuilderFactory
			.newInstance()
			.newDocumentBuilder()
			.parse(resp.getInputStream());
		ActionResponse.Const aresp = ActionResponse.getByDocument(resp);
	}
}