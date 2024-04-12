package com.theendercore

import arrow.core.Either
import arrow.core.getOrElse

fun <R> catchAndLog(f: () -> R) = Either.catch(f).getOrElse { log.info(it.message) }
