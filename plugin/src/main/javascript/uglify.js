// This script replaces UglifyJS to use browserify.

var path = require("path");
var fs = require("fs");
var vm = require("vm");
var sys = require("util");

var UglifyJS = vm.createContext({
    sys           : sys,
    console       : console,
    MOZ_SourceMap : require("source-map")
});

// Original script reads files at runtime,
// which interrupts browserify merging files into one file.
// This version read JavaScript content as string
// which are defined in this file later.
function load_global(code, file) {
    try {
        return vm.runInContext(code, UglifyJS, file);
    } catch(ex) {
        // XXX: in case of a syntax error, the message is kinda
        // useless. (no location information).
        console.error("ERROR in file: " + file + " / " + ex);
        process.exit(1);
    }
};

exports.minify = function(files, options) {
    options = UglifyJS.defaults(options, {
        outSourceMap : null,
        sourceRoot   : null,
        inSourceMap  : null,
        fromString   : false,
        warnings     : false,
        mangle       : {},
        output       : null,
        compress     : {}
    });
    if (typeof files == "string")
        files = [ files ];

    // 1. parse
    var toplevel = null;
    files.forEach(function(file){
        var code = options.fromString
            ? file
            : fs.readFileSync(file, "utf8");
        toplevel = UglifyJS.parse(code, {
            filename: options.fromString ? "?" : file,
            toplevel: toplevel
        });
    });

    // 2. compress
    if (options.compress) {
        var compress = { warnings: options.warnings };
        UglifyJS.merge(compress, options.compress);
        toplevel.figure_out_scope();
        var sq = UglifyJS.Compressor(compress);
        toplevel = toplevel.transform(sq);
    }

    // 3. mangle
    if (options.mangle) {
        toplevel.figure_out_scope();
        toplevel.compute_char_frequency();
        toplevel.mangle_names(options.mangle);
    }

    // 4. output
    var inMap = options.inSourceMap;
    var output = {};
    if (typeof options.inSourceMap == "string") {
        inMap = fs.readFileSync(options.inSourceMap, "utf8");
    }
    if (options.outSourceMap) {
        output.source_map = UglifyJS.SourceMap({
            file: options.outSourceMap,
            orig: inMap,
            root: options.sourceRoot
        });
    }
    if (options.output) {
        UglifyJS.merge(output, options.output);
    }
    var stream = UglifyJS.OutputStream(output);
    toplevel.print(stream);
    return {
        code : stream + "",
        map  : output.source_map + ""
    };
};

load_global(fs.readFileSync("./uglifyjs-lib.js", "utf8"), "uglifyjs-lib.js");

UglifyJS.AST_Node.warn_function = function(txt) {
  console.error("WARN: " + txt);
};
