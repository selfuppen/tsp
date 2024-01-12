package com.dyy.tsp.gateway.tcu.handler;

import com.dyy.tsp.core.base.AbstractBusinessHandler;
import com.dyy.tsp.core.base.IHandler;
import com.dyy.tsp.core.evgb.entity.*;
import com.dyy.tsp.core.evgb.enumtype.CommandType;
import com.dyy.tsp.core.evgb.enumtype.ResponseType;
import com.dyy.tsp.gateway.tcu.config.TcuProperties;
import com.dyy.tsp.gateway.tcu.enumtype.TcuHandlerType;
import com.dyy.tsp.gateway.tcu.netty.TcuChannel;
import com.dyy.tsp.gateway.tcu.vo.RealTimeDataVo;
import com.dyy.tsp.gateway.tcu.vo.VehicleLoginVo;
import com.dyy.tsp.gateway.tcu.vo.VehicleLogoutVo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务处理核心类
 * created by dyy
 */
@Service
public class BusinessHandler extends AbstractBusinessHandler implements ApplicationContextAware {

    @Autowired
    TcuProperties tcuProperties;

    private ApplicationContext applicationContext;
    String mVin;
    String mIccid;
    Integer mSerial;
    BeanTime mBeanTime = new BeanTime();

    @Override
    public void doBusiness(EvGBProtocol protrocol, Channel channel) {
        TcuHandlerType tcuHandlerType = TcuHandlerType.valuesOf(protrocol.getCommandType().getId());
        if(tcuHandlerType.getHandler()!=null){
            IHandler handler = (IHandler) applicationContext.getBean(tcuHandlerType.getHandler());
            handler.doBusiness(protrocol,channel);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void startCarSimulator(){
        mVin = tcuProperties.getVin();
        mIccid = tcuProperties.getIccid();
        mSerial = tcuProperties.getSerialNum();

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        /** 1.登入 **/
                        carLogin();

                        /** 2.循环上报消息 **/
                        for(int i=0; i<1; i++){

                            try{
                                Thread.sleep(1000);
                            }catch (Exception e){
                            }

                            /** 2.1 发送心跳包 **/
                            sendHeartBeat();

                            /** 2.2 上报数据 **/
                            pubRealTimeData();

                            /** 2.3 实时数据重传 **/
                            repubRealTimeData();
                        }

                        /** 3.登出 **/
                       carLogout();
                    }
                }
        ).start();
    }

    /**
     * 车辆登入
     */
    private void carLogin(){
        VehicleLoginVo vehicleLoginVo = new VehicleLoginVo();
        vehicleLoginVo.setVin(mVin);

        /**
         * 车辆登入报文的payload部分格式如下：
         *   1.数据采集时间           6字节
         *   2.登入流水号             2字节
         *   3.ICCID                20字节
         *   4.可充电储能子系统数      1字节
         *   5.可充电储能系统编码长度   1字节
         *   6.可充电储能系统编码      n * m字节
         */

        //1.数据采集时间
        LocalDateTime now = LocalDateTime.now();
        short year = 23;   //1 byte, 有效值范围0~99
        short month = (short) (now.getMonthValue());   //1 byte, 有效值范围1~12
        short day = (short) (now.getDayOfMonth()) ;     //1 byte, 有效值范围1~31
        short hour = (short) (now.getHour());    //1 byte, 有效值范围0~23
        short min = (short) (now.getMinute());     //1 byte, 有效值范围0~59
        short seconds = (short) (now.getSecond()); //1 byte, 有效值范围0~59
        mBeanTime.setYear(year);
        mBeanTime.setMonth(month);
        mBeanTime.setDay(day);
        mBeanTime.setHours(hour);
        mBeanTime.setMinutes(min);
        mBeanTime.setSeconds(seconds);
        vehicleLoginVo.setBeanTime(mBeanTime);

        //2.登入流水号
        vehicleLoginVo.setSerialNum(mSerial);

        //3.ICCID
        vehicleLoginVo.setIccid(mIccid);

        //4.可充电储能子系统数
        List<String> codeList = new ArrayList<>();
        vehicleLoginVo.setCodes(codeList);

        EvGBProtocol protocol = new EvGBProtocol();
        protocol.setCommandType(CommandType.VEHICLE_LOGIN);
        protocol.setResponseType(ResponseType.COMMAND);
        protocol.setVin(vehicleLoginVo.getVin());
        protocol.setBody(new DataBody(vehicleLoginVo.encode()));

        TcuChannel.INSTANCE.getChannelHandlerContext().channel().writeAndFlush(protocol.encode());
    }

    /**
     * 发送心跳包
     */
    private void sendHeartBeat(){
        EvGBProtocol evGBProtocol = new EvGBProtocol();
        evGBProtocol.setVin(mVin);
        evGBProtocol.setCommandType(CommandType.HEARTBEAT);
        evGBProtocol.setBody(null);
        evGBProtocol.setResponseType(ResponseType.COMMAND);
        ByteBuf heartBuf = evGBProtocol.encode();
        TcuChannel.INSTANCE.getChannelHandlerContext().channel().writeAndFlush(heartBuf);
    }

    /**
     * 上报实时数据
     */
    private void pubRealTimeData(){
        RealTimeDataVo realTimeDataVo = new RealTimeDataVo();
        realTimeDataVo.setVin(mVin);
        realTimeDataVo.setCommandType("REALTIME_DATA_REPORTING");

        /**
         * 实时数据上报报文的payload部分格式如下：
         *   1.数据采集时间           6字节
         *   2.信息类型标志1           1字节
         *   3.信息体1               20字节
         *   ...
         *   信息类型标志n             1字节
         *   信息体n                  不同类型有不同长度
         */

        //1.数据采集时间
        LocalDateTime now = LocalDateTime.now();
        short year = 23;   //1 byte, 有效值范围0~99
        short month = (short) (now.getMonthValue());   //1 byte, 有效值范围1~12
        short day = (short) (now.getDayOfMonth()) ;     //1 byte, 有效值范围1~31
        short hour = (short) (now.getHour());    //1 byte, 有效值范围0~23
        short min = (short) (now.getMinute());     //1 byte, 有效值范围0~59
        short seconds = (short) (now.getSecond()); //1 byte, 有效值范围0~59
        mBeanTime.setYear(year);
        mBeanTime.setMonth(month);
        mBeanTime.setDay(day);
        mBeanTime.setHours(hour);
        mBeanTime.setMinutes(min);
        mBeanTime.setSeconds(seconds);
        realTimeDataVo.setBeanTime(mBeanTime);

        //驱动电机数据
        Short driveMotorCnt = 0;
        realTimeDataVo.setDriveMotorCount(driveMotorCnt);
        List<DriveMotorData> driveMotorDatas = new ArrayList<>();
        DriveMotorData driD = new DriveMotorData();
        driD.setControllerDcBusbarCurrent(11);
        driveMotorDatas.add(driD);
        realTimeDataVo.setDriveMotorDatas(driveMotorDatas);

       // 可充电储能装置电压数据
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
        List<SubsystemVoltageData> subvs = new ArrayList<>();
        subvs.add(data);
        realTimeDataVo.setSubsystemVoltageDatas(subvs);
        Short subVlotageCount = (short) subvs.size() ;
        realTimeDataVo.setSubsystemVoltageCount(subVlotageCount);

        // 可充电储能装置温度数据
        short tempCnt = 0;
        realTimeDataVo.setSubsystemTemperatureCount(tempCnt);

        EvGBProtocol protocol = new EvGBProtocol();
        protocol.setCommandType(CommandType.valuesOf(realTimeDataVo.getCommandType()));
        protocol.setResponseType(ResponseType.COMMAND);
        protocol.setVin(realTimeDataVo.getVin());
        protocol.setBody(new DataBody(realTimeDataVo.encode()));

        TcuChannel.INSTANCE.getChannelHandlerContext().channel().writeAndFlush(protocol.encode());
    }

    /**
     * 实时数据补报
     */
    private void repubRealTimeData(){
        RealTimeDataVo realTimeDataVo = new RealTimeDataVo();
        realTimeDataVo.setVin(mVin);
        realTimeDataVo.setCommandType("REPLACEMENT_DATA_REPORTING");

        /**
         * 实时数据上报报文的payload部分格式如下：
         *   1.数据采集时间           6字节
         *   2.信息类型标志1           1字节
         *   3.信息体1               20字节
         *   ...
         *   信息类型标志n             1字节
         *   信息体n                  不同类型有不同长度
         */

        //1.数据采集时间
        LocalDateTime now = LocalDateTime.now();
        short year = 23;   //1 byte, 有效值范围0~99
        short month = (short) (now.getMonthValue());   //1 byte, 有效值范围1~12
        short day = (short) (now.getDayOfMonth()) ;     //1 byte, 有效值范围1~31
        short hour = (short) (now.getHour());    //1 byte, 有效值范围0~23
        short min = (short) (now.getMinute());     //1 byte, 有效值范围0~59
        short seconds = (short) (now.getSecond()); //1 byte, 有效值范围0~59
        mBeanTime.setYear(year);
        mBeanTime.setMonth(month);
        mBeanTime.setDay(day);
        mBeanTime.setHours(hour);
        mBeanTime.setMinutes(min);
        mBeanTime.setSeconds(seconds);
        realTimeDataVo.setBeanTime(mBeanTime);

        //驱动电机数据
        Short driveMotorCnt = 0;
        realTimeDataVo.setDriveMotorCount(driveMotorCnt);
        List<DriveMotorData> driveMotorDatas = new ArrayList<>();
        DriveMotorData driD = new DriveMotorData();
        driD.setControllerDcBusbarCurrent(11);
        driveMotorDatas.add(driD);
        realTimeDataVo.setDriveMotorDatas(driveMotorDatas);

        // 可充电储能装置电压数据
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
        List<SubsystemVoltageData> subvs = new ArrayList<>();
        subvs.add(data);
        realTimeDataVo.setSubsystemVoltageDatas(subvs);
        Short subVlotageCount = (short) subvs.size() ;
        realTimeDataVo.setSubsystemVoltageCount(subVlotageCount);

        // 可充电储能装置温度数据
        short tempCnt = 0;
        realTimeDataVo.setSubsystemTemperatureCount(tempCnt);

        EvGBProtocol protocol = new EvGBProtocol();
        protocol.setCommandType(CommandType.valuesOf(realTimeDataVo.getCommandType()));
        protocol.setResponseType(ResponseType.COMMAND);
        protocol.setVin(realTimeDataVo.getVin());
        protocol.setBody(new DataBody(realTimeDataVo.encode()));

        TcuChannel.INSTANCE.getChannelHandlerContext().channel().writeAndFlush(protocol.encode());
    }

    /**
     * 车辆登出
     */
    private void carLogout(){
        /**
         * 车辆登出报文的payload部分格式如下：
         *   1.登出时间                           6字节
         *   2.登入流水号（与当次登入流水号一致）      2字节
         */

        VehicleLogoutVo vehicleLogoutVo = new VehicleLogoutVo();
        vehicleLogoutVo.setVin(mVin);

        //1.登出时间
        LocalDateTime now = LocalDateTime.now();
        short year = 23;                                //1 byte, 有效值范围0~99
        short month = (short) (now.getMonthValue());   //1 byte, 有效值范围1~12
        short day = (short) (now.getDayOfMonth()) ;     //1 byte, 有效值范围1~31
        short hour = (short) (now.getHour());          //1 byte, 有效值范围0~23
        short min = (short) (now.getMinute());         //1 byte, 有效值范围0~59
        short seconds = (short) (now.getSecond());     //1 byte, 有效值范围0~59
        mBeanTime.setYear(year);
        mBeanTime.setMonth(month);
        mBeanTime.setDay(day);
        mBeanTime.setHours(hour);
        mBeanTime.setMinutes(min);
        mBeanTime.setSeconds(seconds);
        vehicleLogoutVo.setBeanTime(mBeanTime);
        //2.登出流水号
        vehicleLogoutVo.setSerialNum(mSerial);

        EvGBProtocol protocol = new EvGBProtocol();
        protocol.setCommandType(CommandType.VEHICLE_LOGOUT);
        protocol.setResponseType(ResponseType.COMMAND);
        protocol.setVin(vehicleLogoutVo.getVin());
        protocol.setBody(new DataBody(vehicleLogoutVo.encode()));
        TcuChannel.INSTANCE.getChannelHandlerContext().channel().writeAndFlush(protocol.encode());
    }

}
