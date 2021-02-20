package model

import java.sql.Statement

fun initSql(statement: Statement) {


    // 用户表
    statement.execute(
            "CREATE TABLE IF NOT EXISTS `user_list`(\n" +

                    "   `user_id` VARCHAR(30) NOT NULL,\n" +        // 用户id
                    "   `nickname` VARCHAR(30) NOT NULL,\n" +       // 昵称
                    "   `password` VARCHAR(30) NOT NULL,\n" +       // 密码
                    "   `register_date` DATE,\n" +                  // 注册时间
                    "   `sex` VARCHAR(3) NOT NULL,\n" +             // 性别 男，女，保密
                    "   `text` VARCHAR(300) NOT NULL,\n" +          // 文字
                    "   `avatar_url` VARCHAR(1000) NOT NULL,\n" +    // 头像
                    "   `topic` VARCHAR(30) NOT NULL,\n" +           // 话题

                    "   PRIMARY KEY ( `user_id` )\n" +
                    ")ENGINE=InnoDB DEFAULT CHARSET=utf8;")

    // 帖子列表
    statement.execute(
            "CREATE TABLE IF NOT EXISTS `dynamic_list`(\n" +

                    "   `dynamic_id` INT UNSIGNED AUTO_INCREMENT,\n" +    // 动态id
                    "   `user_id` VARCHAR(30) NOT NULL,\n" +              // 发布人id
                    "   `submit_time` DATE,\n" +                          // 发布时间
                    "   `text` VARCHAR(3000) NOT NULL,\n" +               // 内容

                    "   PRIMARY KEY ( `dynamic_id` )\n" +
                    ")ENGINE=InnoDB DEFAULT CHARSET=utf8;")

    // 帖子一级回复列表
    statement.execute(
            "CREATE TABLE IF NOT EXISTS `comment_list`(\n" +

                    "   `dynamic_id` INT UNSIGNED AUTO_INCREMENT,\n" +      // 回复的动态id
                    "   `id` INT UNSIGNED AUTO_INCREMENT,\n" +              // 本条id
                    "   `user_id` VARCHAR(30) NOT NULL,\n" +                // 发布人id
                    "   `submit_time` DATE,\n" +                            // 发布时间
                    "   `text` VARCHAR(3000) NOT NULL,\n" +                 // 内容

                    "   PRIMARY KEY ( `id` )\n" +
                    ")ENGINE=InnoDB DEFAULT CHARSET=utf8;")

    // 帖子二级回复列表
    statement.execute(
            "CREATE TABLE IF NOT EXISTS `reply_list`(\n" +

                    "   `reply_id` INT UNSIGNED AUTO_INCREMENT,\n" +        // 回复的一级id
                    "   `id` INT UNSIGNED AUTO_INCREMENT,\n" +              // 本条id
                    "   `user_id` VARCHAR(30) NOT NULL,\n" +                // 发布人id
                    "   `submit_time` DATE,\n" +                            // 发布时间
                    "   `text` VARCHAR(3000) NOT NULL,\n" +                 // 内容

                    "   PRIMARY KEY ( `id` )\n" +
                    ")ENGINE=InnoDB DEFAULT CHARSET=utf8;")

    // 图片列表
    statement.execute(
            "CREATE TABLE IF NOT EXISTS `pic_list`(\n" +

                    "   `dynamic_id` INT UNSIGNED AUTO_INCREMENT,\n" +    // 图片所属的动态id
                    "   `pic_url` VARCHAR(3000) NOT NULL,\n" +            // 图片内容，用分号分割

                    "   PRIMARY KEY ( `dynamic_id` )\n" +
                    ")ENGINE=InnoDB DEFAULT CHARSET=utf8;")

    // 关注列表
    statement.execute(
            "CREATE TABLE IF NOT EXISTS `pic_list`(\n" +

                    "   `user_id` VARCHAR(30) NOT NULL,\n" +               // 用户id的关注列表
                    "   `followed_user_id` VARCHAR(3000) NOT NULL,\n" +    // 关注的用户id，用分号分割

                    "   PRIMARY KEY ( `user_id` )\n" +
                    ")ENGINE=InnoDB DEFAULT CHARSET=utf8;")


}