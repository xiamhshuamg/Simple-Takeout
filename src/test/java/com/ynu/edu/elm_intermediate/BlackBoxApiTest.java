package com.ynu.edu.elm_intermediate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 黑盒接口测试（建议：直接跑这个类，控制台会输出测试总结）
 *
 * 你只需要按你的项目实际情况，改这三类常量：
 * 1) 登录接口路径 API_LOGIN
 * 2) 一个“需要token才能访问”的受保护接口 API_PROTECTED
 * 3) token的请求头名字 TOKEN_HEADER（常见：token / Authorization）
 *
 * 说明：
 * - 本测试使用 @SpringBootTest + @AutoConfigureMockMvc，会启动完整Spring上下文，避免 @WebMvcTest 引发的 MyBatis sqlSessionFactory 问题
 * - 若你的项目启动依赖数据库，请确保 application.yml 能连上数据库，否则会在“加载上下文”阶段失败（那不是测试代码问题）
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(BlackBoxApiTest.SummaryExtension.class)
public class BlackBoxApiTest {

    // ============ 你需要按实际项目修改的常量（很关键） ============

    /** 登录接口：按你的Controller实际路径改 */
    private static final String API_LOGIN = "/user/login";

    /** 受保护接口：找一个必须带token才能访问的接口路径（例如：/address/list、/order/list、/user/info等） */
    private static final String API_PROTECTED = "/address/list";

    /** token放在哪个请求头：常见 token / Authorization */
    private static final String TOKEN_HEADER = "token";

    /** 如果你用 Authorization: Bearer xxx，就把这里设为 true */
    private static final boolean AUTH_BEARER_MODE = false;

    // ===========================================================

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper OM = new ObjectMapper();

    // 让“有效token测试”能复用
    private static volatile String cachedToken = null;

    // -------------------- 1) 登录功能：多测试方法 --------------------

