package io.swagger.codegen.utils;

import java.io.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class ErlangJsonPrinter extends DefaultPrettyPrinter {

    public ErlangJsonPrinter() {
        super();
    }

    public ErlangJsonPrinter(ErlangJsonPrinter base) {
        super(base);
    }

    //its necessary - to override default PrettyPrinter
    @Override
    public ErlangJsonPrinter createInstance() {
        return new ErlangJsonPrinter(this);
    }


    @Override
    public void writeStartObject(JsonGenerator jg) throws IOException {
        jg.writeRaw("#{");
        if (!_objectIndenter.isInline()) {
            ++_nesting;
        }
    }

    /**
     * Method called after an object field has been output, but
     * before the value is output.
     * <p>
     */
    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
        if (_spacesInObjectEntries) {
            jg.writeRaw(" => ");
        } else {
            jg.writeRaw("=>");
        }
    }
}
