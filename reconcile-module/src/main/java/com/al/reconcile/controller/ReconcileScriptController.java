package com.al.reconcile.controller;

import com.al.common.result.Result;
import com.al.reconcile.bean.dto.ReconcileScriptQueryDto;
import com.al.reconcile.bean.dto.ReconcileScriptSaveDto;
import com.al.reconcile.bean.dto.ReconcileScriptTestDto;
import com.al.reconcile.service.ReconcileScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/script")
public class ReconcileScriptController {

    @Autowired
    private ReconcileScriptService scriptService;

    @PostMapping("/save")
    public Result save(@Valid @RequestBody ReconcileScriptSaveDto dto) {
        return Result.success(scriptService.save(dto));
    }

    @PostMapping("/query")
    public Result query(@RequestBody ReconcileScriptQueryDto dto) {
        return Result.success(scriptService.query(dto));
    }

    @GetMapping("/get")
    public Result get(@RequestParam("scriptCode") String scriptCode) {
        return Result.success(scriptService.getActiveScript(scriptCode));
    }

    @PostMapping("/test")
    public Result test(@Valid @RequestBody ReconcileScriptTestDto dto) {
        return Result.success(scriptService.test(dto));
    }
}
