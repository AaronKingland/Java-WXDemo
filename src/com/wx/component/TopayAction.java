package com.wx.component;

import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.struts2.ServletActionContext;

import com.google.gson.Gson;
import com.wx.utils.GetWxOrderno;
import com.wx.utils.RequestHandler;
import com.wx.utils.Sha1Util;
import com.wx.utils.TenpayUtil;

/**
 * 欢迎大家关注双面人的网络世界博客 http://blog.csdn.net/qq_30997391
 * 大家微信支付时会遇到各种坑，但是我这个demo基本把所有的坑都遇到了，然后写出来的，应该没太大问题，
 * 微信要在微信的浏览器才能测试，所以每次都要发布到自己的服务器然后用微信去测试。
 * 测试过程中可以看看这个页面的log，会发现到底是哪里出了问题。然后更改对应的内容就好了。
 * 只要把参数配置正确了，参照微信的官方文档，微信支付就不会有问题了。
 * 
 * @author aaron
 * 
 */
public class TopayAction extends HttpServlet {

	private String timeStamp;
	private String nonceStr;
	private String packageStr;

	private String paySign;

	/**
	 * 获取JSAPI签名
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public String WxJsApiCheck() {

		String jsapi_ticket = getJsapiTicket();// 看清楚.这是ticket..用token在https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi里换的
		String nonce_str = Sha1Util.getNonceStr();// 随机字符串
		String timestamp = Sha1Util.getTimeStamp();// 时间戳
		String appid = "wxb9*************";// APPID,谁在问我为什么报没有APPID就去死吧
		String url = "www.{example}.com/{project_name}/pay.jsp";// 发起支付的前端页面的URL地址.而且...而且必须在微信支付里面配置才行!!!
		String sign = null;
		try {
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("jsapi_ticket", jsapi_ticket);
			packageParams.put("noncestr", nonce_str);
			packageParams.put("timestamp", timestamp);
			packageParams.put("url", url);
			sign = Sha1Util.createSHA1Sign(packageParams);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String res = "appId : \"" + appid + "\",timestamp : \"" + timestamp
				+ "\", nonceStr : \"" + nonce_str + "\", signature : \"" + sign
				+ "\"";
		return res;
	}

	private String getJsapiTicket() {
		try {
			// 直接访问url来获取返回数据
			// 这里必须是https，该死的微信非得用https，否则获取不到数据的
			String returnData = getReturnData("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxb9**********&secret=*******************");
			Gson gson = new Gson();
			TokenClass tokenClass = gson.fromJson(returnData, TokenClass.class);

			String token = tokenClass.getAccess_token();
			String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="
					+ token + "&type=jsapi";
			String returnData2 = getReturnData(url);
			Ticket ticket = gson.fromJson(returnData2, Ticket.class);
			if (ticket.getErrcode() == 0) {
				return ticket.getTicket();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "kgt8ON7yVITDhtdwci0qeTPi4uLRAN-5WPf5pygQ7_8OEmKM518hH7rc9QLKKqTeV2LY4kIoGCVbaXKf3oIwgw";
	}

	/**
	 * 内部类，用于Gson解析返回的数据，获取内容
	 * 
	 * @author aaron
	 * 
	 */
	private class TokenClass {

		private String access_token;
		private String expires_in;

		public String getAccess_token() {
			return access_token;
		}

		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		public String getExpires_in() {
			return expires_in;
		}

		public void setExpires_in(String expires_in) {
			this.expires_in = expires_in;
		}

	}

	/**
	 * 内部类，用于Gson解析返回的数据，获取内容
	 * 
	 * @author aaron
	 * 
	 */
	private class Ticket {

		private Integer errcode;
		private String errmsg;
		private String ticket;
		private String expires_in;

		public String getExpires_in() {
			return expires_in;
		}

		public void setExpires_in(String expires_in) {
			this.expires_in = expires_in;
		}

		public String getErrmsg() {
			return errmsg;
		}

		public void setErrmsg(String errmsg) {
			this.errmsg = errmsg;
		}

		public String getTicket() {
			return ticket;
		}

		public void setTicket(String ticket) {
			this.ticket = ticket;
		}

		public Integer getErrcode() {
			return errcode;
		}

		public void setErrcode(Integer errcode) {
			this.errcode = errcode;
		}

	}

