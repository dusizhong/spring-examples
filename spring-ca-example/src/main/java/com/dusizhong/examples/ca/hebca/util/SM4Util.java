package com.dusizhong.examples.ca.hebca.util;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.nio.charset.StandardCharsets;

/**
 * SM4加密工具
 * 与C# SM4相对应，实现加解密（C#实现详见我的HebcaBidder WinForm程序）
 * @author dusizhong
 * @since 2023-08-08
 */
public class SM4Util {

	public static String encryptECB(String secretKey, String plainText)
	{
		try
		{
			SM4_Context ctx = new SM4_Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_ENCRYPT;
			SM4 sm4 = new SM4();
			sm4.sm4_setkey_enc(ctx, secretKey.getBytes(StandardCharsets.UTF_8));
			byte[] encrypted = sm4.sm4_crypt_ecb(ctx, plainText.getBytes(StandardCharsets.UTF_8));
//			String cipherText = new BASE64Encoder().encode(encrypted);
//			if (cipherText != null && cipherText.trim().length() > 0)
//			{
//				Pattern p = Pattern.compile("\\s*|\t|\r|\n");
//				Matcher m = p.matcher(cipherText);
//				cipherText = m.replaceAll("");
//			}
			String cipherText = ByteUtils.toHexString(encrypted);
			return cipherText;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] decryptECB(String secretKey, String cipherText)
	{
		try
		{
			SM4_Context ctx = new SM4_Context();
			ctx.isPadding = true;
			ctx.mode = SM4.SM4_DECRYPT;
			SM4 sm4 = new SM4();
			sm4.sm4_setkey_dec(ctx, secretKey.getBytes(StandardCharsets.UTF_8));
			//byte[] decrypted = sm4.sm4_crypt_ecb(ctx, new BASE64Decoder().decodeBuffer(cipherText));
			return sm4.sm4_crypt_ecb(ctx, ByteUtils.fromHexString(cipherText));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		String secretKey = "1234567890123456"; //16位
		String plainText = "{\n" +
				"\t\"Name\": \"张三\",\n" +
				"\t\"idCardNum\": \"XXXXXXX\",\n" +
				"\t\"nation\": \"汉\",\n" +
				"\t\"address\": \"xx省xx县\",\n" +
				"\t\"headImage\": \"data:image/jpeg;base64,xxxxxxxxxx\",\n" +
				"\t\"grantOrg\": \"xx县\",\n" +
				"\t\"startDate\": \"2019-01-30\",\n" +
				"\t\"expiryDate\": \"2029-01-30\",\n" +
				"}";
		String xmlText = "<tenderForm>\n" +
				"  <formField fieldId=\"1\" fieldName=\"报价\" fieldType=\"number\" fieldLength=\"10\" fieldNote=\"单位元\">100.898</formField>\n" +
				"  <formField fieldId=\"2\" fieldName=\"工期\" fieldType=\"text\" fieldLength=\"4\" fieldNote=\"日历天\">1900日历天</formField>\n" +
				"  <formField fieldId=\"3\" fieldName=\"质量要求\" fieldType=\"text\" fieldLength=\"100\" fieldNote=\"请填写质量说明\">阿斯蒂芬萨芬</formField>\n" +
				"</tenderForm>";
		String cipherText = encryptECB(secretKey, xmlText);
		System.out.println("加密结果：\n" + cipherText);
		System.out.println("解密结果：\n" + decryptECB(secretKey, cipherText));
	}
}
