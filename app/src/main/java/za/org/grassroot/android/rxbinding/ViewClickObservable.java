package za.org.grassroot.android.rxbinding;

import android.view.View;
import android.view.View.OnClickListener;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

import static za.org.grassroot.android.rxbinding.Preconditions.checkMainThread;

final class ViewClickObservable extends Observable<Object> {
  private final View view;

  ViewClickObservable(View view) {
    this.view = view;
  }

  @Override protected void subscribeActual(Observer<? super Object> observer) {
    if (!checkMainThread(observer)) {
      return;
    }
    Listener listener = new Listener(view, observer);
    observer.onSubscribe(listener);
    view.setOnClickListener(listener);
  }

  static final class Listener extends MainThreadDisposable implements OnClickListener {
    private final View view;
    private final Observer<? super Object> observer;

    Listener(View view, Observer<? super Object> observer) {
      this.view = view;
      this.observer = observer;
    }

    @Override public void onClick(View v) {
      if (!isDisposed()) {
        observer.onNext(Notification.INSTANCE);
      }
    }

    @Override protected void onDispose() {
      view.setOnClickListener(null);
    }
  }
}
