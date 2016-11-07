package com.bq.camerabq

import android.support.annotation.VisibleForTesting
import android.util.Log
import com.bq.camerabq.redux.Reducer
import com.bq.camerabq.redux.ReduxStore
import com.bq.camerabq.state.Action
import com.bq.camerabq.state.AppState
import io.reactivex.Flowable
import timber.log.Timber

object Store {

    private lateinit var reduxStore: ReduxStore<AppState, Action>

    fun init(initialState: AppState) {
        reduxStore = ReduxStore<AppState, Action>(initialState)
    }

    @VisibleForTesting
    fun get(): ReduxStore<AppState, Action> {
        return reduxStore
    }

    fun addReducer(reducer: Reducer<AppState, out Action>) {
        reduxStore.addReducer(reducer)
    }

    fun dispatch(action: Action) {
        Timber.d("Dispatch: %s" ,action.toString())
        reduxStore.dispatch(action)
    }

    val state: AppState
        get() {
            return reduxStore.state()
        }

    val lastState: AppState
        get() {
            return reduxStore.lastState()
        }

    fun flowable(): Flowable<AppState> {
        return reduxStore.flowable()
    }

    fun <T> flowable(mapFun: (AppState) -> T): Flowable<T> {
        return flowable()
                //Listen for changes on the specified field only
                .filter { state -> mapFun(lastState) != mapFun(state) }
                .map(mapFun)
    }
}
