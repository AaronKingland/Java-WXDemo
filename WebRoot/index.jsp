<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.wx.component.*"%>
<%
	Map<String, String> payentMent=new HashMap<String,String>();
String check=new TopayAction().WxJsApiCheck();
%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
	+ request.getServerName() + ":" + request.getServerPort()
	+ path + "/";
%>
<%@ page import="com.tencent.WXPay.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="viewport"
	content="width=device-width, initial-scale=1, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<title>微信支付Demo</title>
<link rel="stylesheet" href="css/bootstrap.css" type="text/css" />
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/bootstrap.js"></script>
<script type="text/javascript" src="js/sha1.js"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"
	type="text/javascript" charset="utf-8"></script>
<link rel="stylesheet" href="css/pay.css" type="text/css" />


<script type="text/javascript">
	jQuery(function() {

		/**
		*第一步：配置微信支付的相关参数
		*check是直接写的Java代码中的内容
		*
		*/
		wx.config({
		    debug:false // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
		    <%=","+check+","%>// 必填，签名，见附录1
		    jsApiList: ['chooseWXPay'],// 必填，需要使用的JS接口列表，所有JS接口列表见附录2
		});
		wx.ready(function(){
			
		});
		wx.error(function(res){
			alert(res);
		    // config信息验证失败会执行error函数，如签名过期导致验证失败，具体错误信息可以打开config的debug模式查看，也可以在返回的res参数中查看，对于SPA可以在这里更新签名。
		});
		/**
		*第二步：获取openId的第一步获取code
		*这个链接不能再java 中直接通过访问链接来获取返回数据，因为这个链接是只有微信浏览器才能读取的
		*所以只能跳转，来获取code
		*state可以作为可选参数传递，用于上传自己业务需要的参数，我为了上传两个参数，所以将参数拼接了，到时候再解析。
		*因为state只能是数字或字母，所以我用“CUT”作为间隔
		*redirect_uri=http://www.{example}.com/{project_name}/Dopay.action
		*这个是该链接确认后的回调地址，{example}写自己的服务器程序域名，{project_name}写自己的项目名。Dopay.action在Struts中已经配置好
		*
		*/
		 $("#wx-pay").click(
		 	function() {		 
		 	var payCode = $(".payCode").val();
		 	var fee = $(".pay-fee").val();	
		 window.location.href="http://open.weixin.qq.com/connect/oauth2/authorize?appid=wxb9ad6c0c319cf8c2&redirect_uri=http://www.{example}.com/{project_name}/Dopay.action&response_type=code&scope=snsapi_base&state="+fee+"CUT"+payCode+"#wechat_redirect";
		});
	});
</script>
</head>

<body>

	<div id="pay-amount">

		支付金额：￥ <span id="totalFee">0.01</span> <input class="payCode"
			value="0.01" type="hidden" /> <input class="pay-fee"
			value="291871409832893729" type="hidden" />
	</div>

	<div id="pay-choose">
		<div class="radio">
			<div class="pay-label" id="wx-pay">
				<div class="icon weixin-icon"></div>
				<div class="pay-title">微信支付</div>
				<div class="radio"></div>
				<div class="line"></div>
			</div>
		</div>
	</div>
	<div id="ali-div"></div>
</body>
</html>
