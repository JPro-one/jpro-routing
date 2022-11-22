## 0.11.0
* Big rewrite

## 0.10.0
* Renamed the project from jpro-web to jpro-routing
## 0.9.8
* Added Option, to crawl the whole App, which allows to check for leaks, create sitemap or to check for deadlinks.
* It's now possible to unset Links, by calling `Util.setLink` with the link "null".
## 0.9.7
* Fixed the featuers added in the previous update
## 0.9.6 (done use)
## 0.9.5 (done use)
## 0.9.4 (done use)
## 0.9.3 (done use)
* Fixed issue with setLink. Now only one link is set per node. If you update the link of a Node, the old link now gets removed.
* Added the method `ObservableList<String> getHistoryBackward()`, `ObservableList<String> getHistoryForward()` and `StringProperty currentHistoryProperty()` to the session manager.

## 0.9.2
* Ranamed goto to gotoURL in the SessionManager, to make it accessible in Java, because goto is a keyword.
* Added a version of gotoURL with only 1 argument, to make it more accessible from Java.

## 0.9.1
* The desktop version now uses a ScrollPane, when fullscreen is false.
 It also fixes an issue with transitions on desktop
## 0.8.9
* Fixed setLink for popups on desktop.
## 0.8.8
 * Fixed a bug when switching pages

## 0.8.7
* support for refresh
* current link is now available through the sessionmanager
* simplified link areas, avoiding crashing JavaFX bug

## 0.8.6
* Better support for the java module system
## 0.8.5
* Added the method handleURL to the View.
It can be used to handle the url-change yourself, instead of going through the route.
## 0.8.4
 * Removed bintray, updated many many dependencies
## 0.8.3
 * It's now possible to open links in a new tab, while pressing ctrl or shift.
## 0.8.2
 * Pages now have a onClose method.
 * We now use JMemoryBuddy to detect leaking pages

## 0.8.1
 * Fixed performance regression.

## 0.8.0
 * jpro-web now ensures that the size of the webpage is automatically resized. 
   Previously this was handled by the application itself.
 * The class WebApp now has the styleClass `jpro-web-app`. 
## 0.7.1
 * Updated another missed dependency.
## 0.7.0
 * Updated SimpleFX and Scala. The project now only works with Java(FX) 11 and above.
 
## 0.6.1
 * Important Refactoring - The SessionManager now has different implementations

## 0.6.0
 * Added a reference to the SessionManager to the View
 * Added the option to implement an own forward/backward button. 
 This is quite useful for native clients.
 * updated dependencies

## 0.5.1
 * fixed syntax error when using gtags

## 0.5.0
 * The API's are now easier to use from java
 * added support for gtags in combination with google-analytics

## 0.4.2
 * Remvoed View.nativeScrolling
 * Added View.fullscreen
 * This version is compatible with jpro 2018.1.8