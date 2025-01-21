package test.crudboard.annotation;


import test.crudboard.entity.enumtype.ResourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)                // 어노테이션을 메서드에 적용
@Retention(RetentionPolicy.RUNTIME)       // 어노테이션을 런타임까지 유지
public @interface CheckResourceOwner {
    ResourceType type();
}
