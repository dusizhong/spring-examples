var isIE = window.ActiveXObject !== undefined;
var GlobalHebcaWebPDFObject = null;
var fs = null;
//非IE浏览器下
var socket = null;
var host = "ws://localhost:17213/test"; //杜思众修改：默认17214不行，修改为17213端口（CA官网8.3.9为Chrome下有序WebPDF1签章控件的WebSokcet服务使用端口为17213？）
//
var nAppletRunID = 0;       // 0：无需贴合；非0：贴合
var bRunInCurrentPage = true;		// 是否为当前页面加载的程序
var windowsDpiawareness = true;  // 是否对Windows的DPI设置敏感，是：则支持Windows的DPI，否：不支持
var lastPos = { x: 0, y: 0, w: 0, h: 0, isMax: false, nshow: 4 };
var timerScroll;        // 定时器检测窗口位置
//
window.onload = function () {
    if (isIE) { // 创建WebSeal对象
        fs = new HebcaWebPDFObject();
    } else {
        connect();
        // 监听窗口显示、隐藏事件
        WrlVisibilityListener(true);
        // 监听窗口滚动事件
        WrlScrollListener(true);

        // 监听窗口大小改变事件，使滚动状况匹配
        $(window).resize(function () {
            WrlScrollApplet();
        });

        // 浏览器焦点变换
        $(window).focus(function () {
            PageFocusState(true);
            //console.log("focus");
        });
        $(window).blur(function () {
            PageFocusState(false);
            //console.log("blur");
        })

        // 定时检测事件
        timerScroll = setInterval(() => {
            if (nAppletRunID) {
                var divR = document.getElementById('divReaderContainer');
                if (divR != null || divR != undefined) {
                    var infoPos = getElementPagePosition(divR);
                    if (infoPos.x == lastPos.x && infoPos.y == lastPos.y && infoPos.w == lastPos.w && infoPos.h == lastPos.h) {

                    } else {
                        //console.log("timer:\n");
                        Scroll(infoPos);
                        lastPos = infoPos;
                    }
                }


            }
        }, 100);

    }
};

function getElementPagePosition(element) {
    if (windowsDpiawareness == true) {
        return GetElementSrceenPosAndBrowserStateDpiAwareness(element);
    } else {
        return GetElementSrceenPosAndBrowserStateDpiAwarenessInvalid(element);
    }
}

function GetElementSrceenPosAndBrowserStateDpiAwarenessInvalid(element) {
    var posInfo = { x: 0, y: 0, w: 0, h: 0, isMax: false, nshow: 4 };
    if (element != null || element != undefined) {
        var divRect = element.getBoundingClientRect();
        var x = divRect.left;
        var y = divRect.top;
        var w = divRect.width;
        var h = divRect.height;

        //浏览器是否最大化
        var isMax = (window.outerHeight === screen.availHeight && window.outerWidth === screen.availWidth);

        // window.innerHeight: W3C-不包括菜单栏、工具栏以及滚动条等的浏览器窗口高度
        // window.outerHeight: 整个浏览器窗口的高度
        var toolbarH = window.outerHeight - window.innerHeight;

        var diffH = isMax ? 0 : 5;  //非最大化时窗口最上面多出了几个像素
        y = y - diffH;
        //浏览器最大化与非最大时下面的值不同，需要将此偏差值计算进去
        var diffW = (window.outerWidth - window.innerWidth) / 2;
        x = x + diffW;

        var ratio = 1;

        y = Math.ceil((screenTop + toolbarH + y) * ratio);
        x = Math.ceil((screenLeft + x) * ratio);
        w = Math.ceil(w * ratio);
        h = Math.ceil(h * ratio);

        posInfo.x = x;
        posInfo.y = y;
        posInfo.w = w;
        posInfo.h = h;
        posInfo.isMax = isMax;
        posInfo.nshow = 4;
    }

    return posInfo;
}

function MessageHandler() {
    this.oncallback = function (args) {
        if (args[1] == "OnInsertResult")
            this.onInsertResult(args[2]);
    }
    this.onmessage = function (msg) {
    }
    this.onwelcome = function (args) {
    }
    this.onInsertResult = function (e) {
    }
}

var handler = new MessageHandler();

function WebPDFCtrl_OnInsertResult(e) {
    handler.onInsertResult(e);
}


function HebcaWebPDFObject() {
    var webPDFCtrl = null;
    // 一个页面只创建一次
    if (null != GlobalHebcaWebPDFObject) {
        return GlobalHebcaWebPDFObject;
    }
    if (isIE) {
        // IE 浏览器创建插件
        try {
            var plugin_embed = document.createElement("OBJECT");
            plugin_embed.setAttribute("id", "wsHebcaWebPDFPlugin");
            plugin_embed.setAttribute("classid", "CLSID:6EE0E832-A96C-480C-AE0A-EB35A9BB9652");
            plugin_embed.setAttribute("width", "1000");
            plugin_embed.setAttribute("height", "480");
            document.body.appendChild(plugin_embed);
            webPDFCtrl = document.getElementById("wsHebcaWebPDFPlugin");
        } catch (e) {
            throw Error("没有安装客户端软件或IE阻止其运行.");
        }
        GlobalHebcaMailClientObj = webPDFCtrl;
    }
    return webPDFCtrl;
}

//连接WebSocket
function connect() {
    try {
        socket = new WebSocket(host);
        socket.onopen = function () {
            socket.send('0|' + new Date().getTime());
        }
        socket.onclose = function () {
            alert("证书服务无法连接，请启动证书服务。");
        }
        socket.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[0] == 0)
                handler.onwelcome(args);
            else if (args[0] == 2)
                handler.oncallback(args);
            else
                handler.onmessage(msg);
        }
    } catch (exception) {
        alert('Error:' + exception);
    }
}

handler.onwelcome = function (args) {
    var words = args[1].split(' ');
    socket.send('1|MyID|{"ID":"' + words[1] + '"}');
}

function PreSetReaderPos(x, y, w, h, buse) {
    if (isIE) {
        alert("IE not surpport.");
    } else {
        buse == 0 ? nAppletRunID = 0 : nAppletRunID = 1721;
        var msgSend = '1|PreSetReaderPos|{"x":"' + x + '","y":"' + y + '","w":"' + w + '","h":"' + h + '","buse":"' + buse + '"}';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "PreSetReaderPos") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    alert("success");
                } else {
                    alert("error:" + json.msg);
                }
            }
        }
    }
}


function Open() {
    if (isIE) {
        try {
            fs.Open();
        } catch (e) {
            alert(e.message);
        }
    } else {
        var msgSend = '1|Open';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "Open") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("open success");
                } else {
                    //alert("open faild");
                    alert(json.msg);
                }
            }
        }
    }
}

function Close() {
    if (isIE) {
        try {
            fs.Close();
        } catch (e) {
            alert(e.message);
        }
    } else {
        var msgSend = '1|Close';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "Close") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("close success");
                } else {
                    //alert("close faild");
                    alert(json.msg);
                }
            }
        }
    }
}


