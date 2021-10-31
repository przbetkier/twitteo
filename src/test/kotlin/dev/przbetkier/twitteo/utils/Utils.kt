package dev.przbetkier.twitteo.utils

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith

abstract class EnumListArgumentConverter : ArgumentConverter {

    final override fun convert(source: Any, context: ParameterContext) =
        convert(source
            .toString()
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .filter { it.isNotEmpty() }
            .map { it.trim() })


    protected abstract fun convert(items: List<String>): Any
}

class PlainListArgumentConverter : EnumListArgumentConverter() {
    override fun convert(items: List<String>) =
        items.map { it }
}

@ConvertWith(PlainListArgumentConverter::class)
annotation class PlainListConverter
