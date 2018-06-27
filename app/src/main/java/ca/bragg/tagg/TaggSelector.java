package ca.bragg.tagg;

import android.support.annotation.NonNull;

public class TaggSelector implements Comparable<TaggSelector> {
    private String taggName;
    private boolean checked;

    public TaggSelector(String taggName, boolean checked) {
        this.taggName = taggName;
        this.checked = checked;
    }

    public String getTaggName() {
        return taggName;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public int compareTo(@NonNull TaggSelector taggSelector) {
        return taggName.compareTo(taggSelector.getTaggName());
    }
}
