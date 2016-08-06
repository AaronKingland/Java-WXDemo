package com.wxpay.action;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.opensymphony.xwork2.ActionContext;

public class WXPayAction {

	/**
	 * 微信支付成功的回调
	 * 
	 * @return
	 * @throws Exception
	 */
	public String wXPayBack() throws Exception {
		ActionContext context = ActionContext.getContext();
		if (context == null) {
			return "fail";
		}
		// 微信回调回来的是xml格式的文档，最后一个大坑，擦，微信处处是坑。
		// 通过parseXml直接将request请求转换成xml再转换成map，然后就可以键值对取值了
		HttpServletRequest request = (HttpServletRequest) ActionContext
				.getContext().get(ServletActionContext.HTTP_REQUEST);
		Map<String, String> map = parseXml(request);

		System.out.println(map.get("return_code") + "wxxwwxxwxwwwwxwxwxw");
		System.out.println(map.get("result_code") + "wxxwwxxwxwwwwxwxwxw");
		System.out.println(map.get("out_trade_no") + "wxxwwxxwxwwwwxwxwxw");
		System.out.println(map.get("transaction_id") + "wxxwwxxwxwwwwxwxwxw");
		if (map.get("return_code").equals("SUCCESS")) {
			if (map.get("out_trade_no") != null) {
				// TODO 微信支付成功，处理自己的业务逻辑，更改订单状态上传订单号之类的
				return "success";
			}
		}
		return "fail";
	}

	public static Map<String, String> parseXml(HttpServletRequest request)
			throws Exception {
		// 解析结果存储在HashMap
		Map<String, String> map = new HashMap<String, String>();
		InputStream inputStream = request.getInputStream();
		// 读取输入流
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
		// 得到xml根元素
		Element root = document.getRootElement();
		// 得到根元素的所有子节点
		List<Element> elementList = root.elements();

		// 遍历所有子节点
		for (Element e : elementList)
			map.put(e.getName(), e.getText());

		// 释放资源
		inputStream.close();
		inputStream = null;

		return map;
	}

}
