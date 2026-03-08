package com.ynu.edu.elm_intermediate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // 【新增】
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("visit_log")
public class VisitLog {
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 【关键修改】明确指定数据库列名为 business_id
    @TableField("business_id")
    private Integer businessId;

    // 【关键修改】明确指定数据库列名为 visit_time
    @TableField("visit_time")
    private LocalDateTime visitTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getBusinessId() { return businessId; }
    public void setBusinessId(Integer businessId) { this.businessId = businessId; }
    
    public LocalDateTime getVisitTime() { return visitTime; }
    public void setVisitTime(LocalDateTime visitTime) { this.visitTime = visitTime; }
}