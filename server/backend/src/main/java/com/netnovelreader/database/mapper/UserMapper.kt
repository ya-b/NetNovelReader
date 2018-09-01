package com.netnovelreader.database.mapper

import com.netnovelreader.database.entity.User
import org.apache.ibatis.annotations.*

@Mapper
interface UserMapper {
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "username", column = "username"),
        Result(property = "password", column = "password"),
        Result(property = "email", column = "email")
    )
    @Select("SELECT * FROM au_user WHERE username = #{username}")
    fun getUser(@Param("username") username: String): User?

    @Insert("INSERT INTO au_user(username, password) VALUES (#{username}, #{password})")
    fun insert(
        @Param("username") username: String,
        @Param("password") password: String,
        @Param("email") email: String
    ): Int
}
