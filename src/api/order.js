import { http } from "./http";

export function createOrder(data) {
    return http.post("/order/create", data);
}

export function payOrder(data) {
    return http.post("/order/pay", data);
}

export function fetchOrders() {
    return http.get("/order/list");
}

//获取订单详情
export function getOrderDetail(orderId) {
    return http.get(`/order/${orderId}`);
}

//取消订单
export function cancelOrder(orderId) {
    return http.post(`/order/${orderId}/cancel`);
}