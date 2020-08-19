package com.changgou.common.controller;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author L5781
 * @version 1.0
 * @date 2020/8/3 13:53
 */
@Aspect
@Component
public class EasyExceptionHandler {

    /**
     * 定义一个切入点（此时切入点为RequestMapping请求）
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RequestMapping)")
    public void exceptionHandler(){}

    /**
     * 后置异常通知
     *  定义一个名字，该名字用于匹配通知实现方法的一个参数名，当目标方法抛出异常返回后，将把目标方法抛出的异常传给通知方法；
     *  throwing:限定了只有目标方法抛出的异常与通知方法相应参数异常类型时才能执行后置异常通知，否则不执行，
     *           对于throwing对应的通知方法参数为Throwable类型将匹配任何异常。
     * @param joinPoint
     * @param exception
     */
    @AfterThrowing(value = "exceptionHandler()",throwing = "exception")
    public void doAfterThrowingAdvice(JoinPoint joinPoint, Throwable exception){
        //目标方法名：
        System.out.println(joinPoint.getSignature().getName());
        System.out.println("发生了异常：" + exception);
    }
}
