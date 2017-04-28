package io.swagger.codegen.utils.erlang;

import java.io.*;

import com.fasterxml.jackson.core.JsonEncoding;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;

public class ErlangJsonFactory extends JsonFactory {

    public ErlangJsonFactory() {
        super();
    }

    public ErlangJsonFactory(ObjectCodec oc) {
        super(oc);
    }

    @Override
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        WriterBasedErlangJsonGenerator gen = new WriterBasedErlangJsonGenerator(ctxt,
                _generatorFeatures, _objectCodec, out);
        if (_characterEscapes != null) {
            gen.setCharacterEscapes(_characterEscapes);
        }
        return gen;
    }
}
