package com.zzg.client.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.zzg.client.constant.Constants;
import com.zzg.client.handler.SimpleClientHandler;
import com.zzg.client.param.ClientRequest;
import com.zzg.client.param.Response;
import com.zzg.client.zk.ZookeeperFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;

import com.alibaba.fastjson.JSONObject;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class TcpClient {
	static final Bootstrap b = new Bootstrap();
	static ChannelFuture f = null;
	static {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new SimpleClientHandler());
				ch.pipeline().addLast(new StringEncoder());
			}
		});
		
		/**
		 * 【目的】客户端从zk上获取服务的ip和端口，就是服务发现
		 */
		CuratorFramework client = ZookeeperFactory.create();
		String host = "localhost";
		int port = 8080;
		try {
			CuratorWatcher watcher = new ServerWatcher();
			// 加上zk监听服务器的变化
			client.getChildren().usingWatcher(watcher).forPath(Constants.SERVER_PATH);

			List<String> serverPaths = client.getChildren().forPath(Constants.SERVER_PATH);
			for (String serverPath : serverPaths) {
				String[] str = serverPath.split("#");
				/**
				 * 【Netty的客户端去ZK上的服务发现】先判断权重，
				 */
				int weight = Integer.valueOf(str[2]);
				if (weight > 0) {
					for (int w = 0; w <= weight; w++) {
						ChannelManager.realServerPath.add(str[0] + "#" + str[1]);
						/**
						 * 【连接发现的服务】
						 */
						ChannelFuture channelFuture = TcpClient.b.connect(str[0], Integer.valueOf(str[1]));
						ChannelManager.add(channelFuture);
					}
				}

			}

			if (ChannelManager.realServerPath.size() > 0) {
				String[] hostAndPort = ChannelManager.realServerPath.toArray()[0].toString().split("#");
				host = hostAndPort[0];
				port = Integer.valueOf(hostAndPort[1]);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// try {
		// f = b.connect(host, port).sync();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// } // (5)

	}

	// 注意：1点每一个请求都是同一个连接，并发问题
	// 发送数据
	public static Response send(ClientRequest request) {
       /**
        * 从ChannelFuture管理的集合中获取ChannelFuture对象
        */
		f = ChannelManager.get(ChannelManager.position);
		// 以json字符串的形式发送数据
		f.channel().writeAndFlush(JSONObject.toJSONString(request));
		f.channel().writeAndFlush("\r\n");
		DefaultFuture df = new DefaultFuture(request);
		// 获取响应结果
		return df.get();

	}

}
