
## JPro Routing

### Base Concept

In JPro Routing, the Route is the base concept of your Application.
A route is a simple pattern to map a request to a specific page.
Routes can be combined, and transformed with Filters.

This way it is possible to write a complex program that can be reused in different projects.

Some typical use cases might be:
- Write an Authentication Logic as a Filter. (and use it with a single line)
- Wrap your application in a Container that adds debug functionality.
- Trac statistics as a Filter - which can be used by many projects.

This all sounds like "common sense" but in practice, most (JavaFX) UI code doesn't really compose with each other.
It is very common, that each Project has its own logic for Pages, and "hardcodes" any Filter just for a single project.

With JPro Routing, you can write a composable Route once, and reuse it in many projects.

#### Basic Types

Everything starts with a `Request`. A request contains information, like the requested path, device, previous path, etc.
A `Route` is a function from a `Request` to a `Response`. A Response can either be a redirect or a node that represents the page.
It also contains various meta-information, like whether it should be shown fullscreen.

A `Filter` transforms a Route into a new Route. So a Filter is a function from Route to Route.

#### Hello World
// TODO

#### Container Filter
// TODO

#### Authentication
// TODO

#### Tracking
// TODO

#### Samples Overview
// TODO