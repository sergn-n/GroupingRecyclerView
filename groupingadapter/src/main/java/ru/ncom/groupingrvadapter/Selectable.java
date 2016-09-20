package ru.ncom.groupingrvadapter;

/**
 * Implement it when an item should remember its selection state in RecyclerView.
 */
public interface Selectable {
    boolean isSelected();

    void setSelected(boolean selected);
}
