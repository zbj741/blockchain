package com.buaa.blockchain.entity.mapper;

import com.buaa.blockchain.entity.business.CompanyInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompanyInfoMapper {

    @Select("select max(id) from company_info")
    int findMaxId();

    @Select("select * from company_info where company_code=#{company_code}")
    CompanyInfo findCompanyByCode(String company_code);

    @Insert("INSERT INTO company_info ( id, company_code, company_name, social_credit_code, " +
            "change_balance, corporation_name, corporation_id_num, license_thumb, audit_status)" +
            " VALUES" +
            " (#{id}, #{company_code}, #{company_name}, #{social_credit_code}, #{change_balance}, " +
            "#{corporation_name}, #{corporation_id_num}, #{license_thumb}, #{audit_status})")
    int insertCompanyInfo(CompanyInfo companyInfo);
}
