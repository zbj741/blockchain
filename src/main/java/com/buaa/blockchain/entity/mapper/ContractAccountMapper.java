package com.buaa.blockchain.entity.mapper;

import com.buaa.blockchain.entity.ContractAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractAccountMapper {

    @Select("SELECT cKey,cName,fullName,classData,balance,intro,classType,params,data from contractAccount where cName = #{cName}")
    public ContractAccount findContractAccountByName(String cName);

    @Insert("INSERT INTO contractAccount(cKey,cName,fullName,classData,balance,intro,classType,params,data) " +
            "VALUES" +
            " ( #{cKey},#{cName},#{fullName},#{classData},#{balance},#{intro},#{classType},#{params},#{data})")
    public int insertUserAccount(ContractAccount contractAccount);

}
