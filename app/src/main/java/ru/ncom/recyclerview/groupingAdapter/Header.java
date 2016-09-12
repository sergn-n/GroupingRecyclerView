package ru.ncom.recyclerview.groupingAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ника-Ком on 12.09.2016.
 */
public class Header<T extends Titled> implements Titled {

    private String title = null;
    private boolean isCollapsed = false;
    private final List<T> childList = new ArrayList<>();

    public Header(String title) {
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
