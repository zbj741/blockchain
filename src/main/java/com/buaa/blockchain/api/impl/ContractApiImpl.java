package com.buaa.blockchain.api.impl;

import com.buaa.blockchain.api.ContractApi;
import com.buaa.blockchain.entity.Contract;
import com.buaa.blockchain.entity.ContractAccount;
import com.buaa.blockchain.entity.mapper.ContractAccountMapper;
import com.buaa.blockchain.entity.mapper.ContractMapper;
import com.buaa.blockchain.utils.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractApiImpl implements ContractApi {
    private final ContractAccountMapper contractAccountMapper;
    private final ContractMapper contractMapper;

    @Override
    public int findContractNum() {
        return contractAccountMapper.findContractNum();
    }

    @Override
    public ContractAccount findContractById(int id) {
        return contractAccountMapper.findContractById(id);
    }

    public List<ContractAccount> findPageContracts(int page_index, int page_size) {
        int offset = (page_index - 1) * page_size < 0 ? 0 : (page_index - 1) * page_size;
        int count = page_size;
        return contractAccountMapper.findPageContracts(offset, count);
    }

    @Override
    public List<String> findContractNameList() {
        return contractAccountMapper.findContractNameList();
    }

    @Override
    public Page<Contract> findPageList(Long page_index, Long page_size) {
        final Long totalSize = contractMapper.count();
        final Long totalPage = totalSize % page_size == 0 ? totalSize / page_size : totalSize / page_size + 1;

        if (page_index == null || page_index < 0) {
            page_index = 0l;
        }
        List<Contract> data = contractMapper.findPageList((page_index - 1) * page_size, page_size);

        Page<Contract> page = new Page<>();
        page.setPageIndex(page_index);
        page.setPageSize(page_size);
        page.setTotalSize(totalSize);
        page.setTotalPage(totalPage);
        page.setData(data);
        return page;
    }

}
