package fr.loot1.quill.objects;

import java.util.List;

public class ApplicationList {
    private final List<Application> applications;
    private final int archivedBooksCount;

    public ApplicationList(List<Application> list, int count) {
        this.applications = list;
        this.archivedBooksCount = count;
    }

    public List<Application> getData() {
        return applications;
    }

    public int getCount() {
        return archivedBooksCount;
    }
}