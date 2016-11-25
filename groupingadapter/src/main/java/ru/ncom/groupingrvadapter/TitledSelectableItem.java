package ru.ncom.groupingrvadapter;

import java.io.Serializable;

/**
 * Convenience class, item stub
 */
public class TitledSelectableItem implements Titled, Selectable, Serializable{

    private String title = null;
    private transient boolean isSelected = false;

    public TitledSelectableItem(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String name) {
        this.title = name;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
