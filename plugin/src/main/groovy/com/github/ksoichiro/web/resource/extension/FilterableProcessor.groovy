package com.github.ksoichiro.web.resource.extension

class FilterableProcessor extends WebResourceProcessor {
    boolean enabled
    List<String> include
    List<String> exclude
    List<Filter> filters
    boolean minify

    FilterableProcessor(String src, String dest, List<String> include, List<String> exclude) {
        this(src, dest, include, exclude, [])
    }

    FilterableProcessor(String src, String dest, List<String> include, List<String> exclude, List<Filter> filters) {
        super(src, dest)
        enabled = true
        minify = true
        this.include = include
        this.exclude = exclude
        this.filters = filters
    }

    /**
     * Configure filters by closure.
     *
     * @param configureClosure closure to be passed to this config object
     */
    void filters(Closure configureClosure) {
        configureClosure.delegate = this
        configureClosure()
    }

    /**
     * Add an include filter by {@code pattern}.
     *
     * @param pattern Ant pattern string
     */
    void include(String pattern) {
        filters.add(new Filter(include: pattern))
    }

    /**
     * Add an exclude filter by {@code pattern}.
     *
     * @param pattern Ant pattern string
     */
    void exclude(String pattern) {
        filters.add(new Filter(exclude: pattern))
    }
}
