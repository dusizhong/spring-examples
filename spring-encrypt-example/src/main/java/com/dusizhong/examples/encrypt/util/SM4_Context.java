package com.dusizhong.examples.encrypt.util;

public class SM4_Context
{
	/** 控制加密还是解密：1加密，0解密 */
	public int mode;
	/** 轮密钥 SM4 subkeys */
	public long[] sk;
	public boolean isPadding;

	public SM4_Context() 
	{
		this.mode = 1;
		this.sk = new long[32];//SM4为32轮加密变换
		this.isPadding = true;
	}
}
