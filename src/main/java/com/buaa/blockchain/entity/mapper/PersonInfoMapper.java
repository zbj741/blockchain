package com.buaa.blockchain.entity.mapper;


import com.buaa.blockchain.entity.business.PersonInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonInfoMapper {

    @Select("select max(id) from person_info")
    int findMaxId();

    @Select("Select * from person_info where id_num=#{id_num}")
    PersonInfo findPersonInfoByIdnum(String id_num);

    @Select("select change_balance from person_info where id={info_id}")
    float findChangeBalanceById(int info_id);

    @Select("select individual_balance from person_info where id=#{info_id}")
    float findIndividualBalanceById(int info_id);
}
