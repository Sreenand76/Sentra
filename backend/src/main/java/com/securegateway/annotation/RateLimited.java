package com.securegateway.annotation;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int capacity() default 5;
    int refillRate() default 1;
}