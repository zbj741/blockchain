package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.ContractApi;
import com.buaa.blockchain.entity.Contract;
import com.buaa.blockchain.utils.ResultMsg;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Scope("prototype")
@RequestMapping("/contract")
@RequiredArgsConstructor
public class ContractController {
    private final ContractApi contractApi;

    @GetMapping(value = "/findPageList")
    public ResultMsg<Contract> findPageList(@RequestParam Long page_index, @RequestParam Long page_size){
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(contractApi.findPageList(page_index, page_size));
        return result;
    }

    @GetMapping(value = "/findContractById")
    public ResultMsg findContractById(@RequestParam(value = "id") int id){
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(contractApi.findContractById(id));
        result.setMsg("合约信息");
        return result;
    }

    @GetMapping(value = "/findContractNum")
    public ResultMsg findContractNum(){
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(contractApi.findContractNum());
        result.setMsg("合约数量");
        return result;
    }

    @GetMapping(value = "/findContractNameList")
    public ResultMsg findContractNameList(){
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("原生合约", new String[] { "AddUser", "Transfer" });
        map.put("自定义合约", contractApi.findContractNameList());
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(map);
        result.setMsg("合约名列表");
        return result;
    }

}


