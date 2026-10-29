-- =============================================================
-- 图书借阅平台数据库脚本（MySQL 8.0）
-- 使用方式：mysql -uroot -p < sql/library_system.sql
-- 说明：脚本会重建 4 张业务表与 1 张操作日志表，并写入演示数据
-- =============================================================

CREATE DATABASE IF NOT EXISTS `library_system`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `library_system`;

DROP TABLE IF EXISTS `borrow_record`;
DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `admin`;

-- 管理员表
CREATE TABLE `admin` (
    `id`                   int         NOT NULL AUTO_INCREMENT COMMENT '管理员id',
    `name`                 varchar(20) NOT NULL COMMENT '管理员姓名',
    `password`             varchar(20) NOT NULL COMMENT '管理员密码',
    `password_error_count` int         DEFAULT 0 COMMENT '密码错误次数',
    `is_locked`            int         DEFAULT 0 COMMENT '是否锁定：0-正常，1-锁定',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='管理员表';

-- 用户表
CREATE TABLE `user` (
    `id`                   int         NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`             varchar(20) NOT NULL COMMENT '登录账号',
    `password`             varchar(50) NOT NULL COMMENT '登录密码',
    `phone`                varchar(11) DEFAULT NULL COMMENT '手机号',
    `is_locked`            tinyint     DEFAULT 0 COMMENT '0-正常，1-锁定',
    `password_error_count` int         DEFAULT 0 COMMENT '密码错误次数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户表';

-- 图书表
CREATE TABLE `book` (
    `id`     int         NOT NULL AUTO_INCREMENT COMMENT '图书ID',
    `name`   varchar(50) NOT NULL COMMENT '书名',
    `author` varchar(20) DEFAULT NULL COMMENT '作者',
    `stock`  int         DEFAULT 0 COMMENT '库存数量',
    PRIMARY KEY (`id`),
    KEY `idx_book_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='图书表';

-- 借阅记录表
CREATE TABLE `borrow_record` (
    `id`          int      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`     int      NOT NULL COMMENT '借阅人ID',
    `book_id`     int      NOT NULL COMMENT '图书ID',
    `borrow_time` datetime NOT NULL COMMENT '借书时间',
    `is_return`   tinyint  NOT NULL DEFAULT 0 COMMENT '0-未归还 1-已归还',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_user_return` (`user_id`, `is_return`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='借阅记录表';

-- 操作日志表（AOP + 异步线程池写入）
CREATE TABLE `operation_log` (
    `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operator`   varchar(64)  NOT NULL COMMENT '操作人',
    `module`     varchar(32)  NOT NULL COMMENT '业务模块',
    `action`     varchar(64)  NOT NULL COMMENT '操作动作',
    `detail`     varchar(512) DEFAULT NULL COMMENT '请求参数/异常摘要',
    `success`    tinyint      NOT NULL DEFAULT 1 COMMENT '1-成功 0-失败',
    `cost_ms`    bigint       DEFAULT 0 COMMENT '耗时（毫秒）',
    `created_at` datetime     NOT NULL COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='操作日志表';

-- 演示数据：管理员 admin / 123456
INSERT INTO `admin` (`name`, `password`)
VALUES ('admin', '123456');

-- 演示数据：读者账号密码均为 123456
INSERT INTO `user` (`username`, `password`, `phone`)
VALUES ('alice', '123456', '13800000001'),
       ('bob', '123456', '13800000002'),
       ('carol', '123456', '13800000003');

-- 演示数据：图书
INSERT INTO `book` (`name`, `author`, `stock`)
VALUES ('百年孤独', '马尔克斯', 200),
       ('小王子', '圣埃克苏佩里', 100),
       ('活着', '余华', 291),
       ('三体', '刘慈欣', 101),
       ('月亮与六便士', '毛姆', 100),
       ('Java编程思想', 'Bruce Eckel', 122),
       ('红楼梦', '曹雪芹', 130),
       ('撒哈拉的故事', '三毛', 129),
       ('盗墓笔记', '南派三叔', 28),
       ('人类简史', '尤瓦尔·赫拉利', 60);
