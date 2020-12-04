package com.buaa.blockchain.core;


import com.buaa.blockchain.entity.mapper.CompanyInfoMapper;
import com.buaa.blockchain.entity.mapper.PersonInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;


/**
 * 业务逻辑相关的mapper统一在这里管理
 * */
@Slf4j
@Component
@MapperScan(basePackages ="com.buaa.blockchain.entity.mapper")
@ComponentScan(basePackages = "com.buaa.blockchain.*")
public class BusinessMappers {
    public final CompanyInfoMapper companyInfoMapper;
    public final PersonInfoMapper personInfoMapper;

    @Autowired
    public BusinessMappers(CompanyInfoMapper companyInfoMapper, PersonInfoMapper personInfoMapper){
        this.companyInfoMapper = companyInfoMapper;
        this.personInfoMapper = personInfoMapper;
    }

}
