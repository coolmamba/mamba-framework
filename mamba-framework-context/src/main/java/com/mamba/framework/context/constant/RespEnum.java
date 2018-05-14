package com.mamba.framework.context.constant;

public enum RespEnum {
	SUCCESS("0000", "受理成功"),
	/** 9900 --> 9999 , 供系统和基础框架使用 */
	UNSUPPORTED_REQUEST_METHOD("9900", "系统不支持{0}请求"),
	UNSUPPORTED_UPLOAD_FILE("9901", "系统不支持文件上传"),
	UNSUPPORTED_CONTENT_TYPE("9902", "请求报文数据格式错误，系统仅支持JSON数据格式"),
	CONTENT_OVER_LENGTH("9903", "请求报文数据超长"),
	CONTENT_IS_EMPTY("9904", "请求报文数据为空"),
	READ_REQUEST_CONTENT_FAIL("9905", "读取请求报文失败"),
	PARSE_REQUEST_CONTENT_FAIL("9906", "解析请求报文失败"),
	REQUEST_CONTENT_FORMAT_ERR("9907", "请求报文数据格式错误"),
	SYSTEM_UNACCESS_SYSTEM("9908", "渠道：【{0}】 未接入系统"),
	BUSI_CODE_IS_EMPTY("9909", "业务编码为空"),
	BUSI_CODE_SERVICE_UNSUPPORT("9910", "接口服务：{0} 未向渠道：{1} 提供"),
	BUSI_ACCESS_CONFIG_ERR("9911", "接口服务：{0} 内部配置有误"),
	GET_BUSI_ACCESS_SERVICE_FAIL("9912", "获取接口编码：{0} 对应的服务失败。服务类为{1}"),
	CACHE_REFRESH("9913", "缓存重新刷新失败"),
	BUSINESS_SO_FAIL("9998", "业务受理失败"),
	SYSTEM_EXCEPTION("9999", "系统异常");

	public String respCode;
	public String respDesc;

	private RespEnum(String respCode, String respDesc) {
		this.respCode = respCode;
		this.respDesc = respDesc;
	}

}
