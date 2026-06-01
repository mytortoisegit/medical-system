-- ===================================================
-- 中医药品管理系统 - 数据库初始化脚本
-- ===================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `medical_system`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `medical_system`;

-- ==================== 系统用户表 ====================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`                BIGINT(20)   NOT NULL COMMENT '主键ID',
    `username`          VARCHAR(64)  NOT NULL COMMENT '用户名（登录账号）',
    `password`          VARCHAR(256) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name`         VARCHAR(64)  DEFAULT NULL COMMENT '真实姓名',
    `phone`             VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`             VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `gender`            TINYINT(1)   DEFAULT 0 COMMENT '性别（0-未知 1-男 2-女）',
    `avatar`            VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `role_id`           BIGINT(20)   DEFAULT NULL COMMENT '角色ID',
    `status`            TINYINT(1)   DEFAULT 0 COMMENT '账号状态（0-正常 1-禁用）',
    `last_login_ip`     VARCHAR(64)  DEFAULT NULL COMMENT '最后登录IP',
    `last_login_time`   DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `login_fail_count`  INT(11)      DEFAULT 0 COMMENT '登录失败次数',
    `lock_time`         DATETIME     DEFAULT NULL COMMENT '锁定时间',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`         VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `update_by`         VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `del_flag`          TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除（0-未删除 1-已删除）',
    `remark`            VARCHAR(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ==================== 药品分类表 ====================
DROP TABLE IF EXISTS `med_category`;
CREATE TABLE `med_category` (
    `id`            BIGINT(20)   NOT NULL COMMENT '主键ID',
    `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(64)  DEFAULT NULL COMMENT '分类编码',
    `parent_id`     BIGINT(20)   DEFAULT 0 COMMENT '父分类ID（0-顶级分类）',
    `level`         TINYINT(2)   DEFAULT 1 COMMENT '层级',
    `sort_order`    INT(11)      DEFAULT 0 COMMENT '排序号',
    `status`        TINYINT(1)   DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `update_by`     VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `del_flag`      TINYINT(1)   DEFAULT 0 COMMENT '逻辑删除',
    `remark`        VARCHAR(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品分类表';

-- ==================== 中药药品表 ====================
DROP TABLE IF EXISTS `med_medicine`;
CREATE TABLE `med_medicine` (
    `id`                    BIGINT(20)    NOT NULL COMMENT '主键ID',
    `medicine_code`         VARCHAR(64)   DEFAULT NULL COMMENT '药品编码',
    `medicine_name`         VARCHAR(256)  NOT NULL COMMENT '药品名称',
    `pinyin_code`           VARCHAR(128)  DEFAULT NULL COMMENT '拼音码',
    `category_id`           BIGINT(20)    DEFAULT NULL COMMENT '药品分类ID',
    `category_name`         VARCHAR(128)  DEFAULT NULL COMMENT '药品分类名称',
    `alias`                 VARCHAR(256)  DEFAULT NULL COMMENT '别名',
    `property`              VARCHAR(32)   DEFAULT NULL COMMENT '性味（寒、热、温、凉、平）',
    `taste`                 VARCHAR(64)   DEFAULT NULL COMMENT '味（酸、苦、甘、辛、咸）',
    `meridian`              VARCHAR(256)  DEFAULT NULL COMMENT '归经',
    `efficacy`              TEXT          DEFAULT NULL COMMENT '功效',
    `indication`            TEXT          DEFAULT NULL COMMENT '主治',
    `usage_dosage`          VARCHAR(512)  DEFAULT NULL COMMENT '用法用量',
    `contraindication`      TEXT          DEFAULT NULL COMMENT '禁忌',
    `toxicity`              VARCHAR(32)   DEFAULT NULL COMMENT '毒性',
    `origin`                VARCHAR(256)  DEFAULT NULL COMMENT '产地',
    `specification`         VARCHAR(128)  DEFAULT NULL COMMENT '规格',
    `unit`                  VARCHAR(32)   DEFAULT '克' COMMENT '单位',
    `reference_price`       DECIMAL(10,2) DEFAULT NULL COMMENT '参考价格',
    `stock_quantity`        INT(11)       DEFAULT 0 COMMENT '当前库存数量',
    `stock_alert_threshold` INT(11)       DEFAULT 100 COMMENT '库存预警阈值',
    `image_url`             VARCHAR(512)  DEFAULT NULL COMMENT '药品图片URL',
    `status`                TINYINT(1)    DEFAULT 1 COMMENT '状态（0-下架 1-上架）',
    `create_time`           DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`             VARCHAR(64)   DEFAULT NULL COMMENT '创建人',
    `update_by`             VARCHAR(64)   DEFAULT NULL COMMENT '更新人',
    `del_flag`              TINYINT(1)    DEFAULT 0 COMMENT '逻辑删除',
    `remark`                VARCHAR(512)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_medicine_name` (`medicine_name`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_property` (`property`),
    KEY `idx_status` (`status`),
    KEY `idx_pinyin_code` (`pinyin_code`),
    KEY `idx_stock_alert` (`stock_quantity`, `stock_alert_threshold`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中药药品表';

-- ==================== 处方表 ====================
DROP TABLE IF EXISTS `med_prescription`;
CREATE TABLE `med_prescription` (
    `id`                BIGINT(20)    NOT NULL COMMENT '主键ID',
    `prescription_no`   VARCHAR(64)   NOT NULL COMMENT '处方编号',
    `patient_name`      VARCHAR(64)   NOT NULL COMMENT '患者姓名',
    `patient_gender`    TINYINT(1)    DEFAULT 0 COMMENT '患者性别',
    `patient_age`       INT(11)       DEFAULT NULL COMMENT '患者年龄',
    `patient_phone`     VARCHAR(20)   DEFAULT NULL COMMENT '患者联系方式',
    `diagnosis`         VARCHAR(512)  DEFAULT NULL COMMENT '诊断结果',
    `treatment`         VARCHAR(256)  DEFAULT NULL COMMENT '治法',
    `prescription_type` VARCHAR(32)   DEFAULT '内服' COMMENT '处方类型',
    `dose_count`        INT(11)       DEFAULT 1 COMMENT '剂数',
    `prescription_date` DATE          DEFAULT NULL COMMENT '开方日期',
    `doctor_id`         BIGINT(20)    DEFAULT NULL COMMENT '开方医生ID',
    `doctor_name`       VARCHAR(64)   DEFAULT NULL COMMENT '开方医生姓名',
    `total_amount`      DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    `audit_status`      TINYINT(1)    DEFAULT 0 COMMENT '审核状态（0-待审核 1-已审核 2-已驳回）',
    `auditor_id`        BIGINT(20)    DEFAULT NULL COMMENT '审核人ID',
    `audit_opinion`     VARCHAR(512)  DEFAULT NULL COMMENT '审核意见',
    `status`            TINYINT(1)    DEFAULT 1 COMMENT '状态（0-作废 1-有效）',
    `create_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`         VARCHAR(64)   DEFAULT NULL COMMENT '创建人',
    `update_by`         VARCHAR(64)   DEFAULT NULL COMMENT '更新人',
    `del_flag`          TINYINT(1)    DEFAULT 0 COMMENT '逻辑删除',
    `remark`            VARCHAR(512)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prescription_no` (`prescription_no`),
    KEY `idx_patient_name` (`patient_name`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

-- ==================== 处方明细表 ====================
DROP TABLE IF EXISTS `med_prescription_detail`;
CREATE TABLE `med_prescription_detail` (
    `id`              BIGINT(20)    NOT NULL COMMENT '主键ID',
    `prescription_id` BIGINT(20)    NOT NULL COMMENT '处方ID',
    `medicine_id`     BIGINT(20)    NOT NULL COMMENT '药品ID',
    `medicine_name`   VARCHAR(256)  NOT NULL COMMENT '药品名称',
    `specification`   VARCHAR(128)  DEFAULT NULL COMMENT '药品规格',
    `quantity`        INT(11)       NOT NULL COMMENT '数量',
    `unit`            VARCHAR(32)   DEFAULT '克' COMMENT '单位',
    `unit_price`      DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    `amount`          DECIMAL(10,2) DEFAULT NULL COMMENT '金额',
    `usage_method`    VARCHAR(256)  DEFAULT NULL COMMENT '用法',
    `note`            VARCHAR(256)  DEFAULT NULL COMMENT '备注',
    `sort_order`      INT(11)       DEFAULT 0 COMMENT '排序号',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       VARCHAR(64)   DEFAULT NULL COMMENT '创建人',
    `update_by`       VARCHAR(64)   DEFAULT NULL COMMENT '更新人',
    `del_flag`        TINYINT(1)    DEFAULT 0 COMMENT '逻辑删除',
    `remark`          VARCHAR(512)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_prescription_id` (`prescription_id`),
    KEY `idx_medicine_id` (`medicine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

-- ==================== 初始化数据 ====================

-- 插入默认管理员用户（密码：admin123，BCrypt加密）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role_id`, `status`)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6e5He', '系统管理员', 1, 0);

-- 插入常用药品分类
INSERT INTO `med_category` (`id`, `category_name`, `category_code`, `parent_id`, `level`, `sort_order`)
VALUES
(1, '解表药', 'JIEBIAO', 0, 1, 1),
(2, '清热药', 'QINGRE', 0, 1, 2),
(3, '泻下药', 'XIEXIA', 0, 1, 3),
(4, '祛风湿药', 'QUFENGSHI', 0, 1, 4),
(5, '化湿药', 'HUASHI', 0, 1, 5),
(6, '利水渗湿药', 'LISHUI', 0, 1, 6),
(7, '温里药', 'WENLI', 0, 1, 7),
(8, '理气药', 'LIQI', 0, 1, 8),
(9, '消食药', 'XIAOSHI', 0, 1, 9),
(10, '止血药', 'ZHIXUE', 0, 1, 10),
(11, '活血化瘀药', 'HUOXUE', 0, 1, 11),
(12, '化痰止咳平喘药', 'HUATAN', 0, 1, 12),
(13, '安神药', 'ANSHEN', 0, 1, 13),
(14, '补虚药', 'BUXU', 0, 1, 14),
(15, '收涩药', 'SHOUSE', 0, 1, 15);
