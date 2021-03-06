package com.nbteam.hislite.payment.pay.service;

import com.alibaba.fastjson.JSONObject;
import com.nbteam.hislite.common.exception.BizException;
import com.nbteam.hislite.common.utils.*;
import com.nbteam.hislite.frame.config.gateway.GateWayService;
import com.nbteam.hislite.payment.api.common.constant.PaymentConstants;
import com.nbteam.hislite.payment.api.pay.dto.PaymentMerchant;
import com.nbteam.hislite.payment.api.pay.vo.*;
import com.nbteam.hislite.payment.api.exception.PaymentException;
import com.nbteam.hislite.payment.pay.mapper.PaymentMerchantMapper;
import com.nbteam.hislite.payment.pay.utils.PayServiceHandler;
import com.nbteam.hislite.payment.pay.utils.SignUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@PayServiceHandler(name="weixinPayService")
public class WeixinPayService extends IPayServiceHandler
{
	private static final Logger logger = LoggerFactory.getLogger(WeixinPayService.class);

	/** 证书地址 */
	@Value("${wx.merchant.key.file.path}")
	private String CERT_LOCAL_PATH;
	/** 统一下单支付通知url */
	@Value("${wx.pay.notify.url}")
	private String wxNotifyUrl;

	/** 微信统一下单支付url */
	@Value("${wx.unifiedorder.payed.url}")
	private String wxUnifiedOrderURL;

	/** 微信刷卡支付API */
	@Value("${wx.micro.pay.url}")
	private String wxMicroPayURL;

	/** 查询订单url */
	@Value("${wx.micro.orderquery.url}")
	private String wxOrderQueryURL;

	/** 微信撤销URL */
	@Value("${wx.micro.reverse.url}")
	private String wxReverseURL;

	/** 微信退款url */
	@Value("${wx.refund.url}")
	private String wxRefundURL;

	/** 退款单查询url */
	@Value("${wx.refundquery.url}")
	private String refundQueryURL;
	/** 扫码付关闭订单 */
	@Value("${wx.closeorder.url}")
	private String closeOrderURL;

	@Autowired
	private PaymentMerchantMapper paymentMerchantMapper;

