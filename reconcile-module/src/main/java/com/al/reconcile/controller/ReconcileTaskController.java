package com.al.reconcile.controller;

import com.al.common.result.Result;
import com.al.reconcile.bean.dto.ReconcileExecuteDto;
import com.al.reconcile.bean.dto.ReconcileTaskQueryDto;
import com.al.reconcile.service.ReconcileTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/task")
public class ReconcileTaskController {

    @Autowired
    private ReconcileTaskService taskService;

    @PostMapping("/execute")
    public Result execute(@Valid @RequestBody ReconcileExecuteDto dto) {
        return Result.success(taskService.execute(dto));
    }

    @GetMapping("/query")
    public Result queryTask(@RequestParam("taskNo") String taskNo) {
        return Result.success(taskService.queryTask(taskNo));
    }

    @GetMapping("/diff/list")
    public Result queryDiffs(@RequestParam("taskNo") String taskNo) {
        return Result.success(taskService.queryDiffs(taskNo));
    }

    @PostMapping("/list")
    public Result list(@RequestBody ReconcileTaskQueryDto dto) {
        return Result.success(taskService.queryTasks(dto));
    }
}
