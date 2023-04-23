
sealed class Resource<out T> {
    data class Success<T>(
        val data : T,
    ) : Resource<T>()
    data class Failure<T>(
        val error : AppError,
    ) : Resource<T>()

    fun <U> map(f: (T) -> U): Resource<U> {
        return when(this) {
            is Success -> try {
                Success(f(this.data))
            } catch (e: Exception) {
                Failure(AppError.IllegalError("Resource map function throws exception"))
            }
            is Failure -> Failure(this.error)

        }
    }

    fun <U> mapNotNull(f: (T) -> U): Resource<U> = map(f).notNull()

    fun <U> flatMap(f: (T) -> Resource<U>): Resource<U> {
        return when(this) {
            is Success -> {
                try { f(this.data) } catch (e: Exception) {Failure(AppError.IllegalError("flatmapException")) }
            }
            is Failure -> Failure(this.error)
        }
    }

    fun mapFailure(f: (AppError) -> AppError): Resource<T> {
        return when(this) {
            is Success -> Success(this.data)
            is Failure -> Failure(f(this.error))

        }
    }

    fun <U> mapEach(
        onSuccess: (T) -> U,
        onFailure: (AppError) -> AppError
    ):  Resource<U> {
        return when(this) {
            is Success -> Success(onSuccess(this.data))
            is Failure -> Failure(onFailure(this.error))

        }
    }

    /*　Successの値が条件にマッチするか判定する。FalseのばあいはFailuerを返却する */
    fun filter(
        error: AppError = AppError.IllegalError("Condition not match"),
        p: (T) -> Boolean
    ) : Resource<T> {
        return flatMap {
            when(p(it)) {
                true -> this
                else -> Failure(error = error)
            }
        }
    }

    /*  Successの場合は引数の関数を実行する。戻り値は本関数を呼び出す前のResouceとなる */
    fun tap(f:(T) -> Unit) : Resource<T> {
        return when(this) {
            is Success -> try {
                f(this.data)
                this
            }catch (e: Exception) {
                Failure(error = AppError.IllegalError("${e.message}"))
            }
            is Failure -> Failure(this.error)

        }
    }

    suspend fun <U> mapAsync(f: suspend (T) -> U): Resource<U> {
        return when(this) {
            is Success -> try {
                Success(f(this.data))
            } catch (e: Exception) {
                Failure(AppError.IllegalError("Resource map function throws exception"))
            }
            is Failure -> Failure(this.error)
        }
    }

    suspend fun <U> flatMapAsync(f: suspend (T) -> Resource<U>): Resource<U> {
        return when(this) {
            is Success -> {
                try { f(this.data) } catch (e: Exception) {Failure(AppError.IllegalError("flatmapException")) }
            }
            is Failure -> Failure(this.error)

        }
    }

    suspend fun mapFailureAsync(f: suspend (AppError) -> AppError): Resource<T> {
        return when(this) {
            is Success -> Success(this.data)
            is Failure -> Failure(f(this.error))

        }
    }

    /*  Successの場合は引数の関数を実行する。戻り値は本関数を呼び出す前のResouceとなる */
    suspend fun tapAsync(f: suspend (T) -> Unit) : Resource<T> {
        return when(this) {
            is Success -> try {
                f(this.data)
                this
            }catch (e: Exception) {
                Failure(error = AppError.IllegalError("${e.message}"))
            }
            is Failure -> Failure(this.error)

        }
    }

    /*  Successの場合は引数の関数を実行する。戻り値は本関数を呼び出す前のResouceとなる */
    suspend fun tapEachAsync(
        onSuccess : suspend (T) -> Unit = {},
        onFailure : suspend (AppError) -> Unit = {},
        onLoading : suspend () -> Unit = {}
    ) : Resource<T> {
        return try {
            when(this) {
                is Success ->onSuccess(this.data)
                is Failure -> onFailure(this.error)
                else -> onLoading()
            }
            this
        }catch(e: Exception) { Failure(AppError.IllegalError(""))}
    }

