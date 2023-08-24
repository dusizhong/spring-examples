/**
 原文件数字信封不能用，不支持非IE浏览器，对此进行了修改。
 fixed by dusizhong at 2023-08-08
*/
var isIE = window.ActiveXObject !== undefined;
var CertMgr = null;
var recipientCerts = new Array();
var cert = null;
var signCert = null;
var cryptCert = null;
var util = null;
var pkcs7 = null;
//非IE浏览器下
var socket = null;
var host = "ws://localhost:17212/test";
window.onload = function(){
	if(isIE){// 创建WebSeal对象
		CertMgr = new HebcaP11Object();
	}else{
		connect();
	}
};

function HebcaP11Object(){
	var p11Ctrl = null;
    // 一个页面只创建一次
    if (null != CertMgr){
        return CertMgr;
    }
    if (isIE){
        // IE 浏览器创建插件
        try{
            var plugin_embed = document.createElement("OBJECT");
            plugin_embed.setAttribute("id", "npHebcaP11Plugin");
            plugin_embed.setAttribute("style", "width:1px;height:1px;");
            plugin_embed.setAttribute("classid", "CLSID:59B3BFD5-6CC5-4FFA-90E8-C1E5AFCB42E9");
            document.body.appendChild(plugin_embed);
	    			p11Ctrl = document.getElementById("npHebcaP11Plugin");

	    			npHebcaP11Plugin.Licence = "amViY56Xmp2cnpeanZyel5qdnJ6Xmp2cYWhlYoQsftgudcLw21NcfvO8eN13ICbS";

	    			util = npHebcaP11Plugin.Util;
	    			pkcs7 = npHebcaP11Plugin.CreatePkcs7();
        }
        catch(e){
            throw Error("没有安装客户端软件或IE阻止其运行.");
        }
    		CertMgr = p11Ctrl;
    }
    return p11Ctrl;
}

//连接WebSocket
function connect(){
	try{
		socket = new WebSocket(host);
		socket.onopen = function(){
			socket.send('0|'+  new Date().getTime());
		}
		socket.onclose = function(){
			alert("证书服务无法连接，请启动证书服务。");
		}
	} catch(exception){
		alert('Error:'+exception);
	}
}

function hebcaClient()
{

}

