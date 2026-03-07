import { http } from "./http";

export function listAddresses() {
    return http.get("/address/list");
}
export function addAddress(data) {
    return http.post("/address/add", data);
}
export function deleteAddress(id) {
    return http.delete(`/address/${id}`);
}
export function setDefaultAddress(id) {
    return http.post(`/address/${id}/default`);
}