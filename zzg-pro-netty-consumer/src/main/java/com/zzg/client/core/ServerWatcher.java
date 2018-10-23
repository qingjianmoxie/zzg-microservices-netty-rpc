package com.zzg.client.core;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import com.zzg.client.zk.ZookeeperFactory;

import io.netty.channel.ChannelFuture;

/**
 * 【zk的监听器】
 * 服务端监控
 */
public class ServerWatcher implements CuratorWatcher {

	@Override
	public void process(WatchedEvent event) throws Exception {
		 CuratorFramework client = ZookeeperFactory.create();
		 // 获取监听的zk路径
		 String path = event.getPath();
		 // 为了解决zk监听器只监听一次的问题
		 client.getChildren().usingWatcher(this).forPath(path);
		 List<String> serverPaths = client.getChildren().forPath(path);
		 ChannelManager.realServerPath.clear();
		 for(String serverPath : serverPaths){
			 String[] str = serverPath.split("#");
			 int weight = Integer.valueOf(str[2]);
				if(weight>0){
					for(int w=0;w<=weight;w++){
						ChannelManager.realServerPath.add(str[0]+"#"+str[1]);
					}
				}
				
			 ChannelManager.realServerPath.add(str[0]+"#"+str[1]);
		 }
		 
		 ChannelManager.clear();
		 for(String realServer :ChannelManager.realServerPath){
			 String[] str = realServer.split("#");
			 try {
				 int weight = Integer.valueOf(str[2]);
					if(weight>0){
						for(int w=0;w<=weight;w++){
							ChannelFuture  channelFuture = TcpClient.b.connect(str[0], Integer.valueOf(str[1]));
							ChannelManager.add(channelFuture);;
						}
					}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }

	}

}
