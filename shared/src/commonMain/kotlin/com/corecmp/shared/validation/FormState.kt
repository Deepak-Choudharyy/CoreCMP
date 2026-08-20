package com.corecmp.shared.validation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FormState {
    private val _errors = mutableStateMapOf<String, String>()
    val errors: Map<String, String> get() = _errors

    var isSubmitting by mutableStateOf(false)
        private set

    val isValid: Boolean
        get() = _errors.isEmpty()

    fun setError(field: String, message: String?) {
        if (message.isNullOrBlank()) {
            _errors.remove(field)
        } else {
            _errors[field] = message
        }
    }

    fun getError(field: String): String? = _errors[field]

    fun clearError(field: String) {
        _errors.remove(field)
    }

    fun clearErrors() {
        _errors.clear()
    }

    fun setErrors(newErrors: Map<String, String>) {
        _errors.clear()
        _errors.putAll(newErrors.filterValues { it.isNotBlank() })
    }

    fun validate(field: String, isValid: Boolean, errorMessage: String) {
        setError(field, if (isValid) null else errorMessage)
    }

    fun markSubmitting(value: Boolean) {
        isSubmitting = value
    }

    fun submit(block: () -> Unit) {
        if (!isValid || isSubmitting) return
        isSubmitting = true
        try {
            block()
        } finally {
            isSubmitting = false
        }
    }
}
