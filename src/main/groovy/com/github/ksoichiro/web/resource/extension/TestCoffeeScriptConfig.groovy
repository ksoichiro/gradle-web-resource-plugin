package com.github.ksoichiro.web.resource.extension

class TestCoffeeScriptConfig extends FilterableProcessor{
    boolean copyBowerDependencies

    TestCoffeeScriptConfig(String src, String dest, List<String> include, List<String> exclude) {
        super(src, dest, include, exclude, [])
        this.copyBowerDependencies = false
    }
}
