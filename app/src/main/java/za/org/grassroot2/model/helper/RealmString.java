package za.org.grassroot2.model.helper;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by luke on 2017/08/17.
 */
@RealmClass
public class RealmString extends RealmObject {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealmString that = (RealmString) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
