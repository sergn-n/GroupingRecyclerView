package ru.ncom.groupingrvadapter;

/**
 * Convenience class, TitledItem stub
 */
public class TitledItem implements Titled{

    private String title = null;
    private boolean isSelected = false;

    public TitledItem(String title) {
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