    /* Resourceの文脈を維持させたい時などに使用 */
    fun effect(f : () -> Unit) : Resource<T> {
        return try {
            f()
            this
        } catch(e: Exception) { Failure(AppError.IllegalError(""))}
    }

    suspend fun effectAsync(f: suspend () -> Unit) : Resource<T> {
        return try {
            f()
            this
        } catch (e: Exception) { Failure(AppError.IllegalError(""))}
    }

    fun getDataOrNull(): T? {
        return when(this) {
            is Success -> this.data
            else -> null
        }
    }

    fun getErrorOrElse(defaultValue : AppError?) : AppError? {
        return when(this) {
            is Failure -> this.error
            else -> defaultValue
        }
    }

    fun getErrorOrNull(): AppError? = getErrorOrElse(defaultValue = null)

    fun isSuccess() : Boolean = this is Success
    fun isFailuer() : Boolean = this is Failure
    fun isFailuerOf(vararg appErrors: AppError) : Boolean {
        return when(this) {
            is Failure -> this.error.isIn(*appErrors)
            else -> false
        }
    }
    fun <A> A.isIn(vararg xs: A) : Boolean {
        return xs.contains(this)
    }
    fun isNotFailuerOf(vararg appErrors: AppError) = isFailuerOf(*appErrors).not()
    fun onFailure(f: (AppError) -> Unit) {
        return when(this) {
            is Failure -> f(this.error)
            else -> Unit
        }
    }

    suspend fun onFailuerAsync(f: suspend (AppError) -> Unit) {
        return when(this) {
            is Failure -> f(this.error)
            else -> Unit
        }
    }

    companion object {
        fun <A> of(f: () -> A): Resource<A> {
            return try {
                Success(f())
            } catch (e: Exception) {
                Failure(AppError.IllegalError(""))
            }
        }

        suspend fun <A> ofAsync(f:suspend () -> A): Resource<A> {
            return try {
                Success(f())
            } catch (e: Exception) {
                Failure(AppError.IllegalError(""))
            }
        }

        fun <A,B> lift(f:(A) -> B): (Resource<A>)-> Resource<B>  = {ra -> ra.map(f)}
        fun <A,B,C> lift2(f: (A) -> (B) -> C) : (Resource<A>) -> (Resource<B>) -> Resource<C> =
            { ra ->
                { rb ->
                    ra.flatMap { a -> rb.map { b -> f(a)(b) } }
                }
            }
        fun <A,B,C> map2(ra:Resource<A>, rb:Resource<B>, f:(A) -> (B) -> C): Resource<C> =lift2(f)(ra)(rb)
    }


    fun <A> Resource<A?>.notNull() : Resource<A> = ifNull(Resource.Failure(AppError.IllegalError("data is null")))

    fun <A> Resource<A?>.ifNull(ra: Resource<A>) : Resource<A> {
        return when(this) {
            is Resource.Success -> {
                if(this.data == null) {
                    ra
                } else {
                    Resource.Success(this.data)
                }
            }
            is Resource.Failure -> Resource.Failure(this.error)
        }
    }
}

fun <A> Resource<A>.forEach(
    onSuccess: (A) -> Unit = {},
    onFailure: (AppError) -> Unit = {},
) {
    return when(this) {
        is Resource.Success -> onSuccess(this.data)
        is Resource.Failure -> onFailure(this.error)
    }
}

suspend fun <A> Resource<A>.forEachAsync(
    onSuccess: suspend (A) -> Unit = {},
    onFailure: suspend (AppError) -> Unit = {},
) {
    return when(this) {
        is Resource.Success -> onSuccess(this.data)
        is Resource.Failure -> onFailure(this.error)
    }
}

sealed class AppError {
    object HogeError : AppError()
    data class IllegalError(val message: String) : AppError()
}