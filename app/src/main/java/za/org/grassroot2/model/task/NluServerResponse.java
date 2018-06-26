package za.org.grassroot2.model.task;

import java.util.List;

public class NluServerResponse {

    private String conversationUid;

    private NluParseResult result;

    private String dateTimeParsed;

    public NluServerResponse() {
        // for Jackson
    }

    @Override
    public String toString() {
        return "NluServerResponse{" +
                "conversationUid='" + conversationUid + '\'' +
                ", result=" + result +
                ", dateTimeParsed='" + dateTimeParsed + '\'' +
                '}';
    }

    public String getConversationUid() {
        return conversationUid;
    }

    public void setConversationUid(String conversationUid) {
        this.conversationUid = conversationUid;
    }

    public NluParseResult getResult() {
        return result;
    }

    public void setResult(NluParseResult result) {
        this.result = result;
    }

    public String getDateTimeParsed() {
        return dateTimeParsed;
    }

    public void setDateTimeParsed(String dateTimeParsed) {
        this.dateTimeParsed = dateTimeParsed;
    }
}
