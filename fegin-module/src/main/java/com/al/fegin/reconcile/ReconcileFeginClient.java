package com.al.fegin.reconcile;

import com.al.bean.dto.reconcile.ReconcileExecuteDto;
import com.al.bean.dto.reconcile.ReconcileScriptQueryDto;
import com.al.bean.dto.reconcile.ReconcileScriptSaveDto;
import com.al.bean.dto.reconcile.ReconcileTaskQueryDto;
import com.al.bean.vo.reconcile.ReconcileDiffVo;
import com.al.bean.vo.reconcile.ReconcileExecuteResultVo;
import com.al.bean.vo.reconcile.ReconcileScriptVo;
import com.al.bean.vo.reconcile.ReconcileTaskVo;
import com.al.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "reconcile-module", path = "/reconcile")
public interface ReconcileFeginClient {

    @PostMapping("/script/save")
    Result<ReconcileScriptVo> saveScript(@RequestBody ReconcileScriptSaveDto dto);

    @PostMapping("/script/query")
    Result<List<ReconcileScriptVo>> queryScripts(@RequestBody ReconcileScriptQueryDto dto);

    @GetMapping("/script/get")
    Result<ReconcileScriptVo> getScript(@RequestParam("scriptCode") String scriptCode);

    @PostMapping("/task/execute")
    Result<ReconcileExecuteResultVo> execute(@RequestBody ReconcileExecuteDto dto);

    @GetMapping("/task/query")
    Result<ReconcileTaskVo> queryTask(@RequestParam("taskNo") String taskNo);

    @GetMapping("/task/diff/list")
    Result<List<ReconcileDiffVo>> queryDiffs(@RequestParam("taskNo") String taskNo);

    @PostMapping("/task/list")
    Result<List<ReconcileTaskVo>> listTasks(@RequestBody ReconcileTaskQueryDto dto);
}
