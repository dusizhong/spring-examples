package com.dusizhong.examples.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestTemplateController {

    @RequestMapping("/test2")
    public String test() {

        // RestTemplate默认不能返回400、500消息，使用catch捕获400、500消息，但中文乱码
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.set("client_id", clientId);
//        params.set("client_secret", clientSecret);
//        params.set("grant_type", "authorization_code");
//        params.set("code", code);
//        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, httpHeaders);
//        try {
//            JSONObject jsonObject = restTemplate.postForObject(baseUrl + "/o/oauth2/token", httpEntity, JSONObject.class);
//            if(ObjectUtils.isEmpty(jsonObject)) throw new UsernameNotFoundException("获取国资委token失败");
//            log.info("request gzw token success: " + jsonObject.getString("access_token"));
//        } catch (Exception e) {
//            // 处理 4xx / 5xx 错误
//            if (e instanceof HttpClientErrorException) {
//                System.out.println("client error:" + ((HttpClientErrorException)e).getResponseBodyAsString()); //这个方式中文乱码
//            } else if (e instanceof HttpServerErrorException) {
//                System.out.println("server error:" + ((HttpServerErrorException)e).getResponseBodyAsString());
//            }
//            // 或者
//            // HttpClientErrorException 和 HttpServerErrorException 都是 HttpStatusCodeException 的子类
//            if (e instanceof HttpStatusCodeException) {
//                HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) e;
//                // log.error("请求失败[{}]，响应结果[{}]", httpStatusCodeException.getStatusCode().value(),
//                //        httpStatusCodeException.getResponseBodyAsString());
//            }
//        }

        return "success";
    }
}
