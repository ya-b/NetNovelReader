package com.netnovelreader.data.db

import android.arch.persistence.room.*

@Dao
interface ShelfDao {

    @Query("SELECT * FROM shelf order by latest_read DESC")
    fun getAll(): List<ShelfBean>?

    @Query("SELECT * FROM shelf WHERE book_name LIKE :bookname")
    fun getBookInfo(bookname: String): ShelfBean?

    @Query("SELECT max(latest_read) from shelf")
    fun getLatestReaded(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg beans: ShelfBean)

    @Delete
    fun delete(bean: ShelfBean)

    fun replace(bean: ShelfBean){
        val old = getBookInfo(bean.bookName!!)
        if(old == null){
            insert(bean)
        }else{
            ShelfBean(
                    bean._id ?: old._id,
                    bean.bookName,
                    bean.downloadUrl ?: old.downloadUrl,
                    bean.readRecord ?: old.readRecord,
                    bean.isUpdate ?: old.isUpdate,
                    bean.latestChapter ?: old.latestChapter,
                    bean.latestRead ?: old.latestRead
            ).apply { insert(this) }
        }
    }
}