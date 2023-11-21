package org.rustlin.rusult

sealed interface Rusult<T, E> {
    /**
     * Create one contains success value
     */
    class Ok<T, E>(val value: T) : Rusult<T, E> {
        override fun toString(): String = "Ok(${this.value})"

        override fun equals(other: Any?): Boolean {
            if (other is Ok<*, *>) {
                if (other.value == this.value) {
                    return true
                }
            }
            return false
        }

        override fun hashCode(): Int = this.value.hashCode()
    }

    /**
     * Create one contains failure value
     */
    class Err<T, E>(val err: E) : Rusult<T, E> {
        override fun toString(): String = "Err(${this.err})"

        override fun equals(other: Any?): Boolean {
            if (other is Err<*, *>) {
                if (other.err == this.err) {
                    return true
                }
            }
            return false
        }

        override fun hashCode(): Int = this.err.hashCode()
    }

    /**
     * @return true if the rusult is Ok
     */
    fun isOk(): Boolean = isOk(this)

    /**
     * @return true if the rusult is Err
     */
    fun isErr(): Boolean = isErr(this)

    /**
     * @return `true` if the result is `Ok` and the value inside of it matches a predicate.
     */

    fun isOkAnd(f: (T) -> Boolean): Boolean = isOkAnd(this, f)

    /**
     * @return `true` if the result is `Err` and the value inside of it matches a predicate.
     */
    fun isErrAnd(f: (E) -> Boolean): Boolean = isErrAnd(this, f)

    /**
     * Converts `Rusult<T, E>` to `T?`
     * @return `T?` if the Rusult is Ok, and `null` if it is Err
     */

    fun ok(): T? = ok(this)

    /**
     * Convenrts `Rusult<T, E>` to `E?`
     * @return `E?` if the Rusult is Err, and `null` if it is Ok
     */

    fun err(): E? = err(this)

    /**
     * Maps a `Rusult<T, E>` to `Rusult<U, E>` by applying a function to a contained `Ok` value, leaving an `Err` value untouched.
     * # Examples
     * ```
     * val line = "1\n2\n3\n4\n"
     * for (num in line.lines()) {
     *      when (val parsed = runCatching { num.toInt() }.toRusult { IllegalStateException() }) {
     *          is Ok -> println(parsed)
     *          is Err -> {}
     *      }
     * }
     * ```
     */
    fun <U> map(op: (T) -> U): Rusult<U, E> = map(this, op)

    /**
     * Arguments passed to `mapOr` are eagerly evaluated; if you are passing
     * the result of a function call, it is recommended to use `mapOrElse`,
     * which is lazily evaluated.
     * # Examples
     * ```
     * val x: Rusult<String, String> = Ok("foo")
     * assertEquals(3, x.mapOr(42) { it.length })
     * val y: Rusult<String, String> = Err("bar")
     * assertEquals(42, y.mapOr(42) { it.length })
     * ```
     * @return the provided default (if `Err`), or applies a function to the contained value (if `Ok`),
     */
    fun <U> mapOr(default: U, op: (T) -> U): U = mapOr(this, default, op)

    /**
     * Maps a `Rusult<T, E>` to `U` by applying fallback function `default` to
     * a contained `Err` value, or function `op` to a contained `Ok` value.
     * This function can be used to unpack a successful result
     * while handling an error.
     * # Examples
     * ```
     * val k = 21

     * val x: Rusult<String, String> = Ok("foo")
     * assertEquals(3, x.mapOrElse({ k * 2 }, { it.length }))

     * val y: Rusult<String, String> = Err("bar")
     * assertEquals(42, y.mapOrElse({ k * 2 }, { it.length }))
     * ```
     */
    fun <U> mapOrElse(default: (E) -> U, op: (T) -> U): U = mapOrElse(this, default, op)

