package com.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.model.entity.BookOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookOrderMapper extends BaseMapper<BookOrder> {
}