hebcaClient.prototype={
	//登录
	UILogin:function(){
		if(isIE){
		try{
			var certB64 = cert.UILogin();
			callback();
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|UILogin';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "UILogin"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){

				}else{
					alert(json.msg);
				}
			}
		}
	}
	},
	//签名
	Sign:function(content, callback){
	if(isIE){
		try{
			var signature = signCert.SignText(content, 1);
			callback(signature);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|SignText|{"signText":"'+ content +'"}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "SignText"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var signature = json.signature;
					if(signature != ""){
						callback(signature);
					}
				}else{
					alert(json.msg);
				}
			}
		}
	}
    },

    //选择签名证书
    SelectSignCert: function(callback) {
        if(isIE) {
            try {
                cert = signCert = CertMgr.SelectSignCert();
                console.log("选择签名证书完成！");
                callback();
            } catch(e) {
                alert(e.message);
            }
        } else {
            var msgSend ="1|SelectSignCert";
            socket.send(msgSend);
            socket.onmessage = function(msg) {
                var args  = msg.data.split('|');
                if(args[1] == "SelectSignCert") {
                    var json = eval("("+args[2]+")");
                    if(json.ret == 0) {
                        console.log("选择签名证书完成！");
                        callback();
                    } else {
                        alert(json.msg);
                    }
                }
            }
        }
    },

    //选择加密证书（fixed by dusizhong at 20230804）
    SelectCryptCert:function(callback){
	if(isIE){
		try {
			cert = cryptCert = CertMgr.SelectEncryptCert();
			console.log('选择加密证书完成！')
		}catch(e){
			alert(e.message);
			}
	}else{
		var msgSend ="1|SelectCryptCert";
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "SelectCryptCert"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					console.log("选择加密证书完成！");
				}else{
					alert(json.msg);
				}
			}
		}
	}
    },

    //获取证书主题项
    GetSubjectItem: function(index, callback) {
        if(isIE) {
            try{
                var content = cert.GetSubjectItem(index);
                callback(content);
            } catch(e) {
                alert(e.message);
            }
        } else {
            var msgSend ='1|GetSubjectItem|{"index":'+ index +'}';
            socket.send(msgSend);
            socket.onmessage = function(msg) {
                var args  = msg.data.split('|');
                if(args[1] == "GetSubjectItem"){
                    var json = eval("("+args[2]+")");
                    if(json.ret == 0){
                        var content = json.content;
                        if(content != "") {
                            callback(content);
                        }
                    } else {
                        alert(json.msg);
                    }
                }
            }
        }
    },

    //获取证书B64
    GetCertB64:function(callback){
	if(isIE){
		try{
			var content = cert.GetCertB64();
			callback(content);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|GetCertB64';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "GetCertB64"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var cert = json.cert;
					if(cert != ""){
						callback(cert);
					}
				}else{
					alert(json.msg);
				}
			}
		}
	}
    },
    //获取证书数量
    GetCertCount:function(callback){
	if(isIE){
		try{
			var count = CertMgr.GetCertCount();
			callback(count);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|GetCertCount';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "GetCertCount"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var count = json.count;
					callback(count);

				}else{
					alert(json.msg);
				}
			}
		}
	}
   },
  //加密
  Encrypt:function(b64Data, callback){
	if(isIE){
		try{
			var encrypted = cryptCert.EncryptB64(b64Data);
			callback(encrypted);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|EncryptB64|{"b64data":"'+ b64Data +'"}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "EncryptB64"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var encrypted = json.b64encrypted;
					if(encrypted != ""){
						callback(encrypted);
					}
				}else{
					alert(json.msg);
				}
			}
		}
	}
    },
    //解密
    Decrypt:function(b64Data, callback){
	if(isIE){
		try{
			var decrypted = cryptCert.DecryptB64(b64Data);
			callback(decrypted);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|DecryptB64|{"b64data":"'+ b64Data +'"}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "DecryptB64"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var decrypted = json.b64decrypted;
					if(decrypted != ""){
						callback(decrypted);
					}
				}else{
					alert(json.msg);
				}
			}
		}
	}
    },
    //
    GenerateFileKeyB64:function(callback){
	if(isIE){
		try{
			var b64key = util.GenerateFileKeyB64();
			callback(b64key);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|GenerateFileKeyB64';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "GenerateFileKeyB64"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var b64key = json.b64key;
					if(b64key != ""){
						callback(b64key);
					}
				}else{
					alert(json.msg);
				}
			}
		}
	}
    },
   //
   EncryptFile:function(b64Data,srcPath, dstPath, callback){
	if(isIE){
		try{
			util.EncryptFile (b64Data, srcPath, dstPath);
			callback();
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|EncryptFile|{"key":"'+ b64Data +'","src":"' + srcPath + '","dst":"' + dstPath + '"}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "EncryptFile"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
						callback();
					}
				}else{
					alert(json.msg);
				}
			}
		}
	},
  //
    DecryptFile:function(b64Data,srcPath, dstPath, callback){
	if(isIE){
		try{
			util.DecryptFile (b64Data, srcPath, dstPath);
			callback();
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|DecryptFile|{"key":"'+ b64Data +'","src":"' + srcPath + '","dst":"' + dstPath + '"}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "DecryptFile"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
						callback();
					}
				}else{
					alert(json.msg);
				}
			}
		}
	},

	P7Sign:function(b64Data, callback){
	if(isIE){
		try{
			pkcs7.SetSignCert(signCert);
			var signature = pkcs7.SignB64(0, b64Data, 4);
			callback(signature);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|P7SignB64|{"b64data":"'+ b64Data + '"}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "P7SignB64"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var signature = json.b64data;
					if(signature != ""){
						callback(signature);
					}
					}
				}else{
					alert(json.msg);
				}
			}
		}
	},
	//p7验证签名
	P7VerifySign:function(b64Data, callback){
	if(isIE){
		try{
			var valid = pkcs7.VerifyB64(b64Data);
			callback(valid);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|P7VerifyB64|{"b64data":"'+ b64Data + '"}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "P7VerifyB64"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var valid = json.result;
						callback(valid);
					}
				}else{
					alert(json.msg);
				}
			}
		}
	},

  //数字信封添加接收者证书（fixed by dusizhong at 20230804)
  AddRecipient: function(certB64, callback) {
	if(isIE) {
		try {
			pkcs7.AddRecipientCert(certB64);
			callback();
		} catch(e) {
			alert(e.message);
		}
	} else {
		recipientCerts.push(certB64);
		callback();
	}
  },

  ClearRecipient:function(callback){
		if(isIE){
			try{
				var valid = pkcs7.RemoveAllRecipientCert();
			}catch(e){
				alert(e.message);
		  	}
		}
		recipientCerts = [];
	},


Cert2Json:function()
{
	if( recipientCerts.length < 1 )
			return "";
	var result="";
	result = '"certs":[';


	for(var i=0;i<recipientCerts.length;i++){
			result += '{"cert":"';
			result += recipientCerts[i];
			result += '"}';
			if( i != (recipientCerts.length-1))
			 		result += ',';
	}
	result += ']';
	return result;

    },

    //数字信封加密（fixed by dusizhong at 20230804)
	P7Envelop:function(b64Data, callback){
	if(isIE){
		try{
			var signature = pkcs7.EnvelopB64(b64Data, 0);
			callback(signature);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var certs = this.Cert2Json();
		if( certs == "" )
		{
			alert("请添加接收者证书");
		}
		var msgSend ='1|EnvelopB64|{"b64data":"'+ b64Data + '",' + certs + '}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "EnvelopB64"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var signature = json.b64data;
					if(signature != ""){
						callback(signature);
					}
					}
				}else{
					alert(json.msg);
				}
			}
		}
	},

	//数字信封解密
	P7UnEnvelop:function(b64Data, callback){
	if(isIE){
		try{
			cert = cryptCert = CertMgr.SelectEncryptCert();
			pkcs7.SetUnEnvelopCert(cryptCert);
			var signature = pkcs7.UnEnvelopB64(b64Data);
			callback(signature);
		}catch(e){
			alert(e.message);
	  	}
	}else{
		var msgSend ='1|UnEnvelopB64|{"b64data":"'+ b64Data + '"}';
		socket.send(msgSend);
		socket.onmessage = function(msg){
			var args  = msg.data.split('|');
			if(args[1] == "UnEnvelopB64"){
				var json = eval("("+args[2]+")");
				if(json.ret == 0){
					var signature = json.b64data;
					if(signature != ""){
						callback(signature);
					}
					}
				}else{
					alert(json.msg);
				}
			}
		}
	}
	//end
}

var obj=new hebcaClient();