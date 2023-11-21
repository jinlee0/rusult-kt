package org.rustlin.rusult

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.rustlin.rusult.Rusult.Companion.cloned
import org.rustlin.rusult.Rusult.Companion.toRusult
import org.rustlin.rusult.Rusult.Err
import org.rustlin.rusult.Rusult.Ok
import org.rustlin.rusult.util.AssertionExt.assertEquals
import java.lang.Math.toIntExact
import kotlin.math.pow

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
        x.mapErr(::stringify) assertEquals Ok(2)

        val y: Rusult<Int, Int> = Err(13)
        y.mapErr(::stringify) assertEquals Err("error code: 13")
    }

    @Test
    fun inspect() {
        "4".runCatching(String::toDouble)
            .toRusult()
            .inspect { println("original: $it") }
            .map { it.pow(3) }
            .expect("failed to parse number")
    }

    @Test
    fun inspectErr() {
        "nan".runCatching(String::toDouble)
            .toRusult()
            .inspectErr { println("original: $it") }
    }

    @Test
    fun expectShouldPanic() {
        val x: Rusult<Int, String> = Err("emergency failure")
        assertThrows(RuntimeException::class.java) {
            x.expect("Testing expect") // java.lang.RuntimeException: String
        }

        assertThrows(RuntimeException::class.java) {
            withRusult { System.getenv("IMPORTANT_PATH")!! }
                .expect("env variable `IMPORTANT_PATH` should be set by `wrapper_script.sh`");
        }
    }

    @Test
    fun unwrap() {
        val x: Rusult<Int, Unit> = Ok(2)
        x.unwrap() assertEquals 2
    }

    @Test
    fun unwrapShouldPanic() {
        val x: Rusult<Int, String> = Err("emergency failure");
        assertThrows(RuntimeException::class.java) {
            x.unwrap()
        }
    }

    @Test
    fun unwrapOrDefault() {
        val goodYearFromInput = "1909";
        val badYearFromInput = "190blarg";
        val goodYear = runCatching { goodYearFromInput.toInt() }.toRusult().unwrapOrDefault(2023)
        val badYear = runCatching { badYearFromInput.toInt() }.toRusult().unwrapOrDefault(2023)
        1909 assertEquals goodYear
        2023 assertEquals badYear
    }

    @Test
    fun expectErr() {
        val x: Rusult<Int, String> = Ok(10)
        assertThrows(RuntimeException::class.java) {
            x.expectErr("Testing expect_err")
        }
    }

    @Test
    fun unwrapErr() {
        val x: Rusult<Int, String> = Ok(2)
        assertThrows(RuntimeException::class.java) {
            x.unwrapErr()
        }

        val y: Rusult<Int, String> = Err("emergency failure");
        y.unwrapErr() assertEquals "emergency failure"
    }

    @Test
    fun and() {
        val x: Rusult<Int, String> = Ok(2);
        val y: Rusult<String, String> = Err("late error");
        x.and(y) assertEquals Err("late error")


        val x1: Rusult<Int, String> = Err("early error");
        val y1: Rusult<String, String> = Ok("foo");
        x1.and(y1) assertEquals Err("early error")

        val x2: Rusult<Int, String> = Err("not a 2");
        val y2: Rusult<String, String> = Err("late error")
        x2.and(y2) assertEquals Err("not a 2")

        val x3: Rusult<Int, String> = Ok(2)
        val y3: Rusult<String, String> = Ok("different rusult type")
        x3.and(y3) assertEquals Ok("different rusult type")
    }

    @Test
    fun andThen() {
        fun pow(n: Int): Rusult<String, String> =
            n.toLong().times(n.toLong())
                .runCatching(::toIntExact)
                .toRusult { "overflowed" }
                .map { it.toString() }

        Ok<Int, String>(2).andThen(::pow) assertEquals Ok(4.toString())
        Ok<Int, String>(1_000_000).andThen(::pow) assertEquals Err("overflowed")
    }

    @Test
    fun or() {
        val x: Rusult<Int, String> = Ok(2);
        val y: Rusult<Int, String> = Err("late error");
        x.or(y) assertEquals Ok(2)

        val x1: Rusult<Int, String> = Err("early error");
        val y1: Rusult<Int, String> = Ok(2);
        x1.or(y1) assertEquals Ok(2)

        val x2: Rusult<Int, String> = Err("not a 2");
        val y2: Rusult<Int, String> = Err("late error");
        x2.or(y2) assertEquals Err("late error")

        val x3: Rusult<Int, String> = Ok(2);
        val y3: Rusult<Int, String> = Ok(100);
        x3.or(y3) assertEquals Ok(2)
    }

    @Test
    fun orElse() {
        fun sq(x: Int): Rusult<Int, Int> = Ok(x * x)
        fun err(x: Int): Rusult<Int, Int> = Err(x)

        Ok<Int, Int>(2).orElse(::sq).orElse(::sq) assertEquals Ok(2)
        Ok<Int, Int>(2).orElse(::err).orElse(::sq) assertEquals Ok(2)
        Err<Int, Int>(3).orElse(::sq).orElse(::err) assertEquals Ok(9)
        Err<Int, Int>(3).orElse(::err).orElse(::err) assertEquals Err(3)
    }

    @Test
    fun unwrapOr() {
        val default = 2;
        val x: Rusult<Int, String> = Ok(9);
        x.unwrapOr(default) assertEquals 9

        val y: Rusult<Int, String> = Err("error");
        y.unwrapOr(default) assertEquals default
    }

    @Test
    fun unwrapOrElse() {
        fun count(x: String): Int = x.length

        Ok<Int, String>(2).unwrapOrElse(::count) assertEquals 2
        Err<Int, String>("foo").unwrapOrElse(::count) assertEquals 3
    }

    @Test
    fun copied() {
        val value = 12;
        val x: Rusult<Int, Int> = Ok(value);
        x assertEquals Ok(12)
        val copied = x.copied()
        copied assertEquals Ok(12)
    }

    @Test
    fun cloned() {
        class CloneableInt(val i: Int) : Clone<CloneableInt> {
            override fun clone(): CloneableInt = CloneableInt(i)
            override fun equals(other: Any?): Boolean = other is CloneableInt && other.i == i
            override fun hashCode(): Int = i.hashCode()
        }

        val value = 12;
        val x: Rusult<CloneableInt, CloneableInt> = Ok(CloneableInt(value));
        x assertEquals Ok(CloneableInt(12))
        val cloned = x.cloned()
        cloned assertEquals Ok(CloneableInt(12))
    }

    @Test
    fun ifOk() {
        var count = 0

        val ok: Rusult<String, String> = Ok("ok")
        ok.ifOk { count++ }
        count assertEquals 1

        val err: Rusult<String, String> = Err("err")
        err.ifOk { count++ }
        count assertEquals 1
    }

    @Test
    fun ifErr() {
        var count = 0

        val ok: Rusult<String, String> = Ok("ok")
        ok.ifErr { count++ }
        count assertEquals 0

        val err: Rusult<String, String> = Err("err")
        err.ifErr { count++ }
        count assertEquals 1
    }
}
