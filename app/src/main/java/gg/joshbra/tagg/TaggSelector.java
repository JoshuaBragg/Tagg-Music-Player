package gg.joshbra.tagg;

import android.support.annotation.NonNull;

/**
 * Stores a Tagg and checkbox pair
 */
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

    public void toggleChecked() {
        checked = !checked;
    }

    @Override
    public int compareTo(@NonNull TaggSelector taggSelector) {
        return taggName.compareTo(taggSelector.getTaggName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TaggSelector && taggName.equals(((TaggSelector) obj).getTaggName());
    }
}
