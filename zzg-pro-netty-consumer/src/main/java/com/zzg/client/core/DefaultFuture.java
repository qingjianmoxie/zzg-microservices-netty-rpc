package com.zzg.client.core;

import com.zzg.client.param.ClientRequest;
import com.zzg.client.param.Response;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DefaultFuture {
	/**
	 * #我们要对这个所有的DefaultFuture进行遍历处理，把里面所有的超时的DefaultFuture都给它移除掉。
	 */
	public final  static ConcurrentHashMap<Long,DefaultFuture>allDefaultFuture=new ConcurrentHashMap<Long,DefaultFuture>();
    final Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	private Response response;

	/**
	 * 超时时间
	 */
	private long timeout=2*60*1000l;
	// 为超时功能的属性
	private long startTime=System.currentTimeMillis();
	
	
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getStartTime() {
		return startTime;
	}

	public DefaultFuture(ClientRequest request) {
		allDefaultFuture.put(request.getId(), this);
	}

	//主线程获取数据，首先要等待结果
	public Response get() {
		lock.lock();
		try{
		   while(!done()){
			   condition.await();
		   }
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
		return this.response;
	}

	/**
	 * 【目的】获取服务端的响应结果也加上超时时间
	 * @param time
	 * @return
	 */
	public Response get(long time) {
		lock.lock();
		try{
		   while(!done()){
			   /**
				* 等待超时时间这么长
				*/
			   condition.await(time,TimeUnit.SECONDS);
			   // 如果当前时间的毫秒数-初始化的开始时间比设定的超时时间的毫秒数大就说明超时了
			   if((System.currentTimeMillis()-startTime)>time){
				   System.out.println("请求超时！");
				   break;
			   }
		   }
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
		return this.response;
	}

	public static void recive(Response response){
		// 拿到超时的DefaultFuture
		DefaultFuture df = allDefaultFuture.get(response.getId());
		if(df !=null){
			Lock lock =df.lock;
			lock.lock();
			try{
				df.setResponse(response);
				// 打开锁，【唤醒condition.await(time,TimeUnit.SECONDS);继续往下执行】
				df.condition.signal();
				// 删除DefaultFuture
				allDefaultFuture.remove(df);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				lock.unlock();
			}
			
		}
		
		
		
	}
	
	
	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	private boolean done() {
		if(this.response!=null){
			return true;
		}
		return false;
	}

	/**
	 * #开启一个线程类处理超时的DefaultFuture
	 */
	static class FutureThread extends Thread{
		@Override
		public void run() {
			// 获取ConcurrentHashMap<Long,DefaultFuture>的key的集合
			Set<Long>ids = allDefaultFuture.keySet();
			for(Long id : ids){
				// 遍历Set集合，根据键拿到DefaultFuture
				DefaultFuture df = allDefaultFuture.get(id);
				if(df==null){
					// 如果df是空的，就删除
					allDefaultFuture.remove(df);
				}else{
					/**
					 * 假如链路超时：就响应给客户端超时的信息
					 * 当前时间减去最初时间得到的毫秒数比超时的毫秒数大，就证明是超时了。
					 */
					if(df.getTimeout()<(System.currentTimeMillis()-df.getStartTime())){
						Response resp = new Response();
						resp.setId(id);
						resp.setCode("333333");
						resp.setMsg("链路请求超时");
						recive(resp);
					}
					
				}
				
				
				
			}
			
		}
		
	}

	/**
	 * #启动线程
	 */
	static{
		FutureThread futureThread = new FutureThread();
		// 将线程对象设置成守护线程
		futureThread.setDaemon(true);
		// 启动线程
		futureThread.start();
	}

}
