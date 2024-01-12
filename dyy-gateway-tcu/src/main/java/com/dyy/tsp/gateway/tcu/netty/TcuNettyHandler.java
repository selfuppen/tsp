package com.dyy.tsp.gateway.tcu.netty;

import com.alibaba.fastjson.JSONObject;
import com.dyy.tsp.core.evgb.entity.EvGBProtocol;
import com.dyy.tsp.core.handler.AbstractNettyHandler;
import com.dyy.tsp.gateway.tcu.handler.BusinessHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * created by dyy
 */
@ChannelHandler.Sharable
@Service
@SuppressWarnings("all")
public class TcuNettyHandler extends AbstractNettyHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(TcuNettyHandler.class);

    @Autowired
    private BusinessHandler businessHandler;

    @Override
    public void doLogic(ChannelHandlerContext ctx, EvGBProtocol protocol) {
        LOGGER.info("parse protocol:{}", JSONObject.toJSONString(protocol));
        businessHandler.doBusiness(protocol,ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TcuChannel.INSTANCE.setChannelHandlerContext(ctx);
        LOGGER.debug("server ip[{}] connected succeed",ctx.channel().remoteAddress().toString());
        super.channelActive(ctx);
        businessHandler.startCarSimulator();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt){
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent)evt).state();
            if(state == IdleState.WRITER_IDLE){
//                System.out.println("now triggering userEventTriggered");
//                ByteBuf heartBuf = this.getHeartBeat();
//                ctx.channel().writeAndFlush(heartBuf);
            }
        }
    }

}
