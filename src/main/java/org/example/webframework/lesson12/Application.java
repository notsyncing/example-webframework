package org.example.webframework.lesson12;

import org.example.webframework.lesson12.ioc.AnnotationDrivenContainer;
import org.example.webframework.lesson12.ioc.ScanPackages;

@ScanPackages
public class Application {
    public static void main(String[] args) {
        AnnotationDrivenContainer.run(Application.class);
    }
}
