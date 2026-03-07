import { defineStore } from "pinia";
import { ref, watch } from "vue";

const ORDER_KEY = "orders";

export const useOrdersStore = defineStore("orders", () => {
    const orders = ref([]);

    function load() {
        try {
            const raw = localStorage.getItem(ORDER_KEY);
            if (!raw) return;
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed)) orders.value = parsed;
        } catch (e) {
            console.warn("读取订单失败：", e);
        }
    }

    function save() {
        localStorage.setItem(ORDER_KEY, JSON.stringify(orders.value));
    }

    function addOrder(order) {
        orders.value.unshift(order);
    }

    function markPaid(orderId) {
        const o = orders.value.find((x) => x.id === orderId);
        if (o) o.status = "paid";
    }

    load();
    watch(orders, save, { deep: true });

    return { orders, addOrder, markPaid };
});
