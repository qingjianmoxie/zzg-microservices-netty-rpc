package com.zzg.netty.client;

import com.alibaba.fastjson.JSONObject;

import com.zzg.netty.handler.SimpleClientHandler;
import com.zzg.netty.util.Response;
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
import io.netty.util.AttributeKey;

public class TcpClient {

	static final Bootstrap b=new Bootstrap(); 
	static ChannelFuture f=null;
	static{
		 EventLoopGroup workerGroup = new NioEventLoopGroup();
         b.group(workerGroup); // (2)
         b.channel(NioSocketChannel.class); // (3)
         b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
         b.handler(new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) throws Exception {
             	ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));ch.pipeline().addLast(new StringDecoder());
             	ch.pipeline().addLast(new SimpleClientHandler());
             	ch.pipeline().addLast(new StringEncoder());
             }
         });
         String host = "localhost";
	        int port = 8080;
         try {
			 f = b.connect(host, port).sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // (5)
         
	}
	
	//注意：1点每一个请求都是同一个连接，并发问题
	//发送数据
	public static Response send(ClientRequest request){
		f.channel().writeAndFlush(JSONObject.toJSONString(request));
		f.channel().writeAndFlush("\r\n");
		DefaultFuture df = new DefaultFuture(request);
		return df.get();
		
	}
	
	
	
}
