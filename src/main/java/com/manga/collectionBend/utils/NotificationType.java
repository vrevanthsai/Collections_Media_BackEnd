package com.manga.collectionBend.utils;

public enum NotificationType {
    FRIEND_REQUEST,
    FRIEND_ACCEPTED,
    FRIEND_REJECTED,   // optional, if you want to notify rejection too
    COLLECTION_SHARED, // for your future sharing feature
    UNFRIENDED,         // optional
    SUSPENDED_USER, // Admin based value - where Admin account gets Notification of activateRequest from Suspended_user and admin activates his account by using /suspend Admin api
}
