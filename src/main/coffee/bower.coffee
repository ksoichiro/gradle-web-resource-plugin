module.exports = ->
  fs = require 'fs'
  util = require 'util'
  bower = require 'bower'
  chalk = require 'bower/lib/node_modules/chalk'
  Q = require 'q'
  mout = require 'bower/lib/node_modules/mout'
  common = require './common'
  Logger = require './logger'

  projectPath = process.argv[3]
  packages = JSON.parse fs.readFileSync process.argv[4], 'utf-8'
  dependencies = packages.dependencies
  resolutions = packages.resolutions
  options = packages.options
  configs = packages.configs
  parallelize = process.argv[5] is 'true'
  logLevel = parseInt process.argv[6]

  log = new Logger logLevel, 'Bower'

  bowerInstallDependency = (item, cb) ->
    Q.fcall checkCache, item
    .then install
    .catch (error) ->
      log.e "Install failed: #{item.name}: #{if error.stack then error.stack else error}" if error
      common.setExitCode 1
    .done ->
      if !common.hasError() and !fs.existsSync "bower_components/#{item.name}"
        log.e "Install failed: module does not exist: #{item.name}"
        common.setExitCode 1
      cb?()

  getInstalledVersion = (name) ->
    # Some bower.json files don't have "version" attribute.
    # Try .bower.json first, then check bower.json.
    bowerJson = "bower_components/#{name}/.bower.json"
    if fs.existsSync bowerJson
      pkg = JSON.parse fs.readFileSync bowerJson, 'utf8'
      return pkg.version if pkg.version
    bowerJson = "bower_components/#{name}/bower.json"
    if fs.existsSync bowerJson
      pkg = JSON.parse fs.readFileSync bowerJson, 'utf8'
      return pkg.version if pkg.version
    return ''

  saveCurrentDependencyToJson = (name, version) ->
    json = name: "webResource", dependencies: {}, resolutions: {}
    json.dependencies[name] = version
    json.resolutions = resolutions
    jsonStr = JSON.stringify json, null, '  '
    fs.writeFileSync 'bower.json', jsonStr

  saveBowerJson = ->
    json = name: "webResource", dependencies: {}, resolutions: resolutions
    json.dependencies[dependency.name] = dependency.version for dependency in dependencies
    jsonStr = JSON.stringify json, null, '  '
    fs.writeFileSync 'bower.json', jsonStr

  checkCache = (item) ->
    name = item.name
    version = item.version
    cacheName = item.cacheName
    deferred = Q.defer()
    bower.commands.cache.list [cacheName], {}, {}
    .on 'error', (err) ->
      log.e "Checking cache failed: #{err}"
      deferred.reject()
    .on 'end', (r) ->
      offline = false
      for e in r
        if e.pkgMeta.version is version
          offline = true
          break
      deferred.resolve {name: name, version: version, cacheName: cacheName, offline: offline}
    deferred.promise

  install = (item) ->
    isSerialInstall = item isnt undefined
    deferred = Q.defer()

    if isSerialInstall
      name = item.name
      version = item.version
      cacheName = item.cacheName
      offline = item.offline
      installedVersion = getInstalledVersion name
      if installedVersion isnt ''
        # already installed
        if version isnt installedVersion
          deferred.reject new Error("Update required for #{name}: please remove bower_components/#{name} and execute again.")
        else
          deferred.resolve()
        return deferred.promise
      saveCurrentDependencyToJson name, version
    else
      saveBowerJson()

    cached = false
    validate = false
    validCacheName = name
    installOptions = {}
    installConfigs = configs or {}
    installConfigs['offline'] = offline unless 'offline' in installConfigs
    installOptions[mout.string.camelCase k] = options[k] for k of options
    # If 1st arg is specified, they are marked as unresolvable, and resolutions have no effect.
    bower.commands.install [], installOptions, installConfigs
    .on 'log', (l) ->
      if l.id is 'cached'
        cached = true
      else if l.id is 'validate'
        log.d "Validating: #{l.data.resolver.name}"
        validCacheName = l.data.pkgMeta.name
        validate = true
      else if l.id is 'resolve'
        log.i "Resolving: #{l.data.resolver.name}"
      else if l.id is 'download'
        log.i "Downloading: #{l.data.resolver.name}"
      else if l.id is 'progress'
        log.i "Downloading: #{l.data.resolver.name}##{l.data.resolver.target}: #{l.message}"
      else if l.id is 'extract'
        log.i "Extracting: #{l.data.resolver.name}"
      else if l.id is 'install'
        log.d "Installing: #{l.message}"
    .on 'error', (err) ->
      if err
        if err.code is 'ECONFLICT'
          log.e "Install failed: Unable to find a suitable version for #{err.name}#{if data then " while installing #{name}##{version}"}."
          log.w "To resolve this conflict, please define resolution to your build.gradle."
          log.w "Example:"
          log.w "    bower {"
          log.w "        dependencies {"
          log.w "            resolve name: '#{err.name}', version: '#{err.picks[0].pkgMeta.version}'"
          log.w "        }"
          log.w "    }"

          log.w "Candidate versions:"
          for i in [1..err.picks.length]
            pick = err.picks[i - 1]
            dependants = pick.dependants.map (dependant) ->
              result = "#{dependant.pkgMeta.name}##{dependant.pkgMeta.version}"
              result = 'this project' if result is 'webResource#undefined'
              return result
            log.w "    #{chalk.magenta "#{i}) "}#{chalk.cyan "#{pick.endpoint.name}##{pick.endpoint.target}"} which resolved to #{chalk.green pick.pkgMeta.version} and is required by #{chalk.green dependants.join ", "}"
          if isSerialInstall
            log.w "Some candidates might not be shown above because they are not installed yet."
            log.w "This is a limitation of this plugin."
        else
          log.e "Install failed: #{err.code}: #{err.stack}"
      deferred.reject()
    .on 'end', (installed) ->
      if isSerialInstall
        for key in Object.keys(installed)
          i = installed[key]
          log.i "Installed: #{i.pkgMeta.name}##{i.pkgMeta.version}#{if offline then " (offline)" else ""}"
        hasDependencies = Object.keys(installed).length > 1
        if cached and validate and !hasDependencies
          # Installed with cache, but validation occurred
          log.w "  Note: cache key does not match to the package name."
          log.w "  It means, the package \"#{name}\" is cached "
          log.w "  but still needed a network connection to validate the package "
          log.w "  because the cache could not be found by the name \"#{name}\"."
          log.w "  To execute installation without any network connections, add cacheName: \"#{validCacheName}\""
          log.w "  to \"install\" definition in your build.gradle. Example:"
          log.w "      bower {"
          log.w "          dependencies {"
          log.w "              install name: \"#{name}\", version: \"#{version}\", cacheName: \"#{validCacheName}\""
          log.w "          }"
          log.w "      }"
        else if installed[name] isnt undefined and cacheName isnt installed[name].pkgMeta.name
          # Installed without cache but the name doesn't match to the cache key.
          validCacheName = installed[name].pkgMeta.name
          log.w "  Note: cache key does not match to the package name."
          log.w "  It means, the package \"#{name}\" was cached "
          log.w "  but you cannot use this cache in the next installation "
          log.w "  because the cache will not be found by the name \"#{name}\"."
          log.w "  To use this cache in the next installation, add cacheName: \"#{validCacheName}\""
          log.w "  to \"install\" definition in your build.gradle. Example:"
          log.w "      bower {"
          log.w "          dependencies {"
          log.w "              install name: \"#{name}\", version: \"#{version}\", cacheName: \"#{validCacheName}\""
          log.w "          }"
          log.w "      }"
      else
        for key in Object.keys(installed)
          i = installed[key]
          log.i "Installed: #{i.pkgMeta.name}##{i.pkgMeta.version}"
      deferred.resolve()
    deferred.promise

  installParallel = (dummy, cb) ->
    Q.fcall install
    .catch (error) ->
      log.e "Install failed: #{error.stack}" if error
      common.setExitCode 1
    .done -> cb?()

  validateInstalledDependencies = ->
    for item in dependencies
      unless fs.existsSync "bower_components/#{item.name}"
        common.setExitCode 1
        log.w "Not installed: #{item.name}"
    log.e "Some dependencies are not installed." if common.hasError()

  if parallelize
    common.handleExit()
    common.install installParallel
  else
    log.w "parallelize option is set to false."
    log.w "  Parallel installation is recommended because"
    log.w "  the serial installation might cause version resolution issues."
    log.w "  If you continue to use parallelize = false, use it carefully."

    common.handleExit validateInstalledDependencies
    common.installSequentially dependencies, bowerInstallDependency
