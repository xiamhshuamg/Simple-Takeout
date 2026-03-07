import { defineStore } from "pinia";
import { ref, computed, watch } from "vue";
import { createOrder, payOrder } from "@/api/order";

const CART_KEY = "cart";

export const useCartStore = defineStore("cart", () => {
    // 状态
    const business = ref(null);       // 当前商家信息
    const items = ref([]);            // 购物车商品列表
    const deliveryFee = ref(3);       // 配送费
    // 满减规则列表
    const promotions = ref([]);
    // 计算属性：商品总价（未优惠）
    const foodsTotal = computed(() =>
        items.value.reduce((sum, it) => sum + it.price * it.quantity, 0)
    );
    // 计算当前满足的满减金额
    const discountAmount = computed(() => {
        if (!promotions.value || promotions.value.length === 0) return 0;
        const total = foodsTotal.value;
        let maxReduce = 0;
        // 遍历所有规则，找到当前总价能满足的最大减免额
        for (const p of promotions.value) {
            if (total >= p.limit) {
                // 只有当减免金额大于当前已记录的减免金额时才替换
                if (p.reduce > maxReduce) {
                    maxReduce = p.reduce;
                }
            }
        }
        return maxReduce;
    });

    // 最终总价 = 商品总价 - 优惠金额 + 配送费
    const totalPrice = computed(() => {
        const final = foodsTotal.value - discountAmount.value + deliveryFee.value;
        return Math.max(0, final); // 防止负数
    });

    // 方法：设置商家
    function setBusiness(biz) {
        business.value = biz;
        if (biz?.deliveryFee !== undefined) {
            deliveryFee.value = biz.deliveryFee;
        }

        // 解析满减规则
        // 假设后端返回的数据里有一个 promotions 字段，或者我们将规则存放在 desc 里解析
        // 这里模拟：如果 biz.promotions 是字符串 JSON，则解析；如果是数组直接用
        promotions.value = [];
        if (biz?.promotions) {
            try {
                // 如果是 JSON 字符串则解析，如果是对象则直接赋值
                promotions.value = typeof biz.promotions === 'string'
                    ? JSON.parse(biz.promotions)
                    : biz.promotions;
            } catch (e) {
                console.warn("满减规则解析失败", e);
            }
        }
    }

    // 方法：设置菜品列表
    function setFoods(foods) {
        items.value = foods.map((f) => ({
            ...f,
            quantity: f.quantity || 0,
        }));
    }

    // 方法：修改数量
    function setQuantity(foodId, qty) {
        const item = items.value.find((it) => it.id === foodId);
        if (item) {
            item.quantity = Math.max(0, qty);
        }
    }

    // 方法：清空购物车
    function clearCart() {
        business.value = null;
        items.value = [];
        promotions.value = [];
        localStorage.removeItem(CART_KEY);
    }

    // 方法：保存到本地
    function saveToLocalStorage() {
        const data = {
            business: business.value,
            items: items.value,
            deliveryFee: deliveryFee.value,
            promotions: promotions.value // 保存规则
        };
        localStorage.setItem(CART_KEY, JSON.stringify(data));
    }

    // 方法：加载
    function loadFromLocalStorage() {
        try {
            const raw = localStorage.getItem(CART_KEY);
            if (!raw) return;
            const data = JSON.parse(raw);
            if (data.business) business.value = data.business;
            if (Array.isArray(data.items)) items.value = data.items;
            if (data.deliveryFee !== undefined) deliveryFee.value = data.deliveryFee;
            if (data.promotions) promotions.value = data.promotions;
        } catch (e) {
            console.warn("读取购物车失败：", e);
        }
    }

    // 方法：提交订单
    async function submitOrder(payMethod = "alipay", addressId) {
        const orderItems = items.value.filter((it) => it.quantity > 0);
        if (orderItems.length === 0) throw new Error("购物车为空");
        if (!business.value) throw new Error("未选择商家");

        const orderData = {
            businessId: business.value.id,
            addressId: addressId,
            deliveryFee: deliveryFee.value,
            // 这里我们把前端计算的最终价格传过去或者仅仅传商品
            items: orderItems.map((it) => ({
                foodId: it.id,
                quantity: it.quantity,
            })),
            // 可以在这里备注满减信息
            remarks: discountAmount.value > 0 ? `满减优惠：-￥${discountAmount.value}` : ''
        };

        try {
            const res = await createOrder(orderData);
            if (payMethod) {
                const payRes = await payOrder({
                    orderId: res.data.orderId,
                    payMethod: payMethod,
                });
                return { ...res.data, ...payRes.data };
            }
            return res.data;
        } catch (error) {
            console.error("下单失败:", error);
            throw error;
        }
    }

    watch([business, items], saveToLocalStorage, { deep: true });
    loadFromLocalStorage();

    return {
        business,
        items,
        deliveryFee,
        promotions,      // 导出规则
        foodsTotal,      // 原价总和
        discountAmount,  // 优惠金额
        totalPrice,      // 最终支付价格
        setBusiness,
        setFoods,
        setQuantity,
        clearCart,
        submitOrder,
    };
});