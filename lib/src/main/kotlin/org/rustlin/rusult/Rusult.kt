package org.rustlin.rusult

sealed interface Rusult<T, E> {
    class Ok<T, E>(val value: T) : Rusult<T, E>

    class Err<T, E>(val err: E) : Rusult<T, E>

    fun isOk(): Boolean = isOk(this)

    fun isErr(): Boolean = isErr(this)

    fun isOkAnd(f: (T) -> Boolean): Boolean = isOkAnd(this, f)

    fun isErrAnd(f: (E) -> Boolean): Boolean = isErrAnd(this, f)

    fun ok(): T? = ok(this)

    fun err(): E? = err(this)

    fun <U> map(op: (T) -> U): Rusult<U, E> = map(this, op)

    fun <U> mapOr(default: U, op: (T) -> U): U = mapOr(this, default, op)

    fun <U> mapOrElse(default: (E) -> U, op: (T) -> U): U = mapOrElse(this, default, op)

    fun <F> mapErr(op: (E) -> F): Rusult<T, F> = mapErr(this, op)

    fun inspect(f: (T) -> Unit): Rusult<T, E> = inspect(this, f)

    fun inspectErr(f: (E) -> Unit): Rusult<T, E> = inspectErr(this, f)

    fun expect(msg: String): T = expect(this, msg)

    fun unwrap(): T = unwrap(this)

    fun unwrapOrDefault(default: T): T = unwrapOrDefault(this, default)

    fun expectErr(msg: String): E = expectErr(this, msg)

    fun unwrapErr(): E = unwrapErr(this)

    fun <U> and(res: Rusult<U, E>): Rusult<U, E> = and(this, res)

    fun <U> andThen(op: (T) -> Rusult<U, E>): Rusult<U, E> = andThen(this, op)

    fun <F> or(res: Rusult<T, F>): Rusult<T, F> = or(this, res)

    fun <F> orElse(op: (E) -> Rusult<T, F>): Rusult<T, F> = orElse(this, op)

    fun unwrapOr(default: T): T = unwrapOr(this, default)

    fun unwrapOrElse(op: (E) -> T): T = unwrapOrElse(this, op)

    fun copied(): Rusult<T, E> = copied(this)

    fun ifOk(op: (T) -> Unit): Rusult<T, E> = ifOk(this, op)

    fun ifErr(op: (E) -> Unit): Rusult<T, E> = ifErr(this, op)
    fun into(): Result<T> = into(this)

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

        fun <T, E, F> mapErr(self: Rusult<T, E>, op: (E) -> F): Rusult<T, F> =
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

        fun <T, E> Result<T>.into(op: (Throwable) -> E): Rusult<T, E> = from(this, op)

        fun <T, E> into(self: Rusult<T, E>): Result<T> =
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
