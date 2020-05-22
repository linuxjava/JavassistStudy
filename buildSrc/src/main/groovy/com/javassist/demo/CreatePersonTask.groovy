package com.javassist.demo

import javassist.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.lang.reflect.Method
import java.lang.reflect.Modifier

class CreatePersonTask extends DefaultTask {

    @TaskAction
    void action() {
        createInterface()
        createClass();
        modify()
    }

    private void createInterface(){
        ClassPool classPool = ClassPool.getDefault();

        CtClass cc = classPool.makeInterface('com.javassist.demo.IPerson')

        CtMethod method = CtNewMethod.make("void setName(String name);", cc);
        cc.addMethod(method);

        method = CtNewMethod.make("String getName();", cc);
        cc.addMethod(method);

        method = CtNewMethod.make("void printName();", cc);
        cc.addMethod(method);

        cc.writeFile(getProject().getBuildDir().absolutePath + "/intermediates/classes/debug");
    }

    private void createClass(){
        ClassPool classPool = ClassPool.getDefault();
        // 1. 创建一个空类
        CtClass cc = classPool.makeClass("com.javassist.demo.Person")

        // 2. 新增一个字段 private String name;
        // 字段名为name
        CtField nameField = new CtField(classPool.get('java.lang.String'), 'name', cc)
        // 访问级别是 private
        nameField.setModifiers(Modifier.PRIVATE)
        // 初始值是 "xiaoming"
        cc.addField(nameField, CtField.Initializer.constant('xiaoming'))

        // 3. 生成 getter、setter 方法
        cc.addMethod(CtNewMethod.setter("setName", nameField));
        cc.addMethod(CtNewMethod.getter("getName", nameField));

        // 4. 添加无参的构造函数
        CtConstructor constructor = CtNewConstructor.make("public GeneratedClass(){}", cc);
        cc.addConstructor(constructor);

        // 5. 添加有参的构造函数
        constructor = CtNewConstructor.make("public GeneratedClass(String name){this.name = name;}", cc);
        cc.addConstructor(constructor);

        // 6. 创建一个名为printName方法，无参数，无返回值，输出name值
        CtMethod helloM = CtNewMethod.make("public void printName(){ System.out.println(this.name);}", cc);
        cc.addMethod(helloM);

        //7.通过反射的方式调用
        //invokeByReflect(cc)

        //8.通过读取.class 文件的方式调用
        //invokeByClass(classPool)

        //9.通过接口的方式
        invokeByInterface()

        //10.这里会将这个创建的类对象编译为.class文件
        cc.writeFile(getProject().getBuildDir().absolutePath + "/intermediates/classes/debug");
    }

    /**
     *  通过反射的方式调用
     *  注:invokeByReflect与invokeByClass不能同时调用
     * @param cc
     */
    private void invokeByReflect(CtClass cc){
        // 这里不写入文件，直接实例化
        Object person = cc.toClass().newInstance();
        // 设置值
        Method setName = person.getClass().getMethod("setName", String.class);
        setName.invoke(person, "反射调用");
        // 输出值
        Method execute = person.getClass().getMethod("printName");
        execute.invoke(person);
    }

    /**
     * 通过读取.class 文件的方式调用
     * @param cc
     */
    private void invokeByClass(ClassPool pool){
        //ClassPool pool = ClassPool.getDefault();
        // 设置类路径
        //pool.appendClassPath(getProject().getBuildDir().absolutePath + "/intermediates/classes/debug");
        CtClass ctClass = pool.get("com.javassist.demo.Person");
        Object person = ctClass.toClass().newInstance();
        // 设置值
        Method setName = person.getClass().getMethod("setName", String.class);
        setName.invoke(person, "通过读取.class 文件的方式调用");
        // 输出值
        Method execute = person.getClass().getMethod("printName");
        execute.invoke(person);
    }

    /**
     * 通过接口的方式
     * @param pool
     */
    private void invokeByInterface(){
        ClassPool pool = ClassPool.getDefault();
        // 设置类路径
        pool.appendClassPath(getProject().getBuildDir().absolutePath + "/intermediates/classes/debug");

        // 获取接口
        CtClass codeClassI = pool.get("com.javassist.demo.IPerson");
        // 获取上面生成的类
        CtClass ctClass = pool.get("com.javassist.demo.Person");
        // 使代码生成的类，实现 PersonI 接口
        CtClass[] interfaces = [codeClassI];
        ctClass.setInterfaces(interfaces);

        // 以下通过接口直接调用 强转
        IPerson person = (IPerson)ctClass.toClass().newInstance();
        System.out.println(person.getName());
        person.setName("xiaolv");
        person.printName();
    }

    /**
     * 动态修改源码中java类
     */
    private void modify(){
        ClassPool pool = ClassPool.getDefault();
        // 设置类路径
        pool.appendClassPath(getProject().getBuildDir().absolutePath + "/intermediates/classes/debug");
        CtClass cc = pool.get("com.javassist.demo.Test");

        CtMethod personFly = cc.getDeclaredMethod("print");
        personFly.insertBefore("System.out.println(\"起飞之前准备降落伞\");");
        personFly.insertAfter("System.out.println(\"成功落地。。。。\");");

        //修改修改父类
        cc = pool.get("com.javassist.demo.TestActivity");
        cc.replaceClassName("android.app.Activity", "com.tencent.shadow.core.runtime.ShadowActivity");
        cc.writeFile(getProject().getBuildDir().absolutePath + "/intermediates/classes/debug");
    }
}
























