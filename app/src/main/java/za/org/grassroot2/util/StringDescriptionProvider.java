package za.org.grassroot2.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import javax.inject.Inject;

import za.org.grassroot2.R;

/**
 * Created by qbasso on 20.09.2017.
 */

public class StringDescriptionProvider {

    private final Context context;

    @Inject
    public StringDescriptionProvider(Context c) {
        context = c;
    }

    public String getLivewireMediaPrompt() {
        return context.getString(R.string.lwire_media_prompt);
    }

    public String getLivewireConfirmTextMedia(String headline, String groupName, String mediaType) {
        return context.getString(R.string.lwire_confirm_text_media, headline, groupName, mediaType);
    }

    public String getLivewireConfirmNoMedia(String headline, String groupName) {
        return context.getString(R.string.lwire_confirm_text_no_media, headline, groupName);
    }

    public boolean isEmpty(String s) {
        return TextUtils.isEmpty(s);
    }

}