    /**
     * Maps a `Rusult<T, E>` to `Rusult<T, F>` by applying a function to a
     * contained `Err` value, leaving an `Ok` value untouched.
     *
     * This function can be used to pass through a successful result while handling
     * an error.
     *
     *
     * # Examples
     *
     * ```
     * fun stringify(x: Int): String = "error code: $x"
     *
     * val x: Rusult<Int, Int> = Ok(2)
     * x.mapErr(::stringify) assertEquals Ok(2)
     *
     * val y: Rusult<Int, Int> = Err(13)
     * y.mapErr(::stringify) assertEquals Err("error code: 13")
     * ```
     */
    fun <F> mapErr(op: (E) -> F): Rusult<T, F> = mapErr(this, op)

    /** Calls the provided closure with a reference to the contained value (if `Ok`).
     *
     * # Examples
     *
     * ```
     * "4".runCatching(String::toDouble)
     *      .toRusult()
     *      .inspect { println("original: $it") }
     *      .map { it.pow(3) }
     *      .expect("failed to parse number")
     * ```
     */
    fun inspect(f: (T) -> Unit): Rusult<T, E> = inspect(this, f)

    /**
     * Calls the provided closure with a reference to the contained error (if `Err`).
     *
     * # Examples
     *
     * ```
     * "nan".runCatching(String::toDouble)
     *      .toRusult()
     *      .inspectErr { println("original: $it") }
     * ```
     */
    fun inspectErr(f: (E) -> Unit): Rusult<T, E> = inspectErr(this, f)

    /**
     * Returns the contained `Ok` value
     *
     * Because this function may throw exception, its use is generally discouraged.
     * Instead, prefer to use pattern matching and handle the `Err`
     * case explicitly, or call `unwrapOr`, `unwrapOrElse`, or
     * `unwrapOrDefault`.
     *
     * # Panics
     *
     * Panics if the value is an `Err`, with a panic message including the
     * passed message, and the content of the `Err`.
     *
     *
     * # Examples
     *
     * ```
     * // Panic
     * val x: Rusult<Int, String> = Err("emergency failure")
     * assertThrows(RuntimeException::class.java) {
     *      x.expect("Testing expect") // java.lang.RuntimeException: String
     * }
     * ```
     *
     * # Recommended Message Style
     *
     * We recommend that `expect` messages are used to describe the reason you
     * _expect_ the `Rusult` should be `Ok`.
     *
     * ```
     * // Panic
     * withRusult { System.getenv("IMPORTANT_PATH")!! }
     *      .expect("env variable `IMPORTANT_PATH` should be set by `wrapper_script.sh`");
     * ```
     */
    fun expect(msg: String): T = expect(this, msg)

    /**
     * Returns the contained `Ok` value
     *
     * Because this function may panic, its use is generally discouraged.
     * Instead, prefer to use pattern matching and handle the `Err`
     * case explicitly, or call `unwrapOr`, `unwrapOrElse`, or
     * `unwrapOrDefault`.
     *
     * # Panics
     *
     * Panics if the value is an `Err`, with a panic message provided by the
     * `Err`'s value.
     *
     * # Examples
     *
     * Basic usage:
     *
     * ```
     * val x: Rusult<Int, Unit> = Ok(2)
     * x.unwrap() assertEquals 2
     * ```
     *
     * ```
     * // Panic
     * val x: Rusult<Int, String> = Err("emergency failure");
     * x.unwrap()
     * ```
     */
    fun unwrap(): T = unwrap(this)

    /**
     * Returns the contained [Ok] value (if [Ok]) or the default (if [Err]).
     *
     * # Examples
     *
     * ```
     * val goodYearFromInput = "1909";
     * val badYearFromInput = "190blarg";
     * val goodYear = runCatching { goodYearFromInput.toInt() }.toRusult().unwrapOrDefault(2023)
     * val badYear = runCatching { badYearFromInput.toInt() }.toRusult().unwrapOrDefault(2023)
     * 1909 assertEquals goodYear
     * 2023 assertEquals badYear
     */
    fun unwrapOrDefault(default: T): T = unwrapOrDefault(this, default)

