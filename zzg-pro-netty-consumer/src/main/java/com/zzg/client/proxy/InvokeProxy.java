package com.zzg.client.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.zzg.client.annotation.RemoteInvoke;
import com.zzg.client.core.TcpClient;
import com.zzg.client.param.ClientRequest;
import com.zzg.client.param.Response;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

/**
 * ##使用Spring的cglib动态代理实现访问远程的Netty服务
 * 例：在com.zzg.RemoteInvokingTest这个测试类在SpringIOC容器初始化完成后，
 * 需要把这个类中的属性（比如private UserRemote userRemote）改成动态代理的对象
 */
@Component
public class InvokeProxy implements BeanPostProcessor{

	/**
	 * ##【目的】：在com.zzg.client.RemoteInvokingTest这个类初始化之前，
	 * 需要将这个类的属性com.zzg.client.user.remote.UserRemote换成一
	 * 个动态代理的代理。
	 * 
	 * 【知识点】：Spring IOC 容器对 放入到该IOC容器中的Bean 的生命周期进行管理的过程:
     *                             1、通过构造器或工厂方法创建 Bean 实例；
     *                             2、为 Bean 的属性（设置值和对其他 Bean 的引用）；
     *                             3、将 Bean 实例传递给 Bean 后置处理器的 postProcessBeforeInitialization 方法；
     *                             4、调用 Bean 的初始化方法；
     *                             5、将 Bean 实例传递给 Bean 后置处理器的 postProcessAfterInitialization方法；
     *                             6、Bean 可以使用了；
     *                             7、当容器关闭时, 调用 Bean 的销毁方法。
     * 
	 * 
	 * #在SpringIOC中bean对象初始化之前要执行的BeanPostProcessor的postProcessBeforeInitialization方法。
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		/**
		 * 获取Spring IOC 容器bean在初始化之前，bean对象对应的字节码文件对象的反射的属性集合
		 */
		Field[] fields = bean.getClass().getDeclaredFields();
		for(Field field : fields){
			/**
			 * 遍历属性的数组，并找出@RemoteInvoke注解的反射属性；
			 * 【注意】：field.isAnnotationPresent(RemoteInvoke.class) 这个是判断这个属性上是否有@RemoteInvoke注解。
			 */
			if(field.isAnnotationPresent(RemoteInvoke.class)){
				// 将反射属性设置成强制访问（可以修改这个属性的值）
				field.setAccessible(true);
				/**
				 * ##【目的】：封装一个com.zzg.RemoteInvokingTest这样的类中的private UserRemote userRemote 属性的接口
				 * 类型中的方法的反射对象和这个属性的字节码文件对象的Map，以便于在拦截这个被代理的属性类型中的方法的时
				 * 候获取这个被代理的接口的方法的全类名。
				 * ##【同样的解释】就是获取当前反射属性field的类型对应的字节码文件对象和这个属性的类型中方法的的
				 * 反射对象，最后一方法的反射对象为键、以这个属性类型的类的字节码文件对象作为值封装成一个Map。
				 */
				final Map<Method,Class>methodClassMap=new HashMap<Method,Class>();
				// 将反射属性的字节码文件对象中的所有反射方法为键、当前反射属性的字节码文件对象为值存到Map中
				putMethodClass(methodClassMap,field);
				// 创建Spring的cglib动态代理的对象
				Enhancer enhancer = new Enhancer();
				/**
				 * #enhancer.setInterfaces(new Class[]{field.getType()});是设置对哪些接口（因为是以接口注入的）进行动态代理，
				 * #是对@RemoteInvoke注解标记的属性对应的字节码文件对象代表的接口进行动态代理
				 * 将当前反射属性对象对应的字节码文件对象放到cglib动态代理对象的接口数组中
 				 */
				enhancer.setInterfaces(new Class[]{field.getType()});
				/**
				 * #【new MethodInterceptor() {}的含义】：拦截enhancer.setInterfaces(new Class[]{field.getType()});设置的这些接口的方法。
				 * 也就是当执行被代理的这个接口的方法的时候，对这个接口中的方法进行一个拦截
				 *  （比如：执行com.zzg.client.user.remote.UserRemote.saveUser(User)会被这个方法的拦截器给拦截）。
 				 */
				enhancer.setCallback(new MethodInterceptor() {
					/**
					 * 【目的】：通过拦截被注入的目标接口的实现类的对象的方法来间接实现调用远程服务的效果。
					 * ##com.zzg.client.proxy.InvokeProxy.postProcessBeforeInitialization(...).new MethodInterceptor() {...}.intercept(Object, Method, Object[], MethodProxy)的方法参数的解释：
					 *                       第一个参数：instance->代理的目标对象；
					 *                       第二个参数：method->代理的目标方法方法的反射对象；
					 *                       第三个参数：args-> 代理的目标方法的入参；
					 *                       第四个参数：proxy->当前的代理对象。
					 */
					@Override
					public Object intercept(Object instance, Method method, Object[] args, MethodProxy proxy) throws Throwable {
						/**
						 * #采用netty客户端去需要去调用服务器
						 */
						ClientRequest request = new ClientRequest();
						/**
						 * #通过属性的反射方法对象获取这个对象对应的属性的字节码文件对象，
						 * 再获取属性的全类名，再拼接成方法的全类名。
						 * 【目的】: 获取  “接口的全类名+方法的名称” （这个接口就是被代理的接口，方法也是被代理的这个接口的方法）。
 						 */
						request.setCommand(methodClassMap.get(method).getName()+"."+method.getName());
						// 调用远程服务方法的参数
						request.setContent(args[0]);
						//【目的】： 通过Netty客户端发送请求对象到Netty服务端
						Response resp = TcpClient.send(request);
						return resp;
					}
				});
				
				try {
					/**
					 * 【目的】：是将bean对象的这个field属性的值设置成动态代理的对象。
					 * org.springframework.cglib.proxy.Enhancer 这个类是spring cglib创建动态代理对象的类。
					 *  修改@RemoteInvoke注解修饰的属性的值，这个是IOC容器中bean对象的当前这个@RemoteInvoke注解修饰的属性的值为动态代理之后的值
					 */
					field.set(bean, enhancer.create());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		return bean;
	}

	/**
	 * 对属性的所有方法和属性接口类型放入到一个map中
	 * @param methodClassMap
	 * @param field
	 */
	private void putMethodClass(Map<Method, Class> methodClassMap, Field field) {
		// 获取反射属性的字节码文件对象的反射方法对象的集合
		Method[] methods=field.getType().getDeclaredMethods();
		for(Method m : methods){
			// 遍历反射属性对应的字节码文件对象的所有方法，并以反射方法为键、属性的字节码文件对象为值存进Map中
			methodClassMap.put(m, field.getType());
		}
		
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
