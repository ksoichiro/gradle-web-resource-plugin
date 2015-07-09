package com.github.ksoichiro.web.resource

class WebResourceWatchTask extends WebResourceCompileTask {
    static final String NAME = "webResourceWatch"
    WebResourceWatchTask() {
        gulpCommand = 'watch'
    }
}
