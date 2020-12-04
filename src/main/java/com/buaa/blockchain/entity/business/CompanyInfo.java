package com.buaa.blockchain.entity.business;

import lombok.Data;

@Data
public class CompanyInfo {
     int id;
    //公司代码
     String company_code;
    //公司名
     String company_name;
    //社会信用代码
     String social_credit_code;
    //公司账户零钱余额
     float change_balance;
    //法人信息(姓名、身份证号
     String corporation_name;
     String corporation_id_num;
    //营业执照
     String license_thumb;
    //公司信息审核状态
     String audit_status;
    public CompanyInfo(){}
}