	@Autowired
	private GateWayService gateWayService;

//	@Override
//	public F2FPayVo f2fPay(PaySerialVo vo, HcContext context) {
//		logger.info("sequece:{},  当面付WeixinPayServiceImpl.microPay{}", context.getMsgSeq(), toJSON(vo));
//		F2FPayVo result = new F2FPayVo();
//		result.setResultCode(PaymentConstants.RESULT_FAIL_CODE);
//		try {
//			MerchantInfo merchant = getMerchantInfoByKey(vo.getInsideMerchantId(), context);
//			Map<String, Object> paramMap = new HashMap<String, Object>();
//			if (StringUtils.isNotBlank(merchant.getServiceMerchantId())) {
//				paramMap.put("appid", merchant.getServiceAppId());
//				paramMap.put("mch_id", merchant.getServiceMerchantId());
//				paramMap.put("sub_appid", merchant.getAppId());
//				paramMap.put("sub_mch_id", merchant.getMerchantId());
//			} else {
//				paramMap.put("appid", merchant.getAppId());
//				paramMap.put("mch_id", merchant.getMerchantId());
//			}
//			paramMap.put("nonce_str", CommonUtil.CreateNoncestr());
//			paramMap.put("body", vo.getBody());
//			paramMap.put("out_trade_no", vo.getMerchantOrderId());
//			paramMap.put("total_fee", vo.getTotalFee());
//			paramMap.put("spbill_create_ip", vo.getSpbillCreateIp());
//			paramMap.put("auth_code", vo.getAuthCode());
//			WeixinF2FVo payResult = super.getWxRequestResult(wxMicroPayURL, paramMap, merchant.getPaymentKey(), WeixinF2FVo.class,
//					context);
//			if (!PaymentConstants.RETURN_CODE_SUCCESS.equals(payResult.getResult_code())
//					|| !PaymentConstants.RESULT_SUCCESS_CODE.equals(payResult.getResult_code())) {
//				logger.error("sequence:{}, 微信当面付发起失败：{}", context.getMsgSeq(), JSONObject.toJSONString(payResult));
//				// 订单状态未知
//				if (Objects.equals("SYSTEMERROR", payResult.getErr_code()) || Objects.equals("BANKERROR", payResult.getErr_code())
//						|| Objects.equals("USERPAYING", payResult.getErr_code())) {
//					result.setResultCode(PaymentConstants.RESULT_UNKNOW_CODE);
//				}
//				result.setResultMsg(
//						payResult.getReturn_msg() + "|" + payResult.getErr_code() + "-" + payResult.getErr_code_des());
//				return result;
//			}
//			result.setResultCode(PaymentConstants.RESULT_SUCCESS_CODE);
//			result.setOpenId(payResult.getOpenid());
//			if (StringUtils.isNotBlank(payResult.getTime_end())) {
//				result.setPayTime(DateTool.getSimpleDateTime().parse(payResult.getTime_end()));
//			}
//			result.setTotalFee(payResult.getTotal_fee());
//			result.setTradeType(payResult.getTrade_type());
//			result.setAcquireSerialNo(payResult.getTransaction_id());
//			result.setFeeType(payResult.getFee_type());
//			result.setBankType(payResult.getBank_type());
//			return result;
//		} catch (Exception e) {
//			logger.error("sequence:{}, 提交微信刷卡支付(当面付)异常", context.getMsgSeq(), e);
//			throw new WxPayException(WxPayException.SYSTEM_ERROR_CODE, "提交微信刷卡支付(当面付)异常" + e.getMessage());
//		} finally {
//			if (logger.isDebugEnabled()) {
//				logger.debug("sequence:{}, 提交微信刷卡支付(当面付)结束, 返回数据：{}", context.getMsgSeq(), JSONObject.toJSONString(result));
//			}
//		}
//	}
//
//	@Override
//	public PayQueryVo orderQuery(PaySerialVo vo, HcContext context) {
//		logger.info("sequece:{}, 订单状态查询接口：{}", context.getMsgSeq(), JSONObject.toJSONString(vo));
//		PayQueryVo result = new PayQueryVo();
//		result.setResultCode(PaymentConstants.RESULT_FAIL_CODE);
//		MerchantInfo merchant = getMerchantInfoByKey(vo.getInsideMerchantId(), context);
//		try {
//			Map<String, Object> paramMap = new HashMap<String, Object>();
//			if (StringUtils.isNotBlank(merchant.getServiceMerchantId())) {
//				paramMap.put("appid", merchant.getServiceAppId());
//				paramMap.put("mch_id", merchant.getServiceMerchantId());
//				paramMap.put("sub_appid", merchant.getAppId());
//				paramMap.put("sub_mch_id", merchant.getMerchantId());
//			} else {
//				paramMap.put("appid", merchant.getAppId());
//				paramMap.put("mch_id", merchant.getMerchantId());
//			}
//			paramMap.put("nonce_str", CommonUtil.CreateNoncestr());
//			if(StringUtils.isNotBlank(vo.getAcquireSerialNo())){
//				paramMap.put("transaction_id", vo.getAcquireSerialNo());
//			}else{
//				paramMap.put("out_trade_no", vo.getMerchantOrderId());
//			}
//
//			WeixinPayQueryVo queryResult = super.getWxRequestResult(wxOrderQueryURL, paramMap, merchant.getPaymentKey(),
//					WeixinPayQueryVo.class, context);
//			if (!PaymentConstants.RETURN_CODE_SUCCESS.equals(queryResult.getResult_code())
//					|| !PaymentConstants.RESULT_SUCCESS_CODE.equals(queryResult.getResult_code())) {
//				result.setResultMsg(
//						queryResult.getReturn_msg() + "|" + queryResult.getErr_code() + "-" + queryResult.getErr_code_des());
//				return result;
//			}
//			// 根据业务状态判断业务是否成功还是失败
//			result.setResultCode(PaymentConstants.RESULT_SUCCESS_CODE);
//			if (Objects.equals(PayMethodEnum.F2F.getCode(), vo.getPayMethod())) {
//				if (Objects.equals(WeixinTradeStatusEnum.REVOKED.getCode(), queryResult.getTrade_state())
//						|| Objects.equals(WeixinTradeStatusEnum.SUCCESS.getCode(), queryResult.getTrade_state())) {
//					result.setResultCode(PaymentConstants.RESULT_SUCCESS_CODE);
//				} else {
//					result.setResultCode(PaymentConstants.RESULT_FAIL_CODE);
//					result.setResultMsg("支付查询失败");
//				}
//			}
//			if (Objects.equals(WeixinTradeStatusEnum.PAYERROR.getCode(), queryResult.getTrade_state())
//					|| Objects.equals(WeixinTradeStatusEnum.USERPAYING.getCode(), queryResult.getTrade_state())) {
//				result.setResultCode(PaymentConstants.RESULT_UNKNOW_CODE);
//				result.setResultMsg("支付状态未知，请重新发起查询");
//			}
//			result.setOpenId(queryResult.getOpenid());
//			result.setAcquireSerialNo(queryResult.getTransaction_id());
//			result.setBankType(queryResult.getBank_type());
//			result.setMerchantOrderId(queryResult.getOut_trade_no());
//			if (StringUtils.isNotBlank(queryResult.getTime_end())) {
//				result.setPayedTime(DateTool.getSimpleDateTime().parse(queryResult.getTime_end()));
//			}
//			result.setTotalFee(queryResult.getTotal_fee());
//			return result;
//		} catch (Exception e) {
//			logger.error("sequence:{}, 微信订单查询异常{}", context.getMsgSeq(), e);
//			throw new WxPayException(WxPayException.SYSTEM_ERROR_CODE, "微信订单查询异常" + e.getMessage());
//		} finally {
//			if (logger.isDebugEnabled()) {
//				logger.debug("sequence:{}, 微信订单查询结束，查询结果：{}", context.getMsgSeq(), JSONObject.toJSONString(result));
//			}
//		}
//	}