    @Test
    @Order(1)
    @Tag("正向")
    @DisplayName("登录-正常账号密码应返回token/登录成功信息")
    void login_success_shouldReturnTokenOrSuccess() throws Exception {
        // 按你的项目实际字段改：username/password 或 phone/password 等
        String body = """
                {"username":"test","password":"123456"}
                """;

        MvcResult r = mockMvc.perform(
                        post(API_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String resp = r.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // 尝试提取token（如果你登录不返回token，也至少应有成功标识）
        String token = tryExtractToken(resp);
        if (token != null && !token.isBlank()) {
            cachedToken = token;
        } else {
            // 不强制必须包含“token”字样（不同项目返回结构不同），但至少别是空
            Assertions.assertTrue(resp != null && resp.trim().length() > 0,
                    "登录响应不应为空；若你项目登录会返回token，请确认返回字段是否包含 token");
        }
    }

    @Test
    @Order(2)
    @Tag("异常")
    @DisplayName("登录-错误密码应失败（4xx 或 200但业务失败都可）")
    void login_wrongPassword_shouldFail() throws Exception {
        String body = """
                {"username":"test","password":"wrong_password"}
                """;

        MvcResult r = mockMvc.perform(
                        post(API_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andReturn();

        int status = r.getResponse().getStatus();
        String resp = r.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // 兼容：有的项目用4xx表示失败，有的项目用200 + code!=0表示失败
        if (status >= 400) {
            Assertions.assertTrue(true);
        } else {
            Assertions.assertTrue(isBusinessFailure(resp),
                    "返回不是4xx时，也应该体现业务失败（例如 code!=0 / success=false / msg包含失败信息）。响应=" + resp);
        }
    }

    @Test
    @Order(3)
    @Tag("异常")
    @DisplayName("登录-缺少必要字段应失败")
    void login_missingField_shouldFail() throws Exception {
        String body = """
                {"username":"test"}
                """;

        MvcResult r = mockMvc.perform(
                        post(API_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andReturn();

        int status = r.getResponse().getStatus();
        String resp = r.getResponse().getContentAsString(StandardCharsets.UTF_8);

        if (status >= 400) {
            Assertions.assertTrue(true);
        } else {
            Assertions.assertTrue(isBusinessFailure(resp),
                    "返回不是4xx时，也应该体现业务失败。响应=" + resp);
        }
    }

    // -------------------- 2) Token/权限功能：多测试方法 --------------------

    @Test
    @Order(10)
    @Tag("权限")
    @DisplayName("权限-未携带token访问受保护接口应被拒绝(4xx)")
    void protected_noToken_shouldBeRejected() throws Exception {
        mockMvc.perform(get(API_PROTECTED))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(11)
    @Tag("权限")
    @DisplayName("权限-携带错误token访问受保护接口应被拒绝(4xx)")
    void protected_badToken_shouldBeRejected() throws Exception {
        mockMvc.perform(
                        get(API_PROTECTED)
                                .header(TOKEN_HEADER, buildTokenHeaderValue("bad_token_123"))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(12)
    @Tag("权限")
    @DisplayName("权限-携带有效token访问受保护接口应通过(2xx)")
    void protected_validToken_shouldPass() throws Exception {
        String token = ensureToken();

        mockMvc.perform(
                        get(API_PROTECTED)
                                .header(TOKEN_HEADER, buildTokenHeaderValue(token))
                )
                .andExpect(status().is2xxSuccessful());
    }

    // ========================= 工具方法 =========================

    private String ensureToken() throws Exception {
        if (cachedToken != null && !cachedToken.isBlank()) return cachedToken;

        // 如果前面登录没有提取到token，这里再登录一次尝试拿
        String body = """
                {"username":"test","password":"123456"}
                """;

        MvcResult r = mockMvc.perform(
                        post(API_LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String resp = r.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String token = tryExtractToken(resp);

        Assertions.assertTrue(token != null && !token.isBlank(),
                "无法从登录响应中提取token。请确认：\n"
                        + "1) 你项目登录是否返回token字段；\n"
                        + "2) 若返回字段不是 token，请改 tryExtractToken() 的解析逻辑；\n"
                        + "3) 或者你项目token不在登录返回里而在响应头/别的字段。\n"
                        + "登录响应=" + resp);

        cachedToken = token;
        return token;
    }

    private static String buildTokenHeaderValue(String token) {
        if (AUTH_BEARER_MODE) return "Bearer " + token;
        return token;
    }

    /**
     * 尽量“通用”地从JSON里找 token 字段
     */
    private static String tryExtractToken(String resp) {
        if (resp == null || resp.isBlank()) return null;
        try {
            JsonNode root = OM.readTree(resp);
            JsonNode t = deepFind(root, "token");
            if (t != null && t.isTextual()) return t.asText();
            if (t != null && t.isNumber()) return t.asText();
            // 有的项目返回 data: "xxx" 这种（不推荐但存在）
            JsonNode data = root.get("data");
            if (data != null && data.isTextual() && data.asText().length() > 10) return data.asText();
        } catch (Exception ignore) {
            // 非JSON返回就不处理
        }
        // 最后兜底：包含 "token" 字样但解析不到，返回null让断言提示你改解析
        return null;
    }

    private static JsonNode deepFind(JsonNode node, String fieldName) {
        if (node == null) return null;
        if (node.isObject()) {
            if (node.has(fieldName)) return node.get(fieldName);
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                JsonNode found = deepFind(it.next().getValue(), fieldName);
                if (found != null) return found;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = deepFind(child, fieldName);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * 兼容你项目的“业务失败”表示方式：code!=0 / success=false / msg包含失败等
     */
    private static boolean isBusinessFailure(String resp) {
        if (resp == null) return true;
        String s = resp.toLowerCase(Locale.ROOT);
        if (s.contains("\"success\":false")) return true;
        if (s.contains("\"code\":") && !s.contains("\"code\":0")) return true;
        if (s.contains("失败") || s.contains("error") || s.contains("invalid")) return true;
        return false;
    }

    // ========================= 统一输出总结（你要的表格） =========================

    public static class SummaryExtension implements BeforeTestExecutionCallback, TestWatcher, AfterAllCallback {

        private static class Row {
            int idx;
            String method;     // 测试方法（Tag）
            String desc;       // 测试描述（DisplayName）
            String status;     // PASS/FAIL
            long costMs;       // 耗时
            String detail;     // 失败原因
        }

        private final Map<String, Long> startNs = new ConcurrentHashMap<>();
        private final List<Row> rows = new CopyOnWriteArrayList<>();
        private int counter = 0;

        @Override
        public void beforeTestExecution(ExtensionContext context) {
            startNs.put(context.getUniqueId(), System.nanoTime());
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            addRow(context, "PASS", null);
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            addRow(context, "FAIL", cause);
        }

        private void addRow(ExtensionContext ctx, String status, Throwable cause) {
            long end = System.nanoTime();
            Long st = startNs.getOrDefault(ctx.getUniqueId(), end);
            long ms = Duration.ofNanos(end - st).toMillis();

            Row r = new Row();
            r.idx = ++counter;

            // “测试方法”优先取第一个Tag；没有Tag就填“未分类”
            Set<String> tags = ctx.getTags();
            r.method = tags.stream().findFirst().orElse("未分类");

            r.desc = ctx.getDisplayName();
            r.status = status;
            r.costMs = ms;

            if (cause != null) {
                String msg = cause.getMessage();
                if (msg == null) msg = cause.toString();
                r.detail = msg.length() > 160 ? msg.substring(0, 160) + "..." : msg;
            } else {
                r.detail = "";
            }

            rows.add(r);
        }

        @Override
        public void afterAll(ExtensionContext context) {
            // 输出你要的汇总表
            System.out.println();
            System.out.println("======================================");
            System.out.println("BlackBox API Test Summary");
            System.out.println("序号 | 测试方法 | 测试描述 | 状态 | 耗时(ms) | 备注");
            System.out.println("--------------------------------------");

            int pass = 0;
            for (Row r : rows) {
                if ("PASS".equals(r.status)) pass++;
                System.out.printf(
                        Locale.ROOT,
                        "%-4d | %-6s | %-40s | %-4s | %-7d | %s%n",
                        r.idx,
                        safe(r.method, 6),
                        safe(r.desc, 40),
                        r.status,
                        r.costMs,
                        r.detail == null ? "" : r.detail
                );
            }

            System.out.println("--------------------------------------");
            System.out.printf(Locale.ROOT, "通过：%d / %d  (%.2f%%)%n",
                    pass, rows.size(), rows.isEmpty() ? 0.0 : (pass * 100.0 / rows.size()));
            System.out.println("======================================");
            System.out.println();
        }

        private static String safe(String s, int maxLen) {
            if (s == null) return "";
            String t = s.replace("\n", " ").replace("\r", " ").trim();
            if (t.length() <= maxLen) return t;
            return t.substring(0, Math.max(0, maxLen - 3)) + "...";
        }
    }
}
