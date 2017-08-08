package za.org.grassroot.android.dagger.user;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by luke on 2017/08/08.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface UserScope {
}
