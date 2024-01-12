package com.dyy.tsp.gateway.tcu.enumtype;

import com.dyy.tsp.core.base.IStatus;
import com.dyy.tsp.core.evgb.entity.RealTimeData;
import com.dyy.tsp.gateway.tcu.vin.QueryCommandHandler;
import com.dyy.tsp.gateway.tcu.vin.RemoteControlCommandHandler;
import com.dyy.tsp.gateway.tcu.vin.SetCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;

public enum TcuHandlerType {

    //上行指令
    VEHICLE_LOGIN((short)1, "车辆登入",null, null),
    REALTIME_DATA_REPORTING((short)2,"实时信息上报",new RealTimeData(), null),
    REPLACEMENT_DATA_REPORTING((short)3,"补发信息上报",new RealTimeData(),null),
    VEHICLE_LOGOUT((short)4,"车辆登出",null,null),
    PLATFORM_LOGIN((short)5,"平台登入",null, null), //国家过检才用
    PLATFORM_LOGOUT((short)6,"平台登出",null,null), //国家过检才用
    HEARTBEAT((short)7,"心跳",null, null),
    TERMINAL_CHECK_TIME((short)8,"终端校时",null, null),

    //下行指令
    QUERY_COMMAND((short)128,"查询命令",null, QueryCommandHandler.class),
    SET_COMMAND((short)129,"设置命令",null, SetCommandHandler.class),
    REMOTE_CONTROL((short)130,"车载终端控制命令", null,RemoteControlCommandHandler.class),
    ;

    private Short id;
    private String desc;
    private IStatus status;
    private Class handler;

    TcuHandlerType(Short id, String desc, IStatus status, Class handler) {
        this.id = id;
        this.desc = desc;
        this.status = status;
        this.handler = handler;
    }

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public IStatus getStatus() {
        return status;
    }

    public void setStatus(IStatus status) {
        this.status = status;
    }

    public Class getHandler() {
        return handler;
    }

    public void setHandler(Class handler) {
        this.handler = handler;
    }

    public static TcuHandlerType valuesOf(Short id) {
        for (TcuHandlerType enums : TcuHandlerType.values()) {
            if (enums.getId().shortValue()==id.shortValue()) {
                return enums;
            }
        }
        return null;
    }
}
