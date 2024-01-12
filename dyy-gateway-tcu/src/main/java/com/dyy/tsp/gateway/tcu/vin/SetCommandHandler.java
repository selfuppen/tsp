package com.dyy.tsp.gateway.tcu.vin;

import com.dyy.tsp.core.base.AbstractBusinessHandler;
import com.dyy.tsp.core.evgb.entity.EvGBProtocol;
import com.dyy.tsp.core.evgb.enumtype.ResponseType;
import com.dyy.tsp.gateway.tcu.netty.TcuChannel;
import io.netty.channel.Channel;
import org.springframework.stereotype.Service;

/**
 * @Author xicai.cxc
 * @Date 9:08 下午
 */
@Service
public class SetCommandHandler extends AbstractBusinessHandler {

    @Override
    public void doBusiness(EvGBProtocol protocol, Channel channel){
        protocol.setResponseType(ResponseType.SUCCESS);
        protocol.setVin(protocol.getVin());
        protocol.setBody(null);

        TcuChannel.INSTANCE.getChannelHandlerContext().channel().writeAndFlush(protocol.encode());
           System.out.println("hello hit set command output!!!");
    };

}
