package com.buaa.blockchain.entity.mapper;

import com.buaa.blockchain.entity.ContractAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractAccountMapper {

    @Select("SELECT cKey,cName,fullName,classData,balance,intro,classType,params,data from contractAccount where cName = #{cName}")
    public ContractAccount findContractAccountByName(String cName);

    @Insert("INSERT INTO contractAccount(cKey,cName,fullName,classData,balance,intro,classType,params,data) " +
            "VALUES" +
            " ( #{cKey},#{cName},#{fullName},#{classData},#{balance},#{intro},#{classType},#{params},#{data})")
    public int insertUserAccount(ContractAccount contractAccount);

    @Select("SELECT * from contractAccount where id = #{id}")
    public ContractAccount findContractById(int id);

    @Select("SELECT * from contractAccount where cKey = #{ckey}")
    public ContractAccount findContractByCkey(String ckey);

    @Select("SELECT * from contractAccount where cName = #{cName}")
    public ContractAccount findContractByCname(String cname);

    @Select("SELECT count(*) from contractAccount")
    public int findContractNum();

    @Select("SELECT * from contractAccount limit #{offset},#{count}")
    public List<ContractAccount> findPageContracts(int offset, int count);

    @Select("SELECT cName from contractAccount")
    public List<String> findContractNameList();

}
