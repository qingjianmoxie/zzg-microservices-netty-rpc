package com.zzg.netty.medium;

import java.lang.reflect.Method;
import java.util.Map;

import com.zzg.netty.annotation.Remote;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * ##org.springframework.beans.factory.config.BeanPostProcessor通过这个的实现类
 * 的postProcessAfterInitialization方法先把@Remote注解的类的对象信息以该类的接口的方法的全类名为键
 * 方法的字节码文件对象为值存放到com.zzg.netty.medium.Media这个中介者类初始化的时候创建的一个Map中。
 */
@Component
public class InitialMedium implements BeanPostProcessor{

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * ##封装调服务要执行的接口的实现的信息给中介者
	 * #获取@Remote注解的修饰的类（com.zzg.netty.user.remote.UserRemoteImpl）
	 * 的对象的所有方法的反射对象和该类的接口的各个方法的全类名作为键，封装成中介者的Map。
	 *
	 * 重写bean初始化处理之后的这个postProcessAfterInitialization方法
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		/**
		 * ###找到相当于Controller的控制层客户端，被@Remote注解的类
		 * ##获取@Remote注解的类的对象(com.zzg.netty.user.remote.UserRemoteImpl)
		 * bean.getClass().isAnnotationPresent(Remote.class)=>如果bean这个对象对应的类上面有@Remote注解
		 */
		if(bean.getClass().isAnnotationPresent(Remote.class)){
			// 获取@Remote注解的类中的所有的方法的字节码文件对象的数组
			Method[] methods = bean.getClass().getDeclaredMethods();
			for(Method m: methods){
				// 使用@Remote注解的类的对象的第一个接口中的方法的全类名作为键[服务的接口只允许单实现接口]
				String key = bean.getClass().getInterfaces()[0].getName()+"."+m.getName();
				// 获取中介类初始化的那个Map
				Map<String,BeanMethod> beanMap = Media.beanMap;
				BeanMethod beanMethod = new BeanMethod();
				beanMethod.setBean(bean);
				beanMethod.setMethod(m);
				/**
				 * 将@Remote注解标记的类的接口的方法的全类名为键、封装了方法的字节码文件对象和实际
				 * 对象的BeanMethod的对象为值存放到Media类初始化时候创建的Map中。
 				 */
				beanMap.put(key, beanMethod );
			}
		}
		// 将BeanPostProcessor的postProcessAfterInitialization方法的bean对象返回回框架
		return bean;
	}



}
