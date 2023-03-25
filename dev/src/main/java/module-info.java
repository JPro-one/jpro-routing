/**
 * Module descriptor for the Routing Dev module.
 *
 * @author Besmir Beqiri
 */
module one.jpro.routing.dev {
    requires transitive simplefx.extended;
    requires transitive javafx.controls;
    requires one.jpro.routing.core;
    requires org.kordamp.ikonli.javafx;

    exports one.jpro.routing.dev;
}