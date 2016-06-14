package com.tomhat.test;

import java.util.HashMap;
import java.util.Map;

import com.tomhat.util.HttpClientUtil;

//对接口进行测试  
public class TestMain {  
    private String url = "https://vpn.redcross.org.cn/showLogon.do";  
    private String charset = "utf-8";  
    private HttpClientUtil httpClientUtil = null;  
      
    public TestMain(){  
        httpClientUtil = new HttpClientUtil();  
    }  
      
    public void test(){  
        String httpOrgCreateTest = url ;//+ "httpOrg/create";  
        Map<String,String> createMap = new HashMap<String,String>();  
        createMap.put("authuser","qqqqqqqqqqqqqqq");  
        createMap.put("authpass","wwwwwwwwwwwww");  
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