	// 支付页面获取openId后获取的值
	private String code;
	private String state;
	private String totalFee;
	private String payCodeSuccess;

	public String dopay() throws Exception {
		String[] split = state.split("CUT");
		String payCode = split[1];

		// 网页授权后获取传递的参数
		String orderNo = payCode;

		// 金额转化为分为单位，所以传的时候是分的单位，换成元为单位就加两个零
		// 因为我的业务都是整数金额，所以直接拼接两个零，如果涉及到小数，就要进行转换和乘法
		String money = split[0] + "00";

		// 商户相关资料
		String appid = "wxb**************";
		String appsecret = "15beb7f******************";
		String mch_id = "1373*****";// 邮件里的商户号
		String partnerkey = "sdjsdhfksjd**********************";// 在微信商户平台pay.weixin.com里自己写入的那个key

		String openId = getOpenId();
		// 用oath授权得到的openid

		// 获取openId后调用统一支付接口https://api.mch.weixin.qq.com/pay/unifiedorder
		String currTime = TenpayUtil.getCurrTime();
		// 8位日期
		String strTime = currTime.substring(8, currTime.length());
		// 四位随机数
		String strRandom = TenpayUtil.buildRandom(4) + "";
		// 10位序列号,可以自行调整。
		String strReq = strTime + strRandom;

		// 子商户号 非必输
		// String sub_mch_id="";
		// 设备号 非必输
		String device_info = "";
		// 随机数
		String nonce_str = strReq;
		// 商品描述
		// String body = describe;

		// 商品描述根据情况修改
		String body = "微信支付Demo";
		// 附加数据
		// String attach = userId;
		// 商户订单号
		String out_trade_no = orderNo;
		// int intMoney = Integer.parseInt(finalmoney);

		// 总金额以分为单位，不带小数点
		// int total_fee = intMoney;
		// 订单生成的机器 IP
		HttpServletRequest request = ServletActionContext.getRequest();
		String ipAddress = request.getRemoteAddr();
		if (ipAddress.equals("0:0:0:0:0:0:0:1%0")) {
			ipAddress = "172.168.0.1";
		}
		String spbill_create_ip = ipAddress;
		// 订 单 生 成 时 间 非必输
		// String time_start ="";
		// 订单失效时间 非必输
		// String time_expire = "";
		// 商品标记 非必输
		// String goods_tag = "";

		// 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
		String notify_url = "http://www.{example}.com/{project_name}/WXPayBack.action";

		String trade_type = "JSAPI";
		String openid = openId;
		// 非必输
		// String product_id = "";
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		// packageParams.put("attach", attach);
		packageParams.put("out_trade_no", out_trade_no);

		// 这里写的金额为1 分到时修改
		if (money != null) {
			packageParams.put("total_fee", money);
		} else {
			packageParams.put("total_fee", "1000");
		}
		// packageParams.put("total_fee", "finalmoney");
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", notify_url);

		packageParams.put("trade_type", trade_type);
		packageParams.put("openid", openid);

		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.init(appid, appsecret, partnerkey);

		String sign = reqHandler.createSign(packageParams);
		String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>"
				+ mch_id + "</mch_id>" + "<nonce_str>" + nonce_str
				+ "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>"
				+ "<body><![CDATA[" + body + "]]></body>"
				+ "<out_trade_no>"
				+ out_trade_no
				+ "</out_trade_no>"
				+

				// 金额，这里写的1 分到时修改
				"<total_fee>"
				+ money
				+ "</total_fee>"
				+
				// "<total_fee>"+finalmoney+"</total_fee>"+
				"<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>"
				+ "<notify_url>" + notify_url + "</notify_url>"
				+ "<trade_type>" + trade_type + "</trade_type>" + "<openid>"
				+ openid + "</openid>" + "</xml>";
		System.out.println(xml);
		String allParameters = "";
		try {
			allParameters = reqHandler.genPackage(packageParams);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		Map<String, Object> dataMap2 = new HashMap<String, Object>();
		String prepay_id = "";
		try {
			prepay_id = new GetWxOrderno().getPayNo(createOrderURL, xml);
			if (prepay_id.equals("")) {
				System.out.println("统一支付接口获取预支付订单出错");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("prepay_id:" + prepay_id);
		SortedMap<String, String> finalpackage = new TreeMap<String, String>();
		String appid2 = appid;
		String timestamp = Sha1Util.getTimeStamp();
		String nonceStr2 = nonce_str;
		String prepay_id2 = "prepay_id=" + prepay_id;
		String packages = prepay_id2;
		finalpackage.put("appId", appid2);
		finalpackage.put("timeStamp", timestamp);
		finalpackage.put("nonceStr", nonceStr2);
		finalpackage.put("package", packages);
		finalpackage.put("signType", "MD5");
		String finalsign = reqHandler.createSign(finalpackage);
		timeStamp = timestamp;
		nonceStr = nonceStr2;
		packageStr = packages;
		paySign = finalsign;
		payCodeSuccess = out_trade_no;
		String str = "timestamp:\""
				+ timestamp // 这里的也是小写~~
				+ "\",nonceStr:\"" + nonceStr2 + "\",package:\"" + packages
				+ "\",signType: \"MD5" + "\",paySign:\"" + finalsign + "\"";
		return "success";
	}

	private String getOpenId() throws Exception {
		System.out
				.println("************************getOpenId*********************");
		System.out.println("+++++++++++" + getCode() + "++++++++++");
		if (getCode() != null) {

			// 狗日的微信又是必须要https请求，千万不要搞错了
			String url = "https://api.weixin.qq.com/sns/oauth2/access_token?"
					+ "appid=wxb9**************"
					+ "&secret=15beb7f************************" + "&code="
					+ getCode() + "&grant_type=authorization_code";
			System.out.println(url);
			String returnData = getReturnData(url);
			System.out.println(returnData + "################");
			Gson gson = new Gson();
			OpenIdClass openIdClass = gson.fromJson(returnData,
					OpenIdClass.class);

			if (openIdClass.getOpenid() != null) {
				return openIdClass.getOpenid();
			}
		}
		return "oPDemv4uWCuW**************";
	}

	private class OpenIdClass {
		private String access_token;
		private String expires_in;
		private String refresh_token;
		private String openid;
		private String scope;
		private String unionid;

		public String getAccess_token() {
			return access_token;
		}

		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		public String getExpires_in() {
			return expires_in;
		}

		public void setExpires_in(String expires_in) {
			this.expires_in = expires_in;
		}

		public String getRefresh_token() {
			return refresh_token;
		}

		public void setRefresh_token(String refresh_token) {
			this.refresh_token = refresh_token;
		}

		public String getOpenid() {
			return openid;
		}

		public void setOpenid(String openid) {
			this.openid = openid;
		}

		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

		public String getUnionid() {
			return unionid;
		}

		public void setUnionid(String unionid) {
			this.unionid = unionid;
		}

		public String getErrcode() {
			return errcode;
		}

		public void setErrcode(String errcode) {
			this.errcode = errcode;
		}

		public String getErrmsg() {
			return errmsg;
		}

		public void setErrmsg(String errmsg) {
			this.errmsg = errmsg;
		}

		private String errcode;
		private String errmsg;
	}

	public String getReturnData(String urlString) throws Exception {
		String res = "";
		try {
			URL url = new URL(urlString);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url
					.openConnection();
			conn.connect();
			java.io.BufferedReader in = new java.io.BufferedReader(
					new java.io.InputStreamReader(conn.getInputStream(),
							"UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				res += line;
			}
			in.close();
		} catch (Exception e) {
			throw e;
		}
		return res;
	}

	@SuppressWarnings("unused")
	private String getCharsetFromResponse(HttpResponse ressponse) {
		// Content-Type:text/html; charset=GBK
		if (ressponse.getEntity() != null
				&& ressponse.getEntity().getContentType() != null
				&& ressponse.getEntity().getContentType().getValue() != null) {
			String contentType = ressponse.getEntity().getContentType()
					.getValue();
			if (contentType.contains("charset=")) {
				return contentType
						.substring(contentType.indexOf("charset=") + 8);
			}
		}
		return null;
	}

	public String getNonceStr() {
		return nonceStr;
	}

	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	public String getPackageStr() {
		return packageStr;
	}

	public void setPackageStr(String packageStr) {
		this.packageStr = packageStr;
	}

	public String getPaySign() {
		return paySign;
	}

	public void setPaySign(String paySign) {
		this.paySign = paySign;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPayCodeSuccess() {
		return payCodeSuccess;
	}

	public void setPayCodeSuccess(String payCodeSuccess) {
		this.payCodeSuccess = payCodeSuccess;
	}

}