    /**
     * Returns the contained [Err] value.
     *
     * # Panics
     *
     * Panics if the value is an [Ok], with a panic message including the
     * passed message, and the content of the [Ok].
     *
     *
     * # Examples
     *
     * ```
     * val x: Rusult<Int, String> = Ok(10)
     * assertThrows(RuntimeException::class.java) {
     *      x.expectErr("Testing expect_err")
     * }
     * ```
     */
    fun expectErr(msg: String): E = expectErr(this, msg)

    /**
     * Returns the contained [Err] value.
     *
     * # Panics
     *
     * Panics if the value is an [Ok], with a custom panic message provided
     * by the [Ok]'s value.
     *
     * # Examples
     *
     * ```
     * val x: Rusult<Int, String> = Ok(2);
     * assertThrows(RuntimeException::class.java) {
     *      x.unwrapErr()
     * }
     * ```
     *
     * ```
     * val y: Rusult<Int, String> = Err("emergency failure");
     * y.unwrapErr() assertEquals "emergency failure"
     * ```
     */
    fun unwrapErr(): E = unwrapErr(this)

    /**
     * Returns `res` if the result is [Ok], otherwise returns the [Err] value of `self`.
     *
     * Arguments passed to `and` are eagerly evaluated; if you are passing the
     * result of a function call, it is recommended to use [andThen], which is
     * lazily evaluated.
     *
     * # Examples
     *
     * ```
     *   val x: Rusult<Int, String> = Ok(2);
     *   val y: Rusult<String, String> = Err("late error");
     *   x.and(y) assertEquals Err("late error")
     *
     *   val x1: Rusult<Int, String> = Err("early error");
     *   val y1: Rusult<String, String> = Ok("foo");
     *   x1.and(y1) assertEquals Err("early error")
     *
     *   val x2: Rusult<Int, String> = Err("not a 2");
     *   val y2: Rusult<String, String> = Err("late error")
     *   x2.and(y2) assertEquals Err("not a 2")
     *
     *   val x3: Rusult<Int, String> = Ok(2)
     *   val y3: Rusult<String, String> = Ok("different rusult type")
     *   x3.and(y3) assertEquals Ok("different rusult type")
     * ```
     */
    fun <U> and(res: Rusult<U, E>): Rusult<U, E> = and(this, res)

    /**
     * Calls `op` if the result is [Ok], otherwise returns the [Err] value of `self`.
     *
     * # Examples
     *
     * ```
     * fun pow(n: Int): Rusult<String, String> =
     *      n.toLong().times(n.toLong())
     *          .runCatching(::toIntExact)
     *          .toRusult { "overflowed" }
     *          .map { it.toString() }
     * Ok<Int, String>(2).andThen(::pow) assertEquals Ok(4.toString())
     * Ok<Int, String>(1_000_000).andThen(::pow) assertEquals Err("overflowed")
     * ```
     */
    fun <U> andThen(op: (T) -> Rusult<U, E>): Rusult<U, E> = andThen(this, op)

    /**
     * Returns `res` if the result is [Err], otherwise returns the [Ok] value of `self`.
     *
     * Arguments passed to `or` are eagerly evaluated; if you are passing the
     * result of a function call, it is recommended to use [orElse], which is
     * lazily evaluated.
     *
     * # Examples
     *
     * ```
     * val x: Rusult<Int, String> = Ok(2);
     * val y: Rusult<Int, String> = Err("late error");
     * x.or(y) assertEquals Ok(2)
     *
     * val x1: Rusult<Int, String> = Err("early error");
     * val y1: Rusult<Int, String> = Ok(2);
     * x1.or(y1) assertEquals Ok(2)
     *
     * val x2: Rusult<Int, String> = Err("not a 2");
     * val y2: Rusult<Int, String> = Err("late error");
     * x2.or(y2) assertEquals Err("late error")
     *
     * val x3: Rusult<Int, String> = Ok(2);
     * val y3: Rusult<Int, String> = Ok(100);
     * x3.or(y3) assertEquals Ok(2)
     * ```
     */
    fun <F> or(res: Rusult<T, F>): Rusult<T, F> = or(this, res)

