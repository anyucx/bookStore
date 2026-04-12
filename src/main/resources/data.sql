INSERT INTO roles (id,name,code,description,created_time) VALUES
(1,'管理员','ADMIN','系统管理员',NOW()),
(2,'普通用户','CUSTOMER','商城用户',NOW());

INSERT INTO users (id,username,password_hash,display_name,phone,email,role_id,status,created_time,updated_time) VALUES
(1001,'admin','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','系统管理员','13800000000','admin@bookstore.local',1,1,NOW(),NOW()),
(1002,'demo','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','演示用户','13900000000','demo@bookstore.local',2,1,NOW(),NOW());

INSERT INTO categories (id,parent_id,name,sort,status,created_time,updated_time) VALUES
(2001,0,'文学',1,1,NOW(),NOW()),
(2002,2001,'小说',1,1,NOW(),NOW()),
(2003,0,'技术',2,1,NOW(),NOW()),
(2004,2003,'Java',1,1,NOW(),NOW());

INSERT INTO books (id,category_id,name,author,isbn,price,stock,cover_url,description,status,sales,created_time,updated_time) VALUES
(3001,2002,'活着','余华','9787506365437',39.90,'100','/book_img/book1.jpg','经典文学作品',1,12,NOW(),NOW()),
(3002,2004,'Spring Boot 实战示例','开源作者','9787111544937',69.00,'80','/book_img/book1.jpg','演示用技术图书',1,8,NOW(),NOW());