function OpenLocalFile2(lpszFile) {
    if (isIE) {
        try {
            fs.OpenLocalFile2(lpszFile);
        } catch (e) {
            alert(e.message);
        }
    } else {
        // 转编码
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "filepath": lpszFile.toString()
        };
        var msgSend = '1|OpenLocalFile2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "OpenLocalFile2") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("open success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}

function CloseFile2() {
    if (isIE) {
        try {
            fs.CloseFile2();
        } catch (e) {
            alert(e.message);
        }
    } else {
        var msgSend = '1|CloseFile2';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "CloseFile2") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("close PDF success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}


function OpenNetFile(lpszFile) {
    if (isIE) {
        try {
            fs.OpenNetFile(lpszFile);
        } catch (e) {
            alert(e.message);
        }
    } else {
        // 转编码
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "filepath": lpszFile
        };
        var msgSend = '1|OpenNetFile|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "OpenNetFile") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("open success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}

function OpenNetFile2(lpszFile) {
    if (isIE) {
        try {
            fs.put_OpenFlags = 12;
            fs.OpenNetFile2(lpszFile);
        } catch (e) {
            alert(e.message);
        }
    } else {
        // 转编码
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "filepath": lpszFile
        };
        var msgSend = '1|OpenNetFile2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "OpenNetFile2") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("open success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}

// 回调函数原型：function callback(boolean success, string error, string resp)
function SaveNetFile(lpszFile, callback) {
    if (isIE) {
        try {
            var resp = fs.SaveNetFile(lpszFile);
            callback(true, "", resp);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        // 转编码
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "filepath": lpszFile
        };
        var msgSend = '1|SaveNetFile|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SaveNetFile") {
                var json = eval("(" + args[2] + ")");
                callback(json.success, json.error, json.resp);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

// 回调函数原型：function callback(boolean success, string error, string stamp, int isSM2)
function SelectStamp(callback) {
    if (isIE) {
        try {
            var stamp = fs.SelectStamp();
            var isSM2 = fs.IsSM2Cert();
            callback(true, "", stamp, isSM2);
        } catch (e) {
            callback(false, e.message, "", false);
        }
    } else {
        var msgSend = '1|SelectStamp';

        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SelectStamp") {
                var json = eval("(" + args[2] + ")");
                callback(json.success, json.error, json.stamp, json.isSM2);
            } else
                callback(false, "Unknown response " + args[1], "", false);
        }
    }
}

// 回调函数原型：function callback(boolean success, string error, string result)
function SealHash(hashB64, callback) {
    if (isIE) {
        try {
            var result = fs.SealHash_NoOrder(hashB64);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var jsonT = {
            "hash": hashB64
        };
        var msgSend = '1|SealHash_NoOrder|' + JSON.stringify(jsonT);

        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SealHash_NoOrder") {
                var json = eval("(" + args[2] + ")");
                callback(json.success, json.error, json.result);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

function ShowExceptionError(e) {
    if (e.name == "TypeError")
        alert("JS错误：" + e.message);
    else {
        try {
            alert("WebPDF错误：" + fs.GetErrMsg());
        } catch (ee) {
            alert("JS内部错误：" + ee.message);
        }
    }
}

function LoadFile(lpszFile) {
    if (isIE) {
        try {
            fs.LoadFile(lpszFile);
        } catch (e) {
            ShowExceptionError(e);
        }
    } else {
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "filepath": lpszFile
        };
        var msgSend = '1|LoadFile|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "LoadFile") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("LoadFile success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}

function GetCurFilePath(callback) {
    if (isIE) {
        try {
            var result = fs.GetCurFilePath();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetCurFilePath';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetCurFilePath") {
                var json = eval("(" + args[2] + ")");
                callback(json.success, json.error, json.result);
            }
        }
    }
}

function OpenPage(name, url, cookie, width, height, callback) {
    if (cookie == undefined)
        cookie = "";
    if (width == undefined)
        width = 800;
    if (height == undefined)
        height = 600;

    if (isIE) {
        window.open(url, '', 'width=' + width + ',height=' + height);
        if (callback != undefined)
            callback(true, "", "");
    } else {
        name = base64encode(utf16to8(name));
        var jsonT = {
            "name": name.toString(),
            "url": url.toString(),
            "width": width.toString(),
            "height": height.toString(),
            "cookie": cookie.toString()
        };
        var msgSend = '1|OpenPage|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "OpenPage") {
                var json = eval("(" + args[2] + ")");
                if (callback != undefined)
                    callback(json.success, json.error, json.result);
            }
        }
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function InsertSealBase64PDF_SubPacket_Do(curPacketIndex, subPacketCnt, lenPerPacketData, pdfB64, pageNo, x, y, sealSN, pwd, eUnitType, callback) {
    try {
        var subPdfB64 = pdfB64.substr(curPacketIndex * lenPerPacketData, lenPerPacketData);
        var jsonT = {
            "subPacketCnt": subPacketCnt.toString(),
            "curPacketIndex": curPacketIndex.toString(),
            "pdfB64": subPdfB64.toString(),
            "pageNo": pageNo.toString(),
            "x": x.toString(),
            "y": y.toString(),
            "sealSN": sealSN.toString(),
            "pwd": pwd.toString(),
            "eUnitType": eUnitType.toString()
        };
        var msgSend = '1|InsertSealBase64PDF|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            try {
                var args = msg.data.split('|');
                if (args[1] == "InsertSealBase64PDF") {
                    var json = eval("(" + args[2] + ")");
                    if (json.curPacketIndex == undefined || (json.curPacketIndex >= json.subPacketCnt - 1))
                        callback(json.success, json.error, json.result);
                    else
                        InsertSealBase64PDF_SubPacket_Do(++curPacketIndex, subPacketCnt, lenPerPacketData, pdfB64, pageNo, x, y, sealSN, pwd, eUnitType, callback);
                } else
                    callback(false, "Unknown response " + args[1], "");
            } catch (e1) {
                callback(false, "[InsertSealBase64PDF_SubPacket_Do->socket.onmessage = function (msg)] throw exception：" + e1.message, "");
            }
        }

    } catch (e) {
        callback(false, e.message, "", 0, 0);
    }
}

// 回调函数原型：function callback(boolean success, string error, string sealPdfB64)
function InsertSealBase64PDF(pdfB64, pageNo, x, y, sealSN, pwd, eUnitType, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSealBase64PDF(pdfB64, pageNo, x, y, sealSN, pwd, eUnitType);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        try {
            var lenTotal = pdfB64.length;
            var lenPerPacketData = 10240; //10K
            var subPacketCnt = Math.ceil(lenTotal / lenPerPacketData);
            var curPacketIndex = 0;
            InsertSealBase64PDF_SubPacket_Do(curPacketIndex, subPacketCnt, lenPerPacketData, pdfB64, pageNo, x, y, sealSN, pwd, eUnitType, callback);
        } catch (e) {
            callback(false, e.message, "");
        }
    }
}

function SealKeyWordBase64PDF_SubPacket_Do(curPacketIndex, subPacketCnt, lenPerPacketData, pdfB64, keyword, sealTSType, offsetX, offsetY, callback) {
    try {
        var subPdfB64 = pdfB64.substr(curPacketIndex * lenPerPacketData, lenPerPacketData);
        var jsonT = {
            "subPacketCnt": subPacketCnt.toString(),
            "curPacketIndex": curPacketIndex.toString(),
            "pdfB64": subPdfB64.toString(),
            "keyword": keyword.toString(),
            "sealTSType": sealTSType.toString(),
            "offsetX": offsetX.toString(),
            "offsetY": offsetY.toString()
        };
        var msgSend = '1|SealKeyWordBase64PDF|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            try {
                var args = msg.data.split('|');
                if (args[1] == "SealKeyWordBase64PDF") {
                    var json = eval("(" + args[2] + ")");
                    if (json.curPacketIndex == undefined || (json.curPacketIndex >= json.subPacketCnt - 1))
                        callback(json.success, json.error, json.result);
                    else
                        SealKeyWordBase64PDF_SubPacket_Do(++curPacketIndex, subPacketCnt, lenPerPacketData, pdfB64, keyword, sealTSType, offsetX, offsetY, callback);
                } else
                    callback(false, "Unknown response " + args[1], "");
            } catch (e1) {
                callback(false, "[SealKeyWordBase64PDF_SubPacket_Do->socket.onmessage = function (msg)] throw exception：" + e1.message, "");
            }
        }

    } catch (e) {
        callback(false, e.message, "");
    }
}

// 回调函数原型：function callback(boolean success, string error, string sealPdfB64)
function SealKeyWordBase64PDF(pdfB64, keyword, sealTSType, offsetX, offsetY, callback) {
    if (isIE) {
        try {
            var result = fs.SealKeyWordBase64PDF(pdfB64, keyword, sealTSType, offsetX, offsetY);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        try {
            var lenTotal = pdfB64.length;
            var lenPerPacketData = 10240; //10K
            var subPacketCnt = Math.ceil(lenTotal / lenPerPacketData);
            var curPacketIndex = 0;
            var keywordB64 = base64encode(utf16to8(keyword));
            SealKeyWordBase64PDF_SubPacket_Do(curPacketIndex, subPacketCnt, lenPerPacketData, pdfB64, keywordB64, sealTSType, offsetX, offsetY, callback);
        } catch (e) {
            callback(false, e.message, "");
        }
    }
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, int count)
function GetSealCount_Pack(fileURI, callback) {
    var str = fileURI.match(/http:\/\//);
    var isLocal = 0;
    if (str == null) {
        str = fileURI.match(/[a-zA-Z]:\\[a-zA-Z_0-9\\]*/);
        if (str == null) {
            callback(false, "文件地址不正确！", 0);
            return;
        }
        else {
            if (fileURI.charAt(1) == ':') {
                isLocal = 1;
            }
            else {
                callback(false, "文件地址不正确！", 0);
                return;
            }
        }
    }

    if (isIE) {
        try {
            var result = fs.GetSealCount_Pack(fileURI, isLocal);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        // 转编码
        var src = fileURI;
        var dest = base64encode(utf16to8(src));
        fileURI = dest;

        var jsonT = {
            "fileURI": fileURI,
            "isLocal": isLocal.toString()
        };
        var msgSend = '1|GetSealCount_Pack|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetSealCount_Pack") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.count);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}
//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function SetJavaInvokeFlag(isShow, callback) {
    // 此接口专用于静默调用，无IE模式
    var jsonT = {
        "isShow": isShow.toString()
    };
    var msgSend = '1|SetJavaInvokeFlag|' + JSON.stringify(jsonT);
    socket.send(msgSend);
    handler.onmessage = function (msg) {
        var args = msg.data.split('|');
        if (args[1] == "SetJavaInvokeFlag") {
            var json = eval("(" + args[2] + ")");
            callback(json.ret == 0, json.msg);
        } else
            callback(false, "Unknown response " + args[1]);
    }

}


function SetConfig(cg, callback) {
    if (isIE) {
        try {
            fs.SetConfig(cg);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|SetConfig|' + cg;
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SetConfig") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else {
                callback(false, "Unknown response " + args[1]);
            }
        }
    }
}


//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetSealInfo(index, callback) {
    if (isIE) {
        try {
            var result = fs.GetSealInfo(index);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var jsonT = {
            "index": index.toString()
        };
        var msgSend = '1|GetSealInfo|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetSealInfo") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}


//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetSealInfo_Pack(fileURI, index, callback) {
    var str = fileURI.match(/http:\/\//);
    var isLocal = 0;
    if (str == null) {
        str = fileURI.match(/[a-zA-Z]:\\[a-zA-Z_0-9\\]*/);
        if (str == null) {
            callback(false, "文件地址不正确！", 0);
            return;
        }
        else {
            if (fileURI.charAt(1) == ':') {
                isLocal = 1;
            }
            else {
                callback(false, "文件地址不正确！", 0);
                return;
            }
        }
    }

    if (isIE) {
        try {
            var result = fs.GetSealInfo_Pack(fileURI, isLocal, index);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        // 转编码
        var src = fileURI;
        var dest = base64encode(utf16to8(src));
        fileURI = dest;

        var jsonT = {
            "fileURI": fileURI,
            "isLocal": isLocal.toString(),
            "index": index.toString()
        };
        var msgSend = '1|GetSealInfo_Pack|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetSealInfo_Pack") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertSealInfo2(sealInfo, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSealInfo2(sealInfo);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "sealInfo": sealInfo
        };
        var msgSend = '1|InsertSealInfo2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSealInfo2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertHandSignByPos(lx, ly, pagenum, callback) {
    if (isIE) {
        try {
            var result = fs.InsertHandSignByPos(lx, ly, pagenum)
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "lX": lx.toString(),
            "lY": ly.toString(),
            "pageNum": pagenum.toString()
        };
        var msgSend = '1|InsertHandSignByPos|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertHandSignByPos") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function HandSignKeyword(keyword, callback) {
    if (isIE) {
        try {
            var result = fs.HandSignKeyword(keyword);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = keyword;
        var dest = base64encode(utf16to8(src));
        keyword = dest;
        var jsonT = {
            "keyword": keyword
        };
        var msgSend = '1|HandSignKeyword|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "HandSignKeyword") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}


function Scroll(autoScrollJson) {
    var jsonT = {
        "x": autoScrollJson.x.toString(),
        "y": autoScrollJson.y.toString(),
        "w": autoScrollJson.w.toString(),
        "h": autoScrollJson.h.toString(),
        "nshow": autoScrollJson.nshow.toString()
    };
    var msgSend = '1|Scroll|' + JSON.stringify(jsonT);
    if (socket.readyState == 1) {
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "Scroll") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("open success");
                } else {
                    //alert("open faild");
                    console.log(json.msg);
                }
            }
        }
    }
}

function EnableShowReader(bshow) {
    if (nAppletRunID) {
        var divR = document.getElementById('divReaderContainer');
        var infoPos = getElementPagePosition(divR);
        infoPos.nshow = (bshow == false) ? 0 : 1;
        Scroll(infoPos);
        lastPos = infoPos;
        //console.log("EnableShow:" + JSON.stringify(infoPos));
    }
}



// 判断是否为Firefox，用于区别处理页面滚动和页面切换可见性
function isFirefox() {
    if (navigator.userAgent.toLowerCase().indexOf("firefox") != -1)
        return true;
    else
        return false;
}

function hasVerticalScrollbar() {
    if (document.documentElement.clientHeight)
        return document.body.scrollHeight > document.documentElement.clientHeight;
    return document.body.scrollHeight > window.innerHeight;
}

function hasHorizontalScrollbar() {
    if (document.documentElement.clientWidth)
        return document.body.scrollWidth > document.documentElement.clientWidth;
    return document.body.scrollWidth > window.innerWidth;
}

function getScrollbarWidth() {
    var scrollDiv = document.createElement("div");
    scrollDiv.style.cssText = 'width: 99px; height: 99px; overflow: scroll; position: absolute; top: -9999px;';
    document.body.appendChild(scrollDiv);
    var scrollbarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth;
    document.body.removeChild(scrollDiv);
    return scrollbarWidth;
}

function WrlVisibilityListener(AddEvent) {
    if (bRunInCurrentPage) {
        if (AddEvent) {
            if (document.addEventListener)
                document.addEventListener('visibilitychange', PageVisibilityState, false);
            else
                document.attachEvent('onvisibilitychange', PageVisibilityState, false);
        }
        else {
            if (document.removeEventListener)
                document.removeEventListener('visibilitychange', PageVisibilityState, false);
            else
                document.detachEvent('onvisibilitychange', PageVisibilityState, false);
        }
    }
}


function WrlScrollListener(AddEvent) {
    if (!isFirefox()) {
        if (AddEvent) {
            if (window.pageXOffset != undefined)
                document.onscroll = scrollFunc;
            else
                window.onscroll = scrollFunc;
        }
    }
    else {
        if (AddEvent)
            document.addEventListener("scroll", scrollFunc, false);
        else
            document.removeEventListener("scroll", scrollFunc);
    }
}

// 滚动内嵌程序
function scrollFunc(e) {
    if (!nAppletRunID || !bRunInCurrentPage)
        return;

    WrlScrollApplet();
}

function WrlScrollApplet() {
    if (nAppletRunID) {
        var divR = document.getElementById('divReaderContainer');
        var infoPos = getElementPagePosition(divR);
        Scroll(infoPos);
        lastPos = infoPos;
        //console.log("Scroll:" + JSON.stringify(infoPos));
    }
}

function PageVisibilityState() {
    if (nAppletRunID < 1)
        return;// 未启动程序
    // 控制程序显示、仅对当前网页内加载的程序
    var divR = document.getElementById('divReaderContainer');
    if (divR != null || divR != undefined) {
        var infoPos = getElementPagePosition(divR);
        if (document.visibilityState == 'visible') {
            /// 恢复显示
            infoPos = lastPos;
            infoPos.nshow = 4;
        }
        else if (document.visibilityState == 'hidden') {
            /// 需要隐藏
            infoPos.nshow = 8;
        }
        Scroll(infoPos);
        //console.log("Visable:" + JSON.stringify(infoPos));    
    }

}

function PageFocusState(isFocus) {
    if (nAppletRunID < 1)
        return;// 未启动程序
    // 控制程序显示、仅对当前网页内加载的程序
    var divR = document.getElementById('divReaderContainer');
    if (divR != null || divR != undefined) {
        var infoPos = getElementPagePosition(divR);
        if (isFocus) {
            /// 恢复显示
            if (lastPos != null || lastPos != undefined)
                infoPos = lastPos;
            infoPos.nshow = 4;
        }
        else {
            /// 需要隐藏
            infoPos.nshow = 8;
        }
        Scroll(infoPos);
        //console.log("Scroll:" + JSON.stringify(infoPos));    

    }

}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
// 显示隐藏工具栏
function ShowToolbar(bShow, callback) {
    if (isIE) {
        try {
            var result = fs.ShowToolbar(bShow);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "flag": bShow
        };
        var msgSend = '1|ShowToolbar|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "ShowToolbar") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
// 获取文档页数
function GetPageCount(callback) {
    if (isIE) {
        try {
            var result = fs.GetPageCount();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetPageCount|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetPageCount") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.count);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function SealKeyWord(txtKeyWord, txtTSType, callback) {
    if (isIE) {
        try {
            var result = fs.SealKeyWord(txtKeyWord, txtTSType);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = txtKeyWord;
        var dest = base64encode(utf16to8(src));
        txtKeyWord = dest;
        var jsonT = {
            "keyword": txtKeyWord,
            "flag": txtTSType
        };
        var msgSend = '1|SealKeyWord|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SealKeyWord") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function FindText(txtWord, bMatchCase, callback) {
    if (isIE) {
        try {
            var result = fs.FindText(txtWord, bMatchCase);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = txtWord;
        var dest = base64encode(utf16to8(src));
        txtWord = dest;
        var jsonT = {
            "keyword": txtWord,
            "flag": bMatchCase
        };
        var msgSend = '1|FindText|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "FindText") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function FindPrev(callback) {
    if (isIE) {
        try {
            var result = fs.FindPrev();
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|FindPrev|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "FindPrev") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function FindNext(callback) {
    if (isIE) {
        try {
            var result = fs.FindNext();
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|FindNext|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "FindNext") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
// 显示隐藏工具栏
function SetToolbarButtonVisible(lBtnType, bShow, callback) {
    if (isIE) {
        try {
            var result = fs.SetToolbarButtonVisible(lBtnType, bShow);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "flag1": lBtnType,
            "flag2": bShow
        };
        var msgSend = '1|SetToolbarButtonVisible|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SetToolbarButtonVisible") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
// 插入签章
function InsertSeal2(page, x, y, flag, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSeal2(page, x, y, flag);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "page": page,
            "x": x,
            "y": y,
            "flag": flag
        };
        var msgSend = '1|InsertSeal2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSeal2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
// 11获取文档页数
function GetVersion(callback) {
    if (isIE) {
        try {
            var result = fs.GetVersion();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetVersion|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetVersion") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.version);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 12打开本地文件
function OpenLocalFile(lpszFile) {
    if (isIE) {
        try {
            fs.OpenLocalFile(lpszFile);
        } catch (e) {
            alert(e.message);
        }
    } else {
        // 转编码
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "filepath": lpszFile.toString()
        };
        var msgSend = '1|OpenLocalFile|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "OpenLocalFile") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("open success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 14保存本地文件
function SaveLocalFile(lpszFile) {
    if (isIE) {
        try {
            fs.SaveLocalFile(lpszFile);
        } catch (e) {
            alert(e.message);
        }
    } else {
        // 转编码
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "filepath": lpszFile.toString()
        };
        var msgSend = '1|SaveLocalFile|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SaveLocalFile") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("open success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
// 16获取错误码 GetErrCode
function GetErrCode(callback) {
    if (isIE) {
        try {
            var result = fs.GetErrCode();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetErrCode|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetErrCode") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.code);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}
//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
// 17获取错误信息 GetErrMsg
function GetErrMsg(callback) {
    if (isIE) {
        try {
            var result = fs.GetErrMsg();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetErrMsg|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetErrMsg") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.msg);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}
//////////////////////////////////////////////////////////////////////////
// 18关闭PDF文件
function CloseFile() {
    if (isIE) {
        try {
            fs.CloseFile();
        } catch (e) {
            alert(e.message);
        }
    } else {
        var msgSend = '1|CloseFile';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "CloseFile") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("close PDF success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
// 23 设置打开时文件显示方式 SetOpenZoomType :0;1;2
function SetOpenZoomType(zoomType, callback) {
    if (isIE) {
        try {
            var result = fs.SetOpenZoomType(zoomType);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "type": zoomType.toString()
        };
        var msgSend = '1|SetOpenZoomType|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SetOpenZoomType") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
// 31 获取文档中签章数量
function GetSealCount(callback) {
    if (isIE) {
        try {
            var result = fs.GetSealCount();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetSealCount|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetSealCount") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.count);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
// 32 插入签章 InsertSeal
function InsertSeal(page, x, y, sn, pwd, flag, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSeal(page, x, y, sn, pwd, flag);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "pageNo": page,
            "x": x,
            "y": y,
            "sealSN": sn,
            "pwd": pwd,
            "eUnitType": flag
        };
        var msgSend = '1|InsertSeal|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSeal") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
// 33 批量验证签章
function VerifyAllSeal(callback) {
    if (isIE) {
        try {
            var result = fs.VerifyAllSeal();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|VerifyAllSeal|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "VerifyAllSeal") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
// 36 锁定签章
function LockSeal(bLock, callback) {
    if (isIE) {
        try {
            var result = fs.LockSeal(bLock);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "flag": bLock
        };
        var msgSend = '1|LockSeal|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "LockSeal") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertSealInfo(lpszFile, sealInfo, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSealInfo(lpszFile, sealInfo);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;
        var jsonT = {
            "filepath": lpszFile,
            "sealInfo": sealInfo
        };
        var msgSend = '1|InsertSealInfo|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSealInfo") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}


//////////////////////////////////////////////////////////////////////////
// TODO: begin bookmark
//////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function GotoBookmark(title, bExactMatch, callback) {
    if (isIE) {
        try {
            var result = fs.GotoBookmark(title, bExactMatch);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = title;
        var dest = base64encode(utf16to8(src));
        title = dest;
        var jsonT = {
            "bookmark": title,
            "flag": bExactMatch
        };
        var msgSend = '1|GotoBookmark|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GotoBookmark") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function BeginBatchSeal(filepath, flag, callback) {
    if (isIE) {
        try {
            var result = fs.BeginBatchSeal(filepath, flag);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = filepath;
        var dest = base64encode(utf16to8(src));
        filepath = dest;
        var jsonT = {
            "filepath": filepath.toString(),
            "flag": flag.toString()
        };
        var msgSend = '1|BeginBatchSeal|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "BeginBatchSeal") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function BatchSeal(filepath, callback) {
    if (isIE) {
        try {
            var result = fs.BatchSeal(filepath);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = filepath;
        var dest = base64encode(utf16to8(src));
        filepath = dest;
        var jsonT = {
            "filepath": filepath
        };
        var msgSend = '1|BatchSeal|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "BatchSeal") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function EndBatchSeal(callback) {
    if (isIE) {
        try {
            var result = fs.EndBatchSeal();
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|EndBatchSeal|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "EndBatchSeal") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetSealPicB64(callback) {
    if (isIE) {
        try {
            var result = fs.GetSealPicB64();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetSealPicB64|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetSealPicB64") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetAllSealPicB64(callback) {
    if (isIE) {
        try {
            var result = fs.GetAllSealPicB64();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetAllSealPicB64|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetAllSealPicB64") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function Print(callback) {
    if (isIE) {
        try {
            var result = fs.Print();
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|Print|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "Print") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function BatchSeal2(filename, size, digest, callback) {
    if (isIE) {
        try {
            var result = fs.BatchSeal2(filename, size, digest);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = filename;
        var dest = base64encode(utf16to8(src));
        filename = dest;
        var jsonT = {
            "filename": filename,
            "size": size,
            "digest": digest
        };
        var msgSend = '1|BatchSeal2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "BatchSeal2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

function OpenLocalFile3(lpszFile, flag, callback) {
    if (isIE) {
        try {
            fs.OpenLocalFile3(lpszFile);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        // 转编码
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "filepath": lpszFile.toString(),
            "flag": flag.toString()
        };
        var msgSend = '1|OpenLocalFile3|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "OpenLocalFile3") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    callback(json.ret == 0, json.msg);
                } else {
                    callback(false, "Unknown response " + args[1]);
                }
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function BeginBatchSeal3(filepath, flag, callback) {
    if (isIE) {
        try {
            var result = fs.BeginBatchSeal3();
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|BeginBatchSeal3|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "BeginBatchSeal3") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function BatchSeal3(filename, size, digest, callback) {
    if (isIE) {
        try {
            var result = fs.BatchSeal3(filename, size, digest);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message), "";
        }
    } else {
        var src = filename;
        var dest = base64encode(utf16to8(src));
        filename = dest;
        var jsonT = {
            "filename": filename,
            "size": size,
            "digest": digest
        };
        var msgSend = '1|BatchSeal3|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "BatchSeal3") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function EndBatchSeal3(callback) {
    if (isIE) {
        try {
            var result = fs.EndBatchSeal3();
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|EndBatchSeal3|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "EndBatchSeal3") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function DisableSealFunc(bLock, callback) {
    if (isIE) {
        try {
            var result = fs.DisableSealFunc(bLock);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "flag": bLock
        };
        var msgSend = '1|DisableSealFunc|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "DisableSealFunc") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function MessageBox_Pack(filepath, nIslocal, strText, flag, callback) {
    if (isIE) {
        try {
            var result = fs.MessageBox_Pack(filepath, nIslocal, strText, flag);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = filepath;
        var dest = base64encode(utf16to8(src));
        filepath = dest;

        src = strText;
        dest = base64encode(utf16to8(strText));
        strText = dest;

        var jsonT = {
            "fileURI": filepath,
            "isLocal": nIslocal,
            "text": strText,
            "flag": flag
        };
        var msgSend = '1|MessageBox_Pack|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "MessageBox_Pack") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function VerifyAllSeal_Pack(filepath, nIslocal, callback) {
    if (isIE) {
        try {
            var result = fs.VerifyAllSeal_Pack(filepath, nIslocal);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, 0);
        }
    } else {
        var src = filepath;
        var dest = base64encode(utf16to8(src));
        filepath = dest;

        var jsonT = {
            "fileURI": filepath,
            "isLocal": nIslocal
        };
        var msgSend = '1|VerifyAllSeal_Pack|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "VerifyAllSeal_Pack") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetSealCount_Pack(filepath, nIslocal, callback) {
    if (isIE) {
        try {
            var result = fs.GetSealCount_Pack(filepath, nIslocal);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, 0);
        }
    } else {
        var src = filepath;
        var dest = base64encode(utf16to8(src));
        filepath = dest;

        var jsonT = {
            "fileURI": filepath,
            "isLocal": nIslocal
        };
        var msgSend = '1|GetSealCount_Pack|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetSealCount_Pack") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.count);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertSeal_Pack(filepath, nIslocal, page, x, y, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSeal_Pack(filepath, nIslocal, page, x, y);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = filepath;
        var dest = base64encode(utf16to8(src));
        filepath = dest;

        var jsonT = {
            "fileURI": filepath,
            "isLocal": nIslocal,
            "page": page,
            "x": x,
            "y": y
        };
        var msgSend = '1|InsertSeal_Pack|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSeal_Pack") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function ResizeWin(width, height, callback) {
    if (isIE) {
        try {
            var result = fs.ResizeWin(width, height);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "width": width,
            "height": height
        };
        var msgSend = '1|ResizeWin|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "ResizeWin") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetTextSignContent(nIndex, callback) {
    if (isIE) {
        try {
            var result = fs.GetTextSignContent(nIndex);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var jsonT = {
            "index": nIndex
        };
        var msgSend = '1|GetTextSignContent|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetTextSignContent") {
                var json = eval("(" + args[2] + ")");
                var tmpR = json.result;
                var tmpr2= Base64.decode(tmpR);
                callback(json.ret == 0, json.msg, tmpr2);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function RotatePage(nAngle, callback) {
    if (isIE) {
        try {
            var result = fs.RotatePage(nAngle);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "angle": nAngle.toString()
        };
        var msgSend = '1|RotatePage|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "RotatePage") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function SignTextByKeyword(keyword, text, x, y, type, size, callback) {
    if (isIE) {
        try {
            var result = fs.SignTextByKeyword(keyword, text, x, y, type, size);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = keyword;
        var dest = base64encode(utf16to8(src));
        keyword = dest;
        src = text;
        dest = base64encode(utf16to8(src));
        text = dest;
        var jsonT = {
            "keyword": keyword.toString(),
            "text": text.toString(),
            "x": x.toString(),
            "y": y.toString(),
            "type": type.toString(),
            "size": size.toString()
        };
        var msgSend = '1|SignTextByKeyword|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SignTextByKeyword") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function GotoPage(lPage, callback) {
    if (isIE) {
        try {
            var result = fs.GotoPage(lPage);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "page": lPage
        };
        var msgSend = '1|GotoPage|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GotoPage") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function ClickInsertSealButton(callback) {
    if (isIE) {
        try {
            var result = fs.ClickInsertSealButton();
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {

        var msgSend = '1|ClickInsertSealButton|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "ClickInsertSealButton") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetCurrentPageNo(callback) {
    if (isIE) {
        try {
            var result = fs.GetCurrentPageNo();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetCurrentPageNo|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetCurrentPageNo") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertWatermark(text, pages, fontName, fontsize, lcolor, lRotate, lOpacity, lAlign, x, y, bBold, bUnderline, callback) {
    if (isIE) {
        try {
            var result = fs.InsertWatermark(text, pages, fontName, fontsize, lcolor, lRotate, lOpacity, lAlign, x, y, bBold, bUnderline);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = text;
        var dest = base64encode(utf16to8(src));
        text = dest;
        src = fontName;
        dest = base64encode(utf16to8(src));
        fontName = dest;
        var jsonT = {
            "text": text.toString(),
            "pages": pages.toString(),
            "font": fontName.toString(),
            "size": fontsize.toString(),
            "color": lcolor.toString(),
            "rotate": lRotate.toString(),
            "opacity": lOpacity.toString(),
            "x": x.toString(),
            "y": y.toString(),
            "bold": bBold.toString(),
            "underline": bUnderline.toString()
        };
        var msgSend = '1|InsertWatermark|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertWatermark") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function IsReaderOpened(callback) {
    if (isIE) {
        try {
            var result = fs.IsReaderOpened();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|IsReaderOpened|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "IsReaderOpened") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertSeal_2(page, x, y, sn, pwd, flag1, flag2, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSeal_2(page, x, y, sn, pwd, flag1, flag2);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "pageNo": page,
            "x": x,
            "y": y,
            "sealSN": sn,
            "pwd": pwd,
            "eUnitType": flag1,
            "eTsType": flag2
        };
        var msgSend = '1|InsertSeal_2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSeal_2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertSeal2_2(page, x, y, flag1, flag2, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSeal2_2(page, x, y, flag1, flag2);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "pageNo": page,
            "x": x,
            "y": y,
            "eUnitType": flag1,
            "eTsType": flag2
        };
        var msgSend = '1|InsertSeal2_2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSeal2_2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string stamp)
function GetStampPicB64_2(callback) {
    if (isIE) {
        try {
            var stamp = fs.GetStampPicB64_2();
            callback(true, "", stamp);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetStampPicB64_2';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetStampPicB64_2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.error, json.result);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetSealPicB64_2(iIndex, callback) {
    if (isIE) {
        try {
            var result = fs.GetSealPicB64_2(iIndex);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var jsonT = {
            "index": iIndex
        };
        var msgSend = '1|GetSealPicB64_2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetSealPicB64_2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertSeal_Pack_2(filepath, nIslocal, page, x, y, eUnitType, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSeal_Pack_2(filepath, nIslocal, page, x, y, eUnitType);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = filepath;
        var dest = base64encode(utf16to8(src));
        filepath = dest;

        var jsonT = {
            "fileURI": filepath,
            "isLocal": nIslocal,
            "page": page,
            "x": x,
            "y": y,
            "flag": eUnitType
        };
        var msgSend = '1|InsertSeal_Pack_2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSeal_Pack_2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function LoginDevice(sealsn, pwd, callback) {
    if (isIE) {
        try {
            var result = fs.LoginDevice(sealsn, pwd);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = sealsn;
        var dest = base64encode(utf16to8(src));
        sealsn = dest;
        src = pwd;
        dest = base64encode(utf16to8(src));
        pwd = dest;

        var jsonT = {
            "sealSN": sealsn,
            "pwd": pwd
        };
        var msgSend = '1|LoginDevice|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "LoginDevice") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function SetJavaInvokeFlag(flag, callback) {
    if (isIE) {
        try {
            var result = fs.SetJavaInvokeFlag(flag);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "isShow": flag
        };
        var msgSend = '1|SetJavaInvokeFlag|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SetJavaInvokeFlag") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertHandSign(option, callback) {
    if (isIE) {
        try {
            var result = fs.InsertHandSign(option)
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "flag": option.toString()
        };
        var msgSend = '1|InsertHandSign|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertHandSign") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function Delete(callback) {
    if (isIE) {
        try {
            var result = fs.Delete()
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|Delete|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "Delete") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function CleanCachePwd(callback) {
    if (isIE) {
        try {
            var result = fs.CleanCachePwd()
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var msgSend = '1|CleanCachePwd|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "CleanCachePwd") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertPageSeal_Pack(filepath, x, y, eUnitType, beginNum, endNum, callback) {
    if (isIE) {
        try {
            var result = fs.InsertPageSeal_Pack(filepath, x, y, eUnitType, beginNum, endNum);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = filepath;
        var dest = base64encode(utf16to8(src));
        filepath = dest;

        var jsonT = {
            "fileURI": filepath,
            "x": x,
            "y": y,
            "flag": eUnitType,
            "begin": beginNum,
            "end": endNum,
        };
        var msgSend = '1|InsertPageSeal_Pack|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertPageSeal_Pack") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetHandSignRecord(callback) {
    if (isIE) {
        try {
            var result = fs.GetHandSignRecord();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|GetHandSignRecord|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetHandSignRecord") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

//////////////////////////////////////////////////////////////////////////
function OpenNetFile3(lpszFile, tsType) {
    if (isIE) {
        try {
            fs.OpenNetFile3(lpszFile, tsType);
        } catch (e) {
            alert(e.message);
        }
    } else {
        // 转编码
        var src = lpszFile;
        var dest = base64encode(utf16to8(src));
        lpszFile = dest;

        var jsonT = {
            "fileURI": lpszFile,
            "flag": tsType
        };
        var msgSend = '1|OpenNetFile3|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "OpenNetFile3") {
                var json = eval("(" + args[2] + ")");
                if (json.ret == 0) {
                    //alert("open success");
                } else {
                    alert(json.msg);
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function SignTextByKeyword2(keyword, text, x, y, type, size, callback) {
    if (isIE) {
        try {
            var result = fs.SignTextByKeyword2(keyword, text, x, y, type, size);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = keyword;
        var dest = base64encode(utf16to8(src));
        keyword = dest;
        src = text;
        dest = base64encode(utf16to8(src));
        text = dest;
        var jsonT = {
            "keyword": keyword.toString(),
            "text": text.toString(),
            "xoff": x.toString(),
            "yoff": y.toString(),
            "type": type.toString(),
            "size": size.toString()
        };
        var msgSend = '1|SignTextByKeyword2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SignTextByKeyword2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function SetCloudApp(nEnable, appId, appKey, callback) {
    if (isIE) {
        try {
            var result = fs.SetCloudApp(nEnable, appId, appKey);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = appId;
        var dest = base64encode(utf16to8(src));
        appId = dest;
        src = appKey;
        dest = base64encode(utf16to8(src));
        appKey = dest;
        var jsonT = {
            "flag": nEnable.toString(),
            "code": appId,
            "key": appKey
        };
        var msgSend = '1|SetCloudApp|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SetCloudApp") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function HandSignKeywordByOpt(keyword, lHandSignOption, callback) {
    if (isIE) {
        try {
            var result = fs.HandSignKeywordByOpt(keyword, lHandSignOption);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = keyword;
        var dest = base64encode(utf16to8(src));
        keyword = dest;

        var jsonT = {
            "keyword": keyword,
            "flag": lHandSignOption
        };
        var msgSend = '1|HandSignKeywordByOpt|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "HandSignKeywordByOpt") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function CollectHandSign(callback) {
    if (isIE) {
        try {
            var result = fs.CollectHandSign();
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, "");
        }
    } else {
        var msgSend = '1|CollectHandSign|';
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "CollectHandSign") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], "");
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InitHandSign(pval, callback) {
    if (isIE) {
        try {
            var result = fs.InitHandSign(pval);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {       

        var jsonT = {
            "hand": pval
        };
        var msgSend = '1|InitHandSign|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InitHandSign") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function HandSignKeywordInCache(pval, callback) {
    if (isIE) {
        try {
            var result = fs.HandSignKeywordInCache(pval);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = pval;
        var dest = base64encode(utf16to8(src));
        pval = dest;

        var jsonT = {
            "keyword": pval
        };
        var msgSend = '1|HandSignKeywordInCache|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "HandSignKeywordInCache") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

/////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function SealKeyWord2(lpszkeyword, lX, lY, eSealTsType, callback) {
    if (isIE) {
        try {
            var result = fs.SealKeyWord2(lpszkeyword, lX, lY, eSealTsType);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = lpszkeyword;
        var dest = base64encode(utf16to8(src));
        lpszkeyword = dest;

        var jsonT = {
            "keyword": lpszkeyword,
            "x": lX,
            "y": lY,
            "flag": eSealTsType
        };
        var msgSend = '1|SealKeyWord2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "SealKeyWord2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertSealInfo3(sealInfo, lX, lY, callback) {
    if (isIE) {
        try {
            var result = fs.InsertSealInfo3(sealInfo, lX, lY);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "sealinfo": sealInfo,
            "x": lX,
            "y": lY
        };
        var msgSend = '1|InsertSealInfo3|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertSealInfo3") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function VerifyUserPin(lpszUserCode, lpszPwd, callback) {
    if (isIE) {
        try {
            var result = fs.VerifyUserPin(sealInfo, lX, lY);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, 0);
        }
    } else {
        var src = lpszUserCode;
        var dest = base64encode(utf16to8(src));
        lpszUserCode = dest;
        src = lpszPwd;
        dest = base64encode(utf16to8(src));
        lpszPwd = dest;

        var jsonT = {
            "code": lpszUserCode,
            "pwd": lpszPwd
        };
        var msgSend = '1|VerifyUserPin|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "VerifyUserPin") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function ListSealByBusinessCode(lpszUserCode, callback) {
    if (isIE) {
        try {
            var result = fs.ListSealByBusinessCode(sealInfo, lX, lY);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var src = lpszUserCode;
        var dest = base64encode(utf16to8(src));
        lpszUserCode = dest;

        var jsonT = {
            "code": lpszUserCode
        };
        var msgSend = '1|ListSealByBusinessCode|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "ListSealByBusinessCode") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function InsertHandSignByPos2(lStart, lEnd, lX, lY, callback) {
    if (isIE) {
        try {
            var result = fs.InsertHandSignByPos2(lStart, lEnd, lX, lY);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "start": lStart.toString(),
            "end": lEnd.toString(),
            "x": lX.toString(),
            "y": lY.toString(),
        };
        var msgSend = '1|InsertHandSignByPos2|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "InsertHandSignByPos2") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetPageWidth(lPage, callback) {
    if (isIE) {
        try {
            var result = fs.GetPageWidth(lPage);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, 0);
        }
    } else {
        var jsonT = {
            "page": lPage.toString(),
        };
        var msgSend = '1|GetPageWidth|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetPageWidth") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error, string result)
function GetPageHeight(lPage, callback) {
    if (isIE) {
        try {
            var result = fs.GetPageHeight(lPage);
            callback(true, "", result);
        } catch (e) {
            callback(false, e.message, 0);
        }
    } else {
        var jsonT = {
            "page": lPage.toString(),
        };
        var msgSend = '1|GetPageHeight|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "GetPageHeight") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg, json.result);
            } else
                callback(false, "Unknown response " + args[1], 0);
        }
    }
}

//////////////////////////////////////////////////////////////////////////
// 回调函数原型：function callback(boolean success, string error)
function HandSignPosInCache(lStart, lEnd, lX, lY, callback) {
    if (isIE) {
        try {
            var result = fs.HandSignPosInCache(lStart, lEnd, lX, lY);
            callback(true, "");
        } catch (e) {
            callback(false, e.message);
        }
    } else {
        var jsonT = {
            "start": lStart.toString(),
            "end": lEnd.toString(),
            "x": lX.toString(),
            "y": lY.toString(),
        };
        var msgSend = '1|HandSignPosInCache|' + JSON.stringify(jsonT);
        socket.send(msgSend);
        handler.onmessage = function (msg) {
            var args = msg.data.split('|');
            if (args[1] == "HandSignPosInCache") {
                var json = eval("(" + args[2] + ")");
                callback(json.ret == 0, json.msg);
            } else
                callback(false, "Unknown response " + args[1]);
        }
    }
}









//////////////////////////////////////////////////////////////////////////
// 浏览器跟随检测
function DetectZoom() {
    var ratio = 0,
        screen = window.screen,
        ua = navigator.userAgent.toLowerCase();

    if (window.devicePixelRatio !== undefined) {
        ratio = window.devicePixelRatio;
        var r = {
            ratio: ratio
        }
        if (window.outerWidth !== undefined && window.innerWidth !== undefined) {
            r.browserRatio = window.outerWidth / window.innerWidth;
            r.windowsRatio = ratio / r.browserRatio;
        }
        //console.log("ratio: " + JSON.stringify(r))
        return r
    }
    else if (~ua.indexOf('msie')) {
        if (screen.deviceXDPI && screen.logicalXDPI) {
            ratio = screen.deviceXDPI / screen.logicalXDPI;
            return {
                ratio: ratio
            }
        }
    }
    else if (window.outerWidth !== undefined && window.innerWidth !== undefined) {
        ratio = window.outerWidth / window.innerWidth;
        return {
            ratio: ratio
        }
    }

    /*if (ratio) {
        //ratio = Math.round(ratio * 100);
        ratio = ratio;// * 100;
        
    } */
    return {
        ratio: 1
    }
};

function GetElementSrceenPosAndBrowserStateDpiAwareness(element) {
    var divRect = element.getBoundingClientRect();
    var x = divRect.left;
    var y = divRect.top;
    var w = divRect.width;
    var h = divRect.height;

    //浏览器是否最大化
    var isMax = (window.outerHeight === screen.availHeight && window.outerWidth === screen.availWidth);

    var ratio = DetectZoom();
    if (ratio.browserRatio !== 1 && ratio.windowsRatio) {
        //如果是浏览器设置了缩放，则outerHeight不变，而innerHeight会按照浏览器缩放比例缩小。所以要innerHeight * browserRatio, 才可以计算出工具栏大小
        var toolbarH = window.outerHeight - window.innerHeight * ratio.browserRatio;

        var diffH = isMax ? 0 : 5;  //非最大化时窗口最上面多出了几个像素
        y = y - diffH;
        //浏览器最大化与非最大时下面的值不同，需要将此偏差值计算进去
        var diffW = (window.outerWidth - window.innerWidth * ratio.browserRatio) / 2;

        y = Math.ceil(window.screenTop * ratio.windowsRatio + toolbarH * ratio.windowsRatio + y * ratio.ratio);  //工具栏并没有随浏览器缩放比例缩放，所以工具栏*windows缩放。
        x = Math.ceil(window.screenLeft * ratio.windowsRatio + diffW * ratio.windowsRatio + x * ratio.ratio);
        w = Math.ceil(w * ratio.ratio);
        h = Math.ceil(h * ratio.ratio);
    } else {
        // window.innerHeight: W3C-不包括菜单栏、工具栏以及滚动条等的浏览器窗口高度
        // window.outerHeight: 整个浏览器窗口的高度
        var toolbarH = window.outerHeight - window.innerHeight;

        var diffH = isMax ? 0 : 5;  //非最大化时窗口最上面多出了几个像素
        y = y - diffH;
        //浏览器最大化与非最大时下面的值不同，需要将此偏差值计算进去
        var diffW = (window.outerWidth - window.innerWidth) / 2;
        y = Math.ceil((window.screenTop + toolbarH + y) * ratio.ratio);
        x = Math.ceil((window.screenLeft + x + diffW) * ratio.ratio);
        w = Math.ceil(w * ratio.ratio);
        h = Math.ceil(h * ratio.ratio);
    }
    return { x: x, y: y, w: w, h: h, isMax: isMax, nshow: 4 };
}
//////////////////////////////////////////////////////////////////////////
// 转base64相关
var base64EncodeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
var base64DecodeChars = new Array(
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
    -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
    -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
    41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1);

function base64encode(str) {
    var out, i, len;
    var c1, c2, c3;

    len = str.length;
    i = 0;
    out = "";
    while (i < len) {
        c1 = str.charCodeAt(i++) & 0xff;
        if (i == len) {
            out += base64EncodeChars.charAt(c1 >> 2);
            out += base64EncodeChars.charAt((c1 & 0x3) << 4);
            out += "==";
            break;
        }
        c2 = str.charCodeAt(i++);
        if (i == len) {
            out += base64EncodeChars.charAt(c1 >> 2);
            out += base64EncodeChars.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));
            out += base64EncodeChars.charAt((c2 & 0xF) << 2);
            out += "=";
            break;
        }
        c3 = str.charCodeAt(i++);
        out += base64EncodeChars.charAt(c1 >> 2);
        out += base64EncodeChars.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));
        out += base64EncodeChars.charAt(((c2 & 0xF) << 2) | ((c3 & 0xC0) >> 6));
        out += base64EncodeChars.charAt(c3 & 0x3F);
    }
    return out;
}

function base64decode(str) {
    var c1, c2, c3, c4;
    var i, len, out;

    len = str.length;
    i = 0;
    out = "";
    while (i < len) {
        /* c1 */
        do {
            c1 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
        } while (i < len && c1 == -1);
        if (c1 == -1)
            break;

        /* c2 */
        do {
            c2 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
        } while (i < len && c2 == -1);
        if (c2 == -1)
            break;

        out += String.fromCharCode((c1 << 2) | ((c2 & 0x30) >> 4));

        /* c3 */
        do {
            c3 = str.charCodeAt(i++) & 0xff;
            if (c3 == 61)
                return out;
            c3 = base64DecodeChars[c3];
        } while (i < len && c3 == -1);
        if (c3 == -1)
            break;

        out += String.fromCharCode(((c2 & 0XF) << 4) | ((c3 & 0x3C) >> 2));

        /* c4 */
        do {
            c4 = str.charCodeAt(i++) & 0xff;
            if (c4 == 61)
                return out;
            c4 = base64DecodeChars[c4];
        } while (i < len && c4 == -1);
        if (c4 == -1)
            break;
        out += String.fromCharCode(((c3 & 0x03) << 6) | c4);
    }
    return out;
}

function utf16to8(str) {
    var out, i, len, c;

    out = "";
    len = str.length;
    for (i = 0; i < len; i++) {
        c = str.charCodeAt(i);
        if ((c >= 0x0001) && (c <= 0x007F)) {
            out += str.charAt(i);
        } else if (c > 0x07FF) {
            out += String.fromCharCode(0xE0 | ((c >> 12) & 0x0F));
            out += String.fromCharCode(0x80 | ((c >> 6) & 0x3F));
            out += String.fromCharCode(0x80 | ((c >> 0) & 0x3F));
        } else {
            out += String.fromCharCode(0xC0 | ((c >> 6) & 0x1F));
            out += String.fromCharCode(0x80 | ((c >> 0) & 0x3F));
        }
    }
    return out;
}

function utf8to16(str) {
    var out, i, len, c;
    var char2, char3;

    out = "";
    len = str.length;
    i = 0;
    while (i < len) {
        c = str.charCodeAt(i++);
        switch (c >> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                // 0xxxxxxx
                out += str.charAt(i - 1);
                break;
            case 12:
            case 13:
                // 110x xxxx   10xx xxxx
                char2 = str.charCodeAt(i++);
                out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
            case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                char2 = str.charCodeAt(i++);
                char3 = str.charCodeAt(i++);
                out += String.fromCharCode(((c & 0x0F) << 12) |
                    ((char2 & 0x3F) << 6) |
                    ((char3 & 0x3F) << 0));
                break;
        }
    }

    return out;
}

function CharToHex(str) {
    var out, i, len, c, h;

    out = "";
    len = str.length;
    i = 0;
    while (i < len) {
        c = str.charCodeAt(i++);
        h = c.toString(16);
        if (h.length < 2)
            h = "0" + h;

        out += "\\x" + h + " ";
        if (i > 0 && i % 8 == 0)
            out += "\r\n";
    }

    return out;
}

var Base64 = {
    _keyStr: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

    encode: function(e) {
      var t = "";
      var n, r, i, s, o, u, a;
      var f = 0;
      e = Base64._utf8_encode(e);
      while (f < e.length) {
        n = e.charCodeAt(f++);
        r = e.charCodeAt(f++);
        i = e.charCodeAt(f++);
        s = n >> 2;
        o = (n & 3) << 4 | r >> 4;
        u = (r & 15) << 2 | i >> 6;
        a = i & 63;
        if (isNaN(r)) {
          u = a = 64
        } else if (isNaN(i)) {
          a = 64
        }
        t = t + this._keyStr.charAt(s) + this._keyStr.charAt(o) + this._keyStr.charAt(u) + this._keyStr.charAt(a)
      }
      return t
    },

    decode: function(e) {
      var t = "";
      var n, r, i;
      var s, o, u, a;
      var f = 0;
      e = e.replace(/[^A-Za-z0-9+/=]/g, "");
      while (f < e.length) {
        s = this._keyStr.indexOf(e.charAt(f++));
        o = this._keyStr.indexOf(e.charAt(f++));
        u = this._keyStr.indexOf(e.charAt(f++));
        a = this._keyStr.indexOf(e.charAt(f++));
        n = s << 2 | o >> 4;
        r = (o & 15) << 4 | u >> 2;
        i = (u & 3) << 6 | a;
        t = t + String.fromCharCode(n);
        if (u != 64) {
          t = t + String.fromCharCode(r)
        }
        if (a != 64) {
          t = t + String.fromCharCode(i)
        }
      }
      t = Base64._utf8_decode(t);
      return t
    },

    _utf8_encode: function(e) {
      e = e.replace(/rn/g, "n");
      var t = "";
      for (var n = 0; n < e.length; n++) {
        var r = e.charCodeAt(n);
        if (r < 128) {
          t += String.fromCharCode(r)
        } else if (r > 127 && r < 2048) {
          t += String.fromCharCode(r >> 6 | 192);
          t += String.fromCharCode(r & 63 | 128)
        } else {
          t += String.fromCharCode(r >> 12 | 224);
          t += String.fromCharCode(r >> 6 & 63 | 128);
          t += String.fromCharCode(r & 63 | 128)
        }
      }
      return t
    },

    _utf8_decode: function(e) {
      var t = "";
      var n = 0;
      var r = c1 = c2 = 0;
      while (n < e.length) {
        r = e.charCodeAt(n);
        if (r < 128) {
          t += String.fromCharCode(r);
          n++
        } else if (r > 191 && r < 224) {
          c2 = e.charCodeAt(n + 1);
          t += String.fromCharCode((r & 31) << 6 | c2 & 63);
          n += 2
        } else {
          c2 = e.charCodeAt(n + 1);
          c3 = e.charCodeAt(n + 2);
          t += String.fromCharCode((r & 15) << 12 | (c2 & 63) << 6 | c3 & 63);
          n += 3
        }
      }
      return t
    }
  };
//////////////////////////////////////////////////////////////////////////