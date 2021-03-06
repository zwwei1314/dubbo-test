package cn.zw.part.four.serializable.byjava;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SubscriptServer {
	
	
	public  void  bind(int port){
		
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup workers = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(boss, workers).channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					ChannelPipeline p =  ch.pipeline();
					// 对POJO对象进行解码
					p.addLast(new ObjectDecoder(1024 * 1024,
							ClassResolvers.weakCachingResolver(this.getClass().getClassLoader())));
					//  在消息发送的时候，可以自动把实现了序列化接口的对象进行编码
					p.addLast(new ObjectEncoder());
					p.addLast(new SubscriptHandler());
				}
			});
			
			ChannelFuture f =  b.bind(port).sync();
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			boss.shutdownGracefully();
			workers.shutdownGracefully();
		}
		
	}
	
	private class SubscriptHandler extends ChannelHandlerAdapter{
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			SubscriptReq req = (SubscriptReq)msg;
			System.out.println("server : accept  msg :"+req.toString());
			ctx.writeAndFlush(resp(req));
			
		}
		
		
		public  SubscriptResp resp(SubscriptReq  req){
			SubscriptResp resp = new SubscriptResp();
			resp.setSubReqID(req.getSubReqID());
			resp.setRespCode(200);
			resp.setDesc("呵呵，成功了");
			return  resp;
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
				cause.printStackTrace();
				ctx.close();
		}
		
	}
	
	
	public static void main(String[] args) {
		
		new SubscriptServer().bind(9000);
		
	}
	

}
