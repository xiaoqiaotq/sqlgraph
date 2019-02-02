package org.xiaoqiaotq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @SQLJoinField(
//  Class<? extends Resource>, 必填
//  field, 默认取field,或者配置field和column转换规则
//  joinKey, 当joinKey parallel是需显示指定，与joinOnSQLClause
//  joinOnSQLClause, 复杂关联条件sql语句， e.g. aa.bb=cc.dd and dd='ee'
//  joinByClause, 是否根据joinKey自动组装join on关系，否则取joinOnSQLClause
//  tableToAlias, 默认resource的tableName()
//  joinType, oneToOne,manyToOne,
//  reverse, 是否反转
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLJoinField {
    Class<? extends Resource> resource();
    String field() default "";
    String joinKey() default "";
    SQLClause[] joinOnSQLClause() default {};
    boolean joinByClause() default false;
    @Deprecated
    String tableAlias() default "";  // 需不需要自动生成
    @Deprecated
    String joinType() default "";
    boolean reverse() default false;
}
