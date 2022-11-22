package com.jpro.routing

@FunctionalInterface
trait Filter {
    def apply(route: Route): Route

    def compose(y: Filter): Filter = r => this.apply(y.apply(r))
}
