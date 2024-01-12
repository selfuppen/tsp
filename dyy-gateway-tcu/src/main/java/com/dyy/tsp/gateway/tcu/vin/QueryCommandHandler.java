package com.dyy.tsp.gateway.tcu.vin;

import com.dyy.tsp.core.base.AbstractBusinessHandler;
import com.dyy.tsp.core.evgb.entity.DataBody;
import com.dyy.tsp.core.evgb.entity.EvGBProtocol;
import com.dyy.tsp.core.evgb.entity.SubsystemVoltageData;
import com.dyy.tsp.core.evgb.enumtype.CommandType;
import com.dyy.tsp.core.evgb.enumtype.ResponseType;
import com.dyy.tsp.gateway.tcu.netty.TcuChannel;
import io.netty.channel.Channel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.dyy.tsp.core.evgb.enumtype.CommandType.QUERY_COMMAND;

/**
 * @Author xicai.cxc
 * @Date 9:08 下午
 */
@Service
public class QueryCommandHandler extends AbstractBusinessHandler {

    @Override
    public void doBusiness(EvGBProtocol protocol, Channel channel){
           System.out.println("hello hit query command output!!!");

        SubsystemVoltageData data = new SubsystemVoltageData();
        data.setVoltage(3);
        data.setCellCount(4);
        short s1 = 1;
        data.setNum(s1);
        data.setVoltage(5);
        data.setCurrent(6);
        data.setBatteryNumber(7);
        data.setBatteryConnt(s1);
        List<Integer> cellVoltages = new ArrayList<>();
        cellVoltages.add(new Integer(10));
        data.setCellVoltages(cellVoltages);

        //protocol.setCommandType(QUERY_COMMAND);
        protocol.setResponseType(ResponseType.SUCCESS);
        protocol.setVin(protocol.getVin());
        protocol.setBody(new DataBody(data.encode()));
       // protocol.setBody(null);

        TcuChannel.INSTANCE.getChannelHandlerContext().channel().writeAndFlush(protocol.encode());


        System.out.println("send 2hit query command reply!!!");

    };

}
