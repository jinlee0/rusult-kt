package org.rustlin.rusult

import org.rustlin.rusult.Rusult.Companion.toRusult

fun <T> withRusult(op: () -> T): Rusult<T, Unit> = runCatching(op).toRusult()

fun <T, E> withRusult(op: () -> T, errOp: (Throwable) -> E): Rusult<T, E> = runCatching(op).toRusult(errOp)
