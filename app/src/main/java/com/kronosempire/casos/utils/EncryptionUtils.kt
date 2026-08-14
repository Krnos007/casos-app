package com.kronosempire.casos.utils

import java.security.MessageDigest

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(this.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}

fun String.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    val hash = digest.digest(this.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}
