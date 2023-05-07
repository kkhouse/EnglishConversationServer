package kkhouse.com.utils


/**
 * 自作
 * Success,Errorという処理結果を表現する。
 * 以下のように処理をストリームで表現できる。どこかのfunctionで処理が失敗した場合はErrorになり
 * 以降の中間ストリームはスキップされonFailureに流れる。
 *
 * Ex.
 * Resource.ofAsync{ getAnyData() }
 *      .mapAsync(::transformerFunction) // getAnyDataを加工して下流に流す
 *     .tapAsync(::effectFunction) // なんらかの副作用を起こす EX.DB保存などのグローバル更新
 *     .flatMapAsync(::transformerFunctionReturningMonadResult)
 *     .forEachAsync( // 値の取り出し
            onFailure = { error -> chanel.send(errorhoge)},
            onSuccess = { data -> emit(data) },
 *     )
 *
 * 値コンストラクタ（Success,Failure)は二つなのでEitherでも良さそうだが、
 * 「Loading」という型を追加するかもしれないので作成
 */
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
                Failure(AppError.UnKnownError("Resource map function throws exception " +
                        ":  origin error $e: message : ${e.message}"))
            }
            is Failure -> Failure(this.error)

        }
    }

    fun <U> mapNotNull(f: (T) -> U): Resource<U> = map(f).notNull()

    fun <U> flatMap(f: (T) -> Resource<U>): Resource<U> {
        return when(this) {
            is Success -> {
                try { f(this.data) } catch (e: Exception) {
                    Failure(AppError.UnKnownError("flatmapException :  origin error $e: message : ${e.message}")) }
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
    ): Resource<U> {
        return when(this) {
            is Success -> Success(onSuccess(this.data))
            is Failure -> Failure(onFailure(this.error))

        }
    }

    /*　Successの値が条件にマッチするか判定する。FalseのばあいはFailuerを返却する */
    fun filter(
        error: AppError = AppError.UnKnownError("Condition not match"),
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
                Failure(error = AppError.UnKnownError("${e.message}"))
            }
            is Failure -> Failure(this.error)

        }
    }

    suspend fun <U> mapAsync(f: suspend (T) -> U): Resource<U> {
        return when(this) {
            is Success -> try {
                Success(f(this.data))
            } catch (e: Exception) {
                Failure(AppError.UnKnownError("Resource map function throws exception :  origin error $e: message : ${e.message}"))
            }
            is Failure -> Failure(this.error)
        }
    }

    suspend fun <U> flatMapAsync(f: suspend (T) -> Resource<U>): Resource<U> {
        return when(this) {
            is Success -> {
                try { f(this.data) } catch (e: Exception) {
                    Failure(AppError.UnKnownError("flatmapException:  origin error $e: message : ${e.message}")) }
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
                Failure(error = AppError.UnKnownError("${e.message}"))
            }
            is Failure -> Failure(this.error)

        }
    }

    /*  Successの場合は引数の関数を実行する。戻り値は本関数を呼び出す前のResouceとなる */
    suspend fun tapEachAsync(
        onSuccess : suspend (T) -> Unit = {},
        onFailure : suspend (AppError) -> Unit = {},
    ) : Resource<T> {
        return try {
            when(this) {
                is Success ->onSuccess(this.data)
                is Failure -> onFailure(this.error)
            }
            this
        } catch(e: Exception) { Failure(AppError.UnKnownError("")) }
    }

    /* Resourceの文脈を維持させたい時などに使用 */
    fun effect(f : () -> Unit) : Resource<T> {
        return try {
            f()
            this
        } catch(e: Exception) { Failure(AppError.UnKnownError(""))
        }
    }

    suspend fun effectAsync(f: suspend () -> Unit) : Resource<T> {
        return try {
            f()
            this
        } catch (e: Exception) { Failure(AppError.UnKnownError(""))
        }
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
                Failure(AppError.UnKnownError("Resource.of catch Exception ${e.message}"))
            }
        }

        suspend fun <A> ofAsync(f:suspend () -> A): Resource<A> {
            return try {
                Success(f())
            } catch (e: Exception) {
                Failure(AppError.UnKnownError("Resource.ofAsync catch Exception ${e.message}"))
            }
        }

        fun <A,B> lift(f:(A) -> B): (Resource<A>)-> Resource<B> = { ra -> ra.map(f)}
        fun <A,B,C> lift2(f: (A) -> (B) -> C) : (Resource<A>) -> (Resource<B>) -> Resource<C> =
            { ra ->
                { rb ->
                    ra.flatMap { a -> rb.map { b -> f(a)(b) } }
                }
            }
        fun <A,B,C> map2(ra: Resource<A>, rb: Resource<B>, f:(A) -> (B) -> C): Resource<C> = lift2(f)(ra)(rb)
    }


    fun <A> Resource<A?>.notNull() : Resource<A> = ifNull(Failure(AppError.UnKnownError("data is null")))

    fun <A> Resource<A?>.ifNull(ra: Resource<A>) : Resource<A> {
        return when(this) {
            is Success -> {
                if(this.data == null) {
                    ra
                } else {
                    Success(this.data)
                }
            }
            is Failure -> Failure(this.error)
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