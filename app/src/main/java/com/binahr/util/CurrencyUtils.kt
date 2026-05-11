package com.binahr.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Indonesian Rupiah format: Rp 1.500.000
 * Uses id-ID locale which naturally produces dots as thousands separators.
 */
fun Long.toRupiah(): String {
    val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${fmt.format(this)}"
}

fun Int.toRupiah(): String = this.toLong().toRupiah()

fun Double.toRupiah(): String = this.toLong().toRupiah()
