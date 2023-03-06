module jpro.routing.auth {
    exports one.jpro.auth;
    exports one.jpro.auth.oath2;
    exports one.jpro.auth.oath2.provider;
    exports one.jpro.auth.utils;
    exports one.jpro.auth.api;
    exports one.jpro.auth.authentication;
    exports one.jpro.auth.jwt;

    requires org.json;
    requires java.net.http;
    requires com.auth0.jwt;
    requires jwks.rsa;
    requires jpro.webapi;
    requires org.slf4j;
    requires org.jetbrains.annotations;
}