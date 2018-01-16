package com.netnovelreader.reader

/**
 * Created by yangbo on 18-1-13.
 */
class ReaderViewModel : IReaderContract.IReaderViewModel {
    var pageNumber = 0L
    val texts = Array(3){ StringBuilder() }
    var readerBean: ReaderBean? = null

    /**
     * @boolean true表示向后翻页 false表示向前翻页
     */
    override fun getChapterText(boolean: Boolean): Array<StringBuilder> {
        texts.forEach {
            it.delete(0, it.length)
            if(boolean){
                it.append(++pageNumber)
            }else{
                it.append(--pageNumber - 2)
            }
        }
        return texts
    }

    override fun getModel(): ReaderBean? {
        readerBean ?: synchronized(this){
            readerBean ?: run{ readerBean = ReaderBean() }
        }
        return readerBean
    }
}