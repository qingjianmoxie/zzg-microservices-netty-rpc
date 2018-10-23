package com.zzg.netty.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import com.zzg.netty.annotation.RemoteInvoke;
import com.zzg.netty.util.Response;

/**
 * 初始化远程调用的BeanPostProcessor的实现类
 */
@Component
public class InitalInvoke implements BeanPostProcessor{

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * Spring IOC容器中bean初始化完成执行要执行的BeanPostProcessor实现类的postProcessBeforeInitialization方法
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		
		System.out.println(bean.getClass().getName());
		// 获取Spring IOC容器中的对象的属性的字节码文件对象的数组
		Field[] fields = bean.getClass().getDeclaredFields();
		for(Field f: fields){
			// 遍历属性的字节码文件对象的数组，处理如果是@RemoteInvoke注解修饰的属性
			if(f.isAnnotationPresent(RemoteInvoke.class)){
				// 获取@RemoteInvoke注解对象
				RemoteInvoke remoteInvoke = f.getAnnotation(RemoteInvoke.class);
				// 设置访问权限
				f.setAccessible(true);
				// 创建Spring的org.springframework.cglib.proxy.Enhancer这个cglib的动态代理的对象
				Enhancer enhancer = new Enhancer();
				// 将当前遍历到的属性的字节码对象的字节码文件对象设置到动态代理的接口数组中
				enhancer.setInterfaces(new Class[]{f.getType()});
				final Map<Method,Class> methodMap=new HashMap<Method,Class>();
				// 以属性的字节码文件对象的反射方法对象作为键，以当前属性的字节码文件对象作为值存放到Map中。
				putMethod(methodMap, f);
				enhancer.setCallback(new MethodInterceptor() {
					@Override
					public Object intercept(Object instance, Method method, Object[] args, MethodProxy poxy) throws Throwable {
						
						//调用netty客户端去处理
						ClientRequest request = new ClientRequest();
						/**
						 * 从以属性的字节码文件对象的反射方法对象作为键，以当前属性的字节码文件对象作为值的Map
						 * 中通过SpringIOC容器bean初始化完成之后执行当前postProcessBeforeInitialization方法
						 * 传入的接口的字节码文件对象对应的
						 */
						request.setCommand(methodMap.get(method).getName()+"."+method.getName());
						request.setContent(args[0]);
						Response resp = TcpClient.send(request );
//						Class<?>returnType = method.getReturnType();
						return resp;
					}
				});
				
				try {
					f.set(bean, enhancer.create());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return bean;
	}

	
	public static void putMethod(Map<Method,Class> methodMap,Field f){
		// 通过属性的反射对象的getType()方法获取属性的字节码文件对象，再通过字节码文件对象获取属性对应这个类的所有方法的反射对象
		for(Method m: f.getType().getDeclaredMethods()){
			// 遍历属性的反射对象对应的类的字节码文件对象获取到的反射的方法对应的集合，以属性的字节码文件对象的反射方法对象作为键，以当前属性的字节码文件对象作为值存放到Map中
			methodMap.put(m, f.getType());
		}
	}
}
