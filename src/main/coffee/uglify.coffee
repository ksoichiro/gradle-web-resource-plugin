# This script replaces UglifyJS to use browserify.
# Do not rename this file to uglifyjs.js,
# because when uglify-js is executed from npm scripts,
# node_modules/.bin/uglifyjs is invoked
# and uglifyjs.js will hide it, which will break build.

path = require "path"
fs = require "fs"
vm = require "vm"
sys = require "util"

UglifyJS = vm.createContext
  sys:           sys
  console:       console
  MOZ_SourceMap: require "source-map"

# Original script reads files at runtime,
# which interrupts browserify merging files into one file.
# This version read JavaScript content as string
# which are defined in this file later.
load_global = (code, file) ->
  try
    return vm.runInContext code, UglifyJS, file
  catch ex
    # XXX: in case of a syntax error, the message is kinda
    # useless. (no location information).
    console.error "ERROR in file: #{file} / #{ex}"
    process.exit 1

exports.minify = (files, options) ->
  options = UglifyJS.defaults options,
    outSourceMap : null
    sourceRoot   : null
    inSourceMap  : null
    fromString   : false
    warnings     : false
    mangle       : {}
    output       : null
    compress     : {}
  files = [ files ] if typeof files is "string"

  # 1. parse
  toplevel = null
  for file in files
    code = if options.fromString then file else fs.readFileSync file, "utf8"
    toplevel = UglifyJS.parse code,
      filename: if options.fromString then "?" else file
      toplevel: toplevel

  #  2. compress
  if options.compress
    compress = warnings: options.warnings
    UglifyJS.merge compress, options.compress
    toplevel.figure_out_scope()
    sq = UglifyJS.Compressor compress
    toplevel = toplevel.transform sq

  # 3. mangle
  if options.mangle
    toplevel.figure_out_scope()
    toplevel.compute_char_frequency()
    toplevel.mangle_names options.mangle

  # 4. output
  inMap = options.inSourceMap
  output = {}
  if typeof options.inSourceMap is "string"
    inMap = fs.readFileSync options.inSourceMap, "utf8"
  if options.outSourceMap
    output.source_map = UglifyJS.SourceMap
      file: options.outSourceMap
      orig: inMap
      root: options.sourceRoot
  if options.output
    UglifyJS.merge output, options.output
  stream = UglifyJS.OutputStream output
  toplevel.print stream
  code: "#{stream}"
  map:  "#{output.source_map}"

# External files are defined in uglifyjs-lib.js.
# This file would be bundled in JAR.
load_global(fs.readFileSync("./uglifyjs-lib.js", "utf8"), "uglifyjs-lib.js")

UglifyJS.AST_Node.warn_function = (txt) -> console.error "WARN: #{txt}"
