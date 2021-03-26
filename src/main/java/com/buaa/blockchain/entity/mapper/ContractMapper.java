package com.buaa.blockchain.entity.mapper;

import com.buaa.blockchain.entity.Contract;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/16
 * @since JDK1.8
 */
@Repository
public interface ContractMapper {
    String findAll = "SELECT * FROM contract ";
    String findPageList = "SELECT * FROM contract order by id desc limit #{offset}, #{size}";
    String count = "SELECT count(*) FROM contract ";
    String findById = "SELECT * FROM contract WHERE id = #{id}";
    String deleteById = "DELETE from contract WHERE id = #{id}";
    String insert = "INSERT INTO contract (name, codeHash, address, timestamp) VALUES (#{name}, #{codeHash}, #{address}, #{timestamp})";
    String update = "UPDATE contract SET name = #{name}, codeHash = #{codeHash}, address = #{address}, timestamp = #{timestamp} WHERE id = #{id}";

    @Select(findAll)
    List<Contract> findAll();

    @Select(findPageList)
    List<Contract> findPageList(Long offset, Long size);

    @Select(count)
    Long count();

    @Select(findById)
    Contract findById(Long id);

    @Insert(insert)
//    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(String name, String codeHash, String address, Date timestamp);
}
