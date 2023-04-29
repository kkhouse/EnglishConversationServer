import kkhouse.com.utils.AppError
import kkhouse.com.utils.Resource

/*
ResultをResourceに変換する
 */
fun <T> Result<T>.toResource(
    mapAppError: (Throwable) -> AppError = {
        AppError.UnKnownError(
            it.message ?: "Error On Result to Resource Converter "
        )
    }
): Resource<T> {
    return this.fold(
        onSuccess = { Resource.Success(it) },
        onFailure = { Resource.Failure(mapAppError(it)) }
    )
}

/**
 * Result<A>とResult<B>を合成する
 * いずれかFailureの場合はthrowable.messageをとりだして組み合わせたログを作成して返す
 *  message以外の情報は捨ててしまうので注意　
 */
fun <A, B, R> Result<A>.combineResult(
    resultB: Result<B>,
    transformer: (A, B) -> R,
) : Result<R> {
    return when {
        this.isSuccess && resultB.isSuccess -> {
            // どちらも成功の時のみ結果を合成する
            this.fold(
                onSuccess = { dataA ->
                    resultB.fold(
                        onSuccess = { dataB -> Result.success(transformer(dataA, dataB)) },
                        onFailure = { Result.failure(IllegalStateException("combining unexpected impl error")) }
                    )
                },
                onFailure = { Result.failure(IllegalStateException("combining unexpected impl error")) }
            )
        }
        else -> {
            Result.failure(
                IllegalStateException(
                    "combining each result error: " +
                            "resultA : ${this.exceptionOrNull()?.message} ," +
                            "resultB : ${resultB.exceptionOrNull()?.message} ,"
                )
            )
        }
    }
}

/**
 * Result<A> Result<B> Result<C> をzipする
 */
fun <A,B,C,R,R2> Result<A>.zip3Result(
    resultB: Result<B>,
    resultC: Result<C>,
    transformerWithAB: (A, B) -> R,
    transformerWithC: (R, C) -> R2
): Result<R2> = this.combineResult(resultB, transformerWithAB).combineResult(resultC, transformerWithC)