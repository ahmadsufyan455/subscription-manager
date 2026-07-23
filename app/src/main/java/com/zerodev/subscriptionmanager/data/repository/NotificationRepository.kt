package com.zerodev.subscriptionmanager.data.repository

import com.zerodev.subscriptionmanager.data.local.dao.NotificationDao
import com.zerodev.subscriptionmanager.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    fun getUnreadCount(): Flow<Int>
    suspend fun insertNotification(notification: NotificationEntity): Long
    suspend fun markAsRead(id: Int)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: Int)
    suspend fun deleteAllNotifications()
}

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao
) : NotificationRepository {
    override fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications()
    }

    override fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }

    override suspend fun insertNotification(notification: NotificationEntity): Long {
        return notificationDao.insertNotification(notification)
    }

    override suspend fun markAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun deleteNotification(id: Int) {
        notificationDao.deleteNotification(id)
    }

    override suspend fun deleteAllNotifications() {
        notificationDao.deleteAllNotifications()
    }
}
