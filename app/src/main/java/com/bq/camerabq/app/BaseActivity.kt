package com.bq.camerabq.app


import android.support.annotation.CallSuper
import android.support.v4.app.FragmentActivitygi

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*

open class BaseActivity : FragmentActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val pauseActions = ArrayList<Pair<Any, Any.() -> Unit>>()

    @CallSuper
    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
        pauseActions.forEach { it.second(it.first) }
        pauseActions.clear()
    }

    protected fun Disposable.track() {90
        compositeDisposable.add(this)
    }

    protected fun <T : Any> T.now(f: T.() -> Unit): DeferAction<T> {
        this.f()
        return DeferAction(this)
    }

    inner class DeferAction<out T : Any>(val receiver: T) {
        infix fun onPause(g: T.() -> Unit) {
            @Suppress("UNCHECKED_CAST")
            pauseActions.add(receiver to g as Any.() -> Unit)
        }
    }
}
