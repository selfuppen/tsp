package com.dyy.tsp.core.evgb.enumtype;

/**
 * 指令代码类型
 * created by dyy
 */
@SuppressWarnings("all")
public enum CommandType {

    //上行指令
    VEHICLE_LOGIN((short)1, "VEHICLE_LOGIN"),
    VEHICLE_LOGOUT((short)4,"VEHICLE_LOGOUT"),
    REALTIME_DATA_REPORTING((short)2,"REALTIME_DATA_REPORTING"),
    REPLACEMENT_DATA_REPORTING((short)3,"REPLACEMENT_DATA_REPORTING"),
    PLATFORM_LOGIN((short)5,"PLATFORM_LOGIN"), //国家过检才用
    PLATFORM_LOGOUT((short)6,"PLATFORM_LOGOUT"), //国家过检才用
    HEARTBEAT((short)7,"HEARTBEAT"),
    TERMINAL_CHECK_TIME((short)8,"TERMINAL_CHECK_TIME"),

    //下行指令
    QUERY_COMMAND((short)128,"查询命令"),
    SET_COMMAND((short)129,"设置命令"),
    REMOTE_CONTROL((short)130,"车载终端控制命令");
    ;

    private Short id;
    private String desc;

    CommandType(Short id, String desc) {
        this.id = id;
        this.desc = desc;
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

    public static CommandType valuesOf(Short id) {
        for (CommandType enums : CommandType.values()) {
            if (enums.getId().shortValue() == id.shortValue()) {
                return enums;

            }
        }
        return null;
    }

    public static CommandType valuesOf(String name) {
        for (CommandType enums : CommandType.values()) {
            if (enums.name().equals(name)) {
                return enums;
            }
        }
        return null;
    }
}
