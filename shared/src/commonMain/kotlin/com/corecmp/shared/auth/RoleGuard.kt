package com.corecmp.shared.auth

import androidx.compose.runtime.Composable

enum class UserRole {
    AGENT, CUSTOMER, ADMIN, GUEST,
}

@Composable
fun RoleGuard(
    allowedRoles: Set<UserRole>,
    currentRole: UserRole,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    if (currentRole in allowedRoles) {
        content()
    } else {
        fallback()
    }
}

fun UserRole.canAccess(required: Set<UserRole>): Boolean = this in required
