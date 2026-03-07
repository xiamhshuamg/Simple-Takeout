package com.ynu.edu.elm_intermediate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderBlackBoxApiTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RequestMappingHandlerMapping handlerMapping;

    // ====== 按需改成你系统里真实存在的测试账号 ======
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "123456";
    private static final String TEST_PHONE    = "13800000000"; // 你后端若校验格式，必须用这种
    // ===========================================

    private static class Row {
        int idx;
        String methodType; // 正向/异常/权限
        String desc;
        String status;     // PASS/FAIL/SKIP
        long ms;
        String note;

        Row(int idx, String methodType, String desc, String status, long ms, String note) {
            this.idx = idx;
            this.methodType = methodType;
            this.desc = desc;
            this.status = status;
            this.ms = ms;
            this.note = note == null ? "" : note;
        }
    }

    private final List<Row> results = new CopyOnWriteArrayList<>();
    private int seq = 0;

    private String cachedToken = null;

    private String orderEndpointPath = null;
    private HttpMethod orderEndpointMethod = HttpMethod.GET;

    // 路径变量匹配：{xxx}
    private static final Pattern PATH_VAR = Pattern.compile("\\{[^/}]+}");

    @BeforeAll
    void detectOrderEndpoint() {
        // 收集候选（包含 /order 或 /orders）
        List<Candidate> cands = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = e.getKey();

            Set<String> patterns = info.getPatternValues();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();

            for (String p : patterns) {
                String low = p.toLowerCase(Locale.ROOT);
                if (!(low.contains("/order") || low.contains("/orders"))) continue;

                // 方法为空代表不限制；我们这里按 GET 优先
                Set<RequestMethod> ms = methods.isEmpty() ? EnumSet.noneOf(RequestMethod.class) : methods;

                if (ms.isEmpty()) {
                    cands.add(new Candidate(p, HttpMethod.GET, true));
                } else {
                    for (RequestMethod rm : ms) {
                        cands.add(new Candidate(p, toHttpMethod(rm), false));
                    }
                }
            }
        }

        // 打印扫描到的订单相关接口，方便你在控制台核对
        System.out.println("\n[OrderBlackBoxApiTest] Detected order-related endpoints:");
        cands.stream()
                .sorted(Comparator.comparingInt(Candidate::score).reversed())
                .limit(20)
                .forEach(c -> System.out.println("  - " + c.method + " " + c.path));

        // 选择最优：GET 优先、无 { } 优先、list/page/query 关键词优先
        Candidate best = null;
        for (Candidate c : cands) {
            if (best == null || c.score() > best.score()) best = c;
        }

        if (best == null) {
            // 兜底
            best = new Candidate("/order/list", HttpMethod.GET, true);
        }

        this.orderEndpointPath = best.path;
        this.orderEndpointMethod = best.method;

        System.out.println("[OrderBlackBoxApiTest] Using order endpoint: " + orderEndpointMethod + " " + orderEndpointPath + "\n");
    }

    private static class Candidate {
        String path;
        HttpMethod method;
        boolean methodGuessed;

        Candidate(String path, HttpMethod method, boolean methodGuessed) {
            this.path = path;
            this.method = method;
            this.methodGuessed = methodGuessed;
        }

        int score() {
            String low = path.toLowerCase(Locale.ROOT);
            int s = 0;
            if (low.contains("/order")) s += 50;
            if (method == HttpMethod.GET) s += 30;
            if (!low.contains("{")) s += 40;                 // 关键：不带路径变量优先
            if (low.contains("list") || low.contains("page") || low.contains("query")) s += 20;
            if (low.contains("my") || low.contains("user")) s += 10;
            if (methodGuessed) s -= 5;
            return s;
        }
    }

    private HttpMethod toHttpMethod(RequestMethod rm) {
        return switch (rm) {
            case GET -> HttpMethod.GET;
            case POST -> HttpMethod.POST;
            case PUT -> HttpMethod.PUT;
            case DELETE -> HttpMethod.DELETE;
            case PATCH -> HttpMethod.PATCH;
            default -> HttpMethod.GET;
        };
    }

    // ===========================
    // 工具：统一记录一条测试结果
    // ===========================
    private void runCase(String methodType, String desc, Executable exec) {
        int id = ++seq;
        long t0 = System.nanoTime();
        try {
            exec.execute();
            long ms = (System.nanoTime() - t0) / 1_000_000;
            results.add(new Row(id, methodType, desc, "PASS", ms, ""));
        } catch (TestAbortedException skip) {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            results.add(new Row(id, methodType, desc, "SKIP", ms, skip.getMessage()));
        } catch (Throwable ex) {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            String msg = ex.getMessage();
            if (msg == null) msg = ex.getClass().getSimpleName();
            results.add(new Row(id, methodType, desc, "FAIL", ms, msg));
        }
    }

    // ===========================
    // 工具：登录拿 token（自动适配 phone 必填）
    // ===========================
    private String ensureTokenOrNull() throws Exception {
        if (cachedToken != null) return cachedToken;

        String loginPath = "/user/login";

        List<Map<String, Object>> candidates = new ArrayList<>();
        // 1) username + password
        candidates.add(new LinkedHashMap<>() {{
            put("username", TEST_USERNAME);
            put("password", TEST_PASSWORD);
        }});
        // 2) phone + password（你后端现在至少要求 phone 非空）
        candidates.add(new LinkedHashMap<>() {{
            put("phone", TEST_PHONE);
            put("password", TEST_PASSWORD);
        }});
        // 3) username + phone + password
        candidates.add(new LinkedHashMap<>() {{
            put("username", TEST_USERNAME);
            put("phone", TEST_PHONE);
            put("password", TEST_PASSWORD);
        }});

        for (Map<String, Object> body : candidates) {
            MockHttpServletRequestBuilder rb = post(loginPath)
                    .contentType("application/json;charset=UTF-8")
                    .content(objectMapper.writeValueAsString(body));

            var res = mockMvc.perform(rb).andReturn().getResponse();
            int status = res.getStatus();
            String text = res.getContentAsString();

            if (status >= 200 && status < 300) {
                JsonNode root = safeJson(text);
                if (root == null) return null;

                // 常见：{"data":{"token":"..."}} / {"token":"..."} / {"data":"..."}
                String token = null;
                JsonNode data = root.get("data");
                if (data != null) {
                    if (data.isTextual()) token = data.asText();
                    JsonNode t = data.get("token");
                    if (token == null && t != null && t.isTextual()) token = t.asText();
                }
                if (token == null && root.get("token") != null && root.get("token").isTextual()) {
                    token = root.get("token").asText();
                }

                if (token != null && !token.isBlank()) {
                    cachedToken = token;
                    return cachedToken;
                }
                return null;
            }
        }
        return null;
    }

    // ===========================
    // 修复点：如果 path 含 {xxx}，自动填充 dummy 值，避免 “expand orderId” 报错
    // ===========================
    private Object[] buildUriVarsIfNeeded(String path) {
        Matcher m = PATH_VAR.matcher(path);
        List<Object> vars = new ArrayList<>();
        while (m.find()) vars.add(1); // dummy=1；未授权测试无所谓真实存在
        return vars.toArray();
    }

    private MockHttpServletRequestBuilder buildOrderRequest(boolean withToken, String tokenValue) {
        Object[] uriVars = buildUriVarsIfNeeded(orderEndpointPath);

        MockHttpServletRequestBuilder rb;
        if (orderEndpointMethod == HttpMethod.POST) rb = post(orderEndpointPath, uriVars);
        else if (orderEndpointMethod == HttpMethod.PUT) rb = put(orderEndpointPath, uriVars);
        else if (orderEndpointMethod == HttpMethod.DELETE) rb = delete(orderEndpointPath, uriVars);
        else rb = get(orderEndpointPath, uriVars);

        rb.contentType("application/json;charset=UTF-8");

        if (withToken) {
            // 兼容多种写法：Authorization / token / Bearer
            rb.header("Authorization", tokenValue);
            rb.header("token", tokenValue);
            rb.header("Authorization", "Bearer " + tokenValue);
        }
        return rb;
    }

    // ==========================================
    // 订单功能黑盒测试（重点测权限：未带/错 token/正确 token）
    // ==========================================
    @Test
    void order_api_permission_tests_with_summary() {

        runCase("权限", "订单接口-未携带token访问应被拒绝(4xx或2xx业务失败)", () -> {
            var res = mockMvc.perform(buildOrderRequest(false, null)).andReturn().getResponse();
            int status = res.getStatus();
            String body = res.getContentAsString();

            // 允许：4xx；或者 200 但业务 code != 成功
            if (status >= 200 && status < 300) {
                JsonNode root = safeJson(body);
                if (root == null) throw new AssertionError("2xx但返回非JSON，无法判断是否拒绝：" + body);
                int code = root.has("code") ? root.get("code").asInt() : Integer.MIN_VALUE;
                if (isBizSuccess(root, code)) {
                    throw new AssertionError("未携带token却业务成功了，可能权限没拦住。body=" + body);
                }
            } else if (status >= 400 && status < 500) {
                // PASS
            } else {
                throw new AssertionError("预期4xx或2xx业务失败，但实际 status=" + status + ", body=" + body);
            }
        });

        runCase("权限", "订单接口-携带错误token访问应被拒绝(4xx或2xx业务失败)", () -> {
            var res = mockMvc.perform(buildOrderRequest(true, "bad.token.value")).andReturn().getResponse();
            int status = res.getStatus();
            String body = res.getContentAsString();

            if (status >= 200 && status < 300) {
                JsonNode root = safeJson(body);
                if (root == null) throw new AssertionError("2xx但返回非JSON，无法判断是否拒绝：" + body);
                int code = root.has("code") ? root.get("code").asInt() : Integer.MIN_VALUE;
                if (isBizSuccess(root, code)) {
                    throw new AssertionError("错误token却业务成功了，可能权限没拦住。body=" + body);
                }
            } else if (status >= 400 && status < 500) {
                // PASS
            } else {
                throw new AssertionError("预期4xx或2xx业务失败，但实际 status=" + status + ", body=" + body);
            }
        });

        runCase("权限", "订单接口-携带有效token访问应通过(2xx且业务成功/至少不是未授权)", () -> {
            String token = ensureTokenOrNull();
            Assumptions.assumeTrue(token != null && !token.isBlank(),
                    "无法获取token：请确认测试账号/密码正确，且登录接口会返回token（或调整取token字段）");

            var res = mockMvc.perform(buildOrderRequest(true, token)).andReturn().getResponse();
            int status = res.getStatus();
            String body = res.getContentAsString();

            if (!(status >= 200 && status < 300)) {
                throw new AssertionError("携带有效token仍非2xx，status=" + status + ", body=" + body);
            }

            JsonNode root = safeJson(body);
            if (root != null && root.has("code")) {
                int code = root.get("code").asInt();
                if (!isBizSuccess(root, code)) {
                    throw new AssertionError("2xx但业务code非成功，code=" + code + ", body=" + body);
                }
            }
        });

        printSummary("Order BlackBox API Test Summary");
    }

    private JsonNode safeJson(String s) {
        try { return objectMapper.readTree(s); } catch (Exception e) { return null; }
    }

    /**
     * 宽松判断业务是否成功：兼容不同项目的返回结构
     */
    private boolean isBizSuccess(JsonNode root, int code) {
        // 常见：code==0 / 200 / 20000
        if (code == 0 || code == 200 || code == 20000) return true;

        // 有些项目是 success=true
        if (root != null && root.has("success") && root.get("success").isBoolean()) {
            return root.get("success").asBoolean();
        }
        return false;
    }

    private void printSummary(String title) {
        int pass = 0, fail = 0, skip = 0;
        for (Row r : results) {
            if ("PASS".equals(r.status)) pass++;
            else if ("FAIL".equals(r.status)) fail++;
            else skip++;
        }

        System.out.println("\n======================================");
        System.out.println(title);
        System.out.println("序号 | 测试方法 | 测试描述 | 状态 | 耗时(ms) | 备注");
        System.out.println("--------------------------------------");
        for (Row r : results) {
            System.out.printf(
                    Locale.ROOT,
                    "%-4d | %-4s | %-46s | %-4s | %-7d | %s%n",
                    r.idx, r.methodType, trim(r.desc, 46), r.status, r.ms, r.note
            );
        }
        System.out.println("--------------------------------------");
        int total = pass + fail + skip;
        double rate = total == 0 ? 0.0 : (pass * 100.0 / total);
        System.out.printf(Locale.ROOT, "通过：%d / %d  (%.2f%%)  SKIP=%d%n", pass, total, rate, skip);
        System.out.println("======================================\n");
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }
}
