package org.springblade.modules.wechat.pay.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.modules.wechat.pay.dto.PayNotifyResult;
import org.springblade.modules.wechat.pay.dto.PayOrderCreateCmd;
import org.springblade.modules.wechat.pay.dto.RefundCreateCmd;
import org.springblade.modules.wechat.pay.dto.WechatOrderStatusResult;
import org.springblade.modules.wechat.pay.dto.WechatPrepayResult;
import org.springblade.modules.wechat.pay.dto.WechatRefundResult;
import org.springblade.modules.wechat.pay.dto.WechatRefundStatusResult;
import org.springblade.modules.wechat.pay.service.WechatPayService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@AllArgsConstructor
@RequestMapping("blade-wechat/pay")
@Tag(name = "微信支付", description = "微信支付接口")
public class WechatPayController {

	private final WechatPayService wechatPayService;
	private final StringRedisTemplate stringRedisTemplate;

	@PostMapping("/jsapi/prepay")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "创建JSAPI预支付订单")
	public R<WechatPrepayResult> prepay(@RequestBody PayOrderCreateCmd cmd) {
		return R.data(wechatPayService.createJsapiOrder(cmd));
	}

	@GetMapping("/order/{outTradeNo}")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "查询支付订单状态")
	public R<WechatOrderStatusResult> queryOrder(@PathVariable String outTradeNo) {
		return R.data(wechatPayService.queryOrder(outTradeNo));
	}

	@PostMapping("/order/{outTradeNo}/close")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "关闭支付订单")
	public R<Boolean> closeOrder(@PathVariable String outTradeNo) {
		wechatPayService.closeOrder(outTradeNo);
		return R.data(Boolean.TRUE);
	}

	@PostMapping("/refund")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "发起退款")
	public R<WechatRefundResult> createRefund(@RequestBody RefundCreateCmd cmd) {
		return R.data(wechatPayService.createRefund(cmd));
	}

	@GetMapping("/refund/{outRefundNo}")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "查询退款状态")
	public R<WechatRefundStatusResult> queryRefund(@PathVariable String outRefundNo) {
		return R.data(wechatPayService.queryRefund(outRefundNo));
	}

	@PostMapping("/notify")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "微信支付回调通知")
	public ResponseEntity<Map<String, String>> notify(@RequestBody String body, @RequestHeader Map<String, String> headers) {
		try {
			PayNotifyResult notifyResult = wechatPayService.verifyAndParsePayNotify(body, headers);
			String idempotentKey = buildNotifyIdempotentKey(notifyResult);
			Boolean firstHandle = stringRedisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 24, TimeUnit.HOURS);

			if (Boolean.TRUE.equals(firstHandle)) {
				// TODO 这里接入真实业务逻辑（订单状态更新、支付流水记录、积分/库存等后续动作）。
				// 建议在业务服务内再次做数据库层幂等保障（如 outTradeNo + tradeState 唯一约束）。
			}

			return ResponseEntity.ok(successAck());
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(failAck(ex.getMessage()));
		}
	}

	private String buildNotifyIdempotentKey(PayNotifyResult notifyResult) {
		String outTradeNo = notifyResult == null || notifyResult.getOutTradeNo() == null ? "unknown" : notifyResult.getOutTradeNo();
		String transactionId = notifyResult == null || notifyResult.getTransactionId() == null ? "unknown" : notifyResult.getTransactionId();
		return "wechat:pay:notify:" + outTradeNo + ":" + transactionId;
	}

	private Map<String, String> successAck() {
		Map<String, String> map = new HashMap<>();
		map.put("code", "SUCCESS");
		map.put("message", "成功");
		return map;
	}

	private Map<String, String> failAck(String message) {
		Map<String, String> map = new HashMap<>();
		map.put("code", "FAIL");
		map.put("message", message == null ? "处理失败" : message);
		return map;
	}
}

