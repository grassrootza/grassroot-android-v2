package za.org.grassroot.android.rxbinding;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

import static za.org.grassroot.android.rxbinding.Preconditions.checkNotNull;

/**
 * Static factory methods for creating {@linkplain Observable observables} and {@linkplain Consumer
 * actions} for {@link TextView}.
 */
public final class RxTextView {
  /**
   * Create an observable of editor actions on {@code view}.
   * <p>
   * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
   * to free this reference.
   * <p>
   * <em>Warning:</em> The created observable uses {@link TextView.OnEditorActionListener} to
   * observe actions. Only one observable can be used for a view at a time.
   */
  @CheckResult @NonNull
  public static Observable<Integer> editorActions(@NonNull TextView view) {
    checkNotNull(view, "view == null");
    return editorActions(view, Functions.PREDICATE_ALWAYS_TRUE);
  }

  /**
   * Create an observable of editor actions on {@code view}.
   * <p>
   * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
   * to free this reference.
   * <p>
   * <em>Warning:</em> The created observable uses {@link TextView.OnEditorActionListener} to
   * observe actions. Only one observable can be used for a view at a time.
   *
   * @param handled Predicate invoked each occurrence to determine the return value of the
   * underlying {@link TextView.OnEditorActionListener}.
   */
  @CheckResult @NonNull
  public static Observable<Integer> editorActions(@NonNull TextView view,
                                                  @NonNull Predicate<? super Integer> handled) {
    checkNotNull(view, "view == null");
    checkNotNull(handled, "handled == null");
    return new TextViewEditorActionObservable(view, handled);
  }

  /**
   * Create an observable of editor action events on {@code view}.
   * <p>
   * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
   * to free this reference.
   * <p>
   * <em>Warning:</em> The created observable uses {@link TextView.OnEditorActionListener} to
   * observe actions. Only one observable can be used for a view at a time.
   */
  @CheckResult @NonNull
  public static Observable<TextViewEditorActionEvent> editorActionEvents(@NonNull TextView view) {
    checkNotNull(view, "view == null");
    return editorActionEvents(view, Functions.PREDICATE_ALWAYS_TRUE);
  }

  /**
   * Create an observable of editor action events on {@code view}.
   * <p>
   * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
   * to free this reference.
   * <p>
   * <em>Warning:</em> The created observable uses {@link TextView.OnEditorActionListener} to
   * observe actions. Only one observable can be used for a view at a time.
   *
   * @param handled Predicate invoked each occurrence to determine the return value of the
   * underlying {@link TextView.OnEditorActionListener}.
   */
  @CheckResult @NonNull
  public static Observable<TextViewEditorActionEvent> editorActionEvents(@NonNull TextView view,
                                                                         @NonNull Predicate<? super TextViewEditorActionEvent> handled) {
    checkNotNull(view, "view == null");
    checkNotNull(handled, "handled == null");
    return new TextViewEditorActionEventObservable(view, handled);
  }
}
