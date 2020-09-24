package com.buaa.blockchain.entity.mapper;


import com.buaa.blockchain.entity.UserAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;


/**
 * 创建和查看
 * */
@Repository
public interface UserAccountMapper {
    @Select("SELECT userKey, userName,password,intro,balance,data from userAccount where userName = #{userName}")
    public UserAccount findUserAccountByName(String userName);

    @Insert("INSERT INTO userAccount ( userKey, userName, password, intro, balance, data)" +
            " VALUES " +
            "  (  #{userKey}, #{userName}, #{password}, #{intro}, #{balance}, #{data})")
    public void insertUserAccount(UserAccount userAccount);

    @Update("UPDATE userAccount SET balance = #{balance} WHERE userName = #{userName}")
    public void updateBalance(@Param(value = "balance") int balance, @Param(value = "userName") String name);
}
