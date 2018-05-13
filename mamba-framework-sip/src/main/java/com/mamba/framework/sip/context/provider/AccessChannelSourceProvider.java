package com.mamba.framework.sip.context.provider;

import java.util.List;

import com.jf.crm.common.framework.sip.bean.AccessChannelBean;

public interface AccessChannelSourceProvider {
	public List<AccessChannelBean> provide();
}