    /**
     * Calls `op` if the result is [Err], otherwise returns the [Ok] value of `self`.
     *
     * # Examples
     *
     * ```
     *  fun sq(x: Int): Rusult<Int, Int> = Ok(x * x)
     *  fun err(x: Int): Rusult<Int, Int> = Err(x)
     *
     *  Ok<Int, Int>(2).orElse(::sq).orElse(::sq) assertEquals Ok(2)
     *  Ok<Int, Int>(2).orElse(::err).orElse(::sq) assertEquals Ok(2)
     *  Err<Int, Int>(3).orElse(::sq).orElse(::err) assertEquals Ok(9)
     *  Err<Int, Int>(3).orElse(::err).orElse(::err) assertEquals Err(3)
     * ```
     */
    fun <F> orElse(op: (E) -> Rusult<T, F>): Rusult<T, F> = orElse(this, op)

    /**
     * Returns the contained [Ok] value or a provided default.
     *
     * Arguments passed to `unwrapOr` are eagerly evaluated; if you are passing
     * the result of a function call, it is recommended to use [unwrapOrElse],
     * which is lazily evaluated.
     *
     * # Examples
     *
     * ```
     * let default = 2;
     * let x: Result<u32, &str> = Ok(9);
     * assert_eq!(x.unwrap_or(default), 9);
     *
     * let x: Result<u32, &str> = Err("error");
     * assert_eq!(x.unwrap_or(default), default);
     * ```
     */
    fun unwrapOr(default: T): T = unwrapOr(this, default)

    /**
     * Returns the contained [Ok] value or computes it from a closure.
     *
     *
     * # Examples
     *
     * ```
     * fn count(x: &str) -> usize { x.len() }
     *
     * assert_eq!(Ok(2).unwrap_or_else(count), 2);
     * assert_eq!(Err("foo").unwrap_or_else(count), 3);
     * ```
     */
    fun unwrapOrElse(op: (E) -> T): T = unwrapOrElse(this, op)

    /**
     * Maps a `Rusult<T, E>` to a `Rusult<T, E>` by copying the contents of the
     * `Ok` part.
     *
     * # Examples
     *
     * ```
     * val value = 12;
     * val x: Rusult<Int, Int> = Ok(value);
     * x assertEquals Ok(12)
     * val copied = x.copied()
     * copied assertEquals Ok(12)
     * ```
     */
    fun copied(): Rusult<T, E> = copied(this)

    /**
     * Runs `op` if the rusult is [Ok]
     *
     * # Examples
     *
     * ```
     * var count = 0
     *
     * val ok: Rusult<String, String> = Ok("ok")
     * ok.ifOk { count++ }
     * count assertEquals 1
     *
     * val err: Rusult<String, String> = Err("err")
     * err.ifOk { count++ }
     * count assertEquals 1
     * ```
     */
    fun ifOk(op: (T) -> Unit): Rusult<T, E> = ifOk(this, op)

    /**
     * Runs `op` if the rusult is [Err]
     *
     * # Examples
     *
     * ```
     * var count = 0
     *
     * val ok: Rusult<String, String> = Ok("ok")
     * ok.ifErr { count++ }
     * count assertEquals 0
     *
     * val err: Rusult<String, String> = Err("err")
     * err.ifErr { count++ }
     * count assertEquals 1
     * ```
     */
    fun ifErr(op: (E) -> Unit): Rusult<T, E> = ifErr(this, op)

    /**
     * Maps a `Rusult<T,E>` to a `Result<T>`
     * [Ok] -> [Result.success]
     * [Err] -> [Result.failure]
     */
    fun toResult(): Result<T> = toResult(this)

