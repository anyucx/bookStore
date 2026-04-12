package com.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookstore.common.BusinessException;
import com.bookstore.mapper.RoleMapper;
import com.bookstore.mapper.UserMapper;
import com.bookstore.model.entity.Role;
import com.bookstore.model.entity.User;
import com.bookstore.security.SecuritySupport;
import com.bookstore.util.AppUtils;
import com.bookstore.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Resource private UserMapper userMapper;
    @Resource private RoleMapper roleMapper;
    @Resource private RedisCacheUtil redisCacheUtil;
    @Value("${bookstore.auth.token-prefix:bookstore:token:}") private String tokenPrefix;
    @Value("${bookstore.auth.token-ttl-days:7}") private long ttlDays;

    /** 用户注册。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(Map<String, Object> body) {
        String username = AppUtils.str(body, "username");
        String password = AppUtils.str(body, "password");
        String confirmPassword = AppUtils.str(body, "confirmPassword");
        String displayName = AppUtils.str(body, "displayName");
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(displayName)) {
            throw new BusinessException(400, "用户名、密码、昵称不能为空");
        }
        if (!password.equals(confirmPassword)) throw new BusinessException(400, "两次密码不一致");
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", username).last("limit 1")) != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().eq("code", "CUSTOMER").last("limit 1"));
        if (role == null) throw new BusinessException(500, "缺少 CUSTOMER 角色");
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.id = AppUtils.nextId();
        user.username = username;
        user.passwordHash = AppUtils.sha256(password);
        user.displayName = displayName;
        user.phone = AppUtils.str(body, "phone");
        user.email = AppUtils.str(body, "email");
        user.roleId = role.id;
        user.status = 1;
        user.createdTime = now;
        user.updatedTime = now;
        userMapper.insert(user);
        return loginResult(user, role);
    }

    /** 用户或管理员登录。 */
    public Map<String, Object> login(Map<String, Object> body, boolean adminLogin) {
        String username = AppUtils.str(body, "username");
        String password = AppUtils.str(body, "password");
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username).last("limit 1"));
        if (user == null || !AppUtils.passwordMatch(password, user.passwordHash)) throw new BusinessException(400, "用户名或密码错误");
        if (user.status == null || user.status != 1) throw new BusinessException(403, "账号已禁用");
        Role role = role(user.roleId);
        if (adminLogin && !"ADMIN".equalsIgnoreCase(role.code)) throw new BusinessException(403, "当前账号不是管理员");
        return loginResult(user, role);
    }

    /** 退出登录。 */
    public void logout(String token) {
        if (StringUtils.hasText(token)) redisCacheUtil.deleteObject(tokenPrefix + token);
    }

    /** 当前登录用户信息。 */
    public Map<String, Object> me() {
        User user = currentUser();
        return userMap(user, role(user.roleId));
    }

    /** 当前登录用户实体。 */
    public User currentUser() {
        SecuritySupport.LoginUser login = SecuritySupport.current();
        User user = userMapper.selectById(login.userId);
        if (user == null) throw new BusinessException(401, "登录用户不存在");
        return user;
    }

    /** 管理端用户列表。 */
    public List<Map<String, Object>> users(String keyword, Integer status) {
        QueryWrapper<User> qw = new QueryWrapper<User>().orderByDesc("created_time");
        if (status != null) qw.eq("status", status);
        if (StringUtils.hasText(keyword)) qw.and(w -> w.like("username", keyword).or().like("display_name", keyword));
        List<User> users = userMapper.selectList(qw);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (User user : users) list.add(userMap(user, role(user.roleId)));
        return list;
    }

    /** 管理端更新用户。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateUser(Map<String, Object> body) {
        Long userId = AppUtils.lng(body, "userId");
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        String displayName = AppUtils.str(body, "displayName");
        Integer status = AppUtils.integer(body, "status");
        Long roleId = AppUtils.lng(body, "roleId");
        if (StringUtils.hasText(displayName)) user.displayName = displayName;
        if (status != null) user.status = status;
        if (roleId != null) user.roleId = role(roleId).id;
        user.updatedTime = LocalDateTime.now();
        userMapper.updateById(user);
        return userMap(user, role(user.roleId));
    }

    private Role role(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) throw new BusinessException(500, "角色不存在");
        return role;
    }

    private Map<String, Object> loginResult(User user, Role role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        SecuritySupport.LoginUser loginUser = new SecuritySupport.LoginUser();
        loginUser.userId = user.id;
        loginUser.username = user.username;
        loginUser.displayName = user.displayName;
        loginUser.roleCode = role.code;
        redisCacheUtil.setCacheObject(tokenPrefix + token, loginUser, ttlDays, TimeUnit.DAYS);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("token", token);
        map.put("tokenType", "Bearer");
        map.put("expiresInDays", ttlDays);
        map.put("user", userMap(user, role));
        return map;
    }

    private Map<String, Object> userMap(User user, Role role) {
        Map<String, Object> r = new HashMap<String, Object>();
        r.put("id", user.id);
        r.put("username", user.username);
        r.put("displayName", user.displayName);
        r.put("phone", user.phone);
        r.put("email", user.email);
        r.put("status", user.status);
        Map<String, Object> roleMap = new HashMap<String, Object>();
        roleMap.put("id", role.id);
        roleMap.put("name", role.name);
        roleMap.put("code", role.code);
        r.put("role", roleMap);
        r.put("createdTime", user.createdTime);
        return r;
    }
}
