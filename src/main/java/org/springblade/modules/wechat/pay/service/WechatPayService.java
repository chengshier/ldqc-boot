package org.springblade.modules.wechat.pay.service;

import org.springblade.modules.wechat.pay.dto.PayNotifyResult;
import org.springblade.modules.wechat.pay.dto.PayOrderCreateCmd;
import org.springblade.modules.wechat.pay.dto.RefundCreateCmd;
import org.springblade.modules.wechat.pay.dto.WechatOrderStatusResult;
import org.springblade.modules.wechat.pay.dto.WechatPrepayResult;
import org.springblade.modules.wechat.pay.dto.WechatRefundResult;
import org.springblade.modules.wechat.pay.dto.WechatRefundStatusResult;

import java.util.Map;

public interface WechatPayService {

	/**
	 * 创建微信 JSAPI 预支付订单，并返回前端调起支付所需参数。
	 *
	 * @param cmd 下单参数
	 * @return 预支付结果
	 */
	WechatPrepayResult createJsapiOrder(PayOrderCreateCmd cmd);

	/**
	 * 验证并解析支付回调数据（支持 v3 回调密文解密）。
	 *
	 * @param body    回调报文体
	 * @param headers 回调请求头
	 * @return 解析后的支付通知结果
	 */
	PayNotifyResult verifyAndParsePayNotify(String body, Map<String, String> headers);

	/**
	 * 按商户订单号查询微信支付订单状态。
	 *
	 * @param outTradeNo 商户订单号
	 * @return 订单状态
	 */
	WechatOrderStatusResult queryOrder(String outTradeNo);

	/**
	 * 按商户订单号关闭未支付订单。
	 *
	 * @param outTradeNo 商户订单号
	 */
	void closeOrder(String outTradeNo);

	/**
	 * 发起退款申请。
	 *
	 * @param cmd 退款参数
	 * @return 退款结果
	 */
	WechatRefundResult createRefund(RefundCreateCmd cmd);

	/**
	 * 按商户退款单号查询退款状态。
	 *
	 * @param outRefundNo 商户退款单号
	 * @return 退款状态
	 */
	WechatRefundStatusResult queryRefund(String outRefundNo);
}
