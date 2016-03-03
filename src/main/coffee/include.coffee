# Script below is originally a partial copy from wiledal/gulp-include to use include feature.
# Rewrote with CoffeeScript in this project.
#
# Licence
#
# (MIT License)
#
# Copyright (c) 2014 Hugo Wiledal
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

fs = require 'fs'
path = require 'path'
glob = require 'glob'

extensions = null
includedFiles = []

addLeadingWhitespace = (whitespace, string) ->
  return string.split("\n").map (line) ->
    return whitespace + line
  .join "\n"

inExtensions = (filePath) ->
  return true unless extensions
  for extension in extensions
    re = "#{extension}$"
    return true if filePath.match re
  return false

processInclude = (content, filePath) ->
  matches = content.match /^(\s+)?(\/\/|\/\*|\#)(\s+)?=(\s+)?(include|require)(.+$)/mg
  relativeBasePath = path.dirname filePath

  return content unless matches

  for m in matches
    leadingWhitespaceMatch = m.match /^(\s+)/
    leadingWhitespace = null
    if leadingWhitespaceMatch
      leadingWhitespace = leadingWhitespaceMatch[0]
      leadingWhitespace = leadingWhitespaceMatch[0].split("\n")[1] if leadingWhitespaceMatch[0].indexOf "\n" > -1
      leadingWhitespace = leadingWhitespace.replace "\n", ""

    # Remove beginnings, endings and trim.
    includeCommand = m
      .replace /(\s+)/gi, " "
      .replace /(\/\/|\/\*)(\s+)?=(\s+)?/g, ""
      .replace /(\*\/)$/gi, ""
      .replace /['"]/g, ""
      .trim()
    split = includeCommand.split " "

    # Split the directive and the path
    includeType = split[0]
    includePath = "#{relativeBasePath}/#{split[1]}"

    # Use glob for file searching
    fileMatches = glob.sync includePath, mark: true
    replaceContent = null
    for fm, y in fileMatches
      globbedFilePath = fm

      # If directive is of type "require" and file already included, skip to next.
      continue if includeType == "require" and includedFiles.indexOf globbedFilePath > -1

      # If not in extensions, skip this file
      continue unless inExtensions globbedFilePath

      # Get file contents and apply recursive include on result
      fileContents = fs.readFileSync globbedFilePath
      replaceContent = "" unless replaceContent
      fileContents = addLeadingWhitespace leadingWhitespace, fileContents.toString() if leadingWhitespace
      replaceContent += processInclude fileContents.toString(), globbedFilePath

      includedFiles.push globbedFilePath if includedFiles.indexOf globbedFilePath is -1

      # If the last file did not have a line break, and it is not the last file in the matched glob,
      # add a line break to the end
      replaceContent += "\n" if !replaceContent.trim().match /\n$/ and y != fileMatches.length - 1

    # REPLACE
    if replaceContent
      content = content.replace m, -> return replaceContent

  return content

module.exports.processInclude = processInclude
