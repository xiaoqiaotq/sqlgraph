package org.xiaoqiaotq;

import com.google.common.base.CaseFormat;

public interface Resource {
    default String tableName(){
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this.getClass().getSimpleName());
    }

}
