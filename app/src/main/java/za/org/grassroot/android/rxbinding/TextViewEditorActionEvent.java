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

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.widget.TextView;

import com.google.auto.value.AutoValue;

/**
 * Created by luke on 2017/07/07.
 */

@AutoValue
public abstract class TextViewEditorActionEvent {

    @CheckResult
    @NonNull
    public static TextViewEditorActionEvent create(@NonNull TextView view, int actionId,
                                                   @Nullable KeyEvent keyEvent) {
        return new AutoValue_TextViewEditorActionEvent(view, actionId, keyEvent);
    }

    TextViewEditorActionEvent() {
    }

    /** The view from which this event occurred. */
    @NonNull public abstract TextView view();
    public abstract int actionId();
    @Nullable public abstract KeyEvent keyEvent();
}

