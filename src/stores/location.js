import { defineStore } from "pinia";
import { ref } from "vue";

const LOCATION_KEY = "user_location";

export const useLocationStore = defineStore("location", () => {
    // 默认位置：云南大学
    const address = ref("云南大学呈贡校区");
    const city = ref("昆明市");
    const latitude = ref(24.8256);
    const longitude = ref(102.8517);

    // 初始化时从本地缓存读取
    function loadFromStorage() {
        const cached = localStorage.getItem(LOCATION_KEY);
        if (cached) {
            const data = JSON.parse(cached);
            address.value = data.address;
            city.value = data.city;
            latitude.value = data.lat;
            longitude.value = data.lng;
        }
    }

    // 更新位置
    function setLocation(addr, c, lat, lng) {
        address.value = addr;
        city.value = c;
        latitude.value = lat;
        longitude.value = lng;

        // 持久化存储
        localStorage.setItem(LOCATION_KEY, JSON.stringify({
            address: addr,
            city: c,
            lat: lat,
            lng: lng
        }));
    }

    loadFromStorage();

    return { address, city, latitude, longitude, setLocation };
});