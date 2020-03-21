package org.example.webframework.lesson12.ioc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassInfoRegistry {
    private List<ClassInfo> classInfoList = new ArrayList<>();

    public List<ClassInfo> getClassInfoList() {
        return classInfoList;
    }

    public void addClassInfo(ClassInfo info) {
        classInfoList.add(info);
    }

    public List<Class<?>> getListOfType(Class<?> assignableType) {
        return classInfoList.stream()
                .filter(c -> assignableType.isAssignableFrom(c.getType()))
                .map(ClassInfo::getType)
                .collect(Collectors.toList());
    }
}
