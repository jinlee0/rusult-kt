package org.rustlin.rusult

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rustlin.rusult.Rusult.Companion.toRusult
import org.rustlin.rusult.Rusult.Err
import org.rustlin.rusult.Rusult.Ok

class RusultTest {
    @Test
    fun map() {
        val line = "1\n2\n3\n4\n"
        for (num in line.lines()) {
            when (val parsed = runCatching { num.toInt() }.toRusult()) {
                is Ok -> println(parsed)
                is Err -> {}
            }
        }
    }

    @Test
    fun mapOr() {
        val x: Rusult<String, String> = Ok("foo")
        assertEquals(3, x.mapOr(42) { it.length })
        val y: Rusult<String, String> = Err("bar")
        assertEquals(42, y.mapOr(42) { it.length })
    }

    @Test
    fun mapOrElse() {
        val k = 21

        val x: Rusult<String, String> = Ok("foo")
        assertEquals(3, x.mapOrElse({ k * 2 }, { it.length }))

        val y: Rusult<String, String> = Err("bar")
        assertEquals(42, y.mapOrElse({ k * 2 }, { it.length }))
    }

    @Test
    fun mapErr() {
        fun stringify(x: Int): String = "error code: $x"

        val x: Rusult<Int, Int> = Ok(2)
        assertEquals(Ok<Int, Int>(2), x.mapErr(::stringify))
    }
}
