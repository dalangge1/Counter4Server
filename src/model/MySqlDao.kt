package model

import java.sql.DriverManager
import java.sql.Statement

object MySqlDao {
    private const val JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"
    private const val DB_URL = "jdbc:mysql://localhost?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"

    private var statement: Statement

    init {

        Class.forName(JDBC_DRIVER);
        val conn = DriverManager.getConnection(DB_URL, "root", "7424855920");
        conn.createStatement().execute("CREATE DATABASE IF NOT EXISTS counter4_database DEFAULT CHARSET utf8 COLLATE utf8_general_ci;")
        statement = conn.createStatement()
        statement.execute("use counter4_database;")

        // 执行数据库的初始化
        initSql(statement)



    }


    fun getAllDynamic(): String {
        val resultSet = statement.executeQuery("SELECT * FROM dynamic_list")

        while (resultSet.next()) {

        }

        resultSet.close()

    }


}