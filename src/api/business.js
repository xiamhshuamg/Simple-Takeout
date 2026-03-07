import { http } from "./http";

export function fetchBusinesses(params = {}) {
    // 获取商家列表
    return http.get("/business/list", { params });
}

export function fetchFoods(businessId) {
    // 获取商家菜单
    return http.get(`/business/${businessId}/foods`);
}

// 获取单个商家的详细信息
export function fetchBusinessInfo(businessId) {
    return http.get(`/business/${businessId}`);
}

export function searchBusinesses(keyword) {
    return http.get("/business/list", { params: { keyword } });
}

// 商家“我的信息”
export function getMyInfo() {
    return http.get("/business/my-info");
}

// 商家统计
export function getStats() {
    return http.get("/business/stats");
}

// 开关店铺
export function updateStatus(data) {
    return http.post("/business/update-status", data);
}

// 通用上传（头像/图片）
export function uploadCommon(formData) {
    return http.post("/upload/common", formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
}
