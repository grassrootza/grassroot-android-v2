package za.org.grassroot2.model.enums;

/**
 * Created by luke on 2017/08/16.
 * Used to enable high level of abstraction in uploading, downloading and syncing, while picking
 * the correct upload call and entity type
 */

public enum GrassrootEntityType {

    GROUP,
    PUBLIC_GROUP,

    MEETING,
    VOTE,
    TODO,

    GROUP_JOIN_REQUEST,

    LIVEWIRE_ALERT,
    SAFETY_ALERT,

    MEDIA_FILE

}