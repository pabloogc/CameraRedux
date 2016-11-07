package com.bq.camerabq.redux;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class MapReducer<S, A> implements Reducer<S, A> {

  private final HashMap<Class<?>, List<Reducer<S, A>>> actionMap = new HashMap<>();

  @NotNull @Override public S reduce(@NotNull S state, @NotNull A action) {
    List<Reducer<S, A>> reducers = actionMap.get(action.getClass());
    if (reducers == null) return state;
    for (Reducer<S, A> reducer : reducers) {
      state = reducer.reduce(state, action);
    }
    return state;
  }

  @SuppressWarnings("unchecked")
  public <T> void registerReducer(Class<T> actionType, Reducer<S, T> reducer) {
    List<Reducer<S, A>> reducers = actionMap.get(actionType);
    if (reducers == null) {
      reducers = new ArrayList<>();
      reducers.add((Reducer<S, A>) reducer);
      actionMap.put(actionType, reducers);
    } else {
      reducers.add((Reducer<S, A>) reducer);
    }
  }
}
