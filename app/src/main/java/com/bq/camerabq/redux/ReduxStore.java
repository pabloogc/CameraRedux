package com.bq.camerabq.redux;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

public final class ReduxStore<S, A> {

  private final FlowableProcessor<S> stateFlowable = BehaviorProcessor.create();
  private final List<Reducer<S, A>> reducers = new ArrayList<>();

  private S state;
  private S lastState;

  public ReduxStore(S state) {
    this.state = state;
    this.lastState = state;
  }

  public void addReducer(Reducer<S, ? extends A> reducer) {
    //noinspection unchecked
    reducers.add((Reducer<S, A>) reducer);
  }

  public void dispatch(A action) {
    lastState = state;
    for (Reducer<S, A> reducer : reducers) {
      state = reducer.reduce(state, action);
    }
    if (state != lastState) stateFlowable.onNext(state);
  }

  public S state() {
    return state;
  }

  public S lastState() {
    return lastState;
  }

  public Flowable<S> flowable() {
    return stateFlowable;
  }
}
