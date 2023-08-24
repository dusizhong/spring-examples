package com.dusizhong.examples.http;

import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpURLConnectionController {

    public HttpURLConnectionController() throws IOException {

        String url1 = "https://www.ebaoch.com/ebaoch-res/templates/ffe06d47-4d5f-43cc-aaad-cc3226842ea2.pdf";
        URL url2 = new URL(url1);
        HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
        conn.setDoInput(true);
        conn.connect();
        InputStream is = conn.getInputStream();

        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        byte[] data = null;
        // 读取图片字节数组
        try {
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = is.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            data = swapStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(new BASE64Encoder().encode(data).replace("\n", "").replace("\r", "").length());
        String pdf = new BASE64Encoder().encode(data);
        System.out.println(new BASE64Encoder().encode(data));
        Map<String, Object> map = new HashMap<>();
        map.put("success", "success");
        map.put("companyCN", "投标人32");
        map.put("projectCode", "HBCT2019355001002");
        map.put("guaranteeFileData", pdf);


        try {
            // 1. 获取访问地址URL
            URL url = new URL("http://110.249.217.78:6005/EpointWebService/rest/uploaddzbhfile");
            // 2. 创建HttpURLConnection对象
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            /* 3. 设置请求参数等 */
            // 请求方式
            connection.setRequestMethod("POST");
            // 超时时间
            connection.setConnectTimeout(3000);
            // 设置是否输出
            connection.setDoOutput(true);
            // 设置是否读入
            connection.setDoInput(true);
            // 设置是否使用缓存
            connection.setUseCaches(false);
            // 设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向
            connection.setInstanceFollowRedirects(true);
            // 设置使用标准编码格式编码参数的名-值对
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 连接
            connection.connect();
            /* 4. 处理输入输出 */
            // 写入参数到请求中
            String params = "success=success&companyCN=投标人32&projectCode=HBCT2019355001002&guaranteeFileData=" + pdf;
            OutputStream out = connection.getOutputStream();
            out.write(params.getBytes());
            out.flush();
            out.close();
            // 从连接中读取响应信息
            String msg = "";
            int code = connection.getResponseCode();
            if (code == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    msg += line + "\n";
                }
                reader.close();
            }
            // 5. 断开连接
            connection.disconnect();

            // 处理结果
            System.out.println(msg);
        } catch (
                MalformedURLException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}
