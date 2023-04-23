/**
 * Module descriptor for the example module.
 */
module one.jpro.routing.example {
    requires org.controlsfx.controls;
    requires one.jpro.routing.core;
    requires one.jpro.routing.dev;
    requires one.jpro.routing.popup;
    requires one.jpro.auth;
    requires org.json;
    requires scala.library;
    requires simplefx.extended;
    requires com.sandec.mdfx;
    requires atlantafx.base;

    exports example.colors;
}