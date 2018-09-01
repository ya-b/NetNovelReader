package com.netnovelreader.database.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface UserPopedomMapper {

    @Select("SELECT popedom_name FROM au_user " +
            "INNER JOIN au_user_role on au_user.id = au_user_role.user_id " +
            "RIGHT JOIN au_role_popedom on au_user_role.role_id = au_role_popedom.role_id " +
            "INNER JOIN au_popedom on au_role_popedom.popedom_id = au_popedom.id " +
            "WHERE au_user.username = #{username}")
    fun getUserPopedoms(@Param("username") username: String): List<String>
}