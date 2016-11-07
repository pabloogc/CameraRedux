package com.bq.camerabq.redux;


import com.bq.camerabq.state.Action;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface public interface Reducer<S, A> {
  @NotNull S reduce(@NotNull S state, @NotNull A action);
}