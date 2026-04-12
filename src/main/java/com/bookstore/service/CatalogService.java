package com.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.BusinessException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogService {
    @Resource private CategoryMapper categoryMapper;
    @Resource private BookMapper bookMapper;

    /** 前台分类树。 */
    public List<Map<String, Object>> categoryTree() {
        return buildTree(categoryMapper.selectList(new QueryWrapper<Category>().eq("status", 1).orderByAsc("sort").orderByAsc("id")));
    }

    /** 管理端分类列表。 */
    public List<Category> adminCategories() {
        return categoryMapper.selectList(new QueryWrapper<Category>().orderByAsc("sort").orderByAsc("id"));
    }

    /** 保存分类。 */
    @Transactional(rollbackFor = Exception.class)
    public Category saveCategory(Map<String, Object> body) {
        Long id = AppUtils.lng(body, "id");
        Category c = id == null ? new Category() : categoryMapper.selectById(id);
        if (c == null) throw new BusinessException(404, "分类不存在");
        if (id == null) {
            c.id = AppUtils.nextId();
            c.createdTime = LocalDateTime.now();
        }
        Long parentId = AppUtils.lng(body, "parentId");
        if (parentId != null && parentId != 0 && categoryMapper.selectById(parentId) == null) throw new BusinessException(400, "父分类不存在");
        c.parentId = parentId == null ? 0L : parentId;
        c.name = AppUtils.str(body, "name");
        if (!StringUtils.hasText(c.name)) throw new BusinessException(400, "分类名称不能为空");
        c.sort = AppUtils.integer(body, "sort") == null ? 0 : AppUtils.integer(body, "sort");
        c.status = AppUtils.integer(body, "status") == null ? 1 : AppUtils.integer(body, "status");
        c.updatedTime = LocalDateTime.now();
        if (id == null) categoryMapper.insert(c); else categoryMapper.updateById(c);
        return c;
    }

    /** 删除分类。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        if (categoryMapper.selectCount(new QueryWrapper<Category>().eq("parent_id", id)) > 0) throw new BusinessException(400, "存在子分类");
        if (bookMapper.selectCount(new QueryWrapper<Book>().eq("category_id", id)) > 0) throw new BusinessException(400, "分类下存在图书");
        categoryMapper.deleteById(id);
    }

    /** 图书列表。 */
    public Map<String, Object> books(Long categoryId, String keyword, long pageNo, long pageSize, boolean admin) {
        QueryWrapper<Book> qw = new QueryWrapper<Book>().orderByDesc("created_time");
        if (categoryId != null) qw.eq("category_id", categoryId);
        if (!admin) qw.eq("status", 1);
        if (StringUtils.hasText(keyword)) qw.and(w -> w.like("name", keyword).or().like("author", keyword).or().like("isbn", keyword));
        Page<Book> page = bookMapper.selectPage(new Page<Book>(pageNo, pageSize), qw);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Book book : page.getRecords()) list.add(bookMap(book));
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("pageNo", page.getCurrent());
        res.put("pageSize", page.getSize());
        res.put("total", page.getTotal());
        res.put("records", list);
        return res;
    }

    /** 图书详情。 */
    public Map<String, Object> book(Long id, boolean admin) {
        Book b = bookEntity(id, admin);
        return bookMap(b);
    }

    /** 保存图书。 */
    @Transactional(rollbackFor = Exception.class)
    public Book saveBook(Map<String, Object> body) {
        Long id = AppUtils.lng(body, "id");
        Long categoryId = AppUtils.lng(body, "categoryId");
        if (categoryId == null || categoryMapper.selectById(categoryId) == null) throw new BusinessException(400, "分类不存在");
        Book b = id == null ? new Book() : bookMapper.selectById(id);
        if (b == null) throw new BusinessException(404, "图书不存在");
        if (id == null) {
            b.id = AppUtils.nextId();
            b.createdTime = LocalDateTime.now();
            b.sales = 0;
        }
        b.categoryId = categoryId;
        b.name = AppUtils.str(body, "name");
        if (!StringUtils.hasText(b.name)) throw new BusinessException(400, "图书名称不能为空");
        b.author = AppUtils.str(body, "author");
        b.isbn = AppUtils.str(body, "isbn");
        b.price = AppUtils.decimal(body, "price");
        b.stock = AppUtils.integer(body, "stock");
        if (b.price == null || b.stock == null) throw new BusinessException(400, "价格和库存不能为空");
        b.coverUrl = AppUtils.str(body, "coverUrl");
        b.description = AppUtils.str(body, "description");
        b.status = AppUtils.integer(body, "status") == null ? 1 : AppUtils.integer(body, "status");
        b.updatedTime = LocalDateTime.now();
        if (id == null) bookMapper.insert(b); else bookMapper.updateById(b);
        return b;
    }

    /** 删除图书。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        bookMapper.deleteById(id);
    }

    public Book bookEntity(Long id, boolean admin) {
        Book b = bookMapper.selectById(id);
        if (b == null) throw new BusinessException(404, "图书不存在");
        if (!admin && (b.status == null || b.status != 1)) throw new BusinessException(404, "图书已下架");
        return b;
    }

    private List<Map<String, Object>> buildTree(List<Category> list) {
        Map<Long, Map<String, Object>> nodes = new LinkedHashMap<Long, Map<String, Object>>();
        List<Map<String, Object>> roots = new ArrayList<Map<String, Object>>();
        for (Category c : list) {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("id", c.id);
            m.put("parentId", c.parentId);
            m.put("name", c.name);
            m.put("sort", c.sort);
            m.put("status", c.status);
            m.put("children", new ArrayList<Map<String, Object>>());
            nodes.put(c.id, m);
        }
        for (Category c : list) {
            Map<String, Object> cur = nodes.get(c.id);
            if (c.parentId == null || c.parentId == 0 || !nodes.containsKey(c.parentId)) {
                roots.add(cur);
            } else {
                ((List<Map<String, Object>>) nodes.get(c.parentId).get("children")).add(cur);
            }
        }
        return roots;
    }

    private Map<String, Object> bookMap(Book b) {
        Category c = categoryMapper.selectById(b.categoryId);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("id", b.id); m.put("name", b.name); m.put("author", b.author); m.put("isbn", b.isbn);
        m.put("price", b.price); m.put("stock", b.stock); m.put("coverUrl", b.coverUrl); m.put("description", b.description);
        m.put("status", b.status); m.put("sales", b.sales); m.put("createdTime", b.createdTime);
        m.put("categoryId", b.categoryId);
        if (c != null) {
            Map<String, Object> cm = new HashMap<String, Object>();
            cm.put("id", c.id); cm.put("name", c.name);
            m.put("category", cm);
        }
        return m;
    }
}
