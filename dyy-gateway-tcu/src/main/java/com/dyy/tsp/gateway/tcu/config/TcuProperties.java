package com.dyy.tsp.gateway.tcu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("all")
@ConfigurationProperties(prefix = TcuProperties.PREFIX)
@Data
public class TcuProperties {

    public static final String PREFIX = "tcu";

    //网关域名 TODO:需要替换为您的云网关实例的域名
    private String gatewayHost = "192.168.10.37";

    //网关端口 TODO:需要替换为您的云网关实例的端口号
    private Integer gatewayPort = 16003;

    //是否使用TLS TODO:如果使用了TCP直连，需要将其设置为FALSE
    private boolean useSSL = Boolean.FALSE;

    //如果没有使用TLS,请忽略；如果用到了，请设置根证书root-ca.crt的路径位置
    private String CA_PATH = "${Your Absolute Path of root-ca.crt file}";

    //车辆识别代码. 长度为17个字节。TODO: 需要替换为您车辆的vin号，并在控制台以此vin号创建设备
    private String vin = "1234567890123456c";

    //SIM卡的ICCID号，长度为20位字符串
    private String iccid = "12345678901234567890";

    //登入流水号，范围为1~65531，长度为2个字节，每次登录都需要对该值+1
    private Integer serialNum = 1;

    //Tbox终端应该设置4秒1次心跳。这里TCP模拟就设置30秒
    private Integer timeout = 30;

    //最大重连次数，当前设置为0，表示断线不重连。用户可以修改该值以实现重连
    private Integer reconnectMaxNum = 0;

    private Integer maxFrameLength = 65556;

    private Integer lengthFieldOffset = 22;

    private Integer lengthFieldLength = 2;

    private Integer lengthAdjustment = 1;

    private Integer initialBytesToStrip = 0 ;

    private Boolean failFast = Boolean.TRUE;

    private String kafkaAcks = "1";

    private String kafkaLinger = "50";

    private String kafkaRetries = "1";

    private String kafkaRetryBackoff = "2000";

    private String kafkaReconnectBackoff = "3000";

    private String kafkaCompressionType = "gzip";

    //指令下发请求
    private String commandRequestTopic = "dyy_command_request_data";

}
