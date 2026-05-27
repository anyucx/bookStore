package com.bookstore.security;

import com.bookstore.common.BusinessException;
import lombok.Data;

public final class SecuritySupport {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private SecuritySupport() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser current() {
        LoginUser user = HOLDER.get();
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(401, "未登录");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Data
    public static class LoginUser {
        private Long userId;
        private String username;
        private String displayName;
        private String roleCode;
    }
}
