package com.netnovelreader.database.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface UserRoleMapper {
    @Select("SELECT role_name FROM au_user " +
            "INNER JOIN au_user_role ON au_user.id = au_user_role.user_id " +
            "INNER JOIN au_role ON au_user_role.role_id = au_role.id " +
            "WHERE username = #{username}")
    fun getRole(@Param("username") username: String): String
}