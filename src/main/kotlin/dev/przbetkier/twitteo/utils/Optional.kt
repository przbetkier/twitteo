package dev.przbetkier.twitteo.utils

import java.util.Optional

fun <T> Optional<T>.unwrap(): T? = orElse(null)
