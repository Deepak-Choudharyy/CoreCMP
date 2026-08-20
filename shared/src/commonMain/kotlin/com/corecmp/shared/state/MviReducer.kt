package com.corecmp.shared.state

typealias Reducer<S, A> = (S, A) -> S

fun <S, A> reduce(state: S, action: A, reducer: Reducer<S, A>): S =
    reducer(state, action)

class MviReducer<S, A>(private val reducer: Reducer<S, A>) {
    operator fun invoke(state: S, action: A): S = reduce(state, action, reducer)
}
