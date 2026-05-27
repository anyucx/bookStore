package com.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.BusinessException;
import com.bookstore.dto.request.BookSaveRequest;
import com.bookstore.dto.request.CategorySaveRequest;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.CategoryMapper;
import com.bookstore.model.entity.Book;
import com.bookstore.model.entity.Category;
import com.bookstore.util.AppUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CatalogService {
    @Resource private CategoryMapper categoryMapper;
    @Resource private BookMapper bookMapper;

    public List<Map<String, Object>> categoryTree() {
        return buildTree(categoryMapper.selectList(new QueryWrapper<Category>().eq("status", 1).orderByAsc("sort").orderByAsc("id")));
    }

    public List<Category> adminCategories() {
        return categoryMapper.selectList(new QueryWrapper<Category>().orderByAsc("sort").orderByAsc("id"));
    }

    @Transactional(rollbackFor = Exception.class)
    public Category saveCategory(CategorySaveRequest req) {
        Long id = req.getId();
        Category c = id == null ? new Category() : categoryMapper.selectById(id);
        if (c == null) throw new BusinessException(404, "分类不存在");
        if (id == null) {
            c.setId(AppUtils.nextId());
            c.setCreatedTime(LocalDateTime.now());
        }
        if (req.getParentId() != null && req.getParentId() != 0 && categoryMapper.selectById(req.getParentId()) == null) throw new BusinessException(400, "父分类不存在");
        c.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        c.setName(req.getName());
        c.setSort(req.getSort() == null ? 0 : req.getSort());
        c.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        c.setUpdatedTime(LocalDateTime.now());
        if (id == null) categoryMapper.insert(c); else categoryMapper.updateById(c);
        return c;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        if (categoryMapper.selectCount(new QueryWrapper<Category>().eq("parent_id", id)) > 0) throw new BusinessException(400, "存在子分类");
        if (bookMapper.selectCount(new QueryWrapper<Book>().eq("category_id", id)) > 0) throw new BusinessException(400, "分类下存在图书");
        categoryMapper.deleteById(id);
    }

    public Map<String, Object> books(Long categoryId, String keyword, long pageNo, long pageSize, boolean admin) {
        QueryWrapper<Book> qw = new QueryWrapper<Book>().orderByDesc("created_time");
        if (categoryId != null) qw.eq("category_id", categoryId);
        if (!admin) qw.eq("status", 1);
        if (StringUtils.hasText(keyword)) qw.and(w -> w.like("name", keyword).or().like("author", keyword).or().like("isbn", keyword));
        Page<Book> page = bookMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Book book : page.getRecords()) list.add(bookMap(book));
        Map<String, Object> res = new HashMap<>();
        res.put("pageNo", page.getCurrent());
        res.put("pageSize", page.getSize());
        res.put("total", page.getTotal());
        res.put("records", list);
        return res;
    }

    public Map<String, Object> book(Long id, boolean admin) {
        return bookMap(bookEntity(id, admin));
    }

    @Transactional(rollbackFor = Exception.class)
    public Book saveBook(BookSaveRequest req) {
        Long id = req.getId();
        if (req.getCategoryId() == null || categoryMapper.selectById(req.getCategoryId()) == null) throw new BusinessException(400, "分类不存在");
        Book b = id == null ? new Book() : bookMapper.selectById(id);
        if (b == null) throw new BusinessException(404, "图书不存在");
        if (id == null) {
            b.setId(AppUtils.nextId());
            b.setCreatedTime(LocalDateTime.now());
            b.setSales(0);
        }
        b.setCategoryId(req.getCategoryId());
        b.setName(req.getName());
        b.setAuthor(req.getAuthor());
        b.setIsbn(req.getIsbn());
        b.setPrice(req.getPrice());
        b.setStock(req.getStock());
        b.setCoverUrl(req.getCoverUrl());
        b.setDescription(req.getDescription());
        b.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        b.setUpdatedTime(LocalDateTime.now());
        if (id == null) bookMapper.insert(b); else bookMapper.updateById(b);
        return b;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        bookMapper.deleteById(id);
    }

    public Book bookEntity(Long id, boolean admin) {
        Book b = bookMapper.selectById(id);
        if (b == null) throw new BusinessException(404, "图书不存在");
        if (!admin && (b.getStatus() == null || b.getStatus() != 1)) throw new BusinessException(404, "图书已下架");
        return b;
    }

    private List<Map<String, Object>> buildTree(List<Category> list) {
        Map<Long, Map<String, Object>> nodes = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Category c : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("parentId", c.getParentId());
            m.put("name", c.getName());
            m.put("sort", c.getSort());
            m.put("status", c.getStatus());
            m.put("children", new ArrayList<>());
            nodes.put(c.getId(), m);
        }
        for (Category c : list) {
            Map<String, Object> cur = nodes.get(c.getId());
            if (c.getParentId() == null || c.getParentId() == 0 || !nodes.containsKey(c.getParentId())) {
                roots.add(cur);
            } else {
                ((List<Map<String, Object>>) nodes.get(c.getParentId()).get("children")).add(cur);
            }
        }
        return roots;
    }

    private Map<String, Object> bookMap(Book b) {
        Category c = categoryMapper.selectById(b.getCategoryId());
        Map<String, Object> m = new HashMap<>();
        m.put("id", b.getId());
        m.put("name", b.getName());
        m.put("author", b.getAuthor());
        m.put("isbn", b.getIsbn());
        m.put("price", b.getPrice());
        m.put("stock", b.getStock());
        m.put("coverUrl", b.getCoverUrl());
        m.put("description", b.getDescription());
        m.put("status", b.getStatus());
        m.put("sales", b.getSales());
        m.put("createdTime", b.getCreatedTime());
        m.put("categoryId", b.getCategoryId());
        if (c != null) {
            Map<String, Object> cm = new HashMap<>();
            cm.put("id", c.getId());
            cm.put("name", c.getName());
            m.put("category", cm);
        }
        return m;
    }
}
