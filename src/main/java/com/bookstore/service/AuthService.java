package com.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookstore.common.BusinessException;
import com.bookstore.dto.request.LoginRequest;
import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.dto.request.UserUpdateRequest;
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

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) throw new BusinessException(400, "两次密码不一致");
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", req.getUsername()).last("limit 1")) != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().eq("code", "CUSTOMER").last("limit 1"));
        if (role == null) throw new BusinessException(500, "缺少 CUSTOMER 角色");
        LocalDateTime now = LocalDateTime.now();
        User user = new User()
                .setId(AppUtils.nextId())
                .setUsername(req.getUsername())
                .setPasswordHash(AppUtils.sha256(req.getPassword()))
                .setDisplayName(req.getDisplayName())
                .setPhone(req.getPhone())
                .setEmail(req.getEmail())
                .setRoleId(role.getId())
                .setStatus(1)
                .setCreatedTime(now)
                .setUpdatedTime(now);
        userMapper.insert(user);
        return loginResult(user, role);
    }

    public Map<String, Object> login(LoginRequest req, boolean adminLogin) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", req.getUsername()).last("limit 1"));
        if (user == null || !AppUtils.passwordMatch(req.getPassword(), user.getPasswordHash())) throw new BusinessException(400, "用户名或密码错误");
        if (user.getStatus() == null || user.getStatus() != 1) throw new BusinessException(403, "账号已禁用");
        Role role = role(user.getRoleId());
        if (adminLogin && !"ADMIN".equalsIgnoreCase(role.getCode())) throw new BusinessException(403, "当前账号不是管理员");
        return loginResult(user, role);
    }

    public void logout(String token) {
        if (StringUtils.hasText(token)) redisCacheUtil.deleteObject(tokenPrefix + token);
    }

    public Map<String, Object> me() {
        User user = currentUser();
        return userMap(user, role(user.getRoleId()));
    }

    public User currentUser() {
        SecuritySupport.LoginUser login = SecuritySupport.current();
        User user = userMapper.selectById(login.getUserId());
        if (user == null) throw new BusinessException(401, "登录用户不存在");
        return user;
    }

    public List<Map<String, Object>> users(String keyword, Integer status) {
        QueryWrapper<User> qw = new QueryWrapper<User>().orderByDesc("created_time");
        if (status != null) qw.eq("status", status);
        if (StringUtils.hasText(keyword)) qw.and(w -> w.like("username", keyword).or().like("display_name", keyword));
        List<User> users = userMapper.selectList(qw);
        List<Map<String, Object>> list = new ArrayList<>();
        for (User user : users) list.add(userMap(user, role(user.getRoleId())));
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateUser(UserUpdateRequest req) {
        User user = userMapper.selectById(req.getUserId());
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (req.getDisplayName() != null) user.setDisplayName(req.getDisplayName());
        if (req.getStatus() != null) user.setStatus(req.getStatus());
        if (req.getRoleId() != null) user.setRoleId(role(req.getRoleId()).getId());
        user.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(user);
        return userMap(user, role(user.getRoleId()));
    }

    private Role role(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) throw new BusinessException(500, "角色不存在");
        return role;
    }

    private Map<String, Object> loginResult(User user, Role role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        SecuritySupport.LoginUser loginUser = new SecuritySupport.LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setDisplayName(user.getDisplayName());
        loginUser.setRoleCode(role.getCode());
        redisCacheUtil.setCacheObject(tokenPrefix + token, loginUser, ttlDays, TimeUnit.DAYS);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("tokenType", "Bearer");
        map.put("expiresInDays", ttlDays);
        map.put("user", userMap(user, role));
        return map;
    }

    private Map<String, Object> userMap(User user, Role role) {
        Map<String, Object> r = new HashMap<>();
        r.put("id", user.getId());
        r.put("username", user.getUsername());
        r.put("displayName", user.getDisplayName());
        r.put("phone", user.getPhone());
        r.put("email", user.getEmail());
        r.put("status", user.getStatus());
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("id", role.getId());
        roleMap.put("name", role.getName());
        roleMap.put("code", role.getCode());
        r.put("role", roleMap);
        r.put("createdTime", user.getCreatedTime());
        return r;
    }
}
