package com.ddf.boot.common.core.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * 通用基类
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 */
@Data
public class BaseDomain {

    @TableId(type = IdType.INPUT)
    @Id
    protected Long id;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    protected String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @CreatedDate
    protected Date createTime;

    @TableField(value = "modify_by", fill = FieldFill.INSERT_UPDATE)
    protected String modifyBy;

    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    @LastModifiedDate
    protected Date modifyTime;

    /**
     * https://github.com/baomidou/mybatis-plus/issues/707
     * https://github.com/baomidou/mybatis-plus/issues/1387
     * 由于以上问题的存在，禁止使用框架生成的逻辑删除功能
     */
    /**
     * 是否删除 0保留 1删除
     */
    //    @TableLogic
    protected Integer removed = 0;

    @Version
    @TableField(fill = FieldFill.INSERT)
    @org.springframework.data.annotation.Version
    protected Integer version = 1;

}