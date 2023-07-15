package fr.loot1.quill.objects;

import java.util.List;

public class ArchivedBooksList {
    private final List<ArchivedBook> archivedBooks;
    private final int archivedBooksCount;

    public ArchivedBooksList(List<ArchivedBook> list, int count) {
        this.archivedBooks = list;
        this.archivedBooksCount = count;
    }

    public List<ArchivedBook> getData() {
        return archivedBooks;
    }

    public int getCount() {
        return archivedBooksCount;
    }
}