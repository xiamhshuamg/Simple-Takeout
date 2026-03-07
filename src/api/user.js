import { http } from "./http";

export function login(data) {
    return http.post("/user/login", data);
}

export function register(data) {
    return http.post("/user/register", data);
}

export function getCurrentUser() {
    return http.get("/user/info");
}

export function logout() {
    return http.post("/user/logout");
}

export function updateUserInfo(data) {
    return http.post("/user/update", data);
}

export function uploadAvatar(formData) {
    return http.post("/upload/avatar", formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

export function uploadBusinessImage(formData) {
    return http.post("/upload/common", formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}