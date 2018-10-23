package com.zzg.netty.init;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import com.zzg.netty.constant.Constants;
import com.zzg.netty.factory.ZookeeperFactory;
import com.zzg.netty.handler.ServerHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * #使用spring的监听器启动Netty服务器
 */
@Component
public class NettyInital implements ApplicationListener<ContextRefreshedEvent>{
	/**
	 * 启动Netty的服务器
	 */
	public  void start() {
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(parentGroup, childGroup);
			bootstrap.option(ChannelOption.SO_BACKLOG, 128)
			         .childOption(ChannelOption.SO_KEEPALIVE, false)
			         .channel(NioServerSocketChannel.class)
			         .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
			             @Override
			             public void initChannel(SocketChannel ch) throws Exception {
			            	 ch.pipeline().addLast(new DelimiterBasedFrameDecoder(65535, Delimiters.lineDelimiter()[0]));
			            	 ch.pipeline().addLast(new StringDecoder());
			            	 ch.pipeline().addLast(new IdleStateHandler(60, 45, 20, TimeUnit.SECONDS));
			            	 ch.pipeline().addLast(new ServerHandler());
			            	 ch.pipeline().addLast(new StringEncoder());
			             }
			         });
			
			int port = 8080;
			// 权重
			int weight=2;
			ChannelFuture f = bootstrap.bind(8080).sync();
			CuratorFramework client = ZookeeperFactory.create();
			InetAddress netAddress = InetAddress.getLocalHost();
			/**
			 * CreateMode.EPHEMERAL_SEQUENTIAL的目的是有一个序列号
			 * 【服务注册】在Netty服务器启动后就把服务器ip和端口和权重注册到zk上去
			 */
			client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(Constants.SERVER_PATH+"/"+netAddress.getHostAddress()+"#"+port+"#"+weight+"#");
			
			f.channel().closeFuture().sync();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
		
	}

	/**
	 * #spring IOC容器启动的时候就会执行这个方法
	 * @param contextRefreshedEvent
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		this.start();
	}
}
