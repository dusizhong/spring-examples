package com.dusizhong.examples.http;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class HttpClientController {

    /**
     * http服务访问https服务示例
     * @param title
     * @param content
     * @param publishDate
     * @param signTime
     * @param signature
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping("/zbgg")
    public String joint(@RequestParam(defaultValue = "") String title,
                        @RequestParam(defaultValue = "") String content,
                        @RequestParam(defaultValue = "") String publishDate,
                        @RequestParam(defaultValue = "") String signTime,
                        @RequestParam(defaultValue = "") String signature) throws UnsupportedEncodingException {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                .setSocketTimeout(5000).build();
        HttpPost httpPost = new HttpPost("https://hbzbjt.cn/hbzbjt/zbgg");
        httpPost.setConfig(config);
        //设置参数
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("title", title));
        list.add(new BasicNameValuePair("content", content));
        list.add(new BasicNameValuePair("publishDate", publishDate));
        list.add(new BasicNameValuePair("signTime", signTime));
        list.add(new BasicNameValuePair("signature", signature));
        if(list.size() > 0) {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
        }
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        try (CloseableHttpClient httpClient = httpClientBuilder.setSSLSocketFactory(getSslConnectionSocketFactory()).build()) {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private static SSLConnectionSocketFactory getSslConnectionSocketFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    }
}
