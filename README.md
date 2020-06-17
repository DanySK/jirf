# JIRF: Java implicit reflective factory

This project aims at providing a way for reflectively creating arbitrary objects given their name, the context where they are being created, and a set of type conversion functions.
JIRF automatically scans the class constructors, benchmarks them, and tries to build the object in the most appropriate way.
JIRF can be particularly usfeul when trying to build objects from a partially specified configuration file (e.g. XML or JSON or YAML).

## Use in your project

Gradle:
```kotlin
dependencies {
    implementation("org.danilopianini:jirf:<version you need>")
}
```
Note: starting from 0.3.0, JIRF requires Java 11.
Previous versions work on Java 8+.

## Basic concepts

The factory requires to be configured with:

1. *Singleton objects*: they are singleton in the sense that, for what concerns the factory, they are the sole instance of that type the factory can use. They must be registered by providing both an instance and a type bound: all the types in the hierarchy up to the bound (included) will be considered as entirely represented by the specific instance. As a consequence, any time a parameter of a registered type is required, the singleton object will be used, with no further search. Their purpose is to provide a view on the context in which the current object is being built. E.g., if your goal is building and populating a `Tree` object with `Branch` objects and `Leaf` Objects, and the latter two types require the former to be passed as parameter, you probably want Tree to be registered as a singleton: all the objects created by the factory will be passed the Tree instance as parameter. Also, once a `Branch` is built, you probably want to register it as singleton, so that `Leaf` objects can get built with the current branch, and deregister the `Branch` singleton instance once the current branch is entirely populated.

2. *Implicit conversions*: inspired by Scala, those implicit conversions are functions that translate a type into another automatically, without any manual intervention. JIRF automagically manages hierarchies internally to make implict conversions work as you would expect a function to do (e.g.: if you register a method for converting `String` to `Integer`, and you request to build a `Number` with a `String`, the system can do it for you). While in Scala implicit conversions are applied only once, in JIRF a graph of types is built, where types are nodes, and implicit conversion functions are edges. JIRF internally uses shortest-path algorithms to choose the shortest chain of implicit conversions to apply in order to get from a type to another.

## Example

Configure the reflective factory with the type conversions of your like.
You can provide a *singleton object* to be used whenever the type is requested,
write your own *implicit type conversion functions*,
and use the ones provided out of the box.

```java
final Factory f = new FactoryBuilder()
        .withAutoBoxing()
        .withNarrowingConversions()
        .withWideningConversions()
        .withArrayBoxing()
        .withBooleanIntConversions()
        .withAutomaticToString()
        .withArrayWideningConversions()
        .withArrayNarrowingConversions()
        .withArrayBooleanIntConversions()
        .withArrayListConversions(double[].class, int[]::class, String[]::class)
        .build();
f.registerSingleton(Date.class, new Date())
f.registerImplicit(String.class, String[].class, s -> s.split(","))
f.build(MyObj.class, "", 1, 2, 3, 4)
f.convert("Some,strings", String[]::class) // -> Optional({"Some", "strings"})
```
