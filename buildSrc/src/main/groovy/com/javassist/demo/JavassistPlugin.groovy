package com.javassist.demo

import org.gradle.api.Plugin
import org.gradle.api.Project

class JavassistPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.tasks.create('CreatePersonTask', CreatePersonTask.class)
    }
}