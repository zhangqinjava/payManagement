package com.al.reconcile.engine;

import com.al.common.exception.BusinessException;
import com.al.reconcile.model.ReconcileContext;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GroovyScriptRunner {

    private static final String METHOD_PARSE = "parse";
    private static final String METHOD_COMPARE = "compare";

    private final Map<String, CompiledScriptHolder> scriptCache = new ConcurrentHashMap<>();

    public void validate(String scriptContent) {
        createShell().parse(scriptContent);
    }

    public Object invoke(String scriptCode, int version, String scriptContent,
                         String methodName, ReconcileContext ctx, Object... args) {
        String cacheKey = scriptCode + "_" + version;
        CompiledScriptHolder holder = scriptCache.compute(cacheKey, (key, existing) -> {
            if (existing != null) {
                return existing;
            }
            Script script = createShell().parse(scriptContent);
            return new CompiledScriptHolder(script);
        });
        try {
            Binding binding = new Binding();
            binding.setVariable("log", log);
            binding.setVariable("ctx", ctx);
            binding.setVariable("BigDecimal", BigDecimal.class);
            holder.script.setBinding(binding);
            return holder.script.invokeMethod(methodName, args);
        } catch (Exception e) {
            log.error("groovy script execute failed scriptCode={}, method={}", scriptCode, methodName, e);
            throw new BusinessException("脚本执行失败:" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public java.util.List<Map<String, Object>> parse(String scriptCode, int version, String scriptContent,
                                                      ReconcileContext ctx, String rawContent) {
        Object result = invoke(scriptCode, version, scriptContent, METHOD_PARSE, ctx, ctx, rawContent);
        if (result == null) {
            return java.util.Collections.emptyList();
        }
        if (!(result instanceof java.util.List)) {
            throw new BusinessException("解析脚本必须返回List");
        }
        return (java.util.List<Map<String, Object>>) result;
    }

    @SuppressWarnings("unchecked")
    public java.util.List<Map<String, Object>> compare(String scriptCode, int version, String scriptContent,
                                                         ReconcileContext ctx,
                                                         java.util.List<Map<String, Object>> localRows,
                                                         java.util.List<Map<String, Object>> remoteRows) {
        Object result = invoke(scriptCode, version, scriptContent, METHOD_COMPARE, ctx, ctx, localRows, remoteRows);
        if (result == null) {
            return java.util.Collections.emptyList();
        }
        if (!(result instanceof java.util.List)) {
            throw new BusinessException("比对脚本必须返回List");
        }
        return (java.util.List<Map<String, Object>>) result;
    }

    public void invalidate(String scriptCode) {
        scriptCache.keySet().removeIf(key -> key.startsWith(scriptCode + "_"));
    }

    private GroovyShell createShell() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addImport("ReconcileContext", ReconcileContext.class.getName());
        importCustomizer.addStarImports("java.math", "java.util");
        configuration.addCompilationCustomizers(importCustomizer);
        return new GroovyShell(configuration);
    }

    private static class CompiledScriptHolder {
        private final Script script;

        private CompiledScriptHolder(Script script) {
            this.script = script;
        }
    }
}
