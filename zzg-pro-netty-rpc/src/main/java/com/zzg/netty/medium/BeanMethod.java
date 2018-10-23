package com.zzg.netty.medium;

import java.lang.reflect.Method;

/**
 * 封装实际对象和反射方法对象的bean
 */
public class BeanMethod {

	private Object bean;
	private Method method;
	public Object getBean() {
		return bean;
	}
	public void setBean(Object bean) {
		this.bean = bean;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	
	
}
