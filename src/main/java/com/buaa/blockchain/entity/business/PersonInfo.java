package com.buaa.blockchain.entity.business;


import lombok.Data;

import java.util.Date;

@Data
public class PersonInfo {
    private int id;
    //身份证号
    private String id_num;
    //生日
    private Date birthday;
    //性别
    private String sex;
    //零钱余额
    private float change_balance;
    //个人账户余额
    private float individual_balance;
    //当前就业公司编号
    private String company_code;
    //个人状态（已就业、未就业、退休）
    private String work_status;
    //申请理赔（申领养老金）状态（未申请、未审核、审核不通过、审核通过）
    private String claim_status;
    //信息审核状态
    private String audit_status;

    public PersonInfo(){}
}
