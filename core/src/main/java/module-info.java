module com.jpro.routing.core {

    requires transitive simplefx.extended;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    exports com.jpro.routing;
    exports com.jpro.routing.filter.container;
}