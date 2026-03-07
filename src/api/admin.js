import { http } from "./http";

// 列出所有用户/商家
export function listCustomers() {
    return http.get("/admin/customers");
}

export function listBusinesses() {
    return http.get("/admin/businesses");
}

// 删除用户/商家
export function deleteCustomer(id) {
    return http.delete(`/admin/customers/${id}`);
}

export function deleteBusiness(id) {
    return http.delete(`/admin/businesses/${id}`);
}
