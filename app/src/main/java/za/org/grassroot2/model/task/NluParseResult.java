package za.org.grassroot2.model.task;

import java.util.List;

public class NluParseResult {

    private String originalText;

    private LanguageIntent intent;

    private List<LanguageEntity> entities;

    public String getOriginalText() {
        return originalText;
    }


    @Override
    public String toString() {
        return "NluParseResult{" +
                "originalText='" + originalText + '\'' +
                ", intent=" + intent.toString() +
                ", entiities=" + entities +
                '}';
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public LanguageIntent getIntent() {
        return intent;
    }

    public void setIntent(LanguageIntent intent) {
        this.intent = intent;
    }

    public List<LanguageEntity> getEntities() {
        return entities;
    }

    public void setEntiities(List<LanguageEntity> entiities) {
        this.entities = entiities;
    }
}
