package io.github.j_yuhanwang.food_ordering_app.enums;/*
 * @author BlairWang
 * @Date 22/01/2026 9:06 pm
 * @Version 1.0
 */

public enum UserStatus {
    ACTIVE, //normal status, isactive = true
    INACTIVE, // deactivate by user itself, isActive = false
    BANNED // banned by admin, isActive = false
}
