package com.tomhat.web.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

public class TestReqServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// String sURL = "http://127.0.0.1:8080/testhttps/interface";
		String sURL = "https://127.0.0.1:8443/testhttps/interface";
		String xml = "<?xml version='1.0' encoding='UTF-8'?><ecity><msgname>loginReq</msgname><timestamp>20140422094111</timestamp><svccont><token>112255</token></svccont></ecity>";
		toHTTPS(sURL, xml);
	}

	/**
	 * https调用 <功能详细描述>
	 * 
	 * @param sURL
	 *            请求URL
	 * @param xml
	 *            请求报文
	 * @return
	 * @throws IOException
	 * @see [类、类#方法、类#成员]
	 */
	public String toHTTPS(String sURL, String xml) throws IOException {
		sURL = "https://127.0.0.1:8443/testhttps/interface";

		System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\hsinghsu\\Desktop\\https\\client.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		System.setProperty("javax.net.ssl.keyStore", "C:\\Users\\hsinghsu\\Desktop\\https\\client.p12");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		// 注：去掉以上5行则表示使用http协议访问

		String resultXML = "";
		HttpClient httpClient = null;
		PostMethod postMethod = null;
		try {
			httpClient = new HttpClient();
			// 设置超时时间
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(30000);
			postMethod = new PostMethod(sURL);
			postMethod.setRequestEntity(new StringRequestEntity(xml, "text/html", "UTF-8"));

			postMethod.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
			postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			int statusCode = httpClient.executeMethod(postMethod);
			if (statusCode != HttpStatus.SC_OK) {
				System.out.println("Call method failed: " + postMethod.getStatusLine());
			}
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(postMethod.getResponseBodyAsStream(), "utf-8"));
				SAXReader saxReader = new SAXReader();
				Document doc = saxReader.read(new InputStreamReader(postMethod.getResponseBodyAsStream(), "utf-8"));
				resultXML = doc.asXML();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}
		return resultXML;
	}

}