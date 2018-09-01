package com.netnovelreader.utils

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware


class SpringContextUtil : ApplicationContextAware {
    companion object {
        lateinit var context: ApplicationContext
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }


    /**
     * 根据名称获取bean
     * @param beanName
     * @return
     */
    fun getBean(beanName: String): Any {
        return context.getBean(beanName)
    }

    /**
     * 根据bean名称获取指定类型bean
     * @param beanName bean名称
     * @param clazz 返回的bean类型,若类型不匹配,将抛出异常
     */
    fun <T> getBean(beanName: String, clazz: Class<T>): T {
        return context.getBean(beanName, clazz)
    }

    /**
     * 根据类型获取bean
     * @param clazz
     * @return
     */
    fun <T> getBean(clazz: Class<T>): T? {
        var t: T? = null
        val map = context.getBeansOfType(clazz)
        for ((_, value) in map) {
            t = value
        }
        return t
    }

    /**
     * 是否包含bean
     * @param beanName
     * @return
     */
    fun containsBean(beanName: String): Boolean {
        return context.containsBean(beanName)
    }

    /**
     * 是否是单例
     * @param beanName
     * @return
     */
    fun isSingleton(beanName: String): Boolean {
        return context.isSingleton(beanName)
    }

    /**
     * bean的类型
     * @param beanName
     * @return
     */
    fun getType(beanName: String): Class<*>? {
        return context.getType(beanName)
    }
}