package com.corecmp.shared.extension

fun String?.toTitleCase() : String{
    if(this.isNullOrBlank()) return ""
    else return this.replaceFirstChar { it.uppercase() }
}

