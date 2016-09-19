package ru.ncom.groupingrvadapter;

/**
 * Has Title and Selected properties
 */
public interface Titled {
    // title
    String getTitle();
    void setTitle(String title);
    // selected
    boolean isSelected();
    void setSelected(boolean selected);
}
