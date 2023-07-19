package fr.loot1.quill.objects;

import java.util.List;

public class ApplicationList {
    private final List<Application> applications;
    private final int applicationCount;

    public ApplicationList(List<Application> list, int count) {
        this.applications = list;
        this.applicationCount = count;
    }

    public List<Application> getData() {
        return applications;
    }

    public int getCount() {
        return applicationCount;
    }
}