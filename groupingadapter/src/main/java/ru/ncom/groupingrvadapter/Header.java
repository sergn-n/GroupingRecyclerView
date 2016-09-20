package ru.ncom.groupingrvadapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Default header
 */
public class Header<T extends Titled> extends TitledSelectableItem {

    private boolean isCollapsed = false;
    private final List<T> childList = new ArrayList<>();

    public Header(String title) {
        super(title);
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(boolean isCollapsed){
        this.isCollapsed = isCollapsed;
    }

    public List<T> getChildItemList() {
        return childList;
    }
}
