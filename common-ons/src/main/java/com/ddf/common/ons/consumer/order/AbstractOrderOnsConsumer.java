package com.ddf.common.ons.consumer.order;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.OrderConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.ddf.common.ons.consumer.OnsConsumer;
import com.ddf.common.ons.properties.OnsProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * ONS有序消息抽象消费者
 *
 * @author snowball
 * @date 2021/8/26 16:34
 **/
public abstract class AbstractOrderOnsConsumer implements OnsConsumer {

    private final String CONSUME_THREAD_NUMS = "20";

    @Autowired
    protected OnsProperties onsConfiguration;

    public OrderConsumerBean createOrderConsumerBean() {
        OrderConsumerBean orderConsumerBean = new OrderConsumerBean();
        // 配置文件
        Properties properties = onsConfiguration.getOnsProperties();
        properties.setProperty(PropertyKeyConst.GROUP_ID, getGroupId());
        // 设置消费者线程数 默认值为20
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, getConsumeThreadNums());
        orderConsumerBean.setProperties(properties);
        // 订阅关系
        Map<Subscription, MessageOrderListener> subscriptionTable = new HashMap<>();

        Subscription subscription = new Subscription();
        subscription.setTopic(getTopic());
        subscription.setExpression(getExpression());
        subscriptionTable.put(subscription, getMessageOrderListener());

        orderConsumerBean.setSubscriptionTable(subscriptionTable);

        return orderConsumerBean;
    }

    /**
     * 获取并发消费者数量
     * @return
     */
    @Override
    public String getConsumeThreadNums() {
        return CONSUME_THREAD_NUMS;
    }

    /**
     * 获取有序消息监听器
     * @return
     */
    protected abstract MessageOrderListener getMessageOrderListener();

}
