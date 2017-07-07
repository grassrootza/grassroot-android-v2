/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package za.org.grassroot.android.rxbinding;

import android.view.KeyEvent;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import io.reactivex.functions.Predicate;

import static za.org.grassroot.android.rxbinding.Preconditions.checkMainThread;

/**
 * note: derived from RxBinding, copied to avoid minSdk issues
 */

final class TextViewEditorActionEventObservable extends Observable<TextViewEditorActionEvent> {
    private final TextView view;
    private final Predicate<? super TextViewEditorActionEvent> handled;

    TextViewEditorActionEventObservable(TextView view,
                                        Predicate<? super TextViewEditorActionEvent> handled) {
        this.view = view;
        this.handled = handled;
    }

    @Override
    protected void subscribeActual(Observer<? super TextViewEditorActionEvent> observer) {
        if (!checkMainThread(observer)) {
            return;
        }
        Listener listener = new Listener(view, observer, handled);
        observer.onSubscribe(listener);
        view.setOnEditorActionListener(listener);
    }

    static final class Listener extends MainThreadDisposable implements TextView.OnEditorActionListener {
        private final TextView view;
        private final Observer<? super TextViewEditorActionEvent> observer;
        private final Predicate<? super TextViewEditorActionEvent> handled;

        Listener(TextView view, Observer<? super TextViewEditorActionEvent> observer,
                 Predicate<? super TextViewEditorActionEvent> handled) {
            this.view = view;
            this.observer = observer;
            this.handled = handled;
        }

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            TextViewEditorActionEvent event = TextViewEditorActionEvent.create(view, actionId, keyEvent);
            try {
                if (!isDisposed() && handled.test(event)) {
                    observer.onNext(event);
                    return true;
                }
            } catch (Exception e) {
                observer.onError(e);
                dispose();
            }
            return false;
        }

        @Override
        protected void onDispose() {
            view.setOnEditorActionListener(null);
        }
    }
}