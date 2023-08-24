# spring示例
spring示例代码，源于真实项目。

### 
1. 导入jar到本地maven库
   * install:install-file -Dfile=D:\lib\test.jar -DgroupId=com.test -DartifactId=test -Dversion=1.0 -Dpackaging=Jar
2. 运行jar包
   * nohup java -jar test.jar &

### 目录
1. spring-boot-example
   * 基于Spring Boot 1.5.22.RELEASE，用于新建项目。
2. spring-util-example
   * spring常用工具类集合。 
3. spring-jpa-example
   * spring jpa增删改查、Specification/Example/@query/OneToMany动态查询、自定义分页、排序等。
4. spring-form-example
   * jquery、vue、Ajax、axios（axios不支持IE）实现的动态表单示例。
5. spring-file-example
   * word、excel、xml、pdf、ofd等各类文件操作、大文件分片上传、引入本地jar包示例。
6. spring-http-example
   * restTemplate、httpClient、HttpUrlConnection、ssl示例。
7. spring-encrypt-example
   * md5、rsa、pki、sm3、sm4加密解密、tenderFile示例。
8. spring-ca-example
   * 河北ca登录、验签、加密、解密、IE电子签章、chorme电子签章示例。
9. spring-websocket-example
   * 基于Stomp实现，与h5方式相比，在ie等低版本浏览器中能够自动降级为心跳轮询，具有更好兼容性、稳定性。
   
spring cloud example
* 基于spring boot1，由服务注册EurekaServer、统一网关Gateway、用户中心UserServer、业务服务BizServer四部分组成，实现：
用户注册、Oauth2统一认证、自定义短信验证码登录、Ribbon服务调用等。

spring cloud2 example
* 基于spring boot2