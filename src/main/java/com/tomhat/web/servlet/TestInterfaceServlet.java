package com.tomhat.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class TestInterfaceServlet extends HttpServlet {

	/**
	 * 对外接口主入口 用于获取用户请求，返回用户请求使用。
	 */
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httprequest = (HttpServletRequest) request;
		httpResponse.setContentType("text/html;charset=UTF-8");

		Map<String, String> mapHeader = getHeaderMap(httprequest);// 获取http头信息

		String requestXml = "";// 请求过来的xml报文
		String responseXml = "";// 返回的xml报文
		Document doc = null;

		InputStream is = request.getInputStream();
		System.out.println("======http header timestamp is ：" + mapHeader.get("timestamp"));

		SAXReader saxReader = new SAXReader();
		Map<String, String> map = new HashMap<String, String>();

		try {
			doc = saxReader.read(is);
			requestXml = doc.asXML();
			System.out.println("======request xml ：" + requestXml);
			Element rootElm = doc.getRootElement();// 从root根节点获取请求报文
			map = parseXML(rootElm, new HashMap<String, String>());

			String msgname = map.get("msgname");// 从http body中获取到的msgname值

			// 是否https访问
			if (request.isSecure()) {
				// https访问
				if ("loginReq".equals(msgname)) {
					System.out.println("the https+xml request is loginReq");
					responseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ecity><msgname>loginRep</msgname><msgversion>1.0.0</msgversion><result><rspcode>0</rspcode><rspdesc>成功</rspdesc></result><svccont><uid>111</uid></svccont></ecity>";
				} else {
					System.out.println("请求接口名称不在本系统内");
					responseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ecity><msgversion>1.0.0</msgversion><result><rspcode>1</rspcode><rspdesc>请求接口名称不在本系统内</rspdesc></result></ecity>";
				}
			} else {
				// http访问
				if ("logOutReq".equals(msgname)) {
					System.out.println("the http+xml request is logOut");
					responseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ecity><msgname>logOutRep</msgname><msgversion>1.0.0</msgversion><result><rspcode>0</rspcode><rspdesc>成功</rspdesc></result><svccont><uid>111</uid></svccont></ecity>";
				} else {
					System.out.println("请求接口名称不在本系统内");
					responseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ecity><msgversion>1.0.0</msgversion><result><rspcode>1</rspcode><rspdesc>请求接口名称不在本系统内</rspdesc></result></ecity>";
				}
			}

		} catch (Exception e1) {
			System.out.println("请求数据不正确");
			responseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ecity><msgversion>1.0.0</msgversion><result><rspcode>2</rspcode><rspdesc>请求数据不正确</rspdesc></result></ecity>";

		}

		// 对报文进行压缩处理
		String AcceptEncoding = "gzip";
		if (mapHeader != null)
			AcceptEncoding = mapHeader.get("Accept-Encoding");
		if (null != AcceptEncoding && "gzip".equals(AcceptEncoding)) {
			// 使用gzip压缩报文
			System.out.println("======output response xml with gzip is : " + responseXml);
			response.getOutputStream().write(gZip(responseXml.getBytes()));
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} else {
			// 不压缩报文，直接传输
			System.out.println("======output response xml without gzip is : " + responseXml);
			response.getOutputStream().write(responseXml.getBytes());
			response.getOutputStream().flush();
			response.getOutputStream().close();
		}

	}

	/**
	 * 获取http头信息 <功能详细描述>
	 * 
	 * @param request
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public Map<String, String> getHeaderMap(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != request.getHeader("msgname") && !"".equals(request.getHeader("msgname")))
			map.put("msgname", request.getHeader("msgname"));
		if (null != request.getHeader("Accept-Encoding") && !"".equals(request.getHeader("Accept-Encoding")))
			map.put("Accept-Encoding", request.getHeader("Accept-Encoding"));
		if (null != request.getHeader("timestamp") && !"".equals("timestamp"))
			map.put("timestamp", request.getHeader("timestamp"));
		return map;
	}

	/**
	 * 将xml解析成map键值对 <功能详细描述>
	 * 
	 * @param ele
	 *            需要解析的xml对象
	 * @param map
	 *            入参为空，用于内部迭代循环使用
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	private Map<String, String> parseXML(Element ele, Map<String, String> map) {
		for (Iterator<?> i = ele.elementIterator(); i.hasNext();) {
			Element node = (Element) i.next();
			System.out.println("======parseXML node name:" + node.getName());
			if (node.attributes() != null && node.attributes().size() > 0) {
				for (Iterator<?> j = node.attributeIterator(); j.hasNext();) {
					Attribute item = (Attribute) j.next();
					System.out.println("======parseXML property name：" + item.getName() + " property value：" + item.getValue() + "\n");
					map.put(item.getName(), item.getValue());
				}
			}
			if (node.getText().length() > 0) {
				System.out.println("======parseXML node value：" + node.getText());
				map.put(node.getName(), node.getText());
			}
			if (node.elementIterator().hasNext()) {
				parseXML(node, map);
			}
		}
		return map;
	}

	/**
	 * gZip压缩方法 将原报文通过gzip压缩
	 * 
	 * @param data
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static byte[] gZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			gzip.write(data);
			gzip.finish();
			gzip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

}