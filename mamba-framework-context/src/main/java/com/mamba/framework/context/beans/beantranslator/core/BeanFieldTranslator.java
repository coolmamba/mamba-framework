package com.mamba.framework.context.beans.beantranslator.core;

import com.mamba.framework.context.beans.beantranslator.metadata.FieldTranslatorMetadata;

public interface BeanFieldTranslator {
	public Object translate(FieldTranslatorMetadata metaData);
}
