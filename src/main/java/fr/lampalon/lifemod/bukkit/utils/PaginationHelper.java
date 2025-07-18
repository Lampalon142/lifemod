package fr.lampalon.lifemod.bukkit.utils;

import java.util.Collections;
import java.util.List;

public class PaginationHelper<T> {
    private final List<T> items;
    private final int itemsPerPage;

    public PaginationHelper(List<T> items, int itemsPerPage) {
        this.items = items;
        this.itemsPerPage = itemsPerPage;
    }

    public int getPageCount() {
        return (int) Math.ceil((double) items.size() / itemsPerPage);
    }

    public List<T> getPage(int page) {
        if (items.isEmpty() || page < 0) return Collections.emptyList();
        int start = page * itemsPerPage;
        if (start >= items.size()) return Collections.emptyList();
        int end = Math.min(start + itemsPerPage, items.size());
        return items.subList(start, end);
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public int getTotalItems() {
        return items.size();
    }
}
