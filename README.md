# SSLHttpClient
httpClient post

```xml
	  <dependency>
		<groupId>org.apache.httpcomponents</groupId>
		<artifactId>httpclient</artifactId>
		<version>4.5.2</version>
	  </dependency>
	  <dependency>
		<groupId>commons-io</groupId>
		<artifactId>commons-io</artifactId>
		<version>2.4</version>
	  </dependency>
```
由于请求的URL是HTTPS的，为了避免需要证书，所以用一个类继承DefaultHttpClient类，忽略校验过程。
## SSLClient类，继承至HttpClient
```java
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
//用于进行Https请求的HttpClient
public class SSLClient extends DefaultHttpClient{
    public SSLClient() throws Exception{  
        super();  
        SSLContext ctx = SSLContext.getInstance("TLS");  
        X509TrustManager tm = new X509TrustManager() {  
                @Override  
                public void checkClientTrusted(X509Certificate[] chain,  
                        String authType) throws CertificateException {  
                }  
                @Override  
                public void checkServerTrusted(X509Certificate[] chain,  
                        String authType) throws CertificateException {  
                }  
                @Override  
                public X509Certificate[] getAcceptedIssuers() {  
                    return null;  
                }  
        };  
        ctx.init(null, new TrustManager[]{tm}, null);  
        SSLSocketFactory ssf = new SSLSocketFactory(ctx,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
        ClientConnectionManager ccm = this.getConnectionManager();  
        SchemeRegistry sr = ccm.getSchemeRegistry();  
        sr.register(new Scheme("https", 443, ssf));  
    }  
} 
```
## HttpClient发送post请求的类
```java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
/* 
 * 利用HttpClient进行post请求的工具类 
 */
public class HttpClientUtil {
    public String doPost(String url,Map<String,String> map,String charset){  
        HttpClient httpClient = null;  
        HttpPost httpPost = null;  
        String result = null;  
        try{  
            httpClient = new SSLClient();  
            httpPost = new HttpPost(url);  
            //设置参数  
            List<NameValuePair> list = new ArrayList<NameValuePair>();  
            Iterator iterator = map.entrySet().iterator();  
            while(iterator.hasNext()){  
                Entry<String,String> elem = (Entry<String, String>) iterator.next();  
                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));  
            }  
            if(list.size() > 0){  
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);  
                httpPost.setEntity(entity);  
            }  
            HttpResponse response = httpClient.execute(httpPost);  
            if(response != null){  
                HttpEntity resEntity = response.getEntity();  
                if(resEntity != null){  
                    result = EntityUtils.toString(resEntity,charset);  
                }  
            }  
        }catch(Exception ex){  
            ex.printStackTrace();  
        }  
        return result;  
    }  
} 
```
## 调用post请求的测试代码
```java
    import java.util.HashMap;  
    import java.util.Map;  
    //对接口进行测试  
    public class TestMain {  
        private String url = "https://192.168.1.101/";  
        private String charset = "utf-8";  
        private HttpClientUtil httpClientUtil = null;  
          
        public TestMain(){  
            httpClientUtil = new HttpClientUtil();  
        }  
          
        public void test(){  
            String httpOrgCreateTest = url + "httpOrg/create";  
            Map<String,String> createMap = new HashMap<String,String>();  
            createMap.put("authuser","*****");  
            createMap.put("authpass","*****");  
            createMap.put("orgkey","****");  
            createMap.put("orgname","****");  
            String httpOrgCreateTestRtn = httpClientUtil.doPost(httpOrgCreateTest,createMap,charset);  
            System.out.println("result:"+httpOrgCreateTestRtn);  
        }  
          
        public static void main(String[] args){  
            TestMain main = new TestMain();  
            main.test();  
        }  
    }  
```

## SSL单向验证
```xml
<dependency>
	<groupId>commons-httpclient</groupId>
	<artifactId>commons-httpclient</artifactId>
	<version>3.1</version>
</dependency>
```
```java
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientSendData {
    static Log log = LogFactory.getLog(ClientSendData.class);  

    private String Url;  

    // 初始化数据  
    public ClientSendData() {  
        Url = "https://test.yihaodian.com:8443/ims/feedbackToPingAn_getData.action";  
    }  

    public String sendData(String data) {  
        String receivedData = null;  
        try {  

            Map<String, String> paramsData = new HashMap<String, String>();  
            paramsData.put("data", data);  
            receivedData = send(Url, paramsData);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return receivedData;  
    }  

    public static String send(String url, Map<String, String> paramsMap) {  
        String result = null;  
        PostMethod postMethod = null;  
        HttpClient httpClient = new HttpClient();  

        httpClient.getParams().setParameter(  
                HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");  
        postMethod = new PostMethod(url);  

        if (paramsMap != null && paramsMap.size() > 0) {  
            NameValuePair[] datas = new NameValuePair[paramsMap.size()];  
            int index = 0;  
            for (String key : paramsMap.keySet()) {  
                datas[index++] = new NameValuePair(key, paramsMap.get(key));  
            }  
            postMethod.setRequestBody(datas);  

        }  

        HttpClientParams httparams = new HttpClientParams();  
        httparams.setSoTimeout(60000);  
        postMethod.setParams(httparams);  

        try {  
            int statusCode = httpClient.executeMethod(postMethod);  
            if (statusCode == HttpStatus.SC_OK) {  
                result = postMethod.getResponseBodyAsString();  
                log.info("发送成功！");  
            } else {  
                log.error(" http response status is " + statusCode);  
            }  

        } catch (HttpException e) {  
            log.error("error url=" + url, e);  
        } catch (IOException e) {  
            log.error("error url=" + url, e);  
        } finally {  
            if (postMethod != null) {  
                postMethod.releaseConnection();  
            }  
        }  

        return result;  
    }  

    public static void main(String[] args) {  
        ClientSendData t = new ClientSendData();  
        t.sendData("测试SSL单项连接，向服务端发送数据!");  
    }  
} 
```
