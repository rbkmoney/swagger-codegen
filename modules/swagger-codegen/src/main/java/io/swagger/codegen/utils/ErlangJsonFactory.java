package io.swagger.codegen.utils;

import java.io.*;

import com.fasterxml.jackson.core.JsonEncoding;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;

public class ErlangJsonFactory extends JsonFactory {

    public ErlangJsonFactory() {
        super();
        System.out.println("Factory CONSTRUCTOR+++++++++++++++++++++++++++++++++++++++++++++++");
    }

    public ErlangJsonFactory(ObjectCodec oc) {
        super(oc);
        System.out.println("Factory CONSTRUCTOR 2 +++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) throws IOException {
        System.out.println("Factory Create Generator OutputStream +++++++++++++++++++++++++++++++++++++++++++++++");
        return createGenerator(out, JsonEncoding.UTF8);
    }

    @Override
    public JsonGenerator createGenerator(Writer w) throws IOException {
        System.out.println("Factory Create Generator Writer +++++++++++++++++++++++++++++++++++++++++++++++");
        IOContext ctxt = _createContext(w, false);
        return _createGenerator(_decorate(w, ctxt), ctxt);
    }

    @Override
    public JsonGenerator createGenerator(DataOutput out) throws IOException {
        System.out.println("Factory Create Generator DataOutput +++++++++++++++++++++++++++++++++++++++++++++++");
        return createGenerator(_createDataOutputWrapper(out), JsonEncoding.UTF8);
    }

    @Override
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        System.out.println("GENERATOR WrtierBased+++++++++++++++++++++++++++++++++++++++++++++++");
        WriterBasedErlangJsonGenerator gen = new WriterBasedErlangJsonGenerator(ctxt,
                _generatorFeatures, _objectCodec, out);
        if (_characterEscapes != null) {
            gen.setCharacterEscapes(_characterEscapes);
        }
        //SerializableString rootSep = _rootValueSeparator;
        //if (rootSep != DEFAULT_ROOT_VALUE_SEPARATOR) {
        //     gen.setRootValueSeparator(rootSep);
        // }
        return gen;
    }

    @Override
    protected JsonGenerator _createUTF8Generator(OutputStream out, IOContext ctxt) throws IOException {
        System.out.println("GENERATOR UTF8+++++++++++++++++++++++++++++++++++++++++++++++");
        ErlangUTF8JsonGenerator gen = new ErlangUTF8JsonGenerator(ctxt,
                _generatorFeatures, _objectCodec, out);
        if (_characterEscapes != null) {
            gen.setCharacterEscapes(_characterEscapes);
        }
        // SerializableString rootSep = _rootValueSeparator;
        // if (rootSep != DEFAULT_ROOT_VALUE_SEPARATOR) {
        //     gen.setRootValueSeparator(rootSep);
        // }
        return gen;
    }

}
