package com.bookstore.security;

import com.bookstore.common.BusinessException;

public final class SecuritySupport {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<LoginUser>();

    private SecuritySupport() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser current() {
        LoginUser user = HOLDER.get();
        if (user == null || user.userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static class LoginUser {
        public Long userId;
        public String username;
        public String displayName;
        public String roleCode;
    }
}
