package com.dusizhong.examples.user.util;

import com.alibaba.fastjson.JSONObject;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 图片验证码工具
 * @author Dusizhong
 * @since 2022-04-22
 */
@Component
public class CaptchaUtils {

    //图片验证码配置
    @Bean
    public DefaultKaptcha getDefaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        //边框
        properties.setProperty("kaptcha.border", "yes");
        //边框颜色
        properties.setProperty("kaptcha.border.color", "blue");
        //图片宽带
        properties.setProperty("kaptcha.image.width", "110");
        //图片高度
        properties.setProperty("kaptcha.image.height", "40");
        //字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        //字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        //字体
        properties.put("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        //验证码个数
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

    /**
     * 生成图片验证码
     * 返回验证码及BASE64图片
     * @return code img
     */
    public JSONObject createCaptcha() {
        //生成验证码字符串
        String code = getDefaultKaptcha().createText();
        //生成验证码图片
        BufferedImage bufferedImage = getDefaultKaptcha().createImage(code);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //转BASE64图片
        byte[] byteImg = byteArrayOutputStream.toByteArray();
        String base64Img = "data:image/png;base64," + Base64Utils.encodeToString(byteImg);
        //返回验证码
        JSONObject kaptcha = new JSONObject();
        kaptcha.put("code", code);
        kaptcha.put("pic", base64Img);
        return kaptcha;
    }
}
