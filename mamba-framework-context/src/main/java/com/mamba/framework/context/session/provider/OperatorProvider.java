package com.mamba.framework.context.session.provider;

import com.mamba.framework.context.session.core.Operator;

public interface OperatorProvider {
	public Operator getOperator(long operatorId);
}
