# SSLHttpClient
httpClient post

由于请求的URL是HTTPS的，为了避免需要证书，所以用一个类继承DefaultHttpClient类，忽略校验过程。
###SSLClient类，继承至HttpClient
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
###HttpClient发送post请求的类
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
###调用post请求的测试代码
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

