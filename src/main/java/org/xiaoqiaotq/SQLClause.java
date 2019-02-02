package org.xiaoqiaotq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLClause {
    Class<? extends Resource> lr();
    String lf()  ;
    Class<? extends Resource> rr() default Resource.class;
    String rf() default "";
    String rv() default "";
}
