package com.ddf.boot.common.websocket.biz.impl;

import com.ddf.boot.common.exception.GlobalCustomizeException;
import com.ddf.boot.common.util.SpringContextHolder;
import com.ddf.boot.common.util.StringUtil;
import com.ddf.boot.common.websocket.biz.HandlerTemplateType;
import com.ddf.boot.common.websocket.interceptor.SmsParseProcessor;
import com.ddf.boot.common.websocket.model.entity.MerchantBaseDevice;
import com.ddf.boot.common.websocket.model.entity.MerchantMessageInfo;
import com.ddf.boot.common.websocket.model.entity.PlatformMessageTemplate;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.Message;
import com.ddf.boot.common.websocket.model.ws.ParseContent;
import com.ddf.boot.common.websocket.service.MerchantMessageInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认模板解析前置后置处理器实现
 *
 * @author dongfang.ding
 * @date 2019/9/27 9:24
 */
@Service
@Slf4j
public class DefaultSmsParseProcessor implements SmsParseProcessor, InitializingBean {

    @Autowired
    private MerchantMessageInfoService merchantMessageInfoService;


    private static Map<String, HandlerTemplateType> typeStrategyMap;

    /**
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an
     *                   essential property) or if initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, HandlerTemplateType> beansOfType = SpringContextHolder.getBeansOfType(HandlerTemplateType.class);
        if (!beansOfType.isEmpty()) {
            typeStrategyMap = new HashMap<>(beansOfType.size());
            beansOfType.forEach((beanName, beanType) -> {
                for (PlatformMessageTemplate.Type type : beanType.getType()) {
                    typeStrategyMap.put(type.name(), beanType);
                }
            });
        }
    }


    /**
     * 获得模板对应的处理类
     *
     * @param type
     * @return
     * @author dongfang.ding
     * @date 2019/9/27 11:48
     */
    public static HandlerTemplateType getHandler(PlatformMessageTemplate.Type type) {
        HandlerTemplateType handlerTemplateType = null;
        if (typeStrategyMap != null && !typeStrategyMap.isEmpty()) {
            handlerTemplateType = typeStrategyMap.get(type.name());
        }
        if (handlerTemplateType == null) {
            log.warn("没有找到{}对应的处理类", type.name());
        }
        return handlerTemplateType;
    }



    /**
     * 短信解析前处理器
     *
     * @param authPrincipal
     * @param message
     * @param messageInfo
     * @return
     * @author dongfang.ding
     * @date 2019/9/26 21:16
     */
    @Override
    public void before(AuthPrincipal authPrincipal, Message message, MerchantMessageInfo messageInfo) {

    }

    /**
     * 短信解析后处理器
     *
     * @param authPrincipal
     * @param parseContent
     * @param message
     * @param baseDevice
     * @param merchantMessageInfo
     * @return
     * @author dongfang.ding
     * @date 2019/9/27 13:26
     */
    @Override
    public void after(AuthPrincipal authPrincipal, ParseContent parseContent, Message message, MerchantBaseDevice baseDevice
            , MerchantMessageInfo merchantMessageInfo) {
        try {
            HandlerTemplateType handler = getHandler(convertToType(parseContent.getPlatformMessageTemplate().getTemplateType()));
            if (handler != null) {
                handler.handler(authPrincipal, parseContent, baseDevice, merchantMessageInfo, message);
            } else {
                merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_LOGIC_ERROR);
                merchantMessageInfo.setErrorMessage(String.format("没有找到模板类型为%s对应的处理类",
                        parseContent.getPlatformMessageTemplate().getTemplateType()));
                merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice);
            }
        } catch (Exception e) {
            log.error("DefaultSmsParseProcessor执行业务出错！", e);
            merchantMessageInfo.setStatus(MerchantMessageInfo.STATUS_LOGIC_ERROR);
            merchantMessageInfo.setErrorMessage(e.getMessage());
            merchantMessageInfo.setErrorStack(StringUtil.exceptionToString(e));
            merchantMessageInfoService.fillStatus(merchantMessageInfo, baseDevice, parseContent.getTradeNo(),
                    parseContent.getOrderTime());
        }
    }
    
    
    /**
     * 将模板类型映射成枚举
     * 
     * @param type
     * @return
     * @author dongfang.ding
     * @date 2019/9/27 12:48 
     */
    private PlatformMessageTemplate.Type convertToType(Integer type) {
        try {
            return PlatformMessageTemplate.Type.getByValue(type);
        } catch (Exception e) {
            throw new GlobalCustomizeException(String.format("模板类型【%s】映射枚举失败！", type));
        }
    }
}
