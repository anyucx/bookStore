package com.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookstore.mapper.*;
import com.bookstore.model.entity.*;
import com.bookstore.security.SecuritySupport;
import com.bookstore.storage.FileStorageService;
import com.bookstore.storage.StoredFile;
import com.bookstore.util.AppUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    @Resource private UserMapper userMapper;
    @Resource private CategoryMapper categoryMapper;
    @Resource private BookMapper bookMapper;
    @Resource private BookOrderMapper orderMapper;
    @Resource private PaymentRecordMapper paymentRecordMapper;
    @Resource private FileResourceMapper fileResourceMapper;
    @Resource private FileStorageService fileStorageService;

    public Map<String, Object> dashboard() {
        Map<String, Object> m = new HashMap<>();
        Object[] results = new Object[]{
                userMapper.selectCount(new QueryWrapper<User>()),
                categoryMapper.selectCount(new QueryWrapper<Category>()),
                bookMapper.selectCount(new QueryWrapper<Book>()),
                orderMapper.selectCount(new QueryWrapper<BookOrder>()),
                paymentRecordMapper.selectCount(new QueryWrapper<PaymentRecord>())
        };
        m.put("userCount", results[0]);
        m.put("categoryCount", results[1]);
        m.put("bookCount", results[2]);
        m.put("orderCount", results[3]);
        m.put("paymentCount", results[4]);

        List<BookOrder> orders = orderMapper.selectList(
                new QueryWrapper<BookOrder>().select("IFNULL(SUM(total_amount),0) as total_amount"));
        BigDecimal total = BigDecimal.ZERO;
        for (BookOrder o : orders) {
            if (o.getTotalAmount() != null) total = total.add(o.getTotalAmount());
        }
        m.put("totalSales", total);
        return m;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        StoredFile sf = fileStorageService.store(file);
        FileResource fr = new FileResource();
        fr.setId(AppUtils.nextId());
        fr.setBusinessType("COMMON");
        fr.setOriginalName(sf.getOriginalName());
        fr.setStoredName(sf.getStoredName());
        fr.setStoragePath(sf.getRelativePath());
        fr.setAccessUrl(sf.getAccessUrl());
        fr.setContentType(sf.getContentType());
        fr.setSizeBytes(sf.getSize());
        fr.setUploaderId(SecuritySupport.current().getUserId());
        fr.setCreatedTime(LocalDateTime.now());
        fileResourceMapper.insert(fr);
        Map<String, Object> m = new HashMap<>();
        m.put("id", fr.getId());
        m.put("originalName", fr.getOriginalName());
        m.put("accessUrl", fr.getAccessUrl());
        m.put("sizeBytes", fr.getSizeBytes());
        return m;
    }
}
