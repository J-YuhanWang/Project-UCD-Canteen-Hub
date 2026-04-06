package io.github.j_yuhanwang.food_ordering_app.enums;/*
 * @author BlairWang
 * @Date 22/11/2025 9:56 am
 * @Version 1.0
 */

public enum OrderStatus {
    INITIALIZED,
    CONFIRMED, //Payment completed, canteen is preparing.
    READY_FOR_PICKUP, //waiting for students/users to pickup
    COMPLETED, //finished
    CANCELLED,
    FAILED
}