    companion object {
        fun <T, E> isOk(self: Rusult<T, E>): Boolean = self is Ok

        fun <T, E> isErr(self: Rusult<T, E>): Boolean = !isOk(self = self)

        fun <T, E> ifOk(self: Rusult<T, E>, op: (T) -> Unit): Rusult<T, E> =
            self.also {
                when (it) {
                    is Ok -> op(it.value)
                    is Err -> {}
                }
            }

        fun <T, E> ifErr(self: Rusult<T, E>, op: (E) -> Unit): Rusult<T, E> =
            self.also {
                when (it) {
                    is Ok -> {}
                    is Err -> op(it.err)
                }
            }

        fun <T, E> isOkAnd(self: Rusult<T, E>, f: (T) -> Boolean): Boolean =
            when (self) {
                is Ok -> f(self.value)
                is Err -> false
            }

        fun <T, E> isErrAnd(self: Rusult<T, E>, f: (E) -> Boolean): Boolean =
            when (self) {
                is Ok -> false
                is Err -> f(self.err)
            }

        fun <T, E> ok(self: Rusult<T, E>): T? =
            when (self) {
                is Ok -> self.value
                is Err -> null
            }

        fun <T, E> err(self: Rusult<T, E>): E? =
            when (self) {
                is Ok -> null
                is Err -> self.err
            }

        fun <T, E, U> map(self: Rusult<T, E>, op: (T) -> U): Rusult<U, E> =
            when (self) {
                is Ok -> Ok(op(self.value))
                is Err -> Err(self.err)
            }

        fun <T, E, U> mapOr(self: Rusult<T, E>, default: U, op: (T) -> U): U =
            when (self) {
                is Ok -> op(self.value)
                is Err -> default
            }

        fun <T, E, U> mapOrElse(self: Rusult<T, E>, default: (E) -> U, op: (T) -> U): U =
            when (self) {
                is Ok -> op(self.value)
                is Err -> default(self.err)
            }

        inline fun <T, E, F> mapErr(self: Rusult<T, E>, op: (E) -> F): Rusult<T, F> =
            when (self) {
                is Ok -> Ok(self.value)
                is Err -> Err(op(self.err))
            }

        fun <T, E> inspect(self: Rusult<T, E>, f: (T) -> Unit): Rusult<T, E> =
            self.also {
                if (self is Ok) {
                    f(self.value)
                }
            }

        fun <T, E> inspectErr(self: Rusult<T, E>, f: (E) -> Unit): Rusult<T, E> = self.also {
            if (self is Err) {
                f(self.err)
            }
        }

        fun <T, E> expect(self: Rusult<T, E>, msg: String): T =
            when (self) {
                is Ok -> self.value
                is Err -> unwrapFailed(msg = msg, e = self.err)
            }

        fun <T, E> unwrap(self: Rusult<T, E>): T =
            when (self) {
                is Ok -> self.value
                is Err -> unwrapFailed(
                    msg = "called `${Rusult::class.simpleName}::unwrap()` on an `${Err::class.simpleName}` value",
                    e = self.err
                )
            }

        fun <T, E> unwrapOrDefault(self: Rusult<T, E>, default: T): T =
            when (self) {
                is Ok -> self.value
                is Err -> default
            }

        fun <T, E> expectErr(self: Rusult<T, E>, msg: String): E =
            when (self) {
                is Ok -> unwrapFailed(msg = msg, e = self.value)
                is Err -> self.err
            }

        fun <T, E> unwrapErr(self: Rusult<T, E>): E =
            when (self) {
                is Ok -> unwrapFailed(
                    msg = "called `${Rusult::class.simpleName}::unwrapErr` on an `${Err::class.simpleName}` value",
                    e = self.value
                )

                is Err -> self.err
            }

        fun <T, E, U> and(self: Rusult<T, E>, res: Rusult<U, E>): Rusult<U, E> =
            when (self) {
                is Ok -> res
                is Err -> Err(self.err)
            }

        fun <T, E, U> andThen(self: Rusult<T, E>, op: (T) -> Rusult<U, E>): Rusult<U, E> =
            when (self) {
                is Ok -> op(self.value)
                is Err -> Err(self.err)
            }

        fun <T, E, F> or(self: Rusult<T, E>, res: Rusult<T, F>): Rusult<T, F> =
            when (self) {
                is Ok -> Ok(self.value)
                is Err -> res
            }

        fun <T, E, F> orElse(self: Rusult<T, E>, op: (E) -> Rusult<T, F>): Rusult<T, F> =
            when (self) {
                is Ok -> Ok(self.value)
                is Err -> op(self.err)
            }

        fun <T, E> unwrapOr(self: Rusult<T, E>, default: T): T =
            when (self) {
                is Ok -> self.value
                is Err -> default
            }

        fun <T, E> unwrapOrElse(self: Rusult<T, E>, op: (E) -> T): T =
            when (self) {
                is Ok -> self.value
                is Err -> op(self.err)
            }

        fun <T, E> copied(self: Rusult<T, E>): Rusult<T, E> = map(self) { it }

        fun <T, E> transpose(self: Rusult<T?, E>): Rusult<T, E>? =
            when (self) {
                is Ok -> self.value?.let(::Ok)
                is Err -> Err(self.err)
            }


        @JvmName("Rusult::flatten::transpose")
        fun <T, E> Rusult<T?, E>.transpose(): Rusult<T, E>? = transpose(this)

        fun <T, E> flatten(self: Rusult<Rusult<T, E>, E>): Rusult<T, E> = andThen(self) { it }

        @JvmName("Rusult::flatten::extension")
        fun <T, E> Rusult<Rusult<T, E>, E>.flatten(): Rusult<T, E> = flatten(this)

        fun <T : Clone<T>, E : Clone<E>> clone(self: Rusult<T, E>): Rusult<T, E> =
            when (self) {
                is Ok -> Ok(self.value.clone())
                is Err -> Err(self.err.clone())
            }

        @JvmName("Rusult::clone::extension")
        fun <T : Clone<T>, E : Clone<E>> Rusult<T, E>.clone(): Rusult<T, E> = clone(this)

        /**
         * Maps a `Rusult<T, E>` to a `Rusult<T, E>` by cloning the contents of the
         * `Ok` part.
         *
         * # Examples
         *
         * ```
         * class CloneableInt(val i: Int) : Clone<CloneableInt> {
         *      override fun clone(): CloneableInt = CloneableInt(i)
         *      override fun equals(other: Any?): Boolean = other is CloneableInt && other.i == i
         *      override fun hashCode(): Int = i.hashCode()
         * }
         *
         * val value = 12;
         * val x: Rusult<CloneableInt, CloneableInt> = Ok(CloneableInt(value));
         * x assertEquals Ok(CloneableInt(12))
         * val cloned = x.cloned()
         * cloned assertEquals Ok(CloneableInt(12))
         * ```
         */
        fun <T : Clone<T>, E> cloned(self: Rusult<T, E>): Rusult<T, E> = map(self) { it.clone() }

        @JvmName("Rusult::cloned::extension")
        fun <T : Clone<T>, E> Rusult<T, E>.cloned(): Rusult<T, E> = cloned(this)

        fun <T, E> from(result: Result<T>, op: (Throwable) -> E): Rusult<T, E> =
            if (result.isSuccess) {
                Ok(result.getOrThrow())
            } else {
                Err(op(result.exceptionOrNull()!!))
            }

        fun <T, E> from(result: Result<T>, e: E): Rusult<T, E> = from(result) { e }

        fun <T, E> from(run: () -> T, op: (Throwable) -> E): Rusult<T, E> =
            from(runCatching { run() }, op)

        fun <T, E> Result<T>.toRusult(op: (Throwable) -> E): Rusult<T, E> = from(this, op)

        fun <T> Result<T>.toRusult(): Rusult<T, Unit> = from(this) { }

        fun <T, E> toResult(self: Rusult<T, E>): Result<T> =
            when (self) {
                is Ok -> Result.success(self.value)
                is Err -> Result.failure(errorToThrowable(self.err))
            }

        private fun <E> unwrapFailed(msg: String, e: E): Nothing {
            println(msg)
            throw errorToThrowable(e = e)
        }

        private fun <E> errorToThrowable(e: E): Throwable = when (e) {
            is Throwable -> e
            is Enum<*> -> RuntimeException(e.name)
            else -> RuntimeException(e?.let { it::class.simpleName } ?: "null")
        }
    }
}
