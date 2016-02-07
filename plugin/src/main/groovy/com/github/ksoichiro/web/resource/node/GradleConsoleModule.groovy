package com.github.ksoichiro.web.resource.node

import io.apigee.trireme.core.NodeModule
import io.apigee.trireme.core.NodeRuntime
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.annotations.JSFunction

import java.lang.reflect.InvocationTargetException

import static io.apigee.trireme.core.ArgUtils.stringArg

/**
 * Utility to log from JavaScript using println in Gradle.
 * <p>
 * With console.log() on Trireme (or Rhino), log is not printed as expected:
 * <pre>
 *     :webResourceCompileCoffeeScript
 *     :webResourceCompileLessLESS: start: app.less
 *     LESS: start: a1.less
 * </pre>
 * 'LESS: start: app.less' from console.log() should insert new line
 * before it's printed.
 * To avoid this, use Gradle's println instead of console.log().
 * <p>
 * Usage:
 * <pre>
 *     var gconsole = require('gradle-console');
 *     gconsole.log('Hello, world!');
 * </pre>
 */
class GradleConsoleModule implements NodeModule {
    @Override
    String getModuleName() {
        return 'gradle-console'
    }

    @Override
    Scriptable registerExports(Context cx, Scriptable global, NodeRuntime runtime) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        ScriptableObject.defineClass(global, GradleConsoleModuleImpl.class)
        (GradleConsoleModuleImpl) cx.newObject(global, GradleConsoleModuleImpl.CLASS_NAME)
    }

    static class GradleConsoleModuleImpl extends ScriptableObject {
        public static final String CLASS_NAME = "_gradleConsoleModuleClass"

        @Override
        String getClassName() {
            return CLASS_NAME
        }

        @JSFunction
        @SuppressWarnings("unused")
        public static void log(Context cx, Scriptable thisObj, Object[] args, Function func) {
            println stringArg(args, 0)
        }
    }
}
