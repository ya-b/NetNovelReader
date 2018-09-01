USE test;

DROP TABLE IF EXISTS au_user;
CREATE TABLE au_user (
    id int NOT NULL AUTO_INCREMENT,
    username varchar(100) COMMENT "用户名",
    password varchar(100) COMMENT "密码",
    email varchar(100) COMMENT "邮箱",
    PRIMARY KEY (id),
    UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS au_role;
CREATE TABLE au_role (
    id int NOT NULL AUTO_INCREMENT,
    role_name varchar(100) COMMENT "角色名称",
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS au_popedom;
CREATE TABLE au_popedom (
    id int NOT NULL AUTO_INCREMENT,
    popedom_name varchar(100) COMMENT "权限",
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS au_user_role;
CREATE TABLE au_user_role (
    id int NOT NULL AUTO_INCREMENT,
    user_id varchar(100) COMMENT "用户id",
    role_id varchar(100) COMMENT "角色id",
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS au_role_popedom;
CREATE TABLE au_role_popedom (
    id int NOT NULL AUTO_INCREMENT,
    role_id varchar(100) COMMENT "角色id",
    popedom_id varchar(100) COMMENT "权限id",
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
