package utils

import beannew.DynamicItem
import model.Dao
import java.lang.reflect.Field
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

/**
 * 请确保命名规范，数据库中为下划线命名法，javaBean中为驼峰命名法
 */
class SqlConverter {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            println(getListFromSql<DynamicItem>("SELECT * FROM `dynamic_list` order by dynamic_id desc limit 1,5;"))
        }

        val statement: Statement
            get() = DriverManager.getConnection(Dao.DB_URL, "root", "7424855920").createStatement().apply {
                execute("CREATE DATABASE IF NOT EXISTS counter4_database DEFAULT CHARSET utf8 COLLATE utf8_general_ci;")
                execute("use counter4_database;")
            }


        inline fun <reified T> getListFromSql(sqlCommand: String): List<T> {
            val rs = statement.executeQuery(sqlCommand)
            val list = ArrayList<T>()
            while (rs.next()) {
                list.add(convert(rs, T::class.java))
            }
            return list
        }

        inline fun <reified T> getFirstFromSql(sqlCommand: String): T? {
            val rs = statement.executeQuery(sqlCommand)
            var t: T? = null
            while (rs.next()) {
                t = convert(rs, T::class.java)
            }
            return t
        }


        fun <T> convert(rs: ResultSet, clazz: Class<T>): T {
            val rsMeta = rs.metaData
            val instance = clazz.newInstance()
            for (i in 1..rsMeta.columnCount) {
                val colName = rsMeta.getColumnName(i)
                try {
                    clazz.getDeclaredField(reTranName(colName)).apply {
                        isAccessible = true
                        set(instance, getTypedData(this, rs, i))
                    }
                } catch (e: Exception) {
                    // 没有变量，不做任何事
                }
            }
            return instance
        }

        private fun <T> getTypedData(field: Field, rs: ResultSet, index: Int): T? {
            when (field.type.name) {
                "java.lang.String" -> {
                    return field.type.cast(rs.getString(index)) as T
                }
                "int" -> {
                    return rs.getInt(index) as T
                }
                "float" -> {
                    return rs.getFloat(index) as T
                }
                "double" -> {
                    return rs.getDouble(index) as T
                }
                "boolean" -> {
                    return rs.getBoolean(index) as T
                }
                "short" -> {
                    return rs.getShort(index) as T
                }
                "long" -> {
                    // 尝试转换日期类型为long，因为转换不成功有可能是因为是日期类型
                    val t: Long = try {
                        rs.getLong(index)
                    } catch (e: Exception) {
                        rs.getTimestamp(index).time
                    }
                    return t as T

                }
                "char" -> {
                    return Character::class.java.cast(rs.getString(index)[0]) as T
                }
                else -> {
                    return Any::class.java.cast(rs.getObject(index)) as T
                }
            }
            return null
        }

        /**
         * 将驼峰命名法变为下划线命名法
         */
        private fun tranName(name: String): String {
            val s = StringBuilder()
            for (i in name) {
                if (i.isUpperCase()) {
                    s.append("_")
                }
                s.append(i.toLowerCase())
            }
            return s.toString()
        }

        /**
         * 将下划线命名法变为驼峰命名法
         */
        private fun reTranName(name: String): String {
            val s = StringBuilder()
            var t = false
            for (i in name) {

                if (i == '_') {
                    t = true
                } else {
                    if (t) {
                        s.append(i.toUpperCase())
                        t = false
                    } else {
                        s.append(i.toLowerCase())
                    }
                }

            }
            return s.toString()
        }
    }
}