	@Override
	public UnifiedOrderVo unifiedOrder(PaySerialVo vo) {
		if (logger.isDebugEnabled()) {
			logger.debug("创建微信统一下单开始：{}", JSONObject.toJSONString(vo));
		}
		UnifiedOrderVo result = new UnifiedOrderVo();
		PaymentMerchant merchant = paymentMerchantMapper.selectByPrimaryKey(vo.getInsideMerchantId());
		Map<String, Object> paramMap = new HashMap<>();
		String merchantId = null;
		String appId = null;
		paramMap = MapTool.getSOMap("device_info", vo.getDeviceInfo(), "nonce_str",
				RandomStringGenerator.getRandomStringByLength(20), "body", vo.getBody(), "attach", vo.getAttach(), "out_trade_no",
				vo.getMerchantOrderId(), "total_fee", vo.getTotalFee(), "spbill_create_ip", vo.getSpbillCreateIp(), "time_start",
				DateTool.getSimpleDateTime().format(new Date()), "notify_url", wxNotifyUrl, "trade_type",
				getTradeType(vo.getPayMethod()), "product_id", String.valueOf(vo.getPayOrderId()), "limit_pay",
				checkUserCredit(merchant.getSwitchs()));
		if (vo.getOverTime() != null) {
			paramMap.put("time_expire", DateTool.getSimpleDateTime().format(vo.getOverTime()));
		}
		if (StringUtils.isNotBlank(merchant.getServiceMerchantId())) {
			paramMap.putAll(MapTool.getSOMap("appid", merchant.getServiceAppId(), "mch_id", merchant.getServiceMerchantId(), "sub_appid",
							merchant.getAppId(), "sub_mch_id", merchant.getMerchantId(), "sub_openid", vo.getChannelUserId()));
			merchantId = merchant.getServiceMerchantId();
			appId = merchant.getServiceAppId();
		} else {
			paramMap.putAll(MapTool.getSOMap("appid", merchant.getAppId(), "mch_id", merchant.getMerchantId(), "openid",
					vo.getChannelUserId()));
			merchantId = merchant.getMerchantId();
			appId = merchant.getAppId();
		}
		try {
			WeixinUnifiedVo payResult = getWxRequestResult(wxUnifiedOrderURL, paramMap, merchant.getPaymentKey(),
					WeixinUnifiedVo.class);
			result.setResultCode(StringUtils.isBlank(payResult.getResult_code()) ? payResult.getReturn_code() : payResult.getResult_code());
			if (!PaymentConstants.RETURN_CODE_SUCCESS.equals(payResult.getResult_code())
					|| !PaymentConstants.RESULT_SUCCESS_CODE.equals(payResult.getResult_code())) {
				logger.error("微信发起退统一下单操作失败：{}", JSONObject.toJSONString(payResult));
				StringBuffer resultMsg = new StringBuffer();
				if (StringUtils.isNotBlank(payResult.getReturn_msg())) {
					resultMsg.append(payResult.getReturn_msg());
				}
				if (StringUtils.isNotBlank(payResult.getErr_code())) {
					resultMsg.append("|").append(payResult.getErr_code()).append("-").append(payResult.getErr_code_des());
				}
				result.setResultMsg(resultMsg.toString());
				return result;
			}
			result.setMerchantId(merchantId);
			result.setCodeUrl(payResult.getCode_url());
			result.setPrePayId(payResult.getPrepay_id());
			result.setTradeType(payResult.getTrade_type());
			result.setAppId(appId);
			result.setMerchantKey(merchant.getPaymentKey());
			return result;
		} catch (Exception e) {
			logger.error("提交微信统一支付异常", e);
			throw new PaymentException(PaymentException.SYSTEM_ERROR_CODE, "提交微信统一支付异常" + e.getMessage());
		} finally {
			if (logger.isDebugEnabled()) {
				logger.debug("提交微信统一支付下单结束：{}", JSONObject.toJSONString(result));
			}
		}
	}

//	@Override
//	public F2FRevokeVo revoke(PaySerialVo vo, HcContext context) {
//		logger.info("sequece:{}, 调用微信发起撤销操作{}", context.getMsgSeq(), JSONObject.toJSONString(vo));
//		F2FRevokeVo result = new F2FRevokeVo();
//		result.setResultCode(PaymentConstants.RESULT_FAIL_CODE);
//		try {
//			MerchantInfo merchant = getMerchantInfoByKey(vo.getInsideMerchantId(), context);
//			Map<String, Object> paramMap = new HashMap<String, Object>();
//			paramMap.put("nonce_str", CommonUtil.CreateNoncestr());
//			paramMap.put("transaction_id", vo.getAcquireSerialNo());
//			paramMap.put("out_trade_no", vo.getMerchantOrderId());
//			String merchantId = null;
//			if (StringUtils.isNotBlank(merchant.getServiceMerchantId())) {
//				paramMap.put("appid", merchant.getServiceAppId());
//				paramMap.put("mch_id", merchant.getServiceMerchantId());
//				paramMap.put("sub_appid", merchant.getAppId());
//				paramMap.put("sub_mch_id", merchant.getMerchantId());
//				merchantId = merchant.getServiceMerchantId();
//			} else {
//				paramMap.put("appid", merchant.getAppId());
//				paramMap.put("mch_id", merchant.getMerchantId());
//				merchantId = merchant.getMerchantId();
//			}
//			WeixinRevokeVo revokeResult = wxRefundResult(merchantId, wxReverseURL, paramMap, merchant.getPaymentKey(),
//					WeixinRevokeVo.class, context);
//			if (!PaymentConstants.RETURN_CODE_SUCCESS.equals(revokeResult.getResult_code())
//					|| !PaymentConstants.RESULT_SUCCESS_CODE.equals(revokeResult.getResult_code())) {
//				logger.error("sequence:{}, 微信发起退撤销作失败：{}", context.getMsgSeq(), JSONObject.toJSONString(revokeResult));
//				if (Objects.equals("SYSTEMERROR", revokeResult.getErr_code())) {
//					result.setResultCode(PaymentConstants.RESULT_UNKNOW_CODE);
//				}
//				result.setResultMsg(
//						revokeResult.getReturn_msg() + "|" + revokeResult.getErr_code() + "-" + revokeResult.getErr_code_des());
//				return result;
//			}
//			result.setResultCode(PaymentConstants.RESULT_SUCCESS_CODE);
//			return result;
//		} catch (Exception e) {
//			logger.error("sequence:{}, 微信当面付订单撤销异常", context.getMsgSeq(), e);
//			throw new WxPayException(WxPayException.SYSTEM_ERROR_CODE, "微信当面付订单撤销异常" + e.getMessage());
//		} finally {
//			if (logger.isDebugEnabled()) {
//				logger.debug("sequence:{}, 微信当面付订单撤销结束：{}", context.getMsgSeq(), JSONObject.toJSONString(result));
//			}
//		}
//	}
//
//	@Override
//	public RefundOrderCheckVo checkRefundOrderStatus(RefundOrderInfoVo refundOrder, HcContext context) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("sequence:{}, 检查微信退款单状态：refundOrderId：{}", context.getMsgSeq(), refundOrder);
//		}
//		RefundOrderCheckVo result = new RefundOrderCheckVo();
//		result.setResultCode(PaymentConstants.RESULT_FAIL_CODE);
//		try {
//			if (refundOrder == null) {
//				result.setResultMsg("退款单不合法");
//				return result;
//			}
//			MerchantInfo merchant = super.getMerchantInfoByKey(refundOrder.getInsideMerchantId(), context);
//			Map<String, Object> paramMap = null;
//			if (StringUtils.isNotBlank(merchant.getServiceMerchantId())) {
//				paramMap = MapTool.getSOMap("appid", merchant.getServiceAppId(), "mch_id", merchant.getServiceMerchantId(),
//						"sub_appid", merchant.getAppId(), "sub_mch_id", merchant.getMerchantId(), "device_info", null,
//						"nonce_str", RandomStringGenerator.getRandomStringByLength(20), "refund_id",
//						refundOrder.getAcquirerSerialNo());
//			} else {
//				paramMap = MapTool.getSOMap("appid", merchant.getAppId(), "mch_id", merchant.getMerchantId(), "device_info", null,
//						"nonce_str", RandomStringGenerator.getRandomStringByLength(20), "refund_id",
//						refundOrder.getAcquirerSerialNo());
//			}
//			WeixinCheckRefundVo checkResult = getWxRequestResult(refundQueryURL, paramMap, merchant.getPaymentKey(),
//					WeixinCheckRefundVo.class, context);
//			if (!PaymentConstants.RETURN_CODE_SUCCESS.equals(checkResult.getResult_code())
//					|| !PaymentConstants.RESULT_SUCCESS_CODE.equals(checkResult.getResult_code())) {
//				result.setResultMsg(
//						checkResult.getReturn_msg() + "|" + checkResult.getErr_code() + "-" + checkResult.getErr_code_des());
//				return result;
//			}
//			// 认为非成功状态的都为未知退款单状态
//			if (!Objects.equals(PaymentConstants.RESULT_SUCCESS_CODE, checkResult.getRefund_status_0())) {
//				result.setResultCode(PaymentConstants.RESULT_UNKNOW_CODE);
//				result.setResultMsg(checkResult.getRefund_status_0());
//				return result;
//			}
//			result.setResultCode(PaymentConstants.RESULT_SUCCESS_CODE);
//			result.setAcquireSerialNo(checkResult.getRefund_id_0());
//			result.setMerchantOrderId(String.valueOf(checkResult.getOut_refund_no_0()));
//			result.setRealRefundFee(checkResult.getRefund_fee_0());
//			result.setRefundCount(checkResult.getRefund_count());
//			result.setRefundStatus(checkResult.getRefund_status_0());
//			if (StringUtils.isNotBlank(checkResult.getRefund_success_time_0())) {
//				result.setRefundSuccessTime(DateTool.getFullDateTime().parse(checkResult.getRefund_success_time_0()));
//			}
//			result.setRefundTotalFee(checkResult.getRefund_fee_0());
//			return result;
//		} catch (Exception e) {
//			logger.error("sequence:{}, 检查微信退款单状态异常", context.getMsgSeq(), e);
//			throw new PayCenterException(PayCenterException.DEFAULT_CODE, "查询微信退款单状态异常");
//		} finally {
//			if (logger.isDebugEnabled()) {
//				logger.debug("sequence:{}, 检查微信退款单状态调用结束，返回数据：{}", context.getMsgSeq(), JSONObject.toJSONString(result));
//			}
//		}
//	}
//
//	// 待使用
//	private CloseableHttpClient getHttpClient(String merchantId) throws Exception {
//		FileInputStream instream = null;
//		try {
//			KeyStore keyStore = KeyStore.getInstance("PKCS12");
//			instream = new FileInputStream(new File(CERT_LOCAL_PATH + merchantId + "_apiclient_cert.p12"));
//			keyStore.load(instream, merchantId.toCharArray());
//			SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, merchantId.toCharArray()).build();
//			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
//					SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
//			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
//		} finally {
//			if (instream != null) {
//				instream.close();
//				instream = null;
//			}
//		}
//	}
//
	@Override
	public ClosePayOrderVo closePayOrder(PaySerialVo vo) {
		logger.info("调用微信发起关闭操作{}", JSONObject.toJSONString(vo));
		ClosePayOrderVo result = new ClosePayOrderVo();
		result.setResultCode(PaymentConstants.RESULT_FAIL_CODE);
		try {
			PaymentMerchant merchant = paymentMerchantMapper.selectByPrimaryKey(vo.getInsideMerchantId());
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("nonce_str", CommonUtil.CreateNoncestr());
			paramMap.put("out_trade_no", vo.getMerchantOrderId());
			if (StringUtils.isNotBlank(merchant.getServiceMerchantId())) {
				paramMap.put("appid", merchant.getServiceAppId());
				paramMap.put("mch_id", merchant.getServiceMerchantId());
				paramMap.put("sub_appid", merchant.getAppId());
				paramMap.put("sub_mch_id", merchant.getMerchantId());
			} else {
				paramMap.put("appid", merchant.getAppId());
				paramMap.put("mch_id", merchant.getMerchantId());
			}
			WeixinCloseOrderVo closeResult = getWxRequestResult(closeOrderURL, paramMap, merchant.getPaymentKey(),
					WeixinCloseOrderVo.class);
			if (!PaymentConstants.RETURN_CODE_SUCCESS.equals(closeResult.getResult_code())
					|| !PaymentConstants.RESULT_SUCCESS_CODE.equals(closeResult.getResult_code())) {
				logger.error("微信发起关闭操作失败：{}", JSONObject.toJSONString(closeResult));
				result.setResultMsg(
						closeResult.getReturn_msg() + "|" + closeResult.getErr_code() + "-" + closeResult.getErr_code_des());
				return result;
			}
			result.setResultCode(PaymentConstants.RESULT_SUCCESS_CODE);
			return result;
		} catch (Exception e) {
			logger.error("调用微信发起关闭操作异常", e);
			throw new PaymentException(PaymentException.SYSTEM_ERROR_CODE, "调用微信发起关闭操作异常" + e.getMessage());
		} finally {
			if (logger.isDebugEnabled()) {
				logger.debug(" 调用微信发起关闭操作结束：{}", JSONObject.toJSONString(result));
			}
		}
	}
//
//	@Override
//	public RefundResultVo refundOrder(RefundOrderInfoVo vo, HcContext context) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("sequence:{}, 调用微信发起退款，vo：{}", context.getMsgSeq(), JSONObject.toJSONString(vo));
//		}
//		RefundResultVo result = new RefundResultVo();
//		result.setResultCode(PaymentConstants.RESULT_FAIL_CODE);
//		try {
//			MerchantInfo merchant = getMerchantInfoByKey(vo.getInsideMerchantId(), context);
//			Map<String, Object> param = new HashMap<>();
//			String merchantId = null;
//			if (StringUtils.isNotBlank(merchant.getServiceMerchantId())) {
//				param = MapTool.getSOMap("appid", merchant.getServiceAppId(), "mch_id", merchant.getServiceMerchantId(),
//						"op_user_id", merchant.getServiceMerchantId(), "out_refund_no", vo.getRefundOrderId(), "out_trade_no",
//						vo.getMerchantOrderId(), "refund_fee", vo.getRefundFee(), "sub_appid", merchant.getAppId(), "sub_mch_id",
//						merchant.getMerchantId(), "total_fee", vo.getTotalFee(), "transaction_id", vo.getPayAcquirerSerialNo(),
//						"nonce_str", RandomStringGenerator.getRandomStringByLength(20));
//				merchantId = merchant.getServiceMerchantId();
//			} else {
//				param = MapTool.getSOMap("appid", merchant.getAppId(), "mch_id", merchant.getMerchantId(), "op_user_id",
//						merchant.getMerchantId(), "out_refund_no", vo.getRefundOrderId(), "out_trade_no", vo.getMerchantOrderId(),
//						"refund_fee", vo.getRefundFee(), "total_fee", vo.getTotalFee(), "transaction_id",
//						vo.getPayAcquirerSerialNo(), "nonce_str", RandomStringGenerator.getRandomStringByLength(20));
//				merchantId = merchant.getMerchantId();
//			}
//			WeixinRefundVo refundResult = wxRefundResult(merchantId, wxRefundURL, param, merchant.getPaymentKey(),
//					WeixinRefundVo.class, context);
//			if (!PaymentConstants.RETURN_CODE_SUCCESS.equals(refundResult.getResult_code())
//					|| !PaymentConstants.RESULT_SUCCESS_CODE.equals(refundResult.getResult_code())) {
//				logger.error("sequence:{}, 微信发起退款操作失败：{}", context.getMsgSeq(), JSONObject.toJSONString(refundResult));
//				if ("SYSTEMERROR".equals(refundResult.getErr_code())) {
//					result.setResultCode(PaymentConstants.RESULT_UNKNOW_CODE);
//				}
//				result.setResultMsg(
//						refundResult.getReturn_msg() + "|" + refundResult.getErr_code() + "-" + refundResult.getErr_code_des());
//				return result;
//			}
//			result.setResultCode(PaymentConstants.RESULT_SUCCESS_CODE);
//			result.setRefundFee(refundResult.getRefund_fee());
//			result.setRefundNo(refundResult.getRefund_id());
//			result.setPayRefundId(refundResult.getOut_refund_no());
//			return result;
//		} catch (Exception e) {
//			logger.error("sequence:{}, 调用微信发起退款异常", context.getMsgSeq(), e);
//			throw new PayCenterException(PayCenterException.SYSTEM_ERROR_CODE, "调用微信发起退款异常:" + e.getMessage());
//		} finally {
//			if (logger.isDebugEnabled()) {
//				logger.debug("sequence:{}, 调用微信发起退款结束，返回数据：{}", context.getMsgSeq(), JSONObject.toJSONString(result));
//			}
//		}
//	}
//
//	/**
//	 *
//	 * 退款请求. <br/>
//	 *
//	 * @author liujingcheng
//	 * @date: 2017年4月7日 下午9:30:45
//	 * @version 1.0
//	 *
//	 * @param merchantId
//	 * @param requestURL
//	 * @param paramMap
//	 * @param secretKey
//	 * @param context
//	 * @return
//	 * @return
//	 */
//	private <T extends WeixinBaseVo> T wxRefundResult(String merchantId, String requestURL, Map<String, Object> paramMap,
//													  String secretKey, Class<T> t, HcContext context) {
//		HttpPost httpPost = null;
//		try {
//			// 签名
//			String sign = SignUtil.getSign(paramMap, secretKey);
//			paramMap.put("sign", sign);
//			// 将请求参数转换成xml的形式
//			String postDataXML = XMLParser.objToXml(paramMap);
//			logger.info("sequence:{}, 请求微信数据：{}", context.getMsgSeq(), postDataXML);
//			StringEntity postEntity = new StringEntity(postDataXML, "UTF-8");
//			httpPost = new HttpPost(requestURL);
//			httpPost.addHeader("Content-Type", "text/xml");
//			httpPost.setEntity(postEntity);
//			HttpResponse response = getHttpClient(merchantId).execute(httpPost);
//			HttpEntity entity = response.getEntity();
//			String resultStr = EntityUtils.toString(entity, "UTF-8");
//			logger.info("sequence：{}, 获取微信请求返回数据：{}", context.getMsgSeq(), resultStr);
//			// 判断签名结果是否正确
//			if (!SignUtil.compareResultSign(resultStr, secretKey)) {
//				resultStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[INVALID_SIGN]]></return_msg></xml>";
//			}
//			return CommonUtil.getObjectFromXML(resultStr, t);
//		} catch (Exception e) {
//			logger.error("sequece:{}, 获取微信请求异常", context.getMsgSeq(), e);
//			throw new BizException(BizException.DEFAULT_CODE, "获取微信请求异常");
//		}
//	}

	/**
	 *
	 * 请求微信. <br/>
	 *
	 * @param requestURL
	 * @param paramMap
	 * @param secretKey
	 * @param t
	 * @return
	 */
	protected <T extends WeixinBaseVo> T getWxRequestResult(String requestURL, Map<String, Object> paramMap, String secretKey,
															Class<T> t) {
		try {
			// 签名
			String sign = SignUtil.getSign(paramMap, secretKey);
			paramMap.put("sign", sign);
			// 将请求参数转换成xml的形式
			String postDataXML = XMLParser.objToXml(paramMap);
			logger.info("请求微信数据：{}", postDataXML);
			String resultStr = gateWayService.post(requestURL, postDataXML, null);
			logger.info("获取微信请求返回数据：{}", resultStr);
			// 判断签名结果是否正确
			if (!SignUtil.compareResultSign(resultStr, secretKey)) {
				resultStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[INVALID_SIGN]]></return_msg></xml>";
			}
			return CommonUtil.getObjectFromXML(resultStr, t);
		} catch (Exception e) {
			logger.error("获取微信请求异常", e);
			throw new BizException(BizException.DEFAULT_CODE, "获取微信请求异常");
		}
	}
}
