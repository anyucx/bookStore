package com.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.BookOrderMapper;
import com.bookstore.mapper.CategoryMapper;
import com.bookstore.mapper.FileResourceMapper;
import com.bookstore.mapper.PaymentRecordMapper;
import com.bookstore.mapper.UserMapper;
import com.bookstore.model.entity.BookOrder;
import com.bookstore.model.entity.FileResource;
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
    @Resource private AuthService authService;

    /** 管理端看板。 */
    public Map<String, Object> dashboard() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("userCount", userMapper.selectCount(new QueryWrapper<com.bookstore.model.entity.User>()));
        m.put("categoryCount", categoryMapper.selectCount(new QueryWrapper<com.bookstore.model.entity.Category>()));
        m.put("bookCount", bookMapper.selectCount(new QueryWrapper<com.bookstore.model.entity.Book>()));
        m.put("orderCount", orderMapper.selectCount(new QueryWrapper<BookOrder>()));
        m.put("paymentCount", paymentRecordMapper.selectCount(new QueryWrapper<com.bookstore.model.entity.PaymentRecord>()));
        List<BookOrder> orders = orderMapper.selectList(new QueryWrapper<BookOrder>());
        BigDecimal total = BigDecimal.ZERO;
        for (BookOrder o : orders) if (o.totalAmount != null) total = total.add(o.totalAmount);
        m.put("totalSales", total);
        return m;
    }

    /** 上传文件。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        StoredFile sf = fileStorageService.store(file);
        FileResource fr = new FileResource();
        fr.id = AppUtils.nextId();
        fr.businessType = "COMMON";
        fr.originalName = sf.originalName;
        fr.storedName = sf.storedName;
        fr.storagePath = sf.relativePath;
        fr.accessUrl = sf.accessUrl;
        fr.contentType = sf.contentType;
        fr.sizeBytes = sf.size;
        fr.uploaderId = authService.currentUser().id;
        fr.createdTime = LocalDateTime.now();
        fileResourceMapper.insert(fr);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("id", fr.id); m.put("originalName", fr.originalName); m.put("accessUrl", fr.accessUrl); m.put("sizeBytes", fr.sizeBytes);
        return m;
    }
}
