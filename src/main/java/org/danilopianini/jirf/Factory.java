package org.danilopianini.jirf;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jgrapht.GraphPath;

public interface Factory {

    <E> void registerSingleton(Class<? super E> lowerBound, Class<? super E> upperBound, E object);

    <E> void registerSingleton(Class<? super E> bound, E object);

    <E> void registerSingleton(E object);

//    void registerSingleton(String clazz, Object object);
    <E> void registerSupplier(Class<? super E> lowerBound, Class<? super E> upperBound, Class<? super E> clazz, Supplier<? extends E> object);

    <E> void registerSupplier(Class<? super E> bound, Class<? super E> clazz, Supplier<? extends E> object);

    <E> void registerSupplier(Class<? super E> clazz, Supplier<? extends E> object);

    /*
     * Impliciti converto source in destination.
     * Automaticamente posso cominciare a cercare all'indietro, data la classe dell'oggetto in ingresso,
     * che implicito applicare. E.g. se definisco una conversione Number -> int, e una conversione double -> Double,
     * e mi viene passato un double dove voglio un int, allora... cerco nel grafo
     * 
     * ricerca dei percorsi su un grafo, da ordinare per brevità per dare punteggio
     * 
     * grafo orientato che connette le classi di cui si è data conversione via implicito.
     * quando viene inserito un implicito S -> D, vengono registrate anche tutte le conversioni da D alle proprie
     * superclassi con il medesimo implicito, a costo dell'arco zero.
     * Oppure, si mettono archi fra S e tutti i supertipi di D, al costo della conversione
     * 
     * F<S,D>
     * 
     * -> bound su S
     * S in ingresso ad F. Quindi F accetta S-. Upper bound è al più S, lower bound una qualunque sottoclasse di S
     * uguale per D: S può produrre D, o una qualunque sua sottoclasse.
     * la classe di source deve essere S+.e.g. se S=Number, posso registrare Number-
     * la classe di destination deve essere D-
     * 
     * vanno registrati a coppie, per cui
     */
//    <S, D> void registerImplicit(Class<S> source, Class<D> destination, Function<? super S, ? extends D> conversion);

    <E> E build(Class<E> clazz, Object... parameters);

    <E> E build(Class<? super E> clazz, List<?> args);

//    <E> E build(String clazz, Object... parameters);
    
